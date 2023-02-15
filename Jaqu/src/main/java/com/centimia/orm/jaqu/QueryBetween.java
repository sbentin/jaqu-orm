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

/*
 * Update Log
 *
 *  Date			User				Comment
 * ------			-------				--------
 * 22/02/2010		Shai Bentin			 create
 */
package com.centimia.orm.jaqu;

/**
 * This class represents a "between y and z" condition.
 *
 * @param <T> the return type of the query
 * @param <A>the incomplete condition data type
 */
public class QueryBetween<T, A> {

        private Query<T> query;
        private Object x;
        private A y;

        /**
         * Construct a between condition.
         *
         * @param query the query
         * @param x the alias
         * @param y the lower bound of the between condition
         */
        public QueryBetween(Query<T> query, GenericMask<T, A> x, A y) {
                this.query = query;
                this.x = x;
                this.y = y;
        }

        /**
         * Construct a between condition.
         *
         * @param query the query
         * @param x the alias
         * @param y the lower bound of the between condition
         */
        public QueryBetween(Query<T> query, A x, A y) {
                this.query = query;
                this.x = x;
                this.y = y;
        }

        /**
         * Set the upper bound of the between condition.
         *
         * @param z the upper bound of the between condition
         * @return the query
         */
        public QueryWhere<T> and(A z) {
        	query.addConditionToken(new ConditionBetween<>(x, y, z));
        	return new QueryWhere<>(query);
        }
}
