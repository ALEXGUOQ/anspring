package org.xsj.android.spring.common;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.xsj.android.spring.core.ClassInfo;

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
	
	public static boolean containSubClass(Class<?>[] clazzs,Class<?> sub){
		for(Class<?> clazz : clazzs){
			if(clazz.isAssignableFrom(sub)){
				return true;
			}
		}
		return false;
	}

}
