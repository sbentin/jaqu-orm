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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

/**
 * This class represents a parameterized SQL statement.
 */
public class SQLStatement {
    private static final String[] EMPTY_PK = new String[0];
	private Db db;
    private StringBuilder buff = new StringBuilder();
    private String sql;
    private ArrayList<Object> params = new ArrayList<>();

    // used only in batch...
    private PreparedStatement batchPrep;

    SQLStatement(Db db) {
        this.db = db;
    }

    void setSQL(String sql) {
        this.sql = sql;
        buff = new StringBuilder(sql);
    }

    public SQLStatement appendSQL(String s) {
        buff.append(s);
        sql = null;
        return this;
    }

    String getSQL() {
        if (sql == null) {
            sql = buff.toString();
        }
        return sql;
    }

    String logSQL() {
    	StringBuilder log = new StringBuilder(buff.toString()).append(" [");
    	boolean first = true;
    	for (Object obj: params) {
    		if (!first)
    			log.append(", ");
    		if (null != obj)
    			log.append(obj.toString());
    		else
    			log.append(" ");
    		first = false;
    	}
    	log.append("]");
    	return log.toString();
    }

    SQLStatement addParameter(Object o) {
        params.add(o);
        return this;
    }

    <T> T executeQuery(IResultProcessor<T> processor) {
        if (db.factory.isShowSQL())
        	StatementLogger.select(logSQL());
        try (PreparedStatement ps = prepare(EMPTY_PK)) {
        	try (ResultSet rs = ps.executeQuery()) {
        		return processor.processResult(rs);
        	}
        }
        catch (SQLException e) {
        	db.factory.dialect.dialect.handleDeadlockException(e);
        	return null;
        }
    }

    void prepareBatch() {
        try {
			if (null == batchPrep)
				batchPrep = db.prepare(getSQL());
			for (int i = 0; i < params.size(); i++) {
			    Object o = params.get(i);
			    setValue(batchPrep, i + 1, o);
			}
			batchPrep.addBatch();
		}
		catch (SQLException e) {
			throw new JaquError(e, e.getMessage());
		}
    }

    int[] executeBatch(boolean clean) {
    	try {
			int[] result = batchPrep.executeBatch();
			if (clean) {
				// we need to clear this statement from here
				batchPrep = null;
			}
			return result;
		}
		catch (SQLException e) {
			db.factory.dialect.dialect.handleDeadlockException(e);
        	return null;
		}
    }

	int executeUpdate() {
		try (PreparedStatement ps = prepare(EMPTY_PK)) {
        	return ps.executeUpdate();
        }
        catch (SQLException e) {
        	db.factory.dialect.dialect.handleDeadlockException(e);
        	return -1;
        }
    }

	Long executeUpdateWithId(String[] idColumnNames) {
		try (PreparedStatement ps = prepare(idColumnNames)) {
			int size = ps.executeUpdate();
			if (size > 0)
				return getGeneratedKeys(ps.getGeneratedKeys());
			return null;
		}
		catch (SQLException e) {
            throw new JaquError(e, e.getMessage());
        }
	}

	<T> T executeUnion(SQLStatement unionStatement, IResultProcessor<T> processor){
		if (null != unionStatement) {
			this.buff.append(" union ").append(unionStatement.buff);
			this.params.addAll(unionStatement.params);
		}
		return this.executeQuery(processor);
	}

    private Long getGeneratedKeys(ResultSet generatedKeys) {
		try {
			if (generatedKeys.next()) {
				// identity fields and sequences are only BigInt numbers so we can safely assume to get a long type here
				return generatedKeys.getLong(1);
			}
		}
		catch (SQLException e) {
			throw new JaquError("Expected a generated Id but received None. Maybe your DB is not supported. Check supported Db list");
		}
		return null;
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

    private PreparedStatement prepare(String[] idColumnNames) {
        PreparedStatement prep = db.prepare(getSQL(), idColumnNames);
        for (int i = 0; i < params.size(); i++) {
            Object o = params.get(i);
            setValue(prep, i + 1, o);
        }
        return prep;
    }
}