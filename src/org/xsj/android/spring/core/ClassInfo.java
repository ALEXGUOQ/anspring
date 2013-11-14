package org.xsj.android.spring.core;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class ClassInfo {
	static HashMap<Class<?>, ClassInfo> classInfoMap = new HashMap<Class<?>, ClassInfo>();
	
	Class<?> clazz;
	HashMap<String,Field> fieldMap = new HashMap<String,Field>();
	HashMap<String,Method> methodMap = new HashMap<String,Method>();
	ArrayList<Field> fieldOverList = new ArrayList<Field>();
	ArrayList<Method> methodOverList = new ArrayList<Method>();
	
	public static synchronized ClassInfo find(Class<?> clazz){
		ClassInfo classInfo = classInfoMap.get(clazz);
		if(classInfo==null){
			classInfo = new ClassInfo();
			classInfo.clazz = clazz;
			recursiveCollect(classInfo,clazz);
			classInfoMap.put(clazz, classInfo);
		}
		return classInfo;
	}
	private static void recursiveCollect(ClassInfo classInfo, Class<?> clazz){
		if(clazz==Object.class)return;
		for(Field field : clazz.getDeclaredFields()){
			if(!classInfo.fieldMap.containsKey(field.getName())){
				classInfo.fieldMap.put(field.getName(), field);
			}else{
				classInfo.fieldOverList.add(field);
			}
		}
		for(Method method : clazz.getDeclaredMethods()){
			if(!classInfo.methodMap.containsKey(method.getName())){
				classInfo.methodMap.put(method.getName(), method);
			}else{
				classInfo.methodOverList.add(method);
			}
		}
		Class<?> supclazz = clazz.getSuperclass();
		recursiveCollect(classInfo,supclazz);
	}
	public Field getField(String fieldName){
		return fieldMap.get(fieldName);
	}
	public Method getMethod(String methodName){
		return methodMap.get(methodName);
	}
	public Collection<Field> getFieldList() {
		return fieldMap.values();
	}
	public Collection<Method> getMethodList() {
		return methodMap.values();
	}
	public Collection<Field> getFieldListWithOverride(){
		List<Field> flist = new ArrayList<Field>();
		flist.addAll(fieldMap.values());
		flist.addAll(fieldOverList);
		return flist;
	}
	public Collection<Method> getMethodListWithOverride(){
		List<Method> mlist = new ArrayList<Method>();
		mlist.addAll(methodMap.values());
		mlist.addAll(methodOverList);
		return mlist;
	}
}
