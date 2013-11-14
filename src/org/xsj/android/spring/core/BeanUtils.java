package org.xsj.android.spring.core;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class BeanUtils {
	public static<T> T copy(T t) {
		Class<T> tClazz = (Class<T>) t.getClass();
		T o = null;
		try {
			o = tClazz.newInstance();
			copy(t, o);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return o;
	}
	public static void copy(Object src,Object target){
		Class<?> srcClazz = src.getClass();
		Class<?> targetClazz = target.getClass();
		ClassInfo srcInfo = ClassInfo.find(srcClazz);
		ClassInfo targetInfo = ClassInfo.find(targetClazz);
		for(Field srcField : srcInfo.getFieldList()){
			Field targetField = targetInfo.getField(srcField.getName());
			if(targetField == null){
				continue;
			}
			srcField.setAccessible(true);
			targetField.setAccessible(true);
			try {
				targetField.set(target, srcField.get(src));
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		
/*		Method methods[] =  srcClazz.getMethods();
		Method targetMethods[] =  targetClazz.getMethods();
		ArrayList<Method> targetMethodList = new ArrayList<Method>(Arrays.asList(targetMethods));
		for(Method method : methods){
			String methodName = method.getName();
			if(methodName.startsWith("get") && methodName.length()>3){
				String FieldName = methodName.substring(3, methodName.length());
				String targetMethodName = "set"+FieldName;
				Iterator<Method> ite = targetMethodList.iterator();
				while(ite.hasNext()){
					Method m = ite.next();
					if(m.getName().equals(targetMethodName)){
						try {
							m.invoke(target, method.invoke(src));
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						} catch (InvocationTargetException e) {
							e.printStackTrace();
						}
						ite.remove();
					}
				}
			};
			
		}
*/
	}
	public static<S,T> void copyList(List<S> srclist,List<T>targetlist,Class<T> tclazz){
		for(S s : srclist){
			try {
				T t = tclazz.newInstance();
				copy(s, t);
				targetlist.add(t);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			}
		}
	}
}
