/*
 * Copyright 2004-2009 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.jaqu;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.h2.jaqu.annotation.Column;
import org.h2.jaqu.annotation.Entity;
import org.h2.jaqu.annotation.Inherited;
import org.h2.jaqu.annotation.JaquIgnore;
import org.h2.jaqu.annotation.Many2Many;
import org.h2.jaqu.annotation.MappedSuperclass;
import org.h2.jaqu.annotation.One2Many;
import org.h2.jaqu.annotation.PrimaryKey;
import org.h2.jaqu.constant.ErrorCode;
import org.h2.jaqu.jdbc.JdbcSQLException;
import org.h2.jaqu.util.JdbcUtils;
import org.h2.jaqu.util.StatementBuilder;
import org.h2.jaqu.util.StatementLogger;
import org.h2.jaqu.util.StringUtils;
import org.h2.jaqu.util.Utils;

/**
 * A table definition contains the index definitions of a table, the field definitions, the table name, and other meta data.
 * 
 * @param <T>
 *            the table type
 */
class TableDefinition<T> {

	enum FieldType {
		NORMAL, FK, M2M, O2M
	};

	/**
	 * The meta data of an index.
	 */
	static class IndexDefinition {
		boolean unique;
		String indexName;
		List<String> columnNames;
	}

	/**
	 * The meta data of a field.
	 */
	static class FieldDefinition {
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

		Object getValue(Object obj) {
			try {
				if (type == Types.ENUM)
					return field.get(obj).toString();
				return field.get(obj);
			}
			catch (Exception e) {
				throw new JaquError(e);
			}
		}

		@SuppressWarnings("unchecked")
		void initWithNewObject(Object obj) {
			if (type == Types.ENUM) {
				Class enumClass = field.getType();
				setValue(obj, enumClass.getEnumConstants()[0].toString(), null);
			}
			else {
				Object o = Utils.newObject(field.getType());
				setValue(obj, o, null);
			}
		}

		@SuppressWarnings("unchecked")
		void setValue(final Object obj, Object o, final Db db) {
			try {
				Object tmp = o;
				switch (fieldType) {
					case NORMAL:
						if (type == Types.ENUM) {
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
									throw new JdbcSQLException(
											"\nData Consistency error - Foreign relation does not exist!!\nError column was "
													+ field.getName() + " with value " + tmp + " in table " + obj.getClass().getName()
													+ "\nmissing in table " + o.getClass().getName(), null, dataType, ErrorCode.GENERAL_ERROR_1, null, columnName);
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
									
									public String getConditionString(SelectTable<?> st) {
										FieldDefinition fdef = st.getAliasDefinition().getDefinitionForField(relationDefinition.relationFieldName);
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
						throw new IllegalStateException("Field " + columnName
								+ " was marked as relation but has no relation MetaData in define method");
				}
			}
			catch (Exception e) {
				if (e instanceof RuntimeException)
					throw (RuntimeException) e;
				throw new JaquError(e.getMessage(), e);
			}
		}

		Object read(ResultSet rs, Dialect DIALECT) {
			try {
				return DIALECT.getValueByType(type, rs, this.columnName);
			}
			catch (SQLException e) {
				throw new JaquError(e);
			}
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
		/** The name of the column in the relation table for the table holding this definition */
//		String thisNameInRelationship;
		/** The data type of the relationship object */
		Class<?> dataType;
		/** type of cascade relation with the related child. relevant to O2M relations only. Currently only CascadeType.DELETE is supported */
		CascadeType cascadeType = CascadeType.NONE;
	}

	final Dialect DIALECT;
	String tableName;
	private Class<T> clazz;
	private ArrayList<FieldDefinition> fields = Utils.newArrayList();
	private IdentityHashMap<Object, FieldDefinition> fieldMap = Utils.newIdentityHashMap();
	private List<FieldDefinition> primaryKeyColumnNames;
	private List<FieldDefinition> oneToOneRelations;
	private ArrayList<IndexDefinition> indexes = Utils.newArrayList();
	boolean isAggregateParent = false;
	private GeneratorType genType = GeneratorType.NONE;
	private String sequenceQuery = null;
	InheritedType inheritedType = null;
	char discriminatorValue;
	String discriminatorColumn;

	TableDefinition(Class<T> clazz, Dialect DIALECT) {
		this.DIALECT = DIALECT;
		this.clazz = clazz;
		tableName = clazz.getSimpleName();
		// Handle table annotation if entity and Table annotations exist
		if (clazz.getAnnotation(Entity.class) != null) {
			org.h2.jaqu.annotation.Table tableAnnotation = clazz.getAnnotation(org.h2.jaqu.annotation.Table.class);
			if (tableAnnotation != null) {
				setTableName(tableAnnotation.name());
			}
			// we must have primary keys
			primaryKeyColumnNames = Utils.newArrayList();
		}
		Inherited inherited = clazz.getAnnotation(Inherited.class);
		if (inherited != null) {
			this.inheritedType = inherited.inheritedType();
			this.discriminatorValue = inherited.DiscriminatorValue();
			this.discriminatorColumn = inherited.DiscriminatorColumn();
		}
	}

	List<FieldDefinition> getFields() {
		return fields;
	}

	void setTableName(String tableName) {
		if (tableName != null && !"".equals(tableName))
			this.tableName = tableName;
	}

	void setPrimaryKey(Object[] primaryKeyColumns) {
		if (primaryKeyColumns != null && primaryKeyColumns.length > 0) {
			List<FieldDefinition> fields = Utils.newArrayList();
			if (primaryKeyColumns.length == 1) {
				// one primary key for this table
				FieldDefinition fieldDefinition = getDefinitionForColumn(getColumnName(primaryKeyColumns[0]));
				if (fieldDefinition != null) {
					fieldDefinition.isPrimaryKey = true;
					if (genType == GeneratorType.IDENTITY)
						fieldDefinition.dataType = DIALECT.getIdentityType();
					fields.add(fieldDefinition);
				}
			}
			else {
				// multiple primary key fields
				List<String> columnList = Utils.newArrayList();
				for (Object column : primaryKeyColumns)
					columnList.add(getColumnName(column));

				for (FieldDefinition fieldDefinition : this.fields) {
					if (columnList.contains(fieldDefinition.columnName)) {
						// set isPrimaryKey flag for all field definitions
						fieldDefinition.isPrimaryKey = true;
						fields.add(fieldDefinition);
					}
				}
			}
			this.primaryKeyColumnNames = fields;
		}
	}

	void setPrimaryKeys() {
		if (primaryKeyColumnNames != null && primaryKeyColumnNames.size() > 0) {
			if (primaryKeyColumnNames.size() == 1) {
				// one primary key for this table
				FieldDefinition fieldDefinition = primaryKeyColumnNames.get(0);
				if (fieldDefinition != null) {
					fieldDefinition.isPrimaryKey = true;
					if (genType == GeneratorType.IDENTITY)
						fieldDefinition.dataType = DIALECT.getIdentityType();
				}
			}
			else {
				// multiple primary key fields
				for (FieldDefinition fieldDefinition : primaryKeyColumnNames) {
					fieldDefinition.isPrimaryKey = true;
				}
			}
		}
	}

	void addManyToMany(FieldDefinition fieldDefinition, Many2Many many2Many, Db db) {
		Class<?> childType = many2Many.childType();
		if (Object.class.equals(childType)) {
			try {
				childType = (Class<?>) ((ParameterizedType)fieldDefinition.field.getGenericType()).getActualTypeArguments()[0];
			}
			catch (Exception e) {
				throw new JaquError("Tried to figure out the type of the child relation but couldn't. Try setting the 'childType' annotation parameter on @Many2Many annotation", e);
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
//		def.thisNameInRelationship = relationFieldName;

		if (fieldDefinition != null) {
			fieldDefinition.relationDefinition = def;
			fieldDefinition.fieldType = FieldType.M2M;
			def.dataType = childType;
			if (def.dataType == null || def.dataType.getAnnotation(Entity.class) == null)
				throw new IllegalStateException("field " + fieldDefinition.columnName
						+ " was marked as a relationship, but does not point to a TABLE Type");
			def.relationColumnName = relationColumnName;
			def.relationFieldName = relationFieldName;
		}
		// Try to get the primary key of the relationship
		Class<?> childPkType = many2Many.childPkType();
		if (Object.class.equals(childPkType)) {
			try {
				childPkType = extractPrimaryKeyFromClass(childType);
			}
			catch (Exception e) {
				throw new JaquError("You declared a join table, but JaQu was not able to find your child Primary Key. Try setting the 'childPkType' property on the @one2Many annotation", e);
			}
		}
		createRelationTable(childType, many2Many.joinTableName(), relationFieldName, childPkType, def.relationColumnName, db);
	}

	void addOneToMany(FieldDefinition fieldDefinition, One2Many one2ManyAnnotation, Db db) {
		Class<?> childType = one2ManyAnnotation.childType();
		if (Object.class.equals(childType)) {
			try {
				childType = (Class<?>) ((ParameterizedType)fieldDefinition.field.getGenericType()).getActualTypeArguments()[0];
			}
			catch (Exception e) {
				throw new JaquError("Tried to figure out the type of the child relation but couldn't. Try setting the 'childType' annotation parameter on @One2Many annotation", e);
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
//			def.thisNameInRelationship = relationFieldName;
		}
		if (one2ManyAnnotation.cascadeType() != null)
			def.cascadeType = one2ManyAnnotation.cascadeType();

		if (fieldDefinition != null) {
			fieldDefinition.relationDefinition = def;
			fieldDefinition.fieldType = FieldType.O2M;
			def.dataType = childType;
			if (def.dataType.getAnnotation(Entity.class) == null)
				throw new IllegalStateException("field " + fieldDefinition.columnName + " was marked as a relationship, but does not point to an Entity Type");
			def.relationColumnName = relationColumnName;
			def.relationFieldName = relationFieldName;
		}
		
		// add relation table creation
		if (one2ManyAnnotation.joinTableName() != null && !"".equals(one2ManyAnnotation.joinTableName())) {
			// Try to get the primary key of the relationship
			Class<?> childPkType = one2ManyAnnotation.childPkType();
			if (Object.class.equals(childPkType)) {
				try {
					childPkType = extractPrimaryKeyFromClass(childType);
				}
				catch (Exception e) {
					throw new JaquError("You declared a join table, but JaQu was not able to find your child Primary Key. Try setting the 'childPkType' property on the @one2Many annotation", e);
				}
			}
			createRelationTable(childType, one2ManyAnnotation.joinTableName(), relationFieldName, childPkType, def.relationColumnName, db);
		}
	}

	FieldDefinition getDefinitionForField(String fieldName) {
		for (FieldDefinition fieldDefinition : fields) {
			if (fieldName.equals(fieldDefinition.field.getName())) {
				return fieldDefinition;
			}
		}
		return null;
	}
	
	FieldDefinition getDefinitionForColumn(String columnName) {
		for (FieldDefinition fieldDefinition : fields) {
			if (columnName.equals(fieldDefinition.columnName)) {
				return fieldDefinition;
			}
		}
		return null;
	}

	<A> String getColumnName(A fieldObject) {
		FieldDefinition def = fieldMap.get(fieldObject);
		return def == null ? null : def.columnName;
	}

	void addIndex(Object[] columns) {
		IndexDefinition index = new IndexDefinition();
		index.indexName = tableName + "_" + indexes.size();
		index.columnNames = mapColumnNames(columns);
		indexes.add(index);
	}

	void setMaxLength(Object column, int maxLength) {
		String columnName = getColumnName(column);
		for (FieldDefinition f : fields) {
			if (f.columnName.equals(columnName)) {
				f.maxLength = maxLength;
				break;
			}
		}
	}

	void mapFields(Db db) {
		Field[] classFields;
		if (this.inheritedType != null) {
			Field[] superFields = null;
			Class<? super T> superClazz = clazz.getSuperclass();
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
		for (Field f : classFields) {
			if (Modifier.isTransient(f.getModifiers()))
				continue;

			if (Modifier.isStatic(f.getModifiers()))
				continue;
			
			// don't persist ignored fields.
			if (f.getAnnotation(JaquIgnore.class) != null)
				continue;
			Class<?> classType = f.getType();
			if (java.util.Date.class.isAssignableFrom(classType) || java.lang.Number.class.isAssignableFrom(classType)
					|| String.class.isAssignableFrom(classType) || Boolean.class.isAssignableFrom(classType)
					|| Blob.class.isAssignableFrom(classType) || Clob.class.isAssignableFrom(classType) || classType.isArray() || classType.isEnum()) {
				f.setAccessible(true);
				FieldDefinition fieldDef = new FieldDefinition();
				fieldDef.field = f;
				fieldDef.columnName = f.getName();
				fieldDef.dataType = getDataType(f);
				fieldDef.type = getTypes(classType);
				fields.add(fieldDef);
				if (String.class.isAssignableFrom(classType)|| classType.isEnum()) {
					// strings have a default size
					fieldDef.maxLength = 256;
				}
				// handle column name on annotation
				Column columnAnnotation = f.getAnnotation(Column.class);
				if (columnAnnotation != null) {
					if (!StringUtils.isNullOrEmpty(columnAnnotation.name()))
						fieldDef.columnName = columnAnnotation.name();
					int length = columnAnnotation.length();
					if (length != -1)
						fieldDef.maxLength = length;
				}
				PrimaryKey pkAnnotation = f.getAnnotation(PrimaryKey.class);
				if (pkAnnotation != null) {
					primaryKeyColumnNames.add(fieldDef);
					setGenerationType(pkAnnotation.generatorType(), pkAnnotation.seqName());
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
				fieldDef.isSilent = true;
				fields.add(fieldDef);
				this.isAggregateParent = true;

				// find the method...
				String methodName = Pattern.compile(f.getName().substring(0, 1)).matcher(f.getName()).replaceFirst(
						f.getName().substring(0, 1).toUpperCase());
				try {
					Method m = null;
					if (this.inheritedType == null)
						m = clazz.getDeclaredMethod("get" + methodName);
					else
						m = clazz.getMethod("get" + methodName);
					fieldDef.getter = m;
				}
				catch (NoSuchMethodException nsme) {
					throw new JaquError("Relation fields must have a getter in the form of get" + methodName + " in class "
							+ clazz.getName(), nsme);
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
					Column columnAnnotation = f.getAnnotation(Column.class);
					if (columnAnnotation != null) {
						if (!StringUtils.isNullOrEmpty(columnAnnotation.name()))
							fieldDef.columnName = columnAnnotation.name();
					}
				}
			}
			// for entities we should have primary keys
			if (primaryKeyColumnNames != null && !primaryKeyColumnNames.isEmpty())
				setPrimaryKeys();
		}
	}

	/**
	 * @param pkAnnotation
	 */
	void setGenerationType(GeneratorType generationType, String sequenceName) {
		if (generationType != null) {
			this.genType = generationType;
			if (this.genType == GeneratorType.SEQUENCE) {
				if (sequenceName == null)
					throw new IllegalArgumentException("GeneratorType.SEQUENCE must supply a sequence name!!!");
				StatementBuilder builder = new StatementBuilder("SELECT ").append(sequenceName).append(".nextval from dual");
				this.sequenceQuery = builder.toString();
			}
		}
	}

	private Field[] addSuperClassFields(Class<? super T> superClazz) {
		if (superClazz == null || superClazz.equals(Object.class))
			return null;
		Field[] superSuperFields = addSuperClassFields(superClazz.getSuperclass());

		if (superClazz.getAnnotation(MappedSuperclass.class) == null)
			return superSuperFields;

		if (superSuperFields == null) {
			// super class is Object.class or not mapped. However this class is mapped so we can get it fields
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
				throw e;
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
				throw new IllegalStateException("No primary key columns defined for table " + classType + " - no relationship possible");
			else if (def.primaryKeyColumnNames.size() > 1)
				throw new UnsupportedOperationException("Entity relationship is not supported for complex primary keys. Found in "
						+ classType);
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
		return DIALECT.getDataType(fieldClass);
	}

	void insert(Db db, Object obj) {
		if (db.checkReEntrant(obj))
			return;
		SQLStatement stat = new SQLStatement(db);
		StatementBuilder buff = new StatementBuilder("INSERT INTO ");
		buff.append(tableName).append('(');
		if (null == primaryKeyColumnNames) {
			for (FieldDefinition field : fields) {
	            buff.appendExceptFirst(", ");
	            buff.append(field.columnName);
	        }
	        buff.append(") VALUES(");
	        buff.resetCount();
	        for (FieldDefinition field : fields) {
	            buff.appendExceptFirst(", ");
	            buff.append('?');
	            handleValue(db, obj, stat, field);
	        }
	        buff.append(')');
	        stat.setSQL(buff.toString());
			if (db.factory.isShowSQL())
				StatementLogger.insert(stat.getSQL());
			
			stat.executeUpdate();
		}
		else {
			// we insert first basically to get the generated primary key (either Identity or Sequence)
			for (FieldDefinition pkField: primaryKeyColumnNames) {
				buff.appendExceptFirst(", ");
				buff.append(pkField.columnName);
			}
			buff.append(") VALUES(");
			buff.resetCount();
			for (FieldDefinition pkField: primaryKeyColumnNames) {
				buff.appendExceptFirst(", ");
				buff.append('?');
				handleValue(db, obj, stat, pkField);
			}
			buff.append(')');
			stat.setSQL(buff.toString());
			if (db.factory.isShowSQL())
				StatementLogger.insert(stat.getSQL());
			
			stat.executeUpdate();
			if (genType == GeneratorType.IDENTITY)
				callIdentity(obj, db);

			update(db, obj);
		}
	}

	void merge(Db db, Object obj) {
		if (db.checkReEntrant(obj))
			return;
		if (primaryKeyColumnNames == null || primaryKeyColumnNames.size() == 0) {
			throw new IllegalStateException("No primary key columns defined for table " + obj.getClass() + " - no merge possible");
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
				this.update(db, obj);
			}
			else
				this.insert(db, obj);
		}
		catch (SQLException e) {
			throw new JaquError(e);
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
				throw new JaquError("Unable to generate key.", e);
			}
			catch (Exception e) {
				if (e instanceof RuntimeException)
					throw (RuntimeException) e;
				throw new JaquError(e.getMessage(), e);
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
		if (primaryKeyColumnNames == null || primaryKeyColumnNames.size() == 0) {
			throw new IllegalStateException("No primary key columns defined for table " + obj.getClass() + " - no update possible");
		}
		SQLStatement stat = new SQLStatement(db);
		Object alias = Utils.newObject(obj.getClass());
		Query<Object> query = Query.from(db, alias);
		String as = query.getSelectTable().getAs();
		StatementBuilder buff = new StatementBuilder("UPDATE ");
		buff.append(tableName).append(" ").append(as).append(" SET ");
		buff.resetCount();
		for (FieldDefinition field : fields) {
			if (!field.isPrimaryKey) {
				if (!field.isSilent) {
					buff.appendExceptFirst(", ");
					buff.append(as + ".");
					buff.append(field.columnName);
					buff.append(" = ?");
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
	 * TODO this should move to dialect and work for all DB's supporting Identity Type
	 */
	private void callIdentity(Object obj, Db db) {
		ResultSet rs = null;
		try {
			if (primaryKeyColumnNames.get(0).field.get(obj) == null) {
				rs = db.executeQuery("SELECT last_insert_id() AS id;");
				if (rs.next()) {
					primaryKeyColumnNames.get(0).field.set(obj, rs.getLong(1));
				}
			}
		}
		catch (Exception e) {
			if (e instanceof RuntimeException)
				throw (RuntimeException) e;
			throw new JaquError(e.getMessage(), e);
		}
		finally {
			JdbcUtils.closeSilently(rs);
		}
	}

	void delete(Db db, Object obj) {
		if (primaryKeyColumnNames == null || primaryKeyColumnNames.size() == 0) {
			throw new IllegalStateException("No primary key columns defined for table " + obj.getClass() + " - no update possible");
		}
		SQLStatement stat = new SQLStatement(db);
		Object alias = Utils.newObject(obj.getClass());
		Query<Object> query = Query.from(db, alias);
		String as = query.getSelectTable().getAs();
		StatementBuilder buff = new StatementBuilder();
		if (DIALECT == Dialect.MYSQL) {
			buff.append("DELETE " + as + " FROM ");
		}
		else
			buff.append("DELETE FROM ");
		buff.append(tableName).append(" ").append(as).append(" ");
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
		stat.setSQL(buff.toString());
		query.appendWhere(stat);
		if (db.factory.isShowSQL())
			StatementLogger.delete(stat.getSQL());
		stat.executeUpdate();
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
		if (primaryKeyColumnNames != null) {
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
		// TODO create indexes
		return AlterTableDiscriminatorIfRequired(db);
	}

	private TableDefinition<T> AlterTableDiscriminatorIfRequired(Db db) {
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
		return this;
	}

	void mapObject(Object obj) {
		fieldMap.clear();
		initObject(obj, fieldMap);
	}

	void initObject(Object obj, Map<Object, FieldDefinition> map) {
		for (FieldDefinition def : fields) {
			def.initWithNewObject(obj);
			map.put(def.getValue(obj), def);
		}
	}

	@SuppressWarnings("unchecked")
	void initSelectObject(SelectTable<T> table, Object obj, Map<Object, SelectColumn<T>> map) {
		for (FieldDefinition def : fields) {
			def.initWithNewObject(obj);
			SelectColumn<T> column = new SelectColumn<T>(table, def);
			if (def.type == Types.ENUM) {
				Class type = def.field.getType();
				map.put(Enum.valueOf(type, (String)def.getValue(obj)), column);
			}
			else {
				map.put(def.getValue(obj), column);
			}
		}
	}

	void readRow(Object item, ResultSet rs, Db db) {
		for (int i = 0; i < fields.size(); i++) {
			FieldDefinition def = fields.get(i);
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
		for (int i = 0; i < fields.size(); i++) {
			FieldDefinition def = fields.get(i);
			if (!def.isSilent) {
				if (i > 0) {
					selectList.appendSQL(", ");
				}
				selectList.appendSQL(as + "." + def.columnName);
			}
		}
		return selectList;
	}

	<Y, X> SQLStatement getSelectList(Query<Y> query, X x) {
		SQLStatement selectList = new SQLStatement(query.getDb());
		for (int i = 0; i < fields.size(); i++) {
			if (i > 0) {
				selectList.appendSQL(", ");
			}
			FieldDefinition def = fields.get(i);
			Object obj = def.getValue(x);
			query.appendSQL(selectList, obj);
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
	 * @return
	 */
	List<FieldDefinition> getPrimaryKeyFields() {
		return primaryKeyColumnNames;
	}

	private Class<?> extractPrimaryKeyFromClass(Class<?> childType) {
		Field[] fields = childType.getDeclaredFields();
		for (Field field: fields) {
			if (null != field.getAnnotation(PrimaryKey.class))
				return field.getType();
		}
		return null;
	}
	
	private List<String> mapColumnNames(Object[] columns) {
		List<String> columnNames = Utils.newArrayList();
		for (Object column : columns) {
			columnNames.add(getColumnName(column));
		}
		return columnNames;
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
			if (e instanceof RuntimeException)
				throw (RuntimeException) e;
			throw new JaquError(e.getMessage(), e);
		}
	}
}