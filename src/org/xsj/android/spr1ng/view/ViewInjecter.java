package org.xsj.android.spr1ng.view;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.xsj.android.spr1ng.core.SpringContext;
import org.xsj.android.spr1ng.core.StringUtils;
import org.xsj.android.spr1ng.view.annotation.AfterStart;
import org.xsj.android.spr1ng.view.annotation.Click;
import org.xsj.android.spr1ng.view.annotation.ItemClick;
import org.xsj.android.spr1ng.view.annotation.ItemLongClick;
import org.xsj.android.spr1ng.view.annotation.ItemSelected;
import org.xsj.android.spr1ng.view.annotation.LongClick;
import org.xsj.android.spr1ng.view.annotation.RDrawable;
import org.xsj.android.spr1ng.view.annotation.RId;
import org.xsj.android.spr1ng.view.annotation.RLayout;
import org.xsj.android.spr1ng.view.annotation.RString;
import org.xsj.android.spr1ng.view.annotation.RegisterBean;
import org.xsj.android.spr1ng.view.annotation.SystemService;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

public class ViewInjecter {
	private static SpringContext springContext = SpringContext.getInstance();
	public static void inject(Activity activity){
		Class<?> actClazz = activity.getClass();
		RegisterBean actAnn = actClazz.getAnnotation(RegisterBean.class);
		if(actAnn!=null){
			String actName = actAnn.value();
			actName = StringUtils.defaultIfEmpty(actName, StringUtils.uncapitalize(actClazz.getSimpleName()));
			springContext.registerBean(actName, activity);
		}
		Field[] fields = actClazz.getDeclaredFields();
		if(fields!=null && fields.length>0){
			for(Field field : fields){
				field.setAccessible(true);
				Annotation[] annotaions = field.getAnnotations();
				Object bean=null;
				for(Annotation annotation : annotaions){
					Class<?> aclazz = annotation.annotationType();
					if(aclazz == RId.class){
						bean=RId(activity, field, annotation);
						break;
					}else if(aclazz == RLayout.class){
						bean=RLayout(activity, field, annotation);
						break;
					}else if(aclazz == RDrawable.class){
						bean=RDrawable(activity, field, annotation);
						break;
					}else if(aclazz == RString.class){
						bean=RString(activity, field, annotation);
						break;
					}else if(aclazz == SystemService.class){
						bean=SystemService(activity, field, annotation);
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
		Method[] methods = actClazz.getDeclaredMethods();
		if(methods!=null && methods.length>0){
			for(Method method : methods){
				method.setAccessible(true);
				Annotation[] annotaions = method.getAnnotations();
				for(Annotation annotation : annotaions){
					Class<?> aclazz = annotation.annotationType();
					if(aclazz == Click.class){
						Click(activity, method, annotation);
					}else if(aclazz == LongClick.class){
						LongClick(activity, method, annotation);
					}else if(aclazz == ItemClick.class){
						ItemClick(activity, method, annotation);
					}else if(aclazz == ItemLongClick.class){
						ItemLongClick(activity, method, annotation);
					}else if(aclazz == ItemSelected.class){
						ItemSelected(activity, method, annotation);
					}else if(aclazz == AfterStart.class){
						AfterViews(activity, method, annotation);
					}
					
				}
			}
		}
		
		
	}
	private static Object RId(Activity activity,Field field,Annotation annotation){
		RId annotationObj = field.getAnnotation(RId.class);
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
	private static Object RLayout(Activity activity,Field field,Annotation annotation){
		RLayout annotationObj = field.getAnnotation(RLayout.class);
		int layoutId = annotationObj.value();
		try {
			if(layoutId==0)	return null;
			LayoutInflater inflater = LayoutInflater.from(activity);
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
	private static Object RDrawable(Activity activity,Field field,Annotation annotation){
		RDrawable annotationObj = field.getAnnotation(RDrawable.class);
		int drawableId = annotationObj.value();
		try {
			if(drawableId==0)	return null;
			Drawable drawable = activity.getResources().getDrawable(drawableId);
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
	private static Object RString(Activity activity,Field field,Annotation annotation){
		RString annotationObj = field.getAnnotation(RString.class);
		int StringId = annotationObj.value();
		try {
			if(StringId==0)	return null;
			String str = activity.getResources().getString(StringId);
			field.set(activity,str);
			return str;
		} catch (Exception e) {
			springContext.error(e);
		}
		return null;
	}
	private static Object SystemService(Activity activity,Field field,Annotation annotation){
		SystemService annotationObj = field.getAnnotation(SystemService.class);
		String anvalue = annotationObj.value();
		try {
			if(StringUtils.isEmpty(anvalue)){
				String name = field.getType().getSimpleName();
				anvalue = StringUtils.uncapitalize(StringUtils.substringBefore(name, "Manager"));
			}
			Object obj = activity.getSystemService(anvalue);
			field.set(activity,obj);
			return obj;
		} catch (Exception e) {
			springContext.error(e);
		}
		return null;
	}
	private static void Click(final Activity activity,final Method method,Annotation annotation){
		Click annotationObj = method.getAnnotation(Click.class);
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
						throw new Error(e);
					}
				}
			});
		}
	}
	
	private static void LongClick(final Activity activity,final Method method,Annotation annotation){
		LongClick annotationObj = method.getAnnotation(LongClick.class);
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
						throw new Error(e);
					}
					return false;
				}
			});
		}
	}
	
	private static void ItemClick(final Activity activity,final Method method,Annotation annotation){
		ItemClick annotationObj = method.getAnnotation(ItemClick.class);
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
						throw new Error(e);
					}
				}

			});
		}
	}
	
	private static void ItemLongClick(final Activity activity,final Method method,Annotation annotation){
		ItemLongClick annotationObj = method.getAnnotation(ItemLongClick.class);
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
						throw new Error(e);
					}
					return false;
				}

			});
		}
	}
	
	private static void ItemSelected(final Activity activity,final Method method,Annotation annotation){
		ItemSelected annotationObj = method.getAnnotation(ItemSelected.class);
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
						throw new Error(e);
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
				}

			});
		}
	}
	
	private static void AfterViews(final Activity activity,final Method method,Annotation annotation){
		AfterStart annotationObj = method.getAnnotation(AfterStart.class);
		int delayMillis = (int) annotationObj.value();
		activity.getWindow().getDecorView().postDelayed(new Runnable() {
			@Override
			public void run() {
				try {
					method.invoke(activity);
				} catch (Exception e) {
					throw new Error(e);
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
