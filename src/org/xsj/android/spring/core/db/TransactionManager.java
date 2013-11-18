package org.xsj.android.spring.core.db;

import android.database.sqlite.SQLiteDatabase;

public class TransactionManager {
	private DataSource dataSource;
	private ThreadLocal<Integer> threadLocalTranNum;
	
	
	public SQLiteDatabase getDb() {
		return dataSource.getSQLiteDatabase();
	}


	public ThreadLocal<Integer> getThreadLocalTranNum() {
		return threadLocalTranNum;
	}

	public TransactionManager() {
		threadLocalTranNum = new ThreadLocal<Integer>();
	}

	public TransactionManager(DataSource dataSource) {
		this();
		this.dataSource = dataSource;
	}
}
