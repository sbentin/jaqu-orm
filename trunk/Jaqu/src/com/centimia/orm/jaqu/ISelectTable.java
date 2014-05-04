package com.centimia.orm.jaqu;

import java.util.Map;

interface ISelectTable<T> {

	enum JOIN_TYPE {
		OUTER_JOIN("OUTER JOIN"),
		LEFT_OUTER_JOIN("LEFT OUTER JOIN"),
		INNER_JOIN("INNER JOIN"), 
		NONE("");
		
		String type;
		
		JOIN_TYPE(String type){
			this.type = type;
		}
	}
	
	/**
	 * Returns the generated ID given to the table within the select<br>
	 * example: the application generates "select T1.a, T1.b from myTable T1...." this method call will return "T1"
	 * 
	 * @return String
	 */
	public String getAs();

	/**
	 * Returns a list of table IDs given to the joint tables. The ID's are given in the order of the join.
	 * @return Map<Object, String>
	 */
	public Map<Object, String> getJoins();

	/**
	 * Returns the type of join or none if the select table does not represent a joint table.
	 * <b>Note:</b> In a join query the initial table will return a join type of NONE
	 * 
	 * @return JOIN_TYPE
	 */
	public JOIN_TYPE getJoinType();

}