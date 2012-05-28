package com.centimia.orm.jaqu;

public interface ISelectTable<T> {

	/**
	 * Returns the generated ID given to the table within the select<br>
	 * example: the application generates "select T1.a, T1.b from myTable T1...." this method call will return "T1"
	 * 
	 * @return String
	 */
	public String getAs();

}