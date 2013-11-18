package org.xsj.android.spring.core.db;

import android.database.sqlite.SQLiteDatabase;

public abstract class DataSource {
	protected SQLiteDatabase db;
	public DataSource() {
		this.db = injectSQLiteDatabase();
	}
	protected abstract SQLiteDatabase injectSQLiteDatabase();
	public SQLiteDatabase getSQLiteDatabase(){
		return db;
	}
}
