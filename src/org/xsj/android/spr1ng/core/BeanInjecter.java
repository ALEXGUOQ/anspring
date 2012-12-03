package org.xsj.android.spr1ng.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.xsj.android.spr1ng.core.annotation.Autowired;
import org.xsj.android.spr1ng.core.annotation.Qualifier;



public class BeanInjecter {
	public static SpringContext springContext = SpringContext.getInstance();

	public static void inject(Object object){
		if(object==null)	return;
		Class<?> clazz = object.getClass();
		Field[] fields = clazz.getDeclaredFields();
		if(fields!=null && fields.length>0){
			for(Field field: fields){
				Autowired autowired = field.getAnnotation(Autowired.class);
				if(autowired!=null){
					field.setAccessible(true);
					Qualifier qualifier = field.getAnnotation(Qualifier.class);
					Object fieldObj = null;
					String qname = null;
					if(qualifier!=null){
						qname = qualifier.value();
						qname = StringUtils.defaultIfEmpty(qname,field.getName());
						fieldObj = springContext.getBean(qname);
					}
					if(fieldObj==null){
						fieldObj = springContext.obtainBean(field.getType());
					}
					if(fieldObj==null){
						if(autowired.required()){
							String e = "can not inject"+object.getClass()+":"+field.getName();
							springContext.error(e);
						}
					}
					try {
						field.set(object, fieldObj);
					} catch (IllegalArgumentException e) {
						springContext.debug(e);
					} catch (IllegalAccessException e) {
						springContext.debug(e);
					}
				}
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
					for(int i=0;i<psAnns.length;i++){
						Annotation[] pAnns = psAnns[i];
						Object pobj = null;
						String qname = null;
						for(Annotation pAnn : pAnns){
							if(pAnn.annotationType()==Qualifier.class){
								qname = ((Qualifier)pAnn).value();
								qname = StringUtils.defaultIfEmpty(qname,StringUtils.uncapitalize(psClazz[i].getSimpleName()));
								pobj = springContext.getBean(qname);
								break;
							}
						}
						if(pobj==null){
							Class<?> pClazz = psClazz[i];
							pobj = springContext.obtainBean(pClazz);
							if(pobj==null){
								throw new Exception("can not inject ["+qname+"] "+pClazz);
							}
						}
						params[i]=pobj;
					}
					method.invoke(object, params);
				} catch (Exception e) {
					springContext.error(e);
				}
			}
		}
		
	}
}
