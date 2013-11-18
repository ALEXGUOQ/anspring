package org.xsj.android.spring.system;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;

import org.xsj.android.spring.common.Logx;
import org.xsj.android.spring.common.StringUtils;
import org.xsj.android.spring.core.ClassInfo;
import org.xsj.android.spring.core.SpringUtils;
import org.xsj.android.spring.system.annotation.R_Drawable;
import org.xsj.android.spring.system.annotation.R_Layout;
import org.xsj.android.spring.system.annotation.R_String;
import org.xsj.android.spring.system.annotation.SystemService;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;

public class SystemInjecter {
	
	public static void inject(Object object){
		inject(SpringUtils.getSpringContext().getContext(),object);
	}
	public static void inject(Context context,Object object){
		if(object==null)	return;
		Class<?> actClazz = object.getClass();
		Collection<Field> fields = ClassInfo.find(actClazz).getFieldListWithOverride();
		if(fields!=null){
			for(Field field : fields){
				field.setAccessible(true);
				Annotation[] annotaions = field.getAnnotations();
				Object bean=null;
				for(Annotation annotation : annotaions){
					Class<?> aclazz = annotation.annotationType();
					if(aclazz == R_Layout.class){
						bean=RLayout(context,object, field, annotation);
						break;
					}else if(aclazz == R_Drawable.class){
						bean=RDrawable(context,object, field, annotation);
						break;
					}else if(aclazz == R_String.class){
						bean=RString(context,object, field, annotation);
						break;
					}else if(aclazz == SystemService.class){
						bean=SystemService(context,object, field, annotation);
						break;
					}
				}
			}
		}		
	}

	private static Object RLayout(Context context,Object object,Field field,Annotation annotation){
		R_Layout annotationObj = field.getAnnotation(R_Layout.class);
		int layoutId = annotationObj.value();
		try {
			if(layoutId==0)	return null;
			LayoutInflater inflater = LayoutInflater.from(context);
			View view = inflater.inflate(layoutId, null);
			Class<?> fieldClazz = field.getType();
			if(View.class.isAssignableFrom(fieldClazz)){
				field.set(object,view);
				return view;
			}
		} catch (Exception e) {
			runtimeErr(e);
		}
		return null;
	}
	private static Object RDrawable(Context context,Object object,Field field,Annotation annotation){
		R_Drawable annotationObj = field.getAnnotation(R_Drawable.class);
		int drawableId = annotationObj.value();
		try {
			if(drawableId==0)	return null;
			Drawable drawable = context.getResources().getDrawable(drawableId);
			Class<?> fieldClazz = field.getType();
			if(Drawable.class.isAssignableFrom(fieldClazz)){
				field.set(object,drawable);
				return drawable;
			}
		} catch (Exception e) {
			runtimeErr(e);
		}
		return null;
	}
	private static Object RString(Context context,Object object,Field field,Annotation annotation){
		R_String annotationObj = field.getAnnotation(R_String.class);
		int StringId = annotationObj.value();
		try {
			if(StringId==0)	return null;
			String str = context.getResources().getString(StringId);
			field.set(object,str);
			return str;
		} catch (Exception e) {
			runtimeErr(e);
		}
		return null;
	}
	private static Object SystemService(Context context,Object object,Field field,Annotation annotation){
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
			Object obj = context.getSystemService(anvalue);
			field.set(object,obj);
			return obj;
		} catch (Exception e) {
			runtimeErr(e);
		}
		return null;
	}
	
	private static void runtimeErr(Exception e){
		Logx.et("SystemInjecter",e.getMessage());
		throw new RuntimeException(e);
	}
}
