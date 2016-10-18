/*
 * Copyright (c) 2010-2016 Centimia Ltd.
 * All rights reserved.  Unpublished -- rights reserved
 *
 * Use of a copyright notice is precautionary only, and does
 * not imply publication or disclosure.
 *
 * THIS SOFTWARE CONTAINS CONFIDENTIAL INFORMATION AND TRADE
 * SECRETS OF CENTIMIA. USE, DISCLOSURE, OR
 * REPRODUCTION IS PROHIBITED WITHOUT THE PRIOR EXPRESS
 * WRITTEN PERMISSION OF CENTIMIA Ltd.
 */

/*
 ISSUE			DATE			AUTHOR
-------		   ------	       --------
Created		   Feb 12, 2014		shai

*/
package com.centimia.orm.jaqu;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;

import com.centimia.orm.jaqu.TableDefinition.FieldDefinition;
import com.centimia.orm.jaqu.TableDefinition.FieldType;
import com.centimia.orm.jaqu.annotation.Entity;
import com.centimia.orm.jaqu.constant.StatementType;
import com.centimia.orm.jaqu.util.JdbcUtils;
import com.centimia.orm.jaqu.util.StatementBuilder;
import com.centimia.orm.jaqu.util.Utils;

/**
 * Utility to work with prepared statements (especially for batching) independently from regular jaQu work but using the same connection.
 * These utils work only on pojo's and ignore relationships. The update and insert statements use all fields, delete only primary keys.
 * Hense, the pojos must have primary keys for these functions to work.
 * 
 * @author shai
 */
public class PojoUtils {

	private final Db	db;
	private final HashMap<Class<?>, PreparedStatement> insertStatements = new HashMap<Class<?>, PreparedStatement>();
	private final HashMap<Class<?>, PreparedStatement> updateStatements = new HashMap<Class<?>, PreparedStatement>();
	private final HashMap<Class<?>, PreparedStatement> deleteStatements = new HashMap<Class<?>, PreparedStatement>();
	
	PojoUtils(Db db) {
		this.db = db;
	}
	
	/**
     * Insert the array of pojo objects in batch mode.<p>
     * <b>NOTE: using batch insert only works on pojos or {@link Entity} that has no relationship.<br>When relationships exist on the {@link Entity} it will insert but relationships are disregarded</b>
     * 
     * @param batchSize - battch interval size
     * @param tArray
     */
    @SuppressWarnings("unchecked")
	public <T> void insertBatch(final int batchSize, T ... tArray) {
    	if (null == tArray || 0 == tArray.length)
    		return;

    	Class<?> clazz = tArray[0].getClass();
		TableDefinition<?> definition = JaquSessionFactory.define(clazz, db);

		definition.insertBatch(db, batchSize, tArray);
    }
    
    /**
     * returns a PreparedStatement backed up by the current Db session. It is the developers responsibility to populate, execute, and close this statement.
     * @param statement
     * @return {@link PreparedStatement}
     */
    public PreparedStatement getPreparedStatement(String statement) {
    	return db.prepare(statement);
    }
    
    /**
     * This function executes a 'SELECT' preparedStatement and returns a list of resultClass type.
     * Matching between returned columns and existing type is done in the JaQu way. 
     * 
     * @param stmnt
     * @param resultClazz
     * @return List<T>
     */
    public <T> List<T> executeStatement(PreparedStatement stmnt, Class<T> resultClazz) {
        List<T> result = Utils.newArrayList();
        TableDefinition<T> def = JaquSessionFactory.define(resultClazz, db);
        ResultSet rs = null;
        try {
        	rs = stmnt.executeQuery();
        	while (rs.next()) {
                T item =  def.readRow(rs, db);
                db.addSession(item);
                result.add(item);
            }
        } 
        catch (SQLException e) {
            throw new JaquError(e, e.getMessage());
        } 
        finally {
            JdbcUtils.closeSilently(rs);
        }        
        return result;
    }
    
    /**
     * Returns a preparedStatement for the "pojo" given based on the statement type (UPDATE, INSERT, DELETE).
     * This method always returns the same {@link PreparedStatement} object when running in the same db session.
     * 
     * @param clazz
     * @param type
     * @return {@link PreparedStatement}
     */
    public <T> PreparedStatement getPreparedStatement(Class<T> clazz, StatementType type) {
    	return getPreparedStatement(clazz, type, false);
    }
    
    /**
     * Returns a preparedStatement for the pojo given based on the statement type (UPDATE, INSERT, DELETE).
     * This method always returns the same {@link PreparedStatement} object when running in the same db session.
     * 
     * @param clazz
     * @param type
     * @parm externalizePk - if true utils assumes the PK is injected by the user externally even if the PK is Identity type
     * @return {@link PreparedStatement}
     */
    public <T> PreparedStatement getPreparedStatement(Class<T> clazz, StatementType type, boolean externalizePk) {
    	if (null == clazz || null == type)
    		return null;
    	switch (type) {
    		case INSERT: return getPreparedInsertStatement(clazz, externalizePk);
    		case UPDATE: return getPreparedUpdateStatement(clazz);
    		case DELETE: return getPreparedDeleteStatement(clazz);
    		default : return null;
    	}
    }

    /**
     * Prerpares the data on the prepared Statement. If the preparedStatement does not exist it will be created.
     * The statement returned is ready for execution but had not been executed.
     * 
     * @param obj
     * @return {@link PreparedStatement}
     * @throws JaquError
     */
    public <T> PreparedStatement prepareStatement(T obj, StatementType type) {
    	return prepareStatement(obj, type, false);
    }
    
    /**
     * Prerpares the data on the prepared Statement. If the preparedStatement does not exist it will be created.
     * The statement returned is ready for execution but had not been executed.
     * 
     * @param obj
     * @parm externalizePk - if true utils assumes the PK is injected by the user externally even if the PK is Identity type
     * @return {@link PreparedStatement}
     * @throws JaquError
     */
    public <T> PreparedStatement prepareStatement(T obj, StatementType type, boolean externalizePk) {
    	if (null == obj || null == type)
    		return null;
    	switch (type) {
    		case INSERT: return prepareInsertStatement(obj, externalizePk);
    		case UPDATE: return prepareUpdateStatement(obj);
    		case DELETE: return prepareDeleteStatement(obj);
    		default : return null;
    	}
    }
    
    /**
     * Adds the object into the batch of executions. If the preparedStatement does not yet exist it will be created.
     * @param obj
     * @param type
     * @return {@link PreparedStatement}
     */
    public <T> PreparedStatement addBatch(T obj, StatementType type) {
    	return addBatch(obj, type, false);
    }
    
    /**
     * Adds the object into the batch of executions. If the preparedStatement does not yet exist it will be created.
     * @param obj
     * @param type
     * @parm externalizePk - if true utils assumes the PK is injected by the user externally even if the PK is Identity type
     * @return {@link PreparedStatement}
     */
    public <T> PreparedStatement addBatch(T obj, StatementType type, boolean externalizePk) {
    	if (null == obj || null == type)
    		return null;
    	PreparedStatement ps;
    	switch (type) {
    		case INSERT: ps = prepareInsertStatement(obj, externalizePk); break;
    		case UPDATE: ps = prepareUpdateStatement(obj); break;
    		case DELETE: ps = prepareDeleteStatement(obj); break;
    		default : ps = null; break;
    	}
    	
    	if (null != ps) {
    		try {
				ps.addBatch();
			}
			catch (SQLException e) {
				ps = null;
			}
    	}
    	return ps;
    }
    
    /**
     * Submits all batch of commands to the database for execution no matter the type. Does not return an array
     * 
     * @param clazz
     * @return boolean success if any type update was executed.
     */
    public <T> boolean executeBatch(Class<T> clazz) {
    	boolean success = false;
    	for (StatementType type: StatementType.values()) {
    		int[] result = executeBatch(clazz, type);
    		if (null != result && result.length > 0 )
    			success = true;
    	}
    	return success;
    }
    
    /**
     * Submits a batch of commands to the database for execution and if all commands execute successfully, returns an array of update counts. 
     * The int elements of the array that is returned are ordered to correspond to the commands in the batch, 
     * which are ordered according to the order in which they were added to the batch.
     * 
     * @param clazz
     * @param type
     * @return
     */
    public <T> int[] executeBatch(Class<T> clazz, StatementType type) {
    	if (null == clazz)
    		return new int[0];
    	PreparedStatement ps;
    	switch (type) {
    		case INSERT: ps = insertStatements.get(clazz); break;
    		case UPDATE: ps = updateStatements.get(clazz); break;
    		case DELETE: ps = deleteStatements.get(clazz); break;
    		default: ps = null; break;
    	}
    	
    	if (null == ps)
    		return new int[0];
    	
    	try {
			return ps.executeBatch();
		}
		catch (SQLException e) {
			return new int[0];
		}
    }
    
    /**
     * Returns an insert preparedStatement for the pojo given. This method always returns the same {@link PreparedStatement} object when running in the same db session.
     * @param clazz
     * @return {@link PreparedStatement}
     */
    private <T> PreparedStatement getPreparedInsertStatement(Class<T> clazz, boolean externalizePk) {
    	PreparedStatement ps = insertStatements.get(clazz);
    	if (null == ps) {
    		TableDefinition<?> definition = JaquSessionFactory.define(clazz, db);
     		ps = db.prepare(getInsertStatement(definition, externalizePk).toString());
     		insertStatements.put(clazz, ps);
    	}
    	return ps;
    }
    
    /**
     * Returns an update preparedStatement which updates all the fields (accept the primaryKey) for the pojo given. This method always returns the same {@link PreparedStatement} 
     * object when running in the same db session.
     * 
     * @param clazz
     * @return {@link PreparedStatement}
     */
    private <T> PreparedStatement getPreparedUpdateStatement(Class<T> clazz) {
    	PreparedStatement ps = updateStatements.get(clazz);
    	if (null == ps) {
    		TableDefinition<?> definition = JaquSessionFactory.define(clazz, db);
    		StatementBuilder builder = getUpdateStatement(definition, clazz);
    		if (null != builder) {
    			ps = db.prepare(builder.toString());
    			updateStatements.put(clazz, ps);
    		}
    		else
    			return null;
    	}
    	return ps;
    }
    
    /**
     * Returns an update preparedStatement which updates all the fields (accept the primaryKey) for the pojo given. This method always returns the same {@link PreparedStatement} 
     * object when running in the same db session.
     * 
     * @param clazz
     * @return {@link PreparedStatement}
     */
    private <T> PreparedStatement getPreparedDeleteStatement(Class<T> clazz) {
    	PreparedStatement ps = deleteStatements.get(clazz);
    	if (null == ps) {
    		TableDefinition<?> definition = JaquSessionFactory.define(clazz, db);
   			ps = db.prepare(getDeleteStatement(definition, clazz).toString());
    		deleteStatements.put(clazz, ps);
    	}
    	return ps;
    }

   /*
    * prepares the insertStatement for this object with data from the given object
    */
    @SuppressWarnings("unchecked")
	public <T> PreparedStatement prepareInsertStatement(T obj, boolean externalizePk) {
    	Class<T> tClazz = (Class<T>) obj.getClass();
    	PreparedStatement ps = insertStatements.get(tClazz);
    	if (null == ps) {
    		ps = getPreparedInsertStatement(tClazz, externalizePk);
    	}
		TableDefinition<?> definition = JaquSessionFactory.define(tClazz, db);
		int i = 1;
		for (FieldDefinition field : definition.getFields()) {
			if (!externalizePk && field.isPrimaryKey && GeneratorType.IDENTITY == definition.getGenerationtype())
				// skip identity types because these are auto incremented
        		continue;
			
			if (field.isSilent)
				// skip silent fields because they don't really exist.
        		continue;
        	
			if (field.fieldType == FieldType.FK) {
				setValue(ps, i, getFkValue(obj, field));
				i++;
				continue;				
			}
			
        	if (field.fieldType != FieldType.NORMAL)
        		// skip everything which is not a plain field (i.e any type of relationship)
        		continue;
        	Object value = field.getValue(obj);
        	setValue(ps, i, value);
        	i++;
		}
    	return ps;
    }

	/**
	 * Return the primary key of the Fk object value.
	 * @param obj
	 * @param field
	 */
	private <T> Object getFkValue(T obj, FieldDefinition field) {
		// try to get the value from the FK
		Object value = field.getValue(obj);
		TableDefinition<?> fKDefinition = db.define(value.getClass());
		List<FieldDefinition> pks = fKDefinition.getPrimaryKeyFields();
		if (pks.size() != 1) {
			return null;
		}
		else {
			Object pkValue = pks.get(0).getValue(value);
			return pkValue;
		}
	}
    
    /*
     * prepares the updateStatement for this object with data from the given object
     */
    @SuppressWarnings("unchecked")
	private <T> PreparedStatement prepareUpdateStatement(T obj) {
    	Class<T> tClazz = (Class<T>) obj.getClass();
    	PreparedStatement ps = updateStatements.get(tClazz);
    	if (null == ps) {
    		ps = getPreparedUpdateStatement(tClazz);
    	}
    	TableDefinition<?> definition = JaquSessionFactory.define(tClazz, db);
		int i = 1;
    	for (FieldDefinition field : definition.getFields()) {
			if (!field.isPrimaryKey) {
				if (field.fieldType == FieldType.FK) {
					setValue(ps, i, getFkValue(obj, field));
					i++;
					continue;				
				}
				if (!field.isSilent) {
					Object value = field.getValue(obj);
		        	setValue(ps, i, value);
		        	i++;
				}
			}
    	}
    	
		for (FieldDefinition field : definition.getPrimaryKeyFields()) {
			Object value = field.getValue(obj);
        	setValue(ps, i, value);
        	i++;
		}
    	return ps;
    }
    
    /*
     * prepares the deleteStatement for this object with data from the given object
     */
    @SuppressWarnings("unchecked")
	private <T> PreparedStatement prepareDeleteStatement(T obj) {
    	Class<T> tClazz = (Class<T>) obj.getClass();
    	PreparedStatement ps = deleteStatements.get(tClazz);
    	if (null == ps) {
    		ps = getPreparedDeleteStatement(tClazz);
    	}
    	TableDefinition<?> definition = JaquSessionFactory.define(tClazz, db);
		int i = 1;    	
		for (FieldDefinition field : definition.getPrimaryKeyFields()) {
			Object value = field.getValue(obj);
        	setValue(ps, i, value);
        	i++;
		}
    	return ps;
    }
    
	private void setValue(PreparedStatement prep, int parameterIndex, Object x) {
        try {
        	if (x instanceof java.util.Date)
        		x = new Timestamp(((java.util.Date) x).getTime());
            prep.setObject(parameterIndex, x);
        } 
        catch (SQLException e) {
            throw new JaquError(e, e.getMessage());
        }
    }
	
	private StatementBuilder getInsertStatement(TableDefinition<?> def, boolean externalizePk) {
    	StatementBuilder buff = new StatementBuilder("INSERT INTO ");
		StatementBuilder fieldTypes = new StatementBuilder(), valueTypes = new StatementBuilder();
		buff.append(def.tableName).append('(');
		if (InheritedType.DISCRIMINATOR == def.inheritedType) {
			// the inheritense is based on a single table with a discriminator
			fieldTypes.appendExceptFirst(", ");
			fieldTypes.append(def.discriminatorColumn);
			valueTypes.appendExceptFirst(", ");
			valueTypes.append("'" + def.discriminatorValue + "'");
		}
		for (FieldDefinition field : def.getFields()) {
			if (!externalizePk && field.isPrimaryKey && GeneratorType.IDENTITY == def.getGenerationtype())
				// skip identity types because these are auto incremented
        		continue;
			
			if (field.isSilent)
				// skip silent fields because they don't really exist.
        		continue;
			
			if (field.fieldType != FieldType.FK && field.fieldType != FieldType.NORMAL)
        		// skip everything which is not a plain field (i.e any type of relationship)
        		continue;
        	
        	fieldTypes.appendExceptFirst(", ");
        	fieldTypes.append(field.columnName);
        	
        	valueTypes.appendExceptFirst(", ");
        	valueTypes.append('?');
        }
		buff.append(fieldTypes).append(") VALUES(").append(valueTypes).append(')');
		if (db.factory.isShowSQL())
			StatementLogger.info(buff.toString());
		return buff;
    }
	
	private StatementBuilder getUpdateStatement(TableDefinition<?> def, Class<?> clazz) {
		Object alias = Utils.newObject(clazz);
		Query<Object> query = Query.from(db, alias);
		String as = query.getSelectTable().getAs();

		StatementBuilder innerUpdate = new StatementBuilder();
		innerUpdate.resetCount();
		boolean hasNoSilent = false;
		for (FieldDefinition field : def.getFields()) {
			if (!field.isPrimaryKey) {
				if (field.fieldType == FieldType.FK || !field.isSilent) {
					innerUpdate.appendExceptFirst(", ");
					innerUpdate.append(as + ".");
					innerUpdate.append(field.columnName);
					innerUpdate.append(" = ?");
					hasNoSilent = true;
				}
			}
		}
		StatementBuilder buff = def.DIALECT.wrapUpdateQuery(innerUpdate, def.tableName, as);
		buff.append(" WHERE ");
		if (hasNoSilent) {
			// if all fields were silent there would be noting to update here so we disregard the whole thing
			// and we don't do the update.
			boolean firstCondition = true;
			for (FieldDefinition field : def.getPrimaryKeyFields()) {
				if (!firstCondition) {
					buff.append(" AND ");
				}
				buff.append(field.columnName).append(" = ?");				
				firstCondition = false;
			}
		}
		else {
			return null;
		}
		if (db.factory.isShowSQL())
			StatementLogger.info(buff.toString());
		return buff;
	}

	/*
	 * Creates the delete statement String
	 */
	private StatementBuilder getDeleteStatement(TableDefinition<?> def, Class<?> clazz) {
		Object alias = Utils.newObject(clazz);
		Query<Object> query = Query.from(db, alias);
		String as = query.getSelectTable().getAs();

		StatementBuilder innerUpdate = new StatementBuilder();
		innerUpdate.resetCount();
		StatementBuilder buff = def.DIALECT.wrapDeleteQuery(innerUpdate, def.tableName, as);
		buff.append(" WHERE ");
		boolean firstCondition = true;
		for (FieldDefinition field : def.getPrimaryKeyFields()) {
			if (!firstCondition) {
				buff.append(" AND ");
			}
			buff.append(field.columnName).append(" = ?");				
			firstCondition = false;
		}
		if (db.factory.isShowSQL())
			StatementLogger.info(buff.toString());
		return buff;
	}
	
	/*
	 * Close all the statements and clean the maps.
	 */
	void clean() {
		for (PreparedStatement ps: insertStatements.values()) {
			try {
				ps.close();
			}
			catch (SQLException e) {
				// nothing I can do
			}
		}
		for (PreparedStatement ps: updateStatements.values()) {
			try {
				ps.close();
			}
			catch (SQLException e) {
				// nothing I can do
			}
		}
		for (PreparedStatement ps: deleteStatements.values()) {
			try {
				ps.close();
			}
			catch (SQLException e) {
				// nothing I can do
			}
		}
		insertStatements.clear();
		updateStatements.clear();
		deleteStatements.clear();
	}
}
