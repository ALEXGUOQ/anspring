package org.xsj.android.spring.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import org.xsj.android.spring.common.StringUtils;
import org.xsj.android.spring.core.annotation.Autowired;
import org.xsj.android.spring.core.annotation.PostConstruct;
import org.xsj.android.spring.core.annotation.PreDestroy;
import org.xsj.android.spring.core.annotation.Qualifier;
import org.xsj.android.spring.core.annotation.Value;



public class BeanInjecter {
	public static void inject(Object object){
		inject(SpringUtils.getSpringContext(),object);
	}

	public static void inject(SpringContext springContext,Object object){
		if(object==null)	return;
		if(springContext.getStatus()==SpringContext.STATUS_UNLOAD ){
			springContext.error("SpringContext has not load config");
		}
		Class<?> clazz = object.getClass();
		Collection<Field> fields = ClassInfo.find(clazz).getFieldListWithOverride();
		if(fields!=null && fields.size()>0){
			for(Field field: fields){
				if(autowareInject(springContext, field, object) || valueInject(springContext, field, object));
			}
		}
		
		Method[] methods = clazz.getDeclaredMethods();
		for(Method method: methods){
			Autowired autowired = method.getAnnotation(Autowired.class);
			if(autowired!=null){
				try {
					Annotation[][] psAnns = method.getParameterAnnotations();
					Class<?>[] psClazz = method.getParameterTypes();
					Object params[]=new Object[psAnns.length];
					springContext.getConfigurationLoader().assignParam(clazz, psAnns, psClazz, params);
					method.invoke(object, params);
				} catch (Exception e) {
					springContext.error(e);
				}
			}
			if(springContext.getStatus()==SpringContext.STATUS_LOADING ){
				PostConstruct postConstruct = method.getAnnotation(PostConstruct.class);
				if(postConstruct!=null){
					if(method.getReturnType() != Void.TYPE){
						springContext.error("PostConstruct must return void:"+method);
					}else if(method.getParameterTypes().length > 0){
						springContext.error("PostConstruct must no param:"+method);
					}
					springContext.getConfigurationLoader().addPostConstruct(new PlanInvokeMethod(object, method));
				}
				PreDestroy preDestroy = method.getAnnotation(PreDestroy.class);
				if(preDestroy!=null){
					if(method.getReturnType() != Void.TYPE){
						springContext.error("PostConstruct must return void:"+method);
					}else if(method.getParameterTypes().length > 0){
						springContext.error("PostConstruct must no param:"+method);
					}
					springContext.getConfigurationLoader().addPreDestroy(new PlanInvokeMethod(object, method));
				}
			}
		}
		
	}
	private static boolean autowareInject(SpringContext springContext, Field field, Object object) {
		Autowired autowired = field.getAnnotation(Autowired.class);
		if(autowired!=null){
			field.setAccessible(true);
			Qualifier qualifier = field.getAnnotation(Qualifier.class);
			Object fieldObj = null;
			String qname = null;
			if(qualifier!=null){
				qname = qualifier.value();
				qname = StringUtils.defaultIfEmpty(qname,field.getName());
				fieldObj = springContext.getConfigurationLoader().obtainObject(qname);
			}else{
				fieldObj = springContext.getConfigurationLoader().obtainObject(field.getType());
			}
			if(fieldObj==null){
				if(autowired.required()){
					String e = "can not inject"+object.getClass()+":"+field.getName();
					springContext.error(e);
				}
			}
			try {
				field.set(object, fieldObj);
			} catch (Exception e) {
				springContext.debug(e);
			}
			return true;
		}
		return false;
	}
	private static boolean valueInject(SpringContext springContext, Field field, Object object) {
		Value annValue = field.getAnnotation(Value.class);
		if(annValue!=null){
			field.setAccessible(true);
			String pvalueKey = annValue.value().trim();
			Object fieldObj = springContext.getConfigurationLoader().getAnnValueObject(field.getType(), pvalueKey);
			try {
				field.set(object, fieldObj);
			} catch (Exception e) {
				springContext.debug(e);
			}
			return true;
		}
		return false;
	}
}
