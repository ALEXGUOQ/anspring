package org.xsj.android.spring.core;

import org.xsj.android.spring.core.annotation.Configuration;
import org.xsj.android.spring.system.SystemInjecter;
import org.xsj.android.spring.system.activity.ActivityInjecter;

import android.app.Activity;
import android.content.Context;

public class SpringUtils {
	private static SpringContext springContext = new SpringContext();
	
	public static SpringContext getSpringContext(){
		if(springContext==null){
			springContext = new SpringContext();
		}
		return springContext;
	}

	public static<T> T getBean(String name){
		T obj = getSpringContext().<T>getBean(name);
		return obj;
	}
	public static<T> T getBean(Class<T> clazz){
		T obj = getSpringContext().<T>getBean(clazz);
		return obj;
	}
	public static<T> void registerBean(String name,T object){
		getSpringContext().registerBean(name, object);
	}
	public static void unRegisterBean(String name){
		getSpringContext().unRegisterBean(name);
	}
	public static boolean hasLoad(){
		return getSpringContext().hasLoad();
	}
	public static void load(Context context,Class<?> configClass){
		getSpringContext().load(context, configClass);
	}
	public static void load(Context context){
		getSpringContext().load(context);
	}
	public static void reload(Context context,Class<?> configClass){
		unload();
		load(context,configClass);
	}
	public static void unload(){
		getSpringContext().unload();
		springContext = null;
	}
}
