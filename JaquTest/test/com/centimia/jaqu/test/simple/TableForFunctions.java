/*
 * Copyright (c) 2007-2008 Centimia Ltd.
 * All rights reserved.  Unpublished -- rights reserved
 *
 * Use of a copyright notice is precautionary only, and does
 * not imply publication or disclosure.
 *
 * THIS SOFTWARE CONTAINS CONFIDENTIAL INFORMATION AND TRADE
 * SECRETS OF CENTIMIA LTD. USE, DISCLOSURE, OR
 * REPRODUCTION IS PROHIBITED WITHOUT THE PRIOR EXPRESS
 * WRITTEN PERMISSION OF CENTIMIA, INC.
 */

/*
 * Update Log
 * 
 *  Date			User				Comment
 * ------			-------				--------
 * 08/02/2010		Shai Bentin				 create
 */
package com.centimia.jaqu.test.simple;

import java.util.ArrayList;
import java.util.List;

/**
 * @author shai
 *
 */
public class TableForFunctions {

	public Long id;
	public String name;
	public Double value;
	
	public TableForFunctions(){
		
	}
	
	public TableForFunctions(Long id, String name, Double value) {
		super();
		this.id = id;
		this.name = name;
		this.value = value;
	}

	public static List<TableForFunctions> getData(){
		ArrayList<TableForFunctions> data = new ArrayList<TableForFunctions>();
		data.add(new TableForFunctions(1L, "name1", 1.1D));
		data.add(new TableForFunctions(2L, "name1", 2.1D));
		data.add(new TableForFunctions(3L, "name1", 3.1D));
		data.add(new TableForFunctions(4L, "name1", 4.1D));
		data.add(new TableForFunctions(5L, "name1", 5.1D));
		data.add(new TableForFunctions(6L, "name2", 1.2D));
		data.add(new TableForFunctions(7L, "name2", 2.2D));
		data.add(new TableForFunctions(8L, "name2", 3.2D));
		data.add(new TableForFunctions(9L, "name2", 4.2D));
		data.add(new TableForFunctions(10L, "name2", 5.2D));
		data.add(new TableForFunctions(11L, "name3", 1.3D));
		data.add(new TableForFunctions(12L, "name3", 2.3D));
		data.add(new TableForFunctions(13L, "name3", 3.3D));
		data.add(new TableForFunctions(14L, "name3", 4.3D));
		data.add(new TableForFunctions(15L, "name3", 5.3D));
		data.add(new TableForFunctions(16L, null, 0D));
		return data;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}
}
