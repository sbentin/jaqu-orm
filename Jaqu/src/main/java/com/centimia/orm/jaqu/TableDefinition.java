/*
 * Copyright (c) 2007-2010 Centimia Ltd.
 * All rights reserved.  Unpublished -- rights reserved
 *
 * Use of a copyright notice is precautionary only, and does
 * not imply publication or disclosure.
 *
 * Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 2.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group, Centimia Inc.
 */
package com.centimia.orm.jaqu;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import com.centimia.orm.jaqu.annotation.Cascade;
import com.centimia.orm.jaqu.annotation.Column;
import com.centimia.orm.jaqu.annotation.Converter;
import com.centimia.orm.jaqu.annotation.Discriminator;
import com.centimia.orm.jaqu.annotation.Entity;
import com.centimia.orm.jaqu.annotation.Event;
import com.centimia.orm.jaqu.annotation.Extension;
import com.centimia.orm.jaqu.annotation.Immutable;
import com.centimia.orm.jaqu.annotation.Index;
import com.centimia.orm.jaqu.annotation.Indices;
import com.centimia.orm.jaqu.annotation.Inherited;
import com.centimia.orm.jaqu.annotation.Interceptor;
import com.centimia.orm.jaqu.annotation.Lazy;
import com.centimia.orm.jaqu.annotation.Many2Many;
import com.centimia.orm.jaqu.annotation.Many2One;
import com.centimia.orm.jaqu.annotation.MappedSuperclass;
import com.centimia.orm.jaqu.annotation.NoUpdateOnSave;
import com.centimia.orm.jaqu.annotation.One2Many;
import com.centimia.orm.jaqu.annotation.PrimaryKey;
import com.centimia.orm.jaqu.annotation.RelationTypes;
import com.centimia.orm.jaqu.annotation.Transient;
import com.centimia.orm.jaqu.annotation.Version;
import com.centimia.orm.jaqu.constant.Constants;
import com.centimia.orm.jaqu.util.ClassUtils;
import com.centimia.orm.jaqu.util.FieldComperator;
import com.centimia.orm.jaqu.util.JaquConverter;
import com.centimia.orm.jaqu.util.StatementBuilder;
import com.centimia.orm.jaqu.util.StringUtils;
import com.centimia.orm.jaqu.util.Utils;

/**
 * A table definition contains the index definitions of a table, the field definitions, the table name, and other meta data.
 *
 * @param <T> the table type
 */
class TableDefinition<T> {
	private static final String TO_DB = "toDb";

	enum FieldType {
		NORMAL, FK, M2M, O2M, M2O;

		boolean isCollectionRelation() {
			return this == O2M || this == M2M;
		}
	}


	private FieldDefinition version = null;

	/**
	 * The meta data of a field.
	 */
	static class FieldDefinition implements Comparable<FieldDefinition> {
		private static final String OBJECT_VALUE = "[Object: %s], [value: %s]\t";
		
		String columnName;
		Field field;
		String dataType;
		int maxLength = 0;
		boolean isPrimaryKey;
		FieldType fieldType = FieldType.NORMAL;
		RelationDefinition relationDefinition;
		boolean isSilent = false;
		boolean noUpdateField = false;
		Types type;
		Method getter;
		boolean unique;
		boolean notNull;
		boolean isVersion = false;
		boolean isExtension;

		@SuppressWarnings("rawtypes")
		Object getValue(Object obj) {
			try {
				field.setAccessible(true);
				Object actualValue = field.get(obj);
				if (null != actualValue) {
					switch (type) {
						case ENUM: {
							if (null == actualValue.toString())
								return actualValue;
							return actualValue.toString();
						}
						case ENUM_INT: {
							if (null == actualValue.toString())
								return actualValue;
							return ((Enum)actualValue).ordinal();
						}
						case UUID: return actualValue.toString();
						default: break;
					}
				}
				return actualValue;
			}
			catch (Exception e) {
				throw new JaquError(e, "In fieldDefinition.getValue -> %s", e.getMessage());
			}
		}

		Object initWithNewObject(Object obj) {
			if (Types.ENUM == type || Types.ENUM_INT == type) {
				// initialize with the first value in the enum (to be used as key)
				Class<?> enumClass = field.getType();
				Object newEnum = Utils.newEnum(enumClass);
				field.setAccessible(true);
				try {
					field.set(obj, newEnum);
				}
				catch (IllegalArgumentException | IllegalAccessException e) {
					String objectString = (null == obj) ? "null" : obj.getClass().getName();
					String valueString = newEnum.toString();
					String msg = String.format(OBJECT_VALUE, objectString, valueString);
					throw new JaquError(e, msg + e.getMessage());
				}
				return newEnum;
			}
			else if (Types.UUID == type) {
				UUID sUUID = UUID.randomUUID();
				try {					
					field.set(obj, sUUID);
					return sUUID.toString();
				}
				catch (IllegalArgumentException | IllegalAccessException e) {
					String objectString = (null == obj) ? "null" : obj.getClass().getName();
					String valueString = sUUID.toString();
					String msg = String.format(OBJECT_VALUE, objectString, valueString);
					throw new JaquError(e, msg + e.getMessage());
				}
			}
			else if (Types.FK == type) {
				// this is a O2O relation. Must be an entity and thus must have an empty constructor.
				Object o = Utils.newObject(field.getType());
				try {
					field.set(obj, o);
				}
				catch (IllegalArgumentException | IllegalAccessException e) {
					// No reason for this to happen, we set it as null which would cause a null pointer exception
					o = null;
				}
				return o;
			}
			else {
				Object o = Utils.newObject(field.getType());
				setValue(obj, o, null);
				return o;
			}
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		void setValue(final Object objToSet, Object fieldValueFromDb, final Db db) {
			try {
				Object tmp = fieldValueFromDb;
				switch (fieldType) {
					case NORMAL:
						// if 'o' equals null then setting the 'enum' will cause a nullPointerException
						if ((Types.ENUM_INT == type || Types.ENUM == type) && null != fieldValueFromDb) {
							Class enumClass = field.getType();
							if (Types.ENUM_INT == type) {
								field.set(objToSet, enumClass.getEnumConstants()[(Integer)fieldValueFromDb]);
							}
							else {
								field.set(objToSet, Enum.valueOf(enumClass, (String)fieldValueFromDb));
							}
						}
						else if (Types.UUID == type && null != fieldValueFromDb) {
							// object from DB should be a String by mapping
							field.set(objToSet, UUID.fromString((String)fieldValueFromDb));
						}
						else
							field.set(objToSet, fieldValueFromDb);
						break;
					case M2O: {
						if (null != db) {
							// here we need to fetch the parent object based on the id which is in the relationTable
							// this is the same as an FK field accept for the fact that we have no value in the DB, i.e. 'tmp' == null and 'o' == null
							// get the primary key...
							String query = "select " + relationDefinition.relationColumnName + " from " + relationDefinition.relationTableName + " where " + relationDefinition.relationFieldName + " = " + db.factory.getPrimaryKey(objToSet);
							Object result = db.executeQuery(query, rs -> {
								if (rs.next())
									return db.factory.getDialect().getValueByType(type, rs, relationDefinition.relationColumnName);
								return null;
							});
							if (null != result) {
								// field from db would have a 'null' which in this case is in correct so we have to set it up.
								// create the object to hold the data (I could use field.getType() but this way we can find mistakes
								fieldValueFromDb = Utils.newObject(relationDefinition.dataType[0]);
								tmp = result;
								// if parent was found we continue to FK
							}
							else
								break;
						}
						else
							break;
						// we don't explicitly break as there is a possibility case where we continue to FK choice
					}
					case FK: {
						if (null == field.getAnnotation(Lazy.class)) {
							RelationTypes relationTypes = field.getAnnotation(RelationTypes.class);
							Class<?>[] types = null;
							if (null == relationTypes)
								types = new Class<?>[] {field.getType()};
							else
								types = relationTypes.value();

							boolean found = false;
							for (Class<?> innerType: types) {
								fieldValueFromDb = Utils.convert(fieldValueFromDb, innerType);
								if (null != fieldValueFromDb && !innerType.isInstance(tmp)) {
									Object reEntrant = db.reEntrantCache.checkReEntrent(fieldValueFromDb.getClass(), tmp);
									if (null != reEntrant) {
										fieldValueFromDb = reEntrant;
										found = true;
										break;
									}
									else {
										List<?> result = db.from(fieldValueFromDb).primaryKey().is(tmp.toString()).select();
										if (!result.isEmpty()) {
											fieldValueFromDb = result.get(0);
											found = true;
											break;
										}
									}
								}
								else {
									// either found it as an MTO type or with currently have no relationship i.e fieldValueFronDb is null
									found = true;
								}
							}
							if (!found)
								throw new JaquError("\nData Consistency error - Foreign relation does not exist!!\nError column was {%s}"
										+ " with value %s in table %s"
										+ "\nmissing in table %s", field.getName(), tmp, objToSet.getClass().getName(), fieldValueFromDb.getClass().getName());
							field.set(objToSet, fieldValueFromDb);
						}
						else {
							// should be marked as lazy loaded
							if (null != field.getType().getAnnotation(Entity.class) ||
									null != field.getType().getAnnotation(MappedSuperclass.class)) {
								Class<?> innerType;
								boolean isAbstract = Modifier.isAbstract(field.getType().getModifiers());
								if (isAbstract && null != field.getAnnotation(RelationTypes.class)) {
									// take the first class and use that as an instance
									innerType = field.getAnnotation(RelationTypes.class).value()[0];
								}
								else if (!isAbstract) {
									innerType = field.getType();
								}
								else
									throw new JaquError("Can not instanciate a class of type %s on class %s because it is abstract!", field.getType().getName(), objToSet.getClass().getName());
								Field lazyfield = innerType.getField(Constants.IS_LAZY);
								fieldValueFromDb = innerType.getConstructor().newInstance();
					 			lazyfield.setBoolean(fieldValueFromDb, true);
								field.set(objToSet, fieldValueFromDb);
							}
						}
						break;
					}
					case O2M: {
						if (relationDefinition.eagerLoad && db != null) {
							if (relationDefinition.relationTableName != null) {
								List resultList = Utils.newArrayList();
								for (Class<?> lDataType: relationDefinition.dataType) {
									resultList.addAll(db.getRelationByRelationTable(this, db.factory.getPrimaryKey(objToSet), lDataType));
								}

								if (!resultList.isEmpty()) {
									if (relationDefinition.dataType.length > 1 && null != relationDefinition.orderByField) {
										resultList.sort(new FieldComperator(relationDefinition.dataType[0], relationDefinition.orderByField));
									}
									if (this.field.getType().isAssignableFrom(resultList.getClass()))
										fieldValueFromDb = new JaquList(resultList, db, this, db.factory.getPrimaryKey(objToSet));
									else {
										// only when the type is a Set type we will be here
										HashSet set = Utils.newHashSet();
										set.addAll(resultList);
										fieldValueFromDb = new JaquSet(set, db, this, db.factory.getPrimaryKey(objToSet));
									}
								}
								else {
									// on eager loading if no result exists we set to an empty collection
									if (this.field.getType().isAssignableFrom(resultList.getClass()))
										fieldValueFromDb = new JaquList(Utils.newArrayList(), db, this, db.factory.getPrimaryKey(objToSet)) ;
									else
										fieldValueFromDb = new JaquSet(Utils.newHashSet(), db, this, db.factory.getPrimaryKey(objToSet));
								}
							}
							else {
								List resultList = Utils.newArrayList();
								for (Class<?> lDataType: relationDefinition.dataType) {
									Object descriptor = Utils.newObject(lDataType);
									QueryWhere<?> where = db.from(descriptor).where(st -> {
										FieldDefinition fdef = ((SelectTable)st).getAliasDefinition().getDefinitionForField(relationDefinition.relationFieldName);
										Object myPrimaryKey = db.factory.getPrimaryKey(objToSet);
										String pk = (myPrimaryKey instanceof String) ? "'" + myPrimaryKey.toString() + "'" : myPrimaryKey.toString();

										if (null != fdef)
											// this is the case when it is a two sided relationship. To allow that the name of the column in the DB and the name of the field are
											// different we use the columnName property.
											return st.getAs() + "." + fdef.columnName + " = " + pk;
										// This is the case of one sided relationship. In this case the name of the FK is given to us in the relationFieldName
										return st.getAs() + "." + relationDefinition.relationFieldName + " = " + pk;
									});
									String orderByField = relationDefinition.orderByField;
									if (null != orderByField) {
										Field lField = ClassUtils.findField(lDataType, relationDefinition.orderByField);
										lField.setAccessible(true);
										if ("DESC".equals(relationDefinition.direction))
											resultList.addAll(where.orderByDesc(lField.get(descriptor)).select());
										else
											resultList.addAll(where.orderBy(lField.get(descriptor)).select());
									}
									else
										resultList.addAll(where.select());
								}
								if (!resultList.isEmpty()) {
									if (relationDefinition.dataType.length > 1 && null != relationDefinition.orderByField) {
										resultList.sort(new FieldComperator(relationDefinition.dataType[0], relationDefinition.orderByField));
									}
									if (this.field.getType().isAssignableFrom(resultList.getClass()))
										fieldValueFromDb = new JaquList(resultList, db, this, db.factory.getPrimaryKey(objToSet));
									else {
										// only when the type is a Set type we will be here
										HashSet set = Utils.newHashSet();
										set.addAll(resultList);
										fieldValueFromDb = new JaquSet(set, db, this, db.factory.getPrimaryKey(objToSet));
									}
								}
								else {
									// on eager loading if no result exists we set to an empty collection
									if (this.field.getType().isAssignableFrom(resultList.getClass()))
										fieldValueFromDb = new JaquList(Utils.newArrayList(), db, this, db.factory.getPrimaryKey(objToSet)) ;
									else
										fieldValueFromDb = new JaquSet(Utils.newHashSet(), db, this, db.factory.getPrimaryKey(objToSet));
								}
							}
						}
						else {
							// instrument this instance of the class
							Field dbField = objToSet.getClass().getField("db");
							// put the open connection on the object. As long as the connection is open calling the getter method on the
							// 'obj' will produce the relation
							dbField.set(objToSet, db);
							fieldValueFromDb = null;
						}
						field.set(objToSet, fieldValueFromDb);
						break;
					}
					case M2M: {
						// instrument this instance of the class
						Field dbField = objToSet.getClass().getField("db");
						// put the open connection on the object. As long as the connection is open calling the getter method on the 'obj'
						// will produce the relation
						dbField.set(objToSet, db);
						break;
					}
					default:
						throw new JaquError("IllegalState - Field %s was marked as relation but has no relation MetaData in define method", columnName);
				}
			}
			catch (Exception e) {
				String objectString = (null == objToSet) ? "null" : objToSet.getClass().getName();
				String valueString = (null == fieldValueFromDb) ? null : fieldValueFromDb.toString();
				String msg = String.format(OBJECT_VALUE, objectString, valueString);
				throw new JaquError(e, msg + e.getMessage());
			}
		}

		Object read(ResultSet rs, Dialect dialect) {
			try {
				return dialect.getValueByType(type, rs, this.columnName);
			}
			catch (SQLException e) {
				throw new JaquError(e, e.getMessage());
			}
		}

		/*
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return super.hashCode();
		}

		/*
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof FieldDefinition))
				return false;
			return columnName.equals(((FieldDefinition)obj).columnName);
		}

		/**
		 * for sorting. sort by fieldType ascending unless same coloumnName is used.
		 */
		@Override
		public int compareTo(FieldDefinition o) {
			if (columnName.equals(o.columnName))
				return 0;
			return fieldType.compareTo(o.fieldType);
		}
	}

	static class RelationDefinition {
		/** The name of the relationship table. On O2M relationships might be null. */
		String relationTableName;
		/**
		 * The name of the column used for the relation. When O2M relationship and no relation table is used this value is the field holding
		 * the other side relation
		 */
		String relationColumnName;
		/** The name of the field used for holding the other side mapping. */
		String relationFieldName;
		/**
		 * When true, related children will be loaded with the object data, otherwise they will load when the getter function is used.
		 * 'false' is default
		 */
		boolean eagerLoad = false;
		/** The data type of the relationship object */
		Class<?>[] dataType;
		/** type of cascade relation with the related child. relevant to O2M relations only. Currently only CascadeType.DELETE is supported */
		CascadeType cascadeType = CascadeType.NONE;
		/** the name of a field in the other side of the relation that determines the order of the response */
		String orderByField = null;
		/** the name of a column in the other side of the relation that determines the order of the response */
		String orderByColumn = null;
		/** the direction of order by. Default is ASC" */
		String direction = "ASC";
	}

	final Dialect dialect;
	final String tableName;
	private final Class<T> clazz;
	private HashMap<String, FieldDefinition> fieldMap = Utils.newHashMap();
	private List<FieldDefinition> fields = Utils.newArrayList();
	private List<FieldDefinition> primaryKeyColumnNames;
	private List<FieldDefinition> oneToOneRelations;
	boolean isAggregateParent = false;
	private GeneratorType genType = GeneratorType.NONE;
	private String sequenceQuery = null;
	InheritedType inheritedType = InheritedType.NONE;
	char discriminatorValue;
	String discriminatorColumn;
	@SuppressWarnings("rawtypes")
	private CRUDInterceptor	interceptor;
	private Event[]	interceptorEvents;

	TableDefinition(Class<T> clazz, Dialect dialect) {
		this.dialect = dialect;
		this.clazz = clazz;
		String nameOfTable = clazz.getSimpleName();
		// Handle table annotation if entity and Table annotations exist

		com.centimia.orm.jaqu.annotation.Table tableAnnotation = clazz.getAnnotation(com.centimia.orm.jaqu.annotation.Table.class);
		if (tableAnnotation != null && tableAnnotation.name() != null && !"".equals(tableAnnotation.name())) {
			nameOfTable = tableAnnotation.name();
		}
		if (clazz.getAnnotation(Entity.class) != null || (!Modifier.isAbstract(clazz.getModifiers()) && null != clazz.getAnnotation(MappedSuperclass.class))) {
			// we must have primary keys
			primaryKeyColumnNames = Utils.newArrayList();
		}
		this.tableName = nameOfTable;
		Inherited inherited = clazz.getAnnotation(Inherited.class);
		if (inherited != null) {
			this.inheritedType = inherited.inheritedType();
			this.discriminatorValue = inherited.DiscriminatorValue();
			this.discriminatorColumn = inherited.DiscriminatorColumn();
		}
		else {
			Discriminator discriminator = clazz.getAnnotation(Discriminator.class);
			if (null != discriminator) {
				this.inheritedType = InheritedType.DISCRIMINATOR;
				this.discriminatorValue = discriminator.DiscriminatorValue();
				this.discriminatorColumn = discriminator.DiscriminatorColumn();
			}
		}
		com.centimia.orm.jaqu.annotation.Interceptor interceptorAnnot = getInterceptorAnnotation(clazz);
		if (null != interceptorAnnot) {
			this.interceptorEvents = interceptorAnnot.event();
			try {
				interceptor = interceptorAnnot.Class().getConstructor().newInstance();
			}
			catch (Exception e) {
				throw new JaquError("Expected an Interceptor class for Table/ Entity %s. Unable to invoke!!", nameOfTable);
			}
		}
	}

	/*
	 * get the most "forward" "Interceptor Annotation" in the hierarchy tree.
	 */
	private Interceptor getInterceptorAnnotation(Class<?> clazz) {
		if (null == clazz || clazz.equals(Object.class))
			return null;
		Interceptor interceptorAnnot = getInterceptorAnnotation(clazz.getSuperclass());

		Interceptor lInterceptor = clazz.getAnnotation(Interceptor.class);
		return null == lInterceptor ? interceptorAnnot : lInterceptor;
	}

	List<FieldDefinition> getFields() {
		return fields;
	}

	void addManyToMany(FieldDefinition fieldDefinition, Many2Many many2Many, Db db) {
		Class<?>[] childType = many2Many.childType();
		if (Object.class.equals(childType[0])) {
			try {
				childType = new Class<?>[] {(Class<?>) ((ParameterizedType)fieldDefinition.field.getGenericType()).getActualTypeArguments()[0]};
			}
			catch (Exception e) {
				throw new JaquError(e, "Tried to figure out the type of the child relation but couldn't. Try setting the 'childType' " +
						"annotation parameter on @Many2Many annotation");
			}
		}

		String relationFieldName = many2Many.relationFieldName();
		if (relationFieldName == null || "".equals(relationFieldName))
			relationFieldName = tableName;

		String relationColumnName = many2Many.relationColumnName();
		if (null == relationColumnName || "".equals(relationColumnName)) {
			// this can only be where there is only one child type
			relationColumnName = childType[0].getSimpleName();
		}

		RelationDefinition def = new RelationDefinition();
		def.eagerLoad = false;
		def.relationTableName = many2Many.joinTableName();

		if (!"".equals(many2Many.orderBy())) {
			def.orderByField = many2Many.orderBy();
			try {
				Column columnAnnotation = ClassUtils.findField(childType[0], def.orderByField).getAnnotation(Column.class);
				def.orderByColumn = columnAnnotation.name();
			}
			catch (NoSuchFieldException | SecurityException e) {
				throw new JaquError("When using orderBy on a relation the field must exist on child and must have a 'Column' annotation");
			}
		}
		def.direction = many2Many.direction();

		if (fieldDefinition != null) {
			fieldDefinition.relationDefinition = def;
			fieldDefinition.fieldType = FieldType.M2M;
			def.dataType = childType;
			if (def.dataType == null || (null == def.dataType[0].getAnnotation(Entity.class) && null == def.dataType[0].getAnnotation(MappedSuperclass.class)))
				throw new JaquError("IllegalState - field %s "
						+ "was marked as a relationship, but does not point to a TABLE Type", fieldDefinition.columnName);
			def.relationColumnName = relationColumnName;
			def.relationFieldName = relationFieldName;
		}
		if (db.factory.createTable) {
			// Try to get the primary key of the relationship
			Class<?> childPkType = many2Many.childPkType();
			if (Object.class.equals(childPkType)) {
				try {
					childPkType = extractPrimaryKeyFromClass(childType[0]); // all child types must have the same type of primary key
				}
				catch (Exception e) {
					throw new JaquError(
							e,
							"You declared a join table, but JaQu was not able to find your child Primary Key. Try setting the 'childPkType' property on the @one2Many annotation");
				}
			}
			createRelationTable(childType[0], many2Many.joinTableName(), relationFieldName, childPkType, def.relationColumnName, db);
		}
	}

	void addOneToMany(FieldDefinition fieldDefinition, One2Many one2ManyAnnotation, Db db) {
		Class<?>[] childType = one2ManyAnnotation.childType();
		if (Object.class.equals(childType[0])) {
			try {
				childType = new Class<?>[] {(Class<?>) ((ParameterizedType)fieldDefinition.field.getGenericType()).getActualTypeArguments()[0]};
			}
			catch (Exception e) {
				throw new JaquError(e, "Tried to figure out the type of the child relation but couldn't. Try setting the 'childType' annotation parameter on @One2Many annotation");
			}
		}

		String relationFieldName = one2ManyAnnotation.relationFieldName();
		if (relationFieldName == null || "".equals(relationFieldName))
			relationFieldName = tableName;

		String relationColumnName = one2ManyAnnotation.relationColumnName();
		if (null == relationColumnName || "".equals(relationColumnName)) {
			relationColumnName = childType[0].getSimpleName();
		}

		RelationDefinition def = new RelationDefinition();
		def.eagerLoad = one2ManyAnnotation.eagerLoad();
		if (!"".equals(one2ManyAnnotation.joinTableName())) {
			def.relationTableName = one2ManyAnnotation.joinTableName();
		}
		if (!"".equals(one2ManyAnnotation.orderBy())) {
			def.orderByField = one2ManyAnnotation.orderBy();
			try {
				Column columnAnnotation = ClassUtils.findField(childType[0], def.orderByField).getAnnotation(Column.class);
				def.orderByColumn = columnAnnotation.name();
			}
			catch (NoSuchFieldException | SecurityException e) {
				throw new JaquError("When using orderBy on a relation the field must exist on child and must have a 'Column' annotation");
			}
		}
		def.direction = one2ManyAnnotation.direction();
		if (one2ManyAnnotation.cascadeType() != null)
			def.cascadeType = one2ManyAnnotation.cascadeType();

		if (fieldDefinition != null) {
			fieldDefinition.relationDefinition = def;
			fieldDefinition.fieldType = FieldType.O2M;
			def.dataType = childType;
			if (null == def.dataType[0].getAnnotation(Entity.class) && null == def.dataType[0].getAnnotation(MappedSuperclass.class))
				throw new JaquError("IllegalState - field %s was marked as a relationship, but does not point to an Entity Type", fieldDefinition.columnName);
			def.relationColumnName = relationColumnName;
			def.relationFieldName = relationFieldName;
		}

		if (db.factory.createTable && one2ManyAnnotation.joinTableName() != null && !"".equals(one2ManyAnnotation.joinTableName())) {
			// add relation table creation
			// Try to get the primary key of the relationship
			Class<?> childPkType = one2ManyAnnotation.childPkType();
			if (Object.class.equals(childPkType)) {
				try {
					childPkType = extractPrimaryKeyFromClass(childType[0]); // all child types must share the same kind of primary key
				}
				catch (Exception e) {
					throw new JaquError(e, "You declared a join table, but JaQu was not able to find your child Primary Key. Try setting the 'childPkType' property on the @one2Many annotation");
				}
			}
			createRelationTable(childType[0], one2ManyAnnotation.joinTableName(), relationFieldName, childPkType, def.relationColumnName, db);
		}
	}

	FieldDefinition getDefinitionForField(String fieldName) {
		return fieldMap.get(fieldName);
	}

	void mapFields(Db db) {
		Field[] classFields = getAllFields(clazz);
		for (Field f : classFields) {
			if (Modifier.isTransient(f.getModifiers()) || Modifier.isFinal(f.getModifiers()) || Modifier.isStatic(f.getModifiers()) || Constants.IS_LAZY.equals(f.getName()))
				// we ignore this specially generated field for marking lazy O2O relations
				continue;

			// don't persist ignored fields.
			if (null != f.getAnnotation(Transient.class))
				continue;

			Class<?> classType = f.getType();
			if (classType.isPrimitive()) {
				throw new JaquError("Jaqu does not allow primitive types. See documentation! Field %s was decalred %s", f.getName(), f.getType());
			}
			Converter converter = f.getAnnotation(Converter.class);
			if (null != converter) {
				Method[] methods = converter.value().getMethods();
				if (0 < methods.length) {
					for (Method m: methods) {
						if (TableDefinition.TO_DB.equals(m.getName())) {
							classType = m.getReturnType();
							break;
						}
					}
				}
			}
			f.setAccessible(true);
			FieldDefinition fieldDef = new FieldDefinition();
			if (null != f.getAnnotation(Extension.class))
				fieldDef.isExtension = true;
			fieldDef.field = f;
			fieldDef.columnName = f.getName();
			fields.add(fieldDef);
			fieldMap.put(f.getName(), fieldDef);
			if (null != fieldDef.field.getAnnotation(NoUpdateOnSave.class) ||
					null != fieldDef.field.getType().getAnnotation(Immutable.class))
				// if this field is marked as NoUpdateOnSave we mark it here
				fieldDef.noUpdateField = true;

			if (java.time.temporal.Temporal.class.isAssignableFrom(classType) || java.util.Date.class.isAssignableFrom(classType) || java.lang.Number.class.isAssignableFrom(classType)
					|| String.class.isAssignableFrom(classType) || Boolean.class.isAssignableFrom(classType)
					|| Blob.class.isAssignableFrom(classType) || Clob.class.isAssignableFrom(classType)
					|| UUID.class.isAssignableFrom(classType) || classType.isEnum()) {

				if (null != f.getAnnotation(Version.class)) {
					if (null == this.version) {
						fieldDef.isVersion = true;
						this.version = fieldDef;
					}
					else
						throw new JaquError("Too many version fields defined in this class: %s - %s", tableName, clazz);
				}
				// handle column name on annotation
				Column columnAnnotation = f.getAnnotation(Column.class);
				fieldDef.type = getTypes(classType);
				if (String.class.isAssignableFrom(classType) || classType.isEnum()) {
					// strings have a default size
					fieldDef.maxLength = 256;
				}
				if (null != columnAnnotation) {
					if (!StringUtils.isNullOrEmpty(columnAnnotation.name()))
						fieldDef.columnName = columnAnnotation.name();
					int length = columnAnnotation.length();
					if (length != -1)
						fieldDef.maxLength = length;
					fieldDef.unique = columnAnnotation.unique();
					fieldDef.notNull = columnAnnotation.notNull();
				}

				if (!getEnumType(fieldDef, columnAnnotation)) {
					if (null != columnAnnotation && Object.class != columnAnnotation.type())
						fieldDef.dataType = getDataType(columnAnnotation.type());
					else
						fieldDef.dataType = getDataType(f.getType());
				}

				PrimaryKey pkAnnotation = f.getAnnotation(PrimaryKey.class);
				if (pkAnnotation != null) {
					if (null == primaryKeyColumnNames)
						primaryKeyColumnNames = Utils.newArrayList();
					fieldDef.isPrimaryKey = true;
					primaryKeyColumnNames.add(fieldDef);
					this.genType = pkAnnotation.generatorType();
					if (this.genType != null){
						// check if allowed
						if (this.primaryKeyColumnNames.size() > 1){
							throw new JaquError("Too many primary keys with an auto increment field. Using an auto increment " +
									"field requires a single column primary key. For uniqueness consider unique indexs in your table");
						}
						if (genType == GeneratorType.IDENTITY)
							fieldDef.dataType = dialect.getIdentityType();
						else if (this.genType == GeneratorType.SEQUENCE) {
							if (pkAnnotation.seqName() == null)
								throw new JaquError("IllegalArgument - GeneratorType.SEQUENCE must supply a sequence name!!!");
							this.sequenceQuery = db.factory.getDialect().getSequenceQuery(pkAnnotation.seqName());
						}
					}
				}
			}
			else if (Collection.class.isAssignableFrom(classType)) {
				// these are one to many or many to many relations
				fieldDef.dataType = null;
				fieldDef.type = Types.COLLECTION;
				// only entities have relations
				if (clazz.getAnnotation(Entity.class) != null || clazz.getAnnotation(MappedSuperclass.class) != null) {
					this.isAggregateParent = true;
					fieldDef.isSilent = true;
					// find the method...
					String methodName = Pattern.compile(f.getName().substring(0, 1)).matcher(f.getName()).replaceFirst(f.getName().substring(0, 1).toUpperCase());
					try {
						Method m = null;
						if (null == this.inheritedType || InheritedType.NONE == this.inheritedType)
							m = clazz.getDeclaredMethod("get" + methodName);
						else
							m = clazz.getMethod("get" + methodName);
						fieldDef.getter = m;
					}
					catch (NoSuchMethodException nsme) {
						throw new JaquError(nsme, "Relation fields must have a getter in the form of get %s in class %s", methodName, clazz.getName());
					}
					One2Many one2ManyAnnotation = f.getAnnotation(One2Many.class);
					if (one2ManyAnnotation != null) {
						addOneToMany(fieldDef, one2ManyAnnotation, db);
						continue;
					}
					Many2Many many2ManyAnnotation = f.getAnnotation(Many2Many.class);
					if (many2ManyAnnotation != null) {
						addManyToMany(fieldDef, many2ManyAnnotation, db);
					}
				}
			}
			else {
				// this is some kind of Object, we check if it is a table
				Entity entity = classType.getAnnotation(Entity.class);
				MappedSuperclass mapped = classType.getAnnotation(MappedSuperclass.class);
				if (null != entity || null != mapped) {
					// this class is a table
					Column columnAnnotation = f.getAnnotation(Column.class);
					if (columnAnnotation != null && !StringUtils.isNullOrEmpty(columnAnnotation.name())) {
						fieldDef.columnName = columnAnnotation.name();
					}
					fieldDef.type = Types.FK;
					Many2One many2one = f.getAnnotation(Many2One.class);
					if (null == many2one) {
						// its a foreign key
						fieldDef.fieldType = FieldType.FK;
						if (null == this.oneToOneRelations)
							this.oneToOneRelations = Utils.newArrayList();
						this.oneToOneRelations.add(fieldDef);
					}
					else {
						// this is the other side of a O2M relationship which is handled by a join table
						fieldDef.fieldType = FieldType.M2O;
						fieldDef.isSilent = true;
						RelationDefinition def = new RelationDefinition();
						def.eagerLoad = true;
						Class<?> otherSide = f.getType();
						if (!Object.class.equals(many2one.childType()))
							otherSide = many2one.childType();

						One2Many otherSideAnnotation;
						try {
							otherSideAnnotation = ClassUtils.findField(otherSide, many2one.relationFieldName()).getAnnotation(One2Many.class);
						}
						catch (SecurityException | NoSuchFieldException e) {
							throw new JaquError("Field {%s} in class {%s} is anntoated with M2O and declares a parent field {%s} which does not exist!!!", f.getName(), clazz, many2one.relationFieldName());
						}
						def.relationTableName = otherSideAnnotation.joinTableName();
						def.dataType = new Class<?>[] {otherSide};

						def.relationColumnName = otherSideAnnotation.relationFieldName();
						if ("".equals(def.relationColumnName))
							def.relationColumnName = f.getName();
						def.relationFieldName = otherSideAnnotation.relationColumnName();
						if ("".equals(def.relationFieldName))
							def.relationFieldName = clazz.getSimpleName();

						fieldDef.relationDefinition = def;
					}
				}
				else {
					// this will be stored as a blob...
					fieldDef.dataType = dialect.getDataType(Blob.class);
					fieldDef.type = Types.BLOB;

					// handle column name on annotation
					Column columnAnnotation = f.getAnnotation(Column.class);
					if (null != columnAnnotation) {
						if (!StringUtils.isNullOrEmpty(columnAnnotation.name()))
							fieldDef.columnName = columnAnnotation.name();
						fieldDef.unique = columnAnnotation.unique();
						fieldDef.notNull = columnAnnotation.notNull();
						if (Object.class != columnAnnotation.type())
							fieldDef.dataType = getDataType(columnAnnotation.type());
					}
				}
			}
		}
		// make sure the list of fields is sorted according to field type. we want the list to return the normal simple fields first then the
		// FK fields and then O2M and M2M. This way we make sure we have the primary key of the object before we try checking for reentrant.
		Collections.sort(fields);
	}

	private boolean getEnumType(FieldDefinition fieldDef, Column columnAnnotation) {
		if (Types.ENUM == fieldDef.type) {
			 if (null != columnAnnotation && Types.ENUM_INT == columnAnnotation.enumType()) {
				 fieldDef.type = Types.ENUM_INT;
				 fieldDef.dataType = dialect.getDataType(Integer.class);
				 fieldDef.maxLength = 0; // make sure that integer type does not get a length value when creating tables
			 }
			 else {
				 fieldDef.dataType = dialect.getDataType(String.class);
			 }
			 return true;
		}

		return false;
	}

	/**
	 * @return Field[]
	 */
	private <A> Field[] getAllFields(Class<A> clazz) {
		Field[] classFields;
		Inherited inherited = clazz.getAnnotation(Inherited.class);
		if (inherited != null) {
			Field[] superFields = null;
			Class<? super A> superClazz = clazz.getSuperclass();
			superFields = addSuperClassFields(superClazz);
			if (null != superFields) {
				Field[] childFields = clazz.getDeclaredFields();
				classFields = new Field[superFields.length + childFields.length];
				System.arraycopy(superFields, 0, classFields, 0, superFields.length);
				System.arraycopy(childFields, 0, classFields, superFields.length, childFields.length);
			}
			else {
				classFields = clazz.getDeclaredFields();
			}
		}
		else {
			classFields = clazz.getDeclaredFields();
		}
		return classFields;
	}

	private <A> Field[] addSuperClassFields(Class<? super A> superClazz) {
		if (superClazz == null || superClazz.equals(Object.class))
			return null;
		Field[] superSuperFields = addSuperClassFields(superClazz.getSuperclass());

		boolean shouldMap = (superClazz.getAnnotation(MappedSuperclass.class) != null || superClazz.getAnnotation(Entity.class) != null);
		if (!shouldMap)
			return superSuperFields;

		if (null == superSuperFields) {
			// super class is Object.class or not mapped. However this class is mapped so we can get its fields
			return superClazz.getDeclaredFields();
		}
		Field[] declaredFields = superClazz.getDeclaredFields();
		Field[] allFields = new Field[superSuperFields.length + declaredFields.length];
		System.arraycopy(superSuperFields, 0, allFields, 0, superSuperFields.length);
		System.arraycopy(declaredFields, 0, allFields, superSuperFields.length, declaredFields.length);
		return allFields;
	}

	private Types getTypes(Class<?> classType) {
		try {
			if (classType.isEnum())
				return Types.ENUM;
			return Types.valueOf(classType.getSimpleName().toUpperCase());
		}
		catch (RuntimeException e) {
			if (java.util.Date.class.equals(classType))
				return Types.UTIL_DATE;
			else if (java.sql.Date.class.equals(classType))
				return Types.SQL_DATE;
			else
				throw new JaquError(e, e.getMessage());
		}
	}

	void mapOneToOneFields(Db db) {
		if (oneToOneRelations == null)
			return;
		for (FieldDefinition fdef : oneToOneRelations) {
			Class<?> classType = fdef.field.getType();
			TableDefinition<?> def;
			if (null != classType.getAnnotation(Entity.class))
				def = JaquSessionFactory.define(classType, db);
			else
				def = JaquSessionFactory.define(classType, db, false);
			if (def.primaryKeyColumnNames == null || def.primaryKeyColumnNames.isEmpty())
				// no primary keys defined we can't make a DB relation although we expected such a relation to exist.
				throw new JaquError("IllegalState - No primary key columns defined for table %s - no relationship possible", classType);
			else if (def.primaryKeyColumnNames.size() > 1)
				throw new JaquError("UnsupportedOperation - Entity relationship is not supported for complex primary keys. Found in %s", classType);
			else {
				// Single primary key. Here we find out the type and move on....
				for (FieldDefinition innerDef : def.fields) {
					if (innerDef.isPrimaryKey) {
						fdef.dataType = getDataType(innerDef.field.getType());
						fdef.maxLength = innerDef.maxLength;
						break;
					}
				}				
			}
		}
	}

	private String getDataType(Class<?> fieldClass) {
		if (fieldClass.isPrimitive())
			fieldClass = ClassUtils.getWrapperClass(fieldClass);
		else if (fieldClass == java.util.UUID.class)
			fieldClass = java.lang.String.class;
		return dialect.getDataType(fieldClass);
	}

	/*
	 * insert a batch of entities or pojos without entity relationships.
	 */
	void insertBatch(Db db, final int batchSize, Object ... objs) {
		// first we build an insert statement
		SQLStatement stat = new SQLStatement(db);
		StatementBuilder buff = new StatementBuilder("INSERT INTO ");
		StatementBuilder fieldTypes = new StatementBuilder();
		StatementBuilder valueTypes = new StatementBuilder();
		buff.append(tableName).append('(');
		if (InheritedType.DISCRIMINATOR == this.inheritedType) {
			// the inheritance is based on a single table with a discriminator
			fieldTypes.appendExceptFirst(", ");
			fieldTypes.append(this.discriminatorColumn);
			valueTypes.appendExceptFirst(", ");
			valueTypes.append("'" + this.discriminatorValue + "'");
		}
		for (FieldDefinition field : fields) {
			if (field.isSilent || field.isExtension || (field.fieldType != FieldType.NORMAL))
        		// skip everything which is not a plain field (i.e any type of relationship)
        		continue;

        	fieldTypes.appendExceptFirst(", ");
        	fieldTypes.append(field.columnName);

        	valueTypes.appendExceptFirst(", ");
        	valueTypes.append('?');
        }
		buff.append(fieldTypes).append(") VALUES(").append(valueTypes).append(')');
		stat.setSQL(buff.toString());
		int count = 0;
		for (Object o: objs) {
			// add the parameters
			for (FieldDefinition field : fields) {
				if (field.isSilent || field.isExtension || (field.fieldType != FieldType.NORMAL))
	        		// skip everything which is not a plain field (i.e any type of relationship)
	        		continue;

				handleValue(db, o, stat, field);
			}
			stat.prepareBatch();

			if (++count % batchSize == 0) {
		        stat.executeBatch(false);
		    }
		}
		stat.executeBatch(true);
	}

	void insert(Db db, Object obj) {
		if (db.reEntrantCache.checkReEntrent(obj))
			return;
		SQLStatement stat = new SQLStatement(db);
		StatementBuilder buff = new StatementBuilder("INSERT INTO ");
		StatementBuilder fieldTypes = new StatementBuilder();
		StatementBuilder valueTypes = new StatementBuilder();
		buff.append(tableName).append('(');
		if (InheritedType.DISCRIMINATOR == this.inheritedType) {
			// the inheritance is based on a single table with a discriminator
			fieldTypes.appendExceptFirst(", ");
			fieldTypes.append(this.discriminatorColumn);
			valueTypes.appendExceptFirst(", ");
			valueTypes.append("'" + this.discriminatorValue + "'");
		}
		boolean nullIdentityField = false;
		for (FieldDefinition field : fields) {
			if (field.isPrimaryKey && GeneratorType.IDENTITY == genType && null == field.getValue(obj)) {
				// skip identity types because these are auto incremented
        		nullIdentityField = true;
				continue;
			}
			if (field.isExtension || field.isSilent || (field.fieldType != FieldType.NORMAL))
        		// skip everything which is not a plain field (i.e any type of relationship)
        		// its value will be handled in the following update statement
        		continue;

        	if (field.isVersion) {
        		field.field.setAccessible(true);
        		try {
					field.field.set(obj, 0);
				}
				catch (IllegalArgumentException | IllegalAccessException e) {
					// Nothing to do here
					StatementLogger.debug("problem in reflection setting field " + field.field.getName());
				}
        	}
        	fieldTypes.appendExceptFirst(", ");
        	fieldTypes.append(field.columnName);

        	valueTypes.appendExceptFirst(", ");
        	valueTypes.append('?');
            handleValue(db, obj, stat, field);
        }
		buff.append(fieldTypes).append(") VALUES(").append(valueTypes).append(')');
		stat.setSQL(buff.toString());
		if (db.factory.isShowSQL())
			StatementLogger.insert(stat.logSQL());

		if (null != primaryKeyColumnNames && !primaryKeyColumnNames.isEmpty()) {
			if (nullIdentityField && GeneratorType.IDENTITY == genType) {
				// we insert first basically to get the generated primary key on Identity fields
				// note that unlike Identity, Sequence is generated in 'handleValue'
				updateWithId(obj, stat);
			}
			else
				stat.executeUpdate();			
			update(db, obj);
		}
		else {
			// an object with no primary key fields can not have relationships or silent fields so we can just execute the simple db update
			stat.executeUpdate();
		}
	}

	void merge(Db db, Object obj) {
		if (db.reEntrantCache.checkReEntrent(obj))
			return;
		if (primaryKeyColumnNames == null || primaryKeyColumnNames.isEmpty()) {
			throw new JaquError("IllegalState - No primary key columns defined for table %s - no merge possible", obj.getClass());
		}

		if (null == db.factory.getPrimaryKey(obj)) {
			// no primary key so we can only do insert
			db.insert(obj);
			return;
		}
		// check if object exists in the DB
		SQLStatement stat = new SQLStatement(db);
		StatementBuilder buff = new StatementBuilder("SELECT * FROM ");
		buff.append(tableName).append(" WHERE ");
		buff.resetCount();
		for (FieldDefinition field : primaryKeyColumnNames) {
			buff.appendExceptFirst(" AND ");
			buff.append(field.columnName + " = ?");
			handleValue(db, obj, stat, field);
		}

		stat.setSQL(buff.toString());
		if (db.factory.isShowSQL())
			StatementLogger.merge(stat.logSQL());
		stat.executeQuery(rs -> {
			if (rs.next()) {
				// such a row exists do an update
				db.update(obj);
			}
			else
				db.insert(obj);
			return null;
		});
	}

	void update(Db db, Object obj) {
		if (db.reEntrantCache.checkReEntrent(obj))
			return;
		if (null == primaryKeyColumnNames || primaryKeyColumnNames.isEmpty()) {
			throw new JaquError("IllegalState - No primary key columns defined for table %s - can't locate row", obj.getClass());
		}
		SQLStatement stat = new SQLStatement(db);
		Object alias = Utils.newObject(obj.getClass());
		Query<Object> query = Query.from(db, alias);
		String as = query.getSelectTable().getAs();

		StatementBuilder innerUpdate = new StatementBuilder();
		innerUpdate.resetCount();
		boolean hasNoneSilent = false;
		for (FieldDefinition field : fields) {
			if (field.isExtension)
				continue;
			if (!field.isPrimaryKey) {
				if (null != field.field.getAnnotation(Lazy.class)) {
					try {
						Object value = field.getValue(obj);
						if (null != value) {
							Field lazyField = value.getClass().getField(Constants.IS_LAZY);
							boolean isLazy = lazyField.getBoolean(value);
							if (isLazy)
								continue;
						}
						else
							continue; // FIXME we have a problem here when the user actually wants to delete the relation between objects
					}
					catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
						StatementLogger.log("Unable to interrogate Lazy relation " + field.columnName + " " + e.getMessage());
					}
				}
				if (field.isVersion) {
					innerUpdate.appendExceptFirst(", ");
					innerUpdate.append(as + ".");
					innerUpdate.append(field.columnName);
					innerUpdate.append(" = " + field.columnName + " + 1");
					hasNoneSilent = true;
					continue;
				}
				if (!field.isSilent) {
					innerUpdate.appendExceptFirst(", ");
					innerUpdate.append(as + ".");
					innerUpdate.append(field.columnName);
					innerUpdate.append(" = ?");
					hasNoneSilent = true;
				}
				handleValue(db, obj, stat, field);
			}
		}
		if (hasNoneSilent) {
			// if all fields were silent they were handled in handleValue and there would be nothing to do here
			// so we don't do the update.
			boolean firstCondition = true;
			Object primaryKey = null;
			for (FieldDefinition field : primaryKeyColumnNames) {
				Object aliasValue = field.getValue(alias);
				primaryKey = field.getValue(obj);
				if (!firstCondition) {
					query.addConditionToken(ConditionAndOr.AND);
				}
				firstCondition = false;
				query.addConditionToken(new Condition<>(aliasValue, primaryKey, CompareType.EQUAL));
			}
			StatementBuilder buff = dialect.wrapUpdateQuery(innerUpdate, tableName, as);
			stat.setSQL(buff.toString());
			Number aliasValue = null;
			Number lVersion = null;
			if (null != this.version) {
				// if this table is versioned we must find a row that matches our current
				query.addConditionToken(ConditionAndOr.AND);
				aliasValue = (Number)this.version.getValue(alias);
				lVersion = (Number)this.version.getValue(obj);
				query.addConditionToken(new Condition<>(aliasValue, lVersion, CompareType.EQUAL));
			}
			query.appendWhere(stat);
			if (db.factory.isShowSQL())
				StatementLogger.update(stat.logSQL());

			int numOfResults = stat.executeUpdate();
			if (0 == numOfResults) {
				// No update was done. This is probably because of a concurrency error
				// an sql error would be a -1 and a successful update will have a number higher than 0
				if (null != this.version && null != version) {
					db.rollback();
					throw new JaquConcurrencyException(tableName, obj.getClass(), primaryKey, lVersion);
				}
			}
			else if (null != this.version && null != version) {
				// we need to update the instance with the new version
				try {
					this.version.field.set(obj, lVersion.intValue() + 1);
				}
				catch (IllegalArgumentException | IllegalAccessException e) {
					// Nothing to do here
				}
			}
		}
		// if the object inserted successfully and is a Table add the session to it.
		db.addSession(obj);
	}

	@SuppressWarnings("unchecked")
	void delete(Db db, Object obj) {
		db.reEntrantCache.prepareReEntrent(obj);
		if (this.isAggregateParent) {
    		// we have aggregate children
    		for (FieldDefinition fdef: this.getFields()) {
    			if (fdef.fieldType.isCollectionRelation()) { // either O2M or M2M relationship
    				db.deleteParentRelation(fdef, obj); // if it has relations it must be a Table type by design
    			}
    		}
    	}
		if (null != this.oneToOneRelations && !this.oneToOneRelations.isEmpty()) {
			// check for cascade delete on o2o relations
			for (FieldDefinition fdef: this.oneToOneRelations) {
				if (fdef.field.getAnnotation(Cascade.class) != null) {
					// this relation should be deleted as well
					fdef.field.setAccessible(true);
					try {
						Object o2o = fdef.field.get(obj);
						if (null == o2o)
							// attempt to get the relation from the db if exists
							o2o = getOne2OneFromDb(db, obj, primaryKeyColumnNames, fdef);
						if (null != o2o && !db.reEntrantCache.checkReEntrent(o2o))
							db.delete(o2o);
					}
					catch (IllegalArgumentException | IllegalAccessException e) {
						StatementLogger.log("Unable to delete child relation --> " + fdef.field.getName());
					}
				}
			}
		}
        if (null != this.getInterceptor())
        	this.getInterceptor().onDelete(obj);

        // after dealing with the children we delete the object
		if (primaryKeyColumnNames == null || primaryKeyColumnNames.isEmpty()) {
			throw new JaquError("IllegalState - No primary key columns defined for table %s - no update possible", obj.getClass());
		}
		SQLStatement stat = new SQLStatement(db);
		Object alias = Utils.newObject(obj.getClass());
		Query<Object> query = Query.from(db, alias);
		String as = query.getSelectTable().getAs();

		boolean firstCondition = true;
		for (FieldDefinition field : primaryKeyColumnNames) {
			Object value = field.getValue(obj);
			if (null == value) {
				// I don't have a primary key so I can't delete from the underlying db
				return;
			}
			Object aliasValue = field.getValue(alias);
			if (!firstCondition) {
				query.addConditionToken(ConditionAndOr.AND);
			}
			firstCondition = false;
			query.addConditionToken(new Condition<>(aliasValue, value, CompareType.EQUAL));
		}
		StatementBuilder buff = dialect.wrapDeleteQuery(null, tableName, as);
		stat.setSQL(buff.toString());
		query.appendWhere(stat);
		if (db.factory.isShowSQL())
			StatementLogger.delete(stat.logSQL());
		stat.executeUpdate();
		// multi cache is responsible for multi calls to the session thus if removed from the underlying db
		// this object should be removed from the cache.
		db.multiCallCache.removeReEntrent(obj);
	}

	/*
	 * The last identity called using this connection would be the one that inserted the parameter 'obj'. we use it to set the value
	 */
	private void updateWithId(Object obj, SQLStatement stat) {
		if (null != primaryKeyColumnNames) {
			String[] idColumnNames =  primaryKeyColumnNames.stream().map(fd -> fd.columnName).toArray(String[]::new);
			Long generatedId = stat.executeUpdateWithId(idColumnNames);
			if (null != generatedId) {
				try {
					primaryKeyColumnNames.get(0).field.set(obj, generatedId);
				}
				catch (Exception e) {
					throw new JaquError(e, e.getMessage());
				}
			}
			else {
				throw new JaquError("Expected a generated Id but received None. Maybe your DB is not supported. Check supported Db list");
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void handleValue(Db db, Object obj, SQLStatement stat, FieldDefinition field) {
		Object value = field.getValue(obj);
		// Deal with null primary keys (if object is sequence do a sequence query and update object... if identity you need to query the
		// object on the way out).
		if (field.isPrimaryKey && null == value) {
			if (genType == GeneratorType.SEQUENCE) {
				value = db.executeQuery(sequenceQuery, rs -> {
					if (rs.next()) {
						return rs.getLong(1);
					}
					return null;
				});
				try {
					field.field.set(obj, value); // add the new id to the object
				}
				catch (Exception e) {
					throw new JaquError(e, e.getMessage());
				}
			 }
			else if (genType == GeneratorType.UUID || UUID.class.isAssignableFrom(field.field.getType())) {
				try {
					UUID pk = UUID.randomUUID();
					// add the new id to the object
					if (String.class.isAssignableFrom(field.field.getType()))
						field.field.set(obj, pk.toString());
					else if (UUID.class.isAssignableFrom(field.field.getType()))
							field.field.set(obj, pk);

					value = pk.toString(); // the value int underlying Db is varchar
				}
				catch (Exception e) {
					throw new JaquError(e, e.getMessage());
				}
			}
		}
		switch (field.fieldType) {
			case NORMAL:
				Converter converter = field.field.getAnnotation(Converter.class);
				if (null != converter) {
					@SuppressWarnings("rawtypes")
					JaquConverter convert = Utils.newObject(converter.value());
					value = convert.toDb(value);
					stat.addParameter(value);
					break;
				}
				stat.addParameter(value);
				break;
			case FK: {
				// value is a table
				if (value != null) {
					// if this object exists it updates if not it is inserted. ForeignKeys are always table
					Object pk = db.factory.getPrimaryKey(value);
					if (null == pk || !field.noUpdateField) {
						db.reEntrantCache.prepareReEntrent(obj);
						db.merge(value);
					}
					stat.addParameter(pk);
				}
				else
					stat.addParameter(null);
				break;
			}
			case O2M:
			case M2M: {
				// value is a list of tables (we got here only when merge was called from db by outside user
				if (value != null && !((Collection<?>) value).isEmpty()) {
					// value is a Collection type
					for (Object table : (Collection<?>) value) {						
						db.reEntrantCache.prepareReEntrent(obj);
						Object pk = db.factory.getPrimaryKey(table);
						if (null == pk || !field.noUpdateField)
							db.merge(table);
						db.updateRelationship(field, table, obj); // here object can only be a entity
					}
					if (!(value instanceof AbstractJaquCollection)) {
						try {
							if (value instanceof List) {
								JaquList<?> list = new JaquList<>((List<?>)value, db, field, db.getPrimaryKey(obj));
								field.field.set(obj, list);
							}
							else if (value instanceof Set) {
								JaquSet<?> list = new JaquSet<>((Set<?>)value, db, field, db.getPrimaryKey(obj));
								field.field.set(obj, list);
							}
						}
						catch (IllegalArgumentException | IllegalAccessException e) {
							// unable to set keeping the original
						}
					}
				}
				break;
			}
			case M2O: {
				// this is the many side of a join table managed O2M relationship.
				if (value != null) {
					db.reEntrantCache.prepareReEntrent(obj);
					Object pk = db.factory.getPrimaryKey(value);
					if (null == pk || !field.noUpdateField)
						db.merge(value);
					db.updateRelationship(field, obj, value); // the parent(value) is still the one side as 'obj' is the many side
				}
				break;
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Object getOne2OneFromDb(Db db, Object obj, List<FieldDefinition> primaryKeyColumnNames, FieldDefinition fdef) throws IllegalArgumentException, IllegalAccessException {
		Object parent = Utils.newObject(obj.getClass());
		Object desc = Utils.newObject(fdef.field.getType());

		Query query = (Query) db.from(desc);
		QueryJoinWhere queryJoin = query.innerJoin(parent).on(fdef.field.get(parent)).is(desc);

		boolean firstCondition = true;
		for (FieldDefinition field : primaryKeyColumnNames) {
			Object value = field.getValue(obj);
			if (null == value) {
				// I don't have a primary key so I can't delete from the underlying db
				return null;
			}
			Object aliasValue = field.getValue(parent);
			if (!firstCondition) {
				query.addConditionToken(ConditionAndOr.AND);
			}
			firstCondition = false;
			query.addConditionToken(new Condition<>(aliasValue, value, CompareType.EQUAL));
		}
		SQLStatement stat = new SQLStatement(db);
		query.appendWhere(stat);
		return queryJoin.selectFirst();
	}

	int deleteAll(Db db) {
		SQLStatement stat = new SQLStatement(db);
		Object alias = Utils.newObject(this.clazz);
		Query<Object> query = Query.from(db, alias);
		String as = query.getSelectTable().getAs();

		StatementBuilder buff = dialect.wrapDeleteQuery(new StatementBuilder(), tableName, as);
		stat.setSQL(buff.toString());
		if (db.factory.isShowSQL())
			StatementLogger.delete(stat.logSQL());
		return stat.executeUpdate();
	}

	TableDefinition<T> createTableIfRequired(Db db) {
		if (dialect.checkTableExists(tableName, db))
			return this;
		SQLStatement stat = new SQLStatement(db);
		StatementBuilder buff = new StatementBuilder(dialect.getCreateTableStatment(tableName));
		buff.append('(');
		for (FieldDefinition field : fields) {
			if (!field.isSilent && !field.isExtension) {
				buff.appendExceptFirst(", ");
				buff.append(field.columnName).append(' ').append(field.dataType);
				if (field.isPrimaryKey && field.field.getAnnotation(PrimaryKey.class).generatorType() == GeneratorType.IDENTITY) {
					// add identity info
					buff.append(' ').append(this.dialect.getIdentitySuppliment());
				}
				else if (field.maxLength != 0) {
					buff.append('(').append(field.maxLength).append(')');
				}
			}
		}
		if (primaryKeyColumnNames != null && !primaryKeyColumnNames.isEmpty()) {
			buff.append(", PRIMARY KEY(");
			buff.resetCount();
			for (FieldDefinition n : primaryKeyColumnNames) {
				buff.appendExceptFirst(", ");
				buff.append(n.columnName);
			}
			buff.append(')');
		}
		buff.append(')');
		stat.setSQL(buff.toString());
		if (db.factory.isShowSQL())
			StatementLogger.create(stat.logSQL());
		stat.executeUpdate();
		try {
			createIndices(db);
		}
		catch (Exception any){
			StatementLogger.log("Unable to create indices [May be they exist]. " + any.getMessage());
		}
		alterTableDiscriminatorIfRequired(db);
		return this;
	}

	private void createIndices(Db db) {
		// Handle index/ constraint definition
		Indices indices = this.clazz.getAnnotation(Indices.class);
		if (null != indices){
			for (Index index: indices.value()){
				String query = dialect.getIndexStatement(index.name(), this.tableName, index.unique(), index.columns());
				SQLStatement stat = new SQLStatement(db);
				StatementBuilder buff = new StatementBuilder(query);
				stat.setSQL(buff.toString());
				if (db.factory.isShowSQL())
					StatementLogger.alter(stat.logSQL());
				stat.executeUpdate();
			}
		}
	}

	private void alterTableDiscriminatorIfRequired(Db db) {
		if (inheritedType == InheritedType.DISCRIMINATOR && !dialect.checkDiscriminatorExists(tableName, discriminatorColumn, db)) {
			SQLStatement stat = new SQLStatement(db);
			StatementBuilder buff = new StatementBuilder(dialect.getDiscriminatorStatment(tableName, discriminatorColumn));
			stat.setSQL(buff.toString());
			if (db.factory.isShowSQL())
				StatementLogger.alter(stat.logSQL());
			stat.executeUpdate();
		}
	}

	void initSelectObject(SelectTable<T> table, Object obj, Map<Object, SelectColumn<T>> map) {
		for (FieldDefinition def : fields) {
			Object o = def.initWithNewObject(obj);
			SelectColumn<T> column = new SelectColumn<>(table, def);
			map.put(o, column);
		}
	}

	@SuppressWarnings("unchecked")
	T readRow(ResultSet rs, Db db) {
		T item = Utils.newObject(clazz);
		if (null != primaryKeyColumnNames && !primaryKeyColumnNames.isEmpty()) {
			// this class has a primary key
			// 1. get the primaryKey value, 2. check if we have an object with such value in cache, 3. if so return it
			// if not continue.			
			for (FieldDefinition def: primaryKeyColumnNames) {
				Object key = def.read(rs, dialect);
				Object o = db.multiCallCache.checkReEntrent(clazz, key);
				if (null == o)
					o = db.reEntrantCache.checkReEntrent(clazz, key);
				if (null != o) {
					try {
						return (T)o;
					}
					catch (ClassCastException cce) {
						// This error should not happen. For now we just ignore it.
					}
				}
				else {					
					doRead(rs, db, item, def);
				}
			}
		}
		for (FieldDefinition def: fields) {
			if (!def.isPrimaryKey) {
				db.reEntrantCache.prepareReEntrent(item);
				doRead(rs, db, item, def);
				db.reEntrantCache.removeReEntrent(item);
			}
		}
		if (null != primaryKeyColumnNames && !primaryKeyColumnNames.isEmpty()) {
			db.multiCallCache.prepareReEntrent(item);			
		}
		return item;
	}

	SQLStatement getSelectList(Db db, String as) {
		SQLStatement selectList = new SQLStatement(db);
		int i = 0;
		for (FieldDefinition def: fields) {
			if (!def.isSilent && !def.isExtension) {
				if (i > 0) {
					selectList.appendSQL(", ");
				}
				selectList.appendSQL(as + "." + def.columnName);
			}
			i++;
		}
		return selectList;
	}

	<Y, X> SQLStatement getSelectList(Query<Y> query, X x) {
		SQLStatement selectList = new SQLStatement(query.getDb());
		int i = 0;
		for (FieldDefinition def: fields) {
			if (def.isSilent)
				continue;
			if (i > 0) {
				selectList.appendSQL(", ");
			}
			Object obj = def.getValue(x);
			query.appendSQL(selectList, obj, def.field.getType().isEnum(), def.field.getType());

			// since the 'X' type object does not necessarily have field types as the queried objects in the result set we add a column name
			// that conforms with the 'X' type
			selectList.appendSQL(" AS " + def.columnName);
			i++;
		}
		return selectList;
	}

	/**
	 * Returns a list of primary key definitions
	 *
	 * @return List<FieldDefinition>
	 */
	List<FieldDefinition> getPrimaryKeyFields() {
		return primaryKeyColumnNames;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void doRead(ResultSet rs, Db db, T item, FieldDefinition def) {
		if (StatementLogger.isDebugEnabled())
			StatementLogger.debug("Working on Field: " + def.field.getName());
		if (!def.isSilent) {
			Object o;
			try {
				o = def.read(rs, dialect);
			}
			catch (JaquError sqle) {
				if (def.isExtension)
					return;
				throw sqle;
			}
			Converter converter = def.field.getAnnotation(Converter.class);
			if (null != converter) {				
				JaquConverter convert = Utils.newObject(converter.value());
				o = convert.fromDb(o);
			}
			def.setValue(item, o, db);
		}
		else
			// probably a relation is loaded
			def.setValue(item, null, db);
	}
	
	private Class<?> extractPrimaryKeyFromClass(Class<?> childType) {
		Field[] lFields = getAllFields(childType);
		for (Field field: lFields) {
			if (null != field.getAnnotation(PrimaryKey.class))
				return field.getType();
		}
		return null;
	}

	/*
	 * Create the join table if does not exist
	 *
	 * @param childType - The class of the other side of relation.
	 * @param joinTableName
	 * @param myColumnNameInRelation
	 * @param relationPkName
	 * @param relationColumnName
	 */
	private void createRelationTable(Class<?> childType, String joinTableName, String myColumnNameInRelation, Class<?> relationPkClass,
			String relationColumnName, Db db) {
		try {
			if (dialect.checkTableExists(joinTableName, db))
				return;
			FieldDefinition myPkDef = this.getPrimaryKeyFields().get(0);
			String myPkLength = myPkDef.maxLength != 0 ? "(" + myPkDef.maxLength + ")" : "";

			// Locate the pk field of the target relation
			Field[] lFields = getAllFields(childType);
			String relationPkLength = "";
			for (Field f : lFields) {
				if (f.getAnnotation(PrimaryKey.class) != null) {
					// this is the primary key
					Column columnAnnotation = f.getAnnotation(Column.class);
					if (columnAnnotation != null) {
						relationPkLength = columnAnnotation.length() != -1 ? "(" + columnAnnotation.length() + ")" : "";
					}
					break; // Pk Found
				}
			}

			StatementBuilder builder = new StatementBuilder(dialect.getCreateTableStatment(joinTableName)).append(" (").append(myColumnNameInRelation).append(" ");
			builder.append(getDataType(primaryKeyColumnNames.get(0).field.getType())).append(myPkLength + ", ").append(relationColumnName).append(" ");
			builder.append(dialect.getDataType(relationPkClass)).append(relationPkLength + ")");
			db.executeUpdate(false, builder.toString());
		}
		catch (Exception e) {
			throw new JaquError(e, e.getMessage());
		}
	}

	/**
	 * Return the interceptor instance for this table.
	 * @return Interceptor
	 */
	@SuppressWarnings("rawtypes" )
	CRUDInterceptor getInterceptor() {
		return interceptor;
	}

	/**
	 * returns true if the interceptor handles the given event.
	 *
	 * @param update
	 * @return boolean
	 */
	boolean hasInterceptEvent(Event event) {
		for (Event interceptorEvent: interceptorEvents) {
			if (Event.ALL == interceptorEvent || interceptorEvent == event)
				return true;
		}
		return false;
	}

	GeneratorType getGenerationtype() {
		return this.genType;
	}
}