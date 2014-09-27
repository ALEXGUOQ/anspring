package org.xsj.android.spring.core;

import org.xsj.android.spring.core.annotation.Configuration;
import org.xsj.android.spring.core.annotation.DefaultConfigure;
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
		if(springContext!=null){
			return getSpringContext().hasLoad();
		}else{
			return false;
		}
	}
	public static void load(Context context,Class<?> configClass){
		getSpringContext().load(context, configClass);
	}
	/**
	 * 加载spring环境
	 * @param context
	 */
	public static void load(Context context){
		getSpringContext().load(context,DefaultConfigure.class);
	}
	/**
	 * 重新加载spring
	 * @param context
	 * @param configClass
	 */
	public static void reload(Context context,Class<?> configClass){
		unload();
		load(context,configClass);
	}
	/**
	 * 关闭spring
	 */
	public static void unload(){
		getSpringContext().unload();
		springContext = null;
	}
	/**
	 * 使用spring容器中的bean，给object注入成员
	 * @param object
	 */
	public static void injectBean(Object object){
		BeanInjecter.inject(object);
	}
	/**
	 * 给object 注入android 特有的东西，比如各种service
	 * 不包含injectBean，
	 * 可以在spring没有load过的情况下使用。
	 * @param object
	 */
	public static void injectSystem(Object object){
		SystemInjecter.inject(object);
	}
	/**
	 * 给activity注入android activity特有的东西，比如R.id.xxxxx 的组件
	 * 包含injectSystem功能，
	 * 不包含injectBean，
	 * 可以在spring没有load过的情况下使用。
	 * @param activity
	 */
	public static void injectActivity(Activity activity){
		ActivityInjecter.inject(activity);
	}
	
}
