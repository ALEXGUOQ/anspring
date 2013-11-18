package org.xsj.android.spring.core.db;

import android.database.sqlite.SQLiteDatabase;

public class SqliteTemplate {
	protected DataSource dataSource;
	public SqliteTemplate(){
	}
	public SqliteTemplate(DataSource dataSource){
		this();
		this.dataSource = dataSource;
	}
	public DataSource getDataSource() {
		return dataSource;
	}
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	public SQLiteDatabase getDb(){
		return dataSource.injectSQLiteDatabase();
	}
}
