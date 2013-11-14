package org.xsj.android.spring.system.activity;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;

import org.xsj.android.spring.core.BeanInjecter;
import org.xsj.android.spring.core.ClassInfo;
import org.xsj.android.spring.core.SpringContext;
import org.xsj.android.spring.core.SpringUtils;
import org.xsj.android.spring.core.StringUtils;
import org.xsj.android.spring.system.SystemInjecter;
import org.xsj.android.spring.system.activity.annotaion.AfterStart;
import org.xsj.android.spring.system.activity.annotaion.OnClick;
import org.xsj.android.spring.system.activity.annotaion.OnItemClick;
import org.xsj.android.spring.system.activity.annotaion.OnItemLongClick;
import org.xsj.android.spring.system.activity.annotaion.OnItemSelected;
import org.xsj.android.spring.system.activity.annotaion.OnLongClick;
import org.xsj.android.spring.system.activity.annotaion.R_Id;
import org.xsj.android.spring.system.annotation.R_Drawable;
import org.xsj.android.spring.system.annotation.R_Layout;
import org.xsj.android.spring.system.annotation.R_String;
import org.xsj.android.spring.system.annotation.RegisterBean;
import org.xsj.android.spring.system.annotation.SystemService;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

public class ActivityInjecter {
	public static void inject(Activity activity){
		inject(SpringUtils.getSpringContext(), activity);
	}
	public static void inject(SpringContext springContext,Activity activity){
		Class<?> actClazz = activity.getClass();
		Collection<Field> fields = ClassInfo.find(actClazz).getFieldListWithOverride();
		if(fields!=null){
			for(Field field : fields){
				field.setAccessible(true);
				Annotation[] annotaions = field.getAnnotations();
				Object bean=null;
				for(Annotation annotation : annotaions){
					Class<?> aclazz = annotation.annotationType();
					if(aclazz == R_Id.class){
						bean=RId(springContext,activity, field, annotation);
						break;
					}
				}
			}
		}
		Method[] methods = actClazz.getDeclaredMethods();
		if(methods!=null && methods.length>0){
			for(Method method : methods){
				method.setAccessible(true);
				Annotation[] annotaions = method.getAnnotations();
				for(Annotation annotation : annotaions){
					Class<?> aclazz = annotation.annotationType();
					if(aclazz == OnClick.class){
						Click(springContext,activity, method, annotation);
					}else if(aclazz == OnLongClick.class){
						LongClick(springContext,activity, method, annotation);
					}else if(aclazz == OnItemClick.class){
						ItemClick(springContext,activity, method, annotation);
					}else if(aclazz == OnItemLongClick.class){
						ItemLongClick(springContext,activity, method, annotation);
					}else if(aclazz == OnItemSelected.class){
						ItemSelected(springContext,activity, method, annotation);
					}else if(aclazz == AfterStart.class){
						AfterViews(springContext,activity, method, annotation);
					}
					
				}
			}
		}
		SystemInjecter.inject(springContext,activity);
	}
	private static Object RId(SpringContext springContext,Activity activity,Field field,Annotation annotation){
		R_Id annotationObj = field.getAnnotation(R_Id.class);
		int viewId = annotationObj.value();
		try {
			if(viewId==0)	return null;
			View view = activity.findViewById(viewId);
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

	private static void Click(SpringContext springContext,final Activity activity,final Method method,Annotation annotation){
		OnClick annotationObj = method.getAnnotation(OnClick.class);
		int[] viewIds = annotationObj.value();
		for(int viewId : viewIds){
			if(viewId==0)	continue;
			View view = activity.findViewById(viewId);
			final Class<?>[] paramClazzs = method.getParameterTypes();
			final int[] paramsIndex = {-1};
			paramsIndex[0] = _findClassIndex(paramClazzs,View.class);
			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						Object[] params = new Object[paramClazzs.length];
						_setParams(params,paramsIndex, v, 0);
						method.invoke(activity, params);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			});
		}
	}
	
	private static void LongClick(SpringContext springContext,final Activity activity,final Method method,Annotation annotation){
		OnLongClick annotationObj = method.getAnnotation(OnLongClick.class);
		int[] viewIds = annotationObj.value();
		for(int viewId : viewIds){
			if(viewId==0)	continue;
			View view = activity.findViewById(viewId);
			final Class<?>[] paramClazzs = method.getParameterTypes();
			final int[] paramsIndex = {-1};
			paramsIndex[0] = _findClassIndex(paramClazzs, View.class);
			view.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					try {
						Object[] params = new Object[paramClazzs.length];
						_setParams(params,paramsIndex, v, 0);
						Object res = method.invoke(activity, params);
						if(res!=null && res==Boolean.TRUE){
							return true;
						}
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
					return false;
				}
			});
		}
	}
	
	private static void ItemClick(SpringContext springContext,final Activity activity,final Method method,Annotation annotation){
		OnItemClick annotationObj = method.getAnnotation(OnItemClick.class);
		int[] viewIds = annotationObj.value();
		for(int viewId : viewIds){
			if(viewId==0)	continue;
			View view = activity.findViewById(viewId);
			if(!(view instanceof AdapterView<?>)){
				System.out.println(viewId+" is not adapterView!");
				continue;
			}
			final Class<?>[] paramClazzs = method.getParameterTypes();
			final int[] paramsIndex = {-1};
			paramsIndex[0] = _findClassIndex(paramClazzs, AdapterView.class);
			paramsIndex[1] = _findClassIndex(paramClazzs, View.class);
			paramsIndex[2] = _findClassIndex(paramClazzs, Integer.TYPE);
			paramsIndex[3] = _findClassIndex(paramClazzs, Long.TYPE);
			((AdapterView<?>)view).setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					try {
						Object[] params = new Object[paramClazzs.length];
						_setParams(params,paramsIndex, arg0, 0);
						_setParams(params,paramsIndex, arg1, 1);
						_setParams(params,paramsIndex, arg2, 2);
						_setParams(params,paramsIndex, arg3, 3);
						method.invoke(activity, params);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}

			});
		}
	}
	
	private static void ItemLongClick(SpringContext springContext,final Activity activity,final Method method,Annotation annotation){
		OnItemLongClick annotationObj = method.getAnnotation(OnItemLongClick.class);
		int[] viewIds = annotationObj.value();
		for(int viewId : viewIds){
			if(viewId==0)	continue;
			View view = activity.findViewById(viewId);
			if(!(view instanceof AdapterView<?>)){
				System.out.println(viewId+" is not adapterView!");
				continue;
			}
			final Class<?>[] paramClazzs = method.getParameterTypes();
			final int[] paramsIndex = {-1};
			paramsIndex[0] = _findClassIndex(paramClazzs, AdapterView.class);
			paramsIndex[1] = _findClassIndex(paramClazzs, View.class);
			paramsIndex[2] = _findClassIndex(paramClazzs, Integer.TYPE);
			paramsIndex[3] = _findClassIndex(paramClazzs, Long.TYPE);
			((AdapterView<?>)view).setOnItemLongClickListener(new OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					try {
						Object[] params = new Object[paramClazzs.length];
						_setParams(params,paramsIndex, arg0, 0);
						_setParams(params,paramsIndex, arg1, 1);
						_setParams(params,paramsIndex, arg2, 2);
						_setParams(params,paramsIndex, arg3, 3);
						Object res = method.invoke(activity, params);
						if(res!=null && res==Boolean.TRUE){
							return true;
						}
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
					return false;
				}

			});
		}
	}
	
	private static void ItemSelected(SpringContext springContext,final Activity activity,final Method method,Annotation annotation){
		OnItemSelected annotationObj = method.getAnnotation(OnItemSelected.class);
		int[] viewIds = annotationObj.value();
		for(int viewId : viewIds){
			if(viewId==0)	continue;
			View view = activity.findViewById(viewId);
			if(!(view instanceof AdapterView<?>)){
				System.out.println(viewId+" is not adapterView!");
				continue;
			}
			final Class<?>[] paramClazzs = method.getParameterTypes();
			final int[] paramsIndex = {-1};
			paramsIndex[0] = _findClassIndex(paramClazzs, AdapterView.class);
			paramsIndex[1] = _findClassIndex(paramClazzs, View.class);
			paramsIndex[2] = _findClassIndex(paramClazzs, Integer.TYPE);
			paramsIndex[3] = _findClassIndex(paramClazzs, Long.TYPE);
			((AdapterView<?>)view).setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					try {
						Object[] params = new Object[paramClazzs.length];
						_setParams(params,paramsIndex, arg0, 0);
						_setParams(params,paramsIndex, arg1, 1);
						_setParams(params,paramsIndex, arg2, 2);
						_setParams(params,paramsIndex, arg3, 3);
						method.invoke(activity, params);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
				}

			});
		}
	}
	
	private static void AfterViews(SpringContext springContext,final Activity activity,final Method method,Annotation annotation){
		AfterStart annotationObj = method.getAnnotation(AfterStart.class);
		int delayMillis = (int) annotationObj.value();
		activity.getWindow().getDecorView().postDelayed(new Runnable() {
			@Override
			public void run() {
				try {
					method.invoke(activity);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}, delayMillis);
	}
	
	
	private static int _findClassIndex(final Class<?>[] paramClazzs,Class<?> clazz) {
		int viewIndex = -1;
		for (int i = 0; i < paramClazzs.length; i++) {
			if(paramClazzs[i]==clazz){
				viewIndex =  i;
				break;
			}
		}
		return viewIndex;
	}
	private static void _setParams(Object[] params,final int[] paramsIndex,Object arg,int i) {
		int index = paramsIndex[i];
		if(index!=-1){
			params[index]=arg;
		}
	}
	
	
	
}
