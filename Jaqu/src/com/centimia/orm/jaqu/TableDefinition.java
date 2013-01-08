/*
 * Copyright (c) 2007-2010 Centimia Ltd.
 * All rights reserved.  Unpublished -- rights reserved
 *
 * Use of a copyright notice is precautionary only, and does
 * not imply publication or disclosure.
 *  
 * Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
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
import java.util.regex.Pattern;

import com.centimia.orm.jaqu.annotation.Column;
import com.centimia.orm.jaqu.annotation.Entity;
import com.centimia.orm.jaqu.annotation.Event;
import com.centimia.orm.jaqu.annotation.Index;
import com.centimia.orm.jaqu.annotation.Indices;
import com.centimia.orm.jaqu.annotation.Inherited;
import com.centimia.orm.jaqu.annotation.Interceptor;
import com.centimia.orm.jaqu.annotation.JaquIgnore;
import com.centimia.orm.jaqu.annotation.Many2Many;
import com.centimia.orm.jaqu.annotation.MappedSuperclass;
import com.centimia.orm.jaqu.annotation.One2Many;
import com.centimia.orm.jaqu.annotation.PrimaryKey;
import com.centimia.orm.jaqu.util.ClassUtils;
import com.centimia.orm.jaqu.util.JdbcUtils;
import com.centimia.orm.jaqu.util.StatementBuilder;
import com.centimia.orm.jaqu.util.StringUtils;
import com.centimia.orm.jaqu.util.Utils;

/**
 * A table definition contains the index definitions of a table, the field definitions, the table name, and other meta data.
 * 
 * @param <T> the table type
 */
class TableDefinition<T> {

	enum FieldType {
		NORMAL, FK, M2M, O2M
	};
	
	/**
	 * The meta data of a field.
	 */
	static class FieldDefinition implements Comparable<FieldDefinition> {
		String columnName;
		Field field;
		String dataType;
		int maxLength = 0;
		boolean isPrimaryKey;
		FieldType fieldType = FieldType.NORMAL;
		RelationDefinition relationDefinition;
		public boolean isSilent = false;
		private Types type;
		Method getter;
		public boolean unique;
		public boolean notNull;

		Object getValue(Object obj) {
			try {
				if (type == Types.ENUM){
					Object enumObj = field.get(obj);
					if (null != enumObj)
						return enumObj.toString();
					return null;
				}
				return field.get(obj);
			}
			catch (Exception e) {
				throw new JaquError(e, "In fieldDefinition.getValue -> %s", e.getMessage());
			}
		}

		Object initWithNewObject(Object obj) {
			if (type == Types.ENUM) {
				Class<?> enumClass = field.getType();
				String enumValue = enumClass.getEnumConstants()[0].toString();
				setValue(obj, enumValue, null);
				return enumValue;
			}
			else {
				Object o = Utils.newObject(field.getType());
				setValue(obj, o, null);
				return o;
			}
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		void setValue(final Object obj, Object o, final Db db) {
			try {
				Object tmp = o;
				switch (fieldType) {
					case NORMAL:
						// if 'o' equals null then setting the 'enum' will cause a nullPointerException
						if (type == Types.ENUM && null != o) {
							Class enumClass = field.getType();
							field.set(obj, Enum.valueOf(enumClass, (String)o));
						}
						else
							field.set(obj, o);
						break;
					case FK: {
						o = Utils.convert(o, field.getType());
						if (o != null && o.getClass().getAnnotation(Entity.class) != null
								&& !tmp.getClass().isAssignableFrom(field.getType())) {
							Object reEntrant = db.checkReEntrant(o.getClass(), tmp);
							if (reEntrant != null) {
								o = reEntrant;
							}
							else {
								db.prepareRentrant(obj);
								List<?> result = db.from(o).primaryKey().is(tmp.toString()).select();
								if (!result.isEmpty())
									o = result.get(0);
								else
									throw new JaquError("\nData Consistency error - Foreign relation does not exist!!\nError column was {%s}"
													+ " with value %s in table %s" 
													+ "\nmissing in table %s", field.getName(), tmp, obj.getClass().getName(), o.getClass().getName());
								db.removeReentrant(obj);
							}
						}
						field.set(obj, o);
						break;
					}
					case O2M: {
						if (relationDefinition.eagerLoad && db != null) {
							db.prepareRentrant(obj);
							if (relationDefinition.relationTableName != null) {
								List<?> resultList = db.getRelationByRelationTable(this, db.factory.getPrimaryKey(obj),	relationDefinition.dataType);
								if (!resultList.isEmpty()) {
									if (this.field.getType().isAssignableFrom(resultList.getClass()))
										o = new JaquList(resultList, db, this, db.factory.getPrimaryKey(obj));
									else {
										// only when the type is a Set type we will be here
										HashSet set = Utils.newHashSet();
										set.addAll(resultList);
										o = new JaquSet(set, db, this, db.factory.getPrimaryKey(obj));
									}
								}
								else {
									// on eager loading if no result exists we set to an empty collection;
									if (this.field.getType().isAssignableFrom(resultList.getClass()))
										o = new JaquList(Utils.newArrayList(), db, this, db.factory.getPrimaryKey(obj)) ;
									else
										o = new JaquSet(Utils.newHashSet(), db, this, db.factory.getPrimaryKey(obj));
								}
							}
							else {
								Object descriptor = Utils.newObject(relationDefinition.dataType);
								List<?> resultList = db.from(descriptor).where(new StringFilter() {
									
									public String getConditionString(ISelectTable<?> st) {
										FieldDefinition fdef = ((SelectTable)st).getAliasDefinition().getDefinitionForField(relationDefinition.relationFieldName);
										if (null != fdef)
											// this is the case when it is a two sided relationship. To allow that the name of the column in the DB and the name of the field are
											// different we use the columnName property.
											return  st.getAs() + "." + fdef.columnName + " = " + db.factory.getPrimaryKey(obj).toString();
										// This is the case of one sided relationship. In this case the name of the FK is given to us in the relationFieldName
										return st.getAs() + "." + relationDefinition.relationFieldName + " = " + db.factory.getPrimaryKey(obj).toString();
									}
								}).select();
								if (!resultList.isEmpty())
									if (this.field.getType().isAssignableFrom(resultList.getClass()))
										o = new JaquList(resultList, db, this, db.factory.getPrimaryKey(obj));
									else {
										// only when the type is a Set type we will be here
										HashSet set = Utils.newHashSet();
										set.addAll(resultList);
										o = new JaquSet(set, db, this, db.factory.getPrimaryKey(obj));
									}
								else {
									// on eager loading if no result exists we set to an empty collection;
									if (this.field.getType().isAssignableFrom(resultList.getClass()))
										o = new JaquList(Utils.newArrayList(), db, this, db.factory.getPrimaryKey(obj)) ;
									else
										o = new JaquSet(Utils.newHashSet(), db, this, db.factory.getPrimaryKey(obj));
								}
							}
							db.removeReentrant(obj);
						}
						else {
							// instrument this instance of the class
							Field dbField = obj.getClass().getField("db");
							// put the open connection on the object. As long as the connection is open calling the getter method on the
							// 'obj' will produce the relation
							dbField.set(obj, db);
							o = null;
						}
						field.set(obj, o);
						break;
					}
					case M2M: {
						// instrument this instance of the class
						Field dbField = obj.getClass().getField("db");
						// put the open connection on the object. As long as the connection is open calling the getter method on the 'obj'
						// will produce the relation
						dbField.set(obj, db);
						o = null;
						break;
					}
					default:
						throw new JaquError("IllegalState - Field %s " 
								+ "was marked as relation but has no relation MetaData in define method", columnName);
				}
			}
			catch (Exception e) {
				throw new JaquError(e, e.getMessage());
			}
		}

		Object read(ResultSet rs, Dialect DIALECT) {
			try {
				return DIALECT.getValueByType(type, rs, this.columnName);
			}
			catch (SQLException e) {
				throw new JaquError(e, e.getMessage());
			}
		}

		public int compareTo(FieldDefinition o) {
			if (columnName.equals(o.columnName))
				return 0;
			if (fieldType.ordinal() >= o.fieldType.ordinal())
				return 6;
			else
				return -6;
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
		Class<?> dataType;
		/** type of cascade relation with the related child. relevant to O2M relations only. Currently only CascadeType.DELETE is supported */
		CascadeType cascadeType = CascadeType.NONE;
	}

	final Dialect DIALECT;
	final String tableName;
	private final Class<T> clazz;
	private HashMap<String, FieldDefinition> fieldMap = Utils.newHashMap();
	private List<FieldDefinition> fields = Utils.newArrayList();
	private List<FieldDefinition> primaryKeyColumnNames;
	private List<FieldDefinition> oneToOneRelations;
	boolean isAggregateParent = false;
	private GeneratorType genType = GeneratorType.NONE;
	private String sequenceQuery = null;
	InheritedType inheritedType = null;
	char discriminatorValue;
	String discriminatorColumn;
	@SuppressWarnings("rawtypes")
	private CRUDInterceptor	interceptor;
	private Event[]	interceptorEvents;

	TableDefinition(Class<T> clazz, Dialect DIALECT) {
		this.DIALECT = DIALECT;
		this.clazz = clazz;
		String nameOfTable = clazz.getSimpleName();
		// Handle table annotation if entity and Table annotations exist
	
		com.centimia.orm.jaqu.annotation.Table tableAnnotation = clazz.getAnnotation(com.centimia.orm.jaqu.annotation.Table.class);
		if (tableAnnotation != null) {
			if (tableAnnotation.name() != null && !"".equals(tableAnnotation.name()))
				nameOfTable = tableAnnotation.name();
		}
		if (clazz.getAnnotation(Entity.class) != null) {
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
		
		com.centimia.orm.jaqu.annotation.Interceptor interceptorAnnot = getInterceptorAnnotation(clazz);
		if (null != interceptorAnnot) {
			this.interceptorEvents = interceptorAnnot.event();
			try {
				interceptor = interceptorAnnot.Class().newInstance();
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
		
		Interceptor interceptor = clazz.getAnnotation(Interceptor.class);
		return null == interceptor ? interceptorAnnot : interceptor;
	}

	List<FieldDefinition> getFields() {
		return fields;
	}

	void addManyToMany(FieldDefinition fieldDefinition, Many2Many many2Many, Db db) {
		Class<?> childType = many2Many.childType();
		if (Object.class.equals(childType)) {
			try {
				childType = (Class<?>) ((ParameterizedType)fieldDefinition.field.getGenericType()).getActualTypeArguments()[0];
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
			relationColumnName = childType.getSimpleName();
		}
			
		RelationDefinition def = new RelationDefinition();
		def.eagerLoad = false;
		def.relationTableName = many2Many.joinTableName();

		if (fieldDefinition != null) {
			fieldDefinition.relationDefinition = def;
			fieldDefinition.fieldType = FieldType.M2M;
			def.dataType = childType;
			if (def.dataType == null || def.dataType.getAnnotation(Entity.class) == null)
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
					childPkType = extractPrimaryKeyFromClass(childType);
				}
				catch (Exception e) {
					throw new JaquError(
							e,
							"You declared a join table, but JaQu was not able to find your child Primary Key. Try setting the 'childPkType' property on the @one2Many annotation");
				}
			}
			createRelationTable(childType, many2Many.joinTableName(), relationFieldName, childPkType, def.relationColumnName, db);
		}
	}

	void addOneToMany(FieldDefinition fieldDefinition, One2Many one2ManyAnnotation, Db db) {
		Class<?> childType = one2ManyAnnotation.childType();
		if (Object.class.equals(childType)) {
			try {
				childType = (Class<?>) ((ParameterizedType)fieldDefinition.field.getGenericType()).getActualTypeArguments()[0];
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
			relationColumnName = childType.getSimpleName();
		}
		
		RelationDefinition def = new RelationDefinition();
		def.eagerLoad = one2ManyAnnotation.eagerLoad();
		if (!"".equals(one2ManyAnnotation.joinTableName())) {
			def.relationTableName = one2ManyAnnotation.joinTableName();
		}
		if (one2ManyAnnotation.cascadeType() != null)
			def.cascadeType = one2ManyAnnotation.cascadeType();

		if (fieldDefinition != null) {
			fieldDefinition.relationDefinition = def;
			fieldDefinition.fieldType = FieldType.O2M;
			def.dataType = childType;
			if (def.dataType.getAnnotation(Entity.class) == null)
				throw new JaquError("IllegalState - field %s was marked as a relationship, but does not point to an Entity Type", fieldDefinition.columnName);
			def.relationColumnName = relationColumnName;
			def.relationFieldName = relationFieldName;
		}
		
		if (db.factory.createTable){
			// add relation table creation
			if (one2ManyAnnotation.joinTableName() != null && !"".equals(one2ManyAnnotation.joinTableName())) {
				// Try to get the primary key of the relationship
				Class<?> childPkType = one2ManyAnnotation.childPkType();
				if (Object.class.equals(childPkType)) {
					try {
						childPkType = extractPrimaryKeyFromClass(childType);
					}
					catch (Exception e) {
						throw new JaquError(e, "You declared a join table, but JaQu was not able to find your child Primary Key. Try setting the 'childPkType' property on the @one2Many annotation");
					}
				}
				createRelationTable(childType, one2ManyAnnotation.joinTableName(), relationFieldName, childPkType, def.relationColumnName, db);
			}
		}
	}

	FieldDefinition getDefinitionForField(String fieldName) {
		return fieldMap.get(fieldName);
	}
	
	void mapFields(Db db) {
		Field[] classFields = getAllFields(clazz);
		for (Field f : classFields) {
			if (Modifier.isTransient(f.getModifiers()))
				continue;

			if (Modifier.isFinal(f.getModifiers()))
				continue;
			
			if (Modifier.isStatic(f.getModifiers()))
				continue;
			
			// don't persist ignored fields.
			if (f.getAnnotation(JaquIgnore.class) != null)
				continue;
			Class<?> classType = f.getType();
			if (java.util.Date.class.isAssignableFrom(classType) || java.lang.Number.class.isAssignableFrom(classType)
					|| String.class.isAssignableFrom(classType) || Boolean.class.isAssignableFrom(classType)
					|| Blob.class.isAssignableFrom(classType) || Clob.class.isAssignableFrom(classType) || classType.isArray() || classType.isEnum() || classType.isPrimitive()) {
				f.setAccessible(true);
				FieldDefinition fieldDef = new FieldDefinition();
				fieldDef.field = f;
				fieldDef.columnName = f.getName();
				fieldDef.dataType = getDataType(f);
				fieldDef.type = getTypes(classType);
				fields.add(fieldDef);
				fieldMap.put(f.getName(), fieldDef);
				if (String.class.isAssignableFrom(classType)|| classType.isEnum()) {
					// strings have a default size
					fieldDef.maxLength = 256;
				}
				// handle column name on annotation
				Column columnAnnotation = f.getAnnotation(Column.class);
				if (null != columnAnnotation) {
					if (!StringUtils.isNullOrEmpty(columnAnnotation.name()))
						fieldDef.columnName = columnAnnotation.name();
					int length = columnAnnotation.length();
					if (length != -1)
						fieldDef.maxLength = length;
					fieldDef.unique = columnAnnotation.unique();
					fieldDef.notNull = columnAnnotation.notNull();
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
							fieldDef.dataType = DIALECT.getIdentityType();
						else if (this.genType == GeneratorType.SEQUENCE) {
							if (pkAnnotation.seqName() == null)
								throw new JaquError("IllegalArgument - GeneratorType.SEQUENCE must supply a sequence name!!!");
							StatementBuilder builder = new StatementBuilder("SELECT ").append(pkAnnotation.seqName()).append(".nextval from dual");
							this.sequenceQuery = builder.toString();
						}
					}
				}
			}
			else if (Collection.class.isAssignableFrom(classType)) {
				// these are one to many or many to many relations.
				FieldDefinition fieldDef = new FieldDefinition();
				f.setAccessible(true);
				fieldDef.field = f;
				fieldDef.columnName = f.getName();
				fieldDef.dataType = null;
				fieldDef.type = Types.COLLECTION;
				fields.add(fieldDef);
				fieldMap.put(f.getName(), fieldDef);
				// only entities have relations
				if (clazz.getAnnotation(Entity.class) != null) {
					this.isAggregateParent = true;
					fieldDef.isSilent = true;
					// find the method...
					String methodName = Pattern.compile(f.getName().substring(0, 1)).matcher(f.getName()).replaceFirst(f.getName().substring(0, 1).toUpperCase());
					try {
						Method m = null;
						if (this.inheritedType == null)
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
						continue;
					}
				}
			}
			else {
				// this is some kind of Object, we check if it is a table
				if (classType.getAnnotation(Entity.class) != null) {
					// this class is a table
					f.setAccessible(true);
					FieldDefinition fieldDef = new FieldDefinition();
					fieldDef.field = f;
					fieldDef.type = Types.FK;
					fieldDef.columnName = f.getName();
					fieldDef.fieldType = FieldType.FK;
					if (this.oneToOneRelations == null)
						this.oneToOneRelations = Utils.newArrayList();
					this.oneToOneRelations.add(fieldDef);
					fields.add(fieldDef);
					fieldMap.put(f.getName(), fieldDef);
					Column columnAnnotation = f.getAnnotation(Column.class);
					if (columnAnnotation != null) {
						if (!StringUtils.isNullOrEmpty(columnAnnotation.name()))
							fieldDef.columnName = columnAnnotation.name();
					}
				}
				else {
					// this will be stored as a blob...
					f.setAccessible(true);
					FieldDefinition fieldDef = new FieldDefinition();
					fieldDef.field = f;
					fieldDef.columnName = f.getName();
					fieldDef.dataType = DIALECT.getDataType(Blob.class);
					fieldDef.type = Types.BLOB;
					fields.add(fieldDef);
					fieldMap.put(f.getName(), fieldDef);
					
					// handle column name on annotation
					Column columnAnnotation = f.getAnnotation(Column.class);
					if (null != columnAnnotation) {
						if (!StringUtils.isNullOrEmpty(columnAnnotation.name()))
							fieldDef.columnName = columnAnnotation.name();
						fieldDef.unique = columnAnnotation.unique();
						fieldDef.notNull = columnAnnotation.notNull();
					}
				}
			}
		}
		// make sure the list of fields is sorted according to field type. we want the list to return the normal simple fields first then the
		// FK fields and then O2M and M2M. This way we make sure we have the primary key of the object before we try checking for reentrant.
		Collections.sort(fields);
	}

	/**
	 * @return
	 */
	private <A> Field[] getAllFields(Class<A> clazz) {
		Field[] classFields;
		Inherited inherited = clazz.getAnnotation(Inherited.class);
		if (inherited != null) {
			Field[] superFields = null;
			Class<? super A> superClazz = clazz.getSuperclass();
			superFields = addSuperClassFields(superClazz);
			if (superFields != null) {
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

		if (superSuperFields == null) {
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
			TableDefinition<?> def = JaquSessionFactory.define(classType, db);
			if (def.primaryKeyColumnNames == null || def.primaryKeyColumnNames.isEmpty())
				// no primary keys defined we can't make a DB relation although we expected such a relation to exist.
				throw new JaquError("IllegalState - No primary key columns defined for table %s - no relationship possible", classType);
			else if (def.primaryKeyColumnNames.size() > 1)
				throw new JaquError("UnsupportedOperation - Entity relationship is not supported for complex primary keys. Found in %s", classType);
			else {
				// Single primary key is here we find out the type and move on....
				Field primaryKeyDataType = null;
				for (FieldDefinition innerDef : def.fields) {
					if (innerDef.isPrimaryKey) {
						primaryKeyDataType = innerDef.field;
						break;
					}
				}
				fdef.dataType = getDataType(primaryKeyDataType);
			}
		}
	}

	private String getDataType(Field field) {
		Class<?> fieldClass = field.getType();
		if (fieldClass.isPrimitive())
			fieldClass = ClassUtils.getWrapperClass(fieldClass);
		return DIALECT.getDataType(fieldClass);
	}

	void insert(Db db, Object obj) {
		if (db.checkReEntrant(obj))
			return;
		SQLStatement stat = new SQLStatement(db);
		StatementBuilder buff = new StatementBuilder("INSERT INTO ");
		StatementBuilder fieldTypes = new StatementBuilder(), valueTypes = new StatementBuilder();
		buff.append(tableName).append('(');		
		for (FieldDefinition field : fields) {
			if (field.isPrimaryKey && GeneratorType.IDENTITY == genType)
				// skip identity types because these are auto incremented
        		continue;
			
			if (field.isSilent)
				// skip silent fields because they don't really exist.
        		continue;
        	
        	if (field.fieldType != FieldType.NORMAL)
        		// skip everything which is not a plain field (i.e any type of relationship)
        		continue;
        	
        	fieldTypes.appendExceptFirst(", ");
        	fieldTypes.append(field.columnName);
        	
        	valueTypes.appendExceptFirst(", ");
        	valueTypes.append('?');
            handleValue(db, obj, stat, field);
        }
		buff.append(fieldTypes).append(") VALUES(").append(valueTypes).append(')');
        stat.setSQL(buff.toString());
		if (db.factory.isShowSQL())
			StatementLogger.insert(stat.getSQL());
		
		if (null != primaryKeyColumnNames && !primaryKeyColumnNames.isEmpty()) {
			if (GeneratorType.IDENTITY == genType)
				// we insert first basically to get the generated primary key on Identity fields
				// note that unlike Idnetity, Sequence is generated in 'handleValue'
				updateWithId(obj, stat);
			else
				stat.executeUpdate();
			update(db, obj);
		}
		else {
			stat.executeUpdate();
		}		
	}

	void merge(Db db, Object obj) {
		if (db.checkReEntrant(obj))
			return;
		if (primaryKeyColumnNames == null || primaryKeyColumnNames.isEmpty()) {
			throw new JaquError("IllegalState - No primary key columns defined for table %s - no merge possible", obj.getClass());
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
			StatementLogger.merge(stat.getSQL());
		ResultSet rs  = stat.executeQuery();
		try {
			if (rs.next()) {
				// such a row exists do an update
				db.update(obj);
			}
			else
				db.insert(obj);
		}
		catch (SQLException e) {
			throw new JaquError(e, e.getMessage());
		}
		finally {
            JdbcUtils.closeSilently(rs);
        }
	}
	
	private void handleValue(Db db, Object obj, SQLStatement stat, FieldDefinition field) {
		Object value = field.getValue(obj);
		// Deal with null primary keys (if object is sequence do a sequence query and update object... if identity you need to query the
		// object on the way out).
		if (field.isPrimaryKey && value == null && genType == GeneratorType.SEQUENCE) {
			ResultSet rs = null;
			try {
				rs = db.executeQuery(sequenceQuery);
				if (rs.next()) {
					value = rs.getLong(1);
				}
				field.field.set(obj, value); // add the new id to the object
			}
			catch (SQLException e) {
				throw new JaquError(e, "Unable to generate key.");
			}
			catch (Exception e) {
				throw new JaquError(e, e.getMessage());
			}
			finally {
				JdbcUtils.closeSilently(rs);
			}
		}
		switch (field.fieldType) {
			case NORMAL:
				stat.addParameter(value);
				break;
			case FK: {
				// value is a table
				if (value != null) {
					// if this object exists it updates if not it is inserted. ForeignKeys are always table
					db.prepareRentrant(obj);
					db.merge(value);
					db.removeReentrant(obj);
					stat.addParameter(db.factory.getPrimaryKey(value));
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
						db.prepareRentrant(obj);
						db.merge(table);
						db.updateRelationship(field, table, obj); // here object can only be a entity
						db.removeReentrant(obj);
					}
				}
				break;
			}
		}
	}

	void update(Db db, Object obj) {
		if (db.checkReEntrant(obj))
			return;
		if (primaryKeyColumnNames == null || primaryKeyColumnNames.isEmpty()) {
			throw new JaquError("IllegalState - No primary key columns defined for table %s - can't locate row", obj.getClass());
		}
		SQLStatement stat = new SQLStatement(db);
		Object alias = Utils.newObject(obj.getClass());
		Query<Object> query = Query.from(db, alias);
		String as = query.getSelectTable().getAs();
		
		StatementBuilder innerUpdate = new StatementBuilder();
		innerUpdate.resetCount();
		for (FieldDefinition field : fields) {
			if (!field.isPrimaryKey) {
				if (!field.isSilent) {
					innerUpdate.appendExceptFirst(", ");
					innerUpdate.append(as + ".");
					innerUpdate.append(field.columnName);
					innerUpdate.append(" = ?");
				}
				handleValue(db, obj, stat, field);
			}
		}
		boolean firstCondition = true;
		for (FieldDefinition field : primaryKeyColumnNames) {
			Object aliasValue = field.getValue(alias);
			Object value = field.getValue(obj);
			if (!firstCondition) {
				query.addConditionToken(ConditionAndOr.AND);
			}
			firstCondition = false;
			query.addConditionToken(new Condition<Object>(aliasValue, value, CompareType.EQUAL));
		}
		StatementBuilder buff = DIALECT.wrapUpdateQuery(innerUpdate, tableName, as);		
		stat.setSQL(buff.toString());
		query.appendWhere(stat);
		if (db.factory.isShowSQL())
			StatementLogger.update(stat.getSQL());
		stat.executeUpdate();

		// if the object inserted successfully and is a Table add the session to it.
		db.addSession(obj);
	}

	/*
	 * The last identity called using this connection would be the one that inserted the parameter 'obj'. we use it to set the value
	 */
	private void updateWithId(Object obj, SQLStatement stat) {
		Long generatedId = stat.executeUpdateWithId();
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

	void delete(Db db, Object obj) {
		if (primaryKeyColumnNames == null || primaryKeyColumnNames.isEmpty()) {
			throw new JaquError("IllegalState - No primary key columns defined for table %s - no update possible", obj.getClass());
		}
		SQLStatement stat = new SQLStatement(db);
		Object alias = Utils.newObject(obj.getClass());
		Query<Object> query = Query.from(db, alias);
		String as = query.getSelectTable().getAs();
		StatementBuilder innerDelete = new StatementBuilder();
		
		boolean firstCondition = true;
		for (FieldDefinition field : primaryKeyColumnNames) {
			Object aliasValue = field.getValue(alias);
			Object value = field.getValue(obj);
			if (!firstCondition) {
				query.addConditionToken(ConditionAndOr.AND);
			}
			firstCondition = false;
			query.addConditionToken(new Condition<Object>(aliasValue, value, CompareType.EQUAL));
		}
		StatementBuilder buff = DIALECT.wrapDeleteQuery(innerDelete, tableName, as);
		stat.setSQL(buff.toString());
		query.appendWhere(stat);
		if (db.factory.isShowSQL())
			StatementLogger.delete(stat.getSQL());
		stat.executeUpdate();
	}

	int deleteAll(Db db) {
		SQLStatement stat = new SQLStatement(db);
		Object alias = Utils.newObject(this.clazz);
		Query<Object> query = Query.from(db, alias);
		String as = query.getSelectTable().getAs();
		
		StatementBuilder buff = DIALECT.wrapDeleteQuery(new StatementBuilder(), tableName, as);
		stat.setSQL(buff.toString());
		if (db.factory.isShowSQL())
			StatementLogger.delete(stat.getSQL());
		return stat.executeUpdate();
	}
	
	TableDefinition<T> createTableIfRequired(Db db) {
		if (DIALECT.checkTableExists(tableName, db))
			return this;
		SQLStatement stat = new SQLStatement(db);
		StatementBuilder buff = new StatementBuilder(DIALECT.getCreateTableStatment(tableName));
		buff.append('(');
		for (FieldDefinition field : fields) {
			if (!field.isSilent) {
				buff.appendExceptFirst(", ");
				buff.append(field.columnName).append(' ').append(field.dataType);
				if (field.maxLength != 0) {
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
			StatementLogger.create(stat.getSQL());
		stat.executeUpdate();
		createIndices(db);
		AlterTableDiscriminatorIfRequired(db);
		return this;
	}

	private void createIndices(Db db) {
		// Handle index/ constraint definition
		Indices indices = this.clazz.getAnnotation(Indices.class);
		if (null != indices){
			for (Index index: indices.value()){
				String query = DIALECT.getIndexStatement(index.name(), this.tableName, index.unique(), index.columns());
				SQLStatement stat = new SQLStatement(db);
				StatementBuilder buff = new StatementBuilder(query);
				stat.setSQL(buff.toString());
				if (db.factory.isShowSQL())
					StatementLogger.alter(stat.getSQL());
				stat.executeUpdate();
			}
		}
	}

	private void AlterTableDiscriminatorIfRequired(Db db) {
		if (inheritedType == InheritedType.DISCRIMINATOR) {
			if (!DIALECT.checkDiscriminatorExists(tableName, discriminatorColumn, db)) {
				SQLStatement stat = new SQLStatement(db);
				StatementBuilder buff = new StatementBuilder(DIALECT.getDiscriminatorStatment(tableName, discriminatorColumn));
				stat.setSQL(buff.toString());
				if (db.factory.isShowSQL())
					StatementLogger.alter(stat.getSQL());
				stat.executeUpdate();
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	void initSelectObject(SelectTable<T> table, Object obj, Map<Object, SelectColumn<T>> map) {
		for (FieldDefinition def : fields) {
			Object o = def.initWithNewObject(obj);
			SelectColumn<T> column = new SelectColumn<T>(table, def);
			if (def.type == Types.ENUM) {
				Class type = def.field.getType();
				map.put(Enum.valueOf(type, (String)o), column);
			}
			else {
				map.put(o, column);
			}
		}
	}

	void readRow(Object item, ResultSet rs, Db db) {
		for (FieldDefinition def: fields) {
			if (!def.isSilent) {
				Object o = def.read(rs, DIALECT);
				def.setValue(item, o, db);
			}
			else
				// probably a relation is loaded
				def.setValue(item, null, db);
		}
	}

	SQLStatement getSelectList(Db db, String as) {
		SQLStatement selectList = new SQLStatement(db);
		int i = 0;
		for (FieldDefinition def: fields) {
			if (!def.isSilent) {
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
			query.appendSQL(selectList, obj);
			
			// since the 'X' type object does not necessarily have field types as the queried objects in the result set we add a column name
			// that conforms with the 'X' type
			selectList.appendSQL(" AS " + def.columnName);
			i++;
		}
		return selectList;
	}

	<Y, X> void copyAttributeValues(Query<Y> query, X to, X map) {
		for (FieldDefinition def : fields) {
			Object obj = def.getValue(map);
			SelectColumn<Y> col = query.getSelectColumn(obj);
			Object value = col.getCurrentValue();
			def.setValue(to, value, null);
		}
	}

	/**
	 * Returns a list of primary key definitions
	 * 
	 * @return List<FieldDefinition>
	 */
	List<FieldDefinition> getPrimaryKeyFields() {
		return primaryKeyColumnNames;
	}

	private Class<?> extractPrimaryKeyFromClass(Class<?> childType) {
		Field[] fields = getAllFields(childType);
		for (Field field: fields) {
			if (null != field.getAnnotation(PrimaryKey.class))
				return field.getType();
		}
		return null;
	}
	
	/*
	 * Create the join table if does not exist TODO need to support inheritance here as well
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
			if (DIALECT.checkTableExists(joinTableName, db))
				return;
			FieldDefinition myPkDef = this.getPrimaryKeyFields().get(0);
			String myPkLength = myPkDef.maxLength != 0 ? "(" + myPkDef.maxLength + ")" : "";

			// Locate the pk field of the target relation
			Field[] fields = childType.getFields();
			String relationPkLength = "";
			for (Field f : fields) {
				if (f.getAnnotation(PrimaryKey.class) != null) {
					// this is the primary key
					Column columnAnnotation = f.getAnnotation(Column.class);
					if (columnAnnotation != null) {
						relationPkLength = columnAnnotation.length() != -1 ? "(" + columnAnnotation.length() + ")" : "";
					}
					break; // Pk Found
				}
			}

			StatementBuilder builder = new StatementBuilder(DIALECT.getCreateTableStatment(joinTableName)).append(" (").append(myColumnNameInRelation).append(" ");
			builder.append(getDataType(primaryKeyColumnNames.get(0).field)).append(myPkLength + ", ").append(relationColumnName).append(" ");
			builder.append(DIALECT.getDataType(relationPkClass)).append(relationPkLength + ")");
			db.executeUpdate(builder.toString());
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
}