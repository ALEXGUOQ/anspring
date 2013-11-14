package org.xsj.android.spring.db;

import org.xsj.android.spring.core.StringUtils;

public class PoInfo<T> {
	Class<T> clazz;
	String dbName;
	PoFieldInfo id;
	PoFieldInfo[] poFieldInfoList;
	
	public String createSaveSql(){
		String poFileInfos="";
		for(PoFieldInfo poFieldInfo:poFieldInfoList){
			if(StringUtils.isEmpty(poFileInfos)){
				poFileInfos = poFieldInfo.createSql();
			}else{
				poFileInfos += ","+poFieldInfo.createSql();
			}
		}
		String sql = "create tabel "+dbName+" ("+poFileInfos+") ";
		return sql;
	}
}
