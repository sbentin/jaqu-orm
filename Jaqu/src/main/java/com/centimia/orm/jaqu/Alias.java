/*
 * Copyright (c) 2020-2024 Shai Bentin & Centimia Inc..
 * All rights reserved.  Unpublished -- rights reserved
 *
 * Use of a copyright notice is precautionary only, and does
 * not imply publication or disclosure.
 *
 * THIS SOFTWARE CONTAINS CONFIDENTIAL INFORMATION AND TRADE
 * SECRETS OF Shai Bentin USE, DISCLOSURE, OR
 * REPRODUCTION IS PROHIBITED WITHOUT THE PRIOR EXPRESS
 * WRITTEN PERMISSION OF Shai Bentin & CENTIMIA, INC.
 */
package com.centimia.orm.jaqu;

import java.io.Serializable;

/**
 * @author shai
 */
public class Alias implements Serializable {
	private static final long serialVersionUID = 6148657754364675980L;

	/** Holds the entity descriptor for the table being aliased */
	public Object aliasEntity;
	
	/** Holds the alias table name in the query */
	public String alias;
	
	public Alias(Object aliasEntity, String as) {
		this.aliasEntity = aliasEntity;
		this.alias = as;
	}

	/*
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (null != this.aliasEntity)
			return aliasEntity.hashCode();
		return super.hashCode();
	}

	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (null == obj)
			return false;
		if (!(obj instanceof Alias))
			return false;
		if (null == this.aliasEntity)
			return false;
		
		return this.aliasEntity.equals(((Alias)obj).aliasEntity);
	}

	/*
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.alias;
	}
}
