package org.xsj.android.spring.system;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;

import org.xsj.android.spring.core.BeanInjecter;
import org.xsj.android.spring.core.ClassInfo;
import org.xsj.android.spring.core.SpringContext;
import org.xsj.android.spring.core.SpringUtils;
import org.xsj.android.spring.core.StringUtils;
import org.xsj.android.spring.system.annotation.R_Drawable;
import org.xsj.android.spring.system.annotation.R_Layout;
import org.xsj.android.spring.system.annotation.R_String;
import org.xsj.android.spring.system.annotation.RegisterBean;
import org.xsj.android.spring.system.annotation.SystemService;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;

public class SystemInjecter {
	public static void inject(Object obj){
		inject(SpringUtils.getSpringContext(),obj);
	}
	public static void inject(SpringContext springContext,Object obj){
		Class<?> actClazz = obj.getClass();
		RegisterBean actAnn = actClazz.getAnnotation(RegisterBean.class);
		if(actAnn!=null){
			String actName = actAnn.value();
			actName = StringUtils.defaultIfEmpty(actName, StringUtils.uncapitalize(actClazz.getSimpleName()));
			springContext.registerBean(actName, obj);
		}
		Collection<Field> fields = ClassInfo.find(actClazz).getFieldListWithOverride();
		if(fields!=null){
			for(Field field : fields){
				field.setAccessible(true);
				Annotation[] annotaions = field.getAnnotations();
				Object bean=null;
				for(Annotation annotation : annotaions){
					Class<?> aclazz = annotation.annotationType();
					if(aclazz == R_Layout.class){
						bean=RLayout(springContext,obj, field, annotation);
						break;
					}else if(aclazz == R_Drawable.class){
						bean=RDrawable(springContext,obj, field, annotation);
						break;
					}else if(aclazz == R_String.class){
						bean=RString(springContext,obj, field, annotation);
						break;
					}else if(aclazz == SystemService.class){
						bean=SystemService(springContext,obj, field, annotation);
						break;
					}
				}
				if(bean!=null){
					RegisterBean fAnn = field.getAnnotation(RegisterBean.class);
					if(fAnn!=null){
						String fName = fAnn.value();
						fName = StringUtils.defaultIfEmpty(fName, field.getName());
						springContext.registerBean(fName,bean);
					}
				}

			}
		}		
		BeanInjecter.inject(springContext,obj);
	}

	private static Object RLayout(SpringContext springContext,Object activity,Field field,Annotation annotation){
		R_Layout annotationObj = field.getAnnotation(R_Layout.class);
		int layoutId = annotationObj.value();
		try {
			if(layoutId==0)	return null;
			LayoutInflater inflater = LayoutInflater.from(springContext.getContext());
			View view = inflater.inflate(layoutId, null);
			Class<?> fieldClazz = field.getType();
			if(View.class.isAssignableFrom(fieldClazz)){
				field.set(activity,view);
				return view;
			}
		} catch (Exception e) {
			springContext.error(e);
		}
		return null;
	}
	private static Object RDrawable(SpringContext springContext,Object activity,Field field,Annotation annotation){
		R_Drawable annotationObj = field.getAnnotation(R_Drawable.class);
		int drawableId = annotationObj.value();
		try {
			if(drawableId==0)	return null;
			Drawable drawable = springContext.<Context>getBean("").getResources().getDrawable(drawableId);
			Class<?> fieldClazz = field.getType();
			if(Drawable.class.isAssignableFrom(fieldClazz)){
				field.set(activity,drawable);
				return drawable;
			}
		} catch (Exception e) {
			springContext.error(e);
		}
		return null;
	}
	private static Object RString(SpringContext springContext,Object activity,Field field,Annotation annotation){
		R_String annotationObj = field.getAnnotation(R_String.class);
		int StringId = annotationObj.value();
		try {
			if(StringId==0)	return null;
			String str = springContext.getContext().getResources().getString(StringId);
			field.set(activity,str);
			return str;
		} catch (Exception e) {
			springContext.error(e);
		}
		return null;
	}
	private static Object SystemService(SpringContext springContext,Object activity,Field field,Annotation annotation){
		SystemService annotationObj = field.getAnnotation(SystemService.class);
		String anvalue = annotationObj.value();
		try {
			if(StringUtils.isEmpty(anvalue)){
				String name = field.getType().getSimpleName();
				anvalue = StringUtils.uncapitalize(StringUtils.substringBefore(name, "Manager"));
				StringBuffer buf = new StringBuffer();
				for(int i=0;i<anvalue.length();i++){
					char c = anvalue.charAt(i);
					if(Character.isUpperCase(c)){
						buf.append('_').append(Character.toLowerCase(c));
					}else{
						buf.append(c);
					}
				}
				anvalue=buf.toString();
			}
			Context context = springContext.getContext();
			Object obj = context.getSystemService(anvalue);
			field.set(activity,obj);
			return obj;
		} catch (Exception e) {
			springContext.error(e);
		}
		return null;
	}
	
	
}
