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

import java.sql.BatchUpdateException;

/**
 * 
 */
public class JaquBatchError extends RuntimeException {
	private static final long serialVersionUID = -948083078650635421L;

	private final int batchCount;
	
	public JaquBatchError(BatchUpdateException batchEx, int batchCount) {
		super(batchEx);
		this.batchCount = batchCount;
	}

	/**
	 * Get the original BatchUpdateException thrown by the JDBC driver which has data and information
	 * about the batch failure. From this exception you can get the update counts for the failed batch.
	 * You can do the following:
	 * <pre>
	 * int i = 0;
	 * for (int count: jaquBatchError.getBatchException().getUpdateCounts()) {
	 *    // process count
	 *    if (count == Statement.EXECUTE_FAILED) {
	 *    	// handle failed update with index i (remember to adjust for batch size and batch number using jaquBatchError.getBatchCount())
	 *    }
	 *    i++;
	 * }
	 * </pre>
	 * 
	 * @return BatchUpdateException the original exception thrown by the JDBC driver.
	 */
	public BatchUpdateException getBatchException() {
		return (BatchUpdateException)getCause();
	}

	/**
	 * since the failing batch could be in the middle of a larger batch, this method returns the index of the batch
	 * that failed. To get the correct position in the original batch multiply this number by the batch size and add the iteration
	 * count on the updateCount that failed. see {@link java.sql.BatchUpdateException#getUpdateCounts()}
	 * 
	 * 
	 * @return the batchCount
	 */
	int getBatchCount() {
		return batchCount;
	}
}
