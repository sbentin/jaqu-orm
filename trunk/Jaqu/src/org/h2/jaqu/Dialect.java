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
 * Initial Developer: Centimia Inc.
 */

/*
 * Update Log
 * 
 *  Date			User				Comment
 * ------			-------				--------
 * 09/02/2010		Shai Bentin			 create
 */
package org.h2.jaqu;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.h2.jaqu.dialect.DB2Dialect;
import org.h2.jaqu.dialect.H2Dialect;
import org.h2.jaqu.dialect.MySqlDialect;
import org.h2.jaqu.dialect.OracleDialect;
import org.h2.jaqu.dialect.SQLServerDialect;

/**
 * 
 * @author Shai Bentin
 */
public enum Dialect {
	H2 (new H2Dialect()), 
	ORACLE (new OracleDialect()),
	DB2 (new DB2Dialect()),
	MYSQL (new MySqlDialect()),
	SQLSERVER (new SQLServerDialect());
	
	final SQLDialect dialect;
	
	Dialect(SQLDialect dialect){
		this.dialect = dialect;
	}
	
	String getDataType(Class<?> fieldClass) {
		return dialect.getDataType(fieldClass);
	}
	
	String getCreateTableStatment(String tableName) {
		return dialect.createTableString(tableName);
	}
	
	Object getValueByType(Types type, ResultSet rs, String columnName) throws SQLException {
		return dialect.getValueByType(type, rs, columnName);
	}

	boolean checkTableExists(String joinTableName, Db db) {
		return dialect.checkTableExists(joinTableName, db);		
	}

	String getDiscriminatorStatment(String tableName, String discriminatorName) {
		return dialect.createDiscrimantorColumn(tableName, discriminatorName);
	}
	
	boolean checkDiscriminatorExists(String joinTableName, String discriminatorName, Db db) {
		return dialect.checkDiscriminatorExists(joinTableName, discriminatorName, db);
	}
	
	String getIdentityType() {
		return dialect.getIdentityType();
	}
	
	/**
	 * Returns the SQLDialect based on the driver class. Default H2
	 * @param clazz
	 * @return Dialect
	 */
	Dialect getDialect(String clazz) {
		if (clazz.toLowerCase().indexOf("oracle") != -1)
			return ORACLE;
		if (clazz.toLowerCase().indexOf("db2") != -1)
			return DB2;
		if (clazz.toLowerCase().indexOf("mysql") != -1)
			return MYSQL;
		if (clazz.toLowerCase().indexOf("microsoft") != -1)
			return SQLSERVER;
		if (clazz.toLowerCase().indexOf("h2") != -1)
			return H2;
		return H2;
	}
}
