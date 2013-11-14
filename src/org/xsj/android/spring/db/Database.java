package org.xsj.android.spring.db;

import java.util.HashMap;

import org.xsj.android.spring.core.SpringUtils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class Database {
	HashMap<Class<?>, PoInfo> PoInfoMap;
	SQLiteDatabase sdb;
	public Database() {
		sdb = SQLiteDatabase.openOrCreateDatabase("spring.db",null);
		PoInfoMap = new HashMap<Class<?>, PoInfo>();
	}
	public <T>void save(T t){
		Class<T> clazz = (Class<T>) t.getClass();
		PoInfo<T> poInfo = PoInfoMap.get(clazz);
		if(poInfo==null){
			//TODO check(0;
		}
		String sqlstr = poInfo.createSaveSql();
		sdb.execSQL(sqlstr);
	}
}
