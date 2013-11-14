package org.xsj.android.spring.db;

import java.lang.reflect.Field;

public class PoFieldInfo {
	Field field;
	String dbName;
	String dbProperty;
	
	public String createSql(){
		String sql = dbName+" "+dbProperty;
		return sql;
	}
}
