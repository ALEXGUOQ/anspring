package org.xsj.android.spring.core;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;

import org.xsj.android.spring.core.Bean.ScopeType;
import org.xsj.android.spring.core.annotation.Autowired;
import org.xsj.android.spring.core.annotation.Component;
import org.xsj.android.spring.core.annotation.ComponentScan;
import org.xsj.android.spring.core.annotation.Qualifier;
import org.xsj.android.spring.core.annotation.Scope;
import org.xsj.android.spring.core.annotation.Configuration;
import org.xsj.android.spring.system.SystemInjecter;
import org.xsj.android.spring.system.activity.ActivityInjecter;

import dalvik.system.DexFile;
import dalvik.system.PathClassLoader;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.util.Log;



/*
 * 大致过程
  	load读取
		_initPackage
			对每个class执行 _obtainBean(class)
				如果beanClassMap里没有，使用_createObj(class)
					如果有构造函数需要bean，使用_obtainBean(qname,class)获得参数
				把得到的bean，_regBean注册，使用标注的name
				使用inject对这个bean注入属性
					属性bean使用了_obtainBean(qname,class)
					进入了递归。。。
 */
/**
 * 
 * @author jjliu
 *
 */
public class SpringContext {
	public final static String tag = "SpringContext";
	public final static String CONTEXT = "CONTEXT";
	private Context context;
	
	public boolean hasLoadConfig;
	public String[] basePackage;
	public boolean debug;
	/**
	 * 被component标注的类和名字
	 */
	private Map<String, Class<?>> specifyNameClazzMap ;
	/**
	 * 被component标注的类
	 */
	private List<Class<?>> specifyNameList;
	/**
	 * 类和bean映射
	 */
	private Map<Class<?>, Bean> beanClazzMap ;
	/**
	 * bean名和bean的隐射
	 */
	private Map<String, Bean> beanNameMap ;
	/**
	 * 循环载入bean时，用到
	 */
	private Set<Class<?>> waitClazzSet ;
	/**
	 * 不能导入的bean
	 */
	private Set<Class<?>> faultClazzSet ;
	
	
	public SpringContext() {
		specifyNameClazzMap = new HashMap<String, Class<?>>();
		specifyNameList = new ArrayList<Class<?>>();
		beanClazzMap = new ConcurrentHashMap<Class<?>, Bean>();
		beanNameMap = new ConcurrentHashMap<String, Bean>();
		faultClazzSet = new HashSet<Class<?>>();
		waitClazzSet = new HashSet<Class<?>>();
		_init();
	}
	private void _init() {
		specifyNameClazzMap.clear();
		specifyNameList.clear();
		beanClazzMap.clear();
		beanNameMap.clear();
		faultClazzSet.clear();
		waitClazzSet.clear();
		hasLoadConfig=false;
		basePackage=null;
		debug=true;
	}
	public<T> void registerBean(String name,T object){
		if(!StringUtils.isEmpty(name) && object!=null){
			_regBean(name,object.getClass(),object);
		}
	}
	public<T> void unRegisterBean(String name){
		if(!StringUtils.isEmpty(name)){
			Bean bean = beanNameMap.get(name);
			if(bean!=null){
				beanClazzMap.remove(bean.clazz);
				beanNameMap.remove(name);
			}
		}
	}

	public<T> T getBean(String name){
		T obj = null;
		Bean bean = beanNameMap.get(name);
		if(bean!=null){
			if(bean.scope == ScopeType.singleton){
				obj =  (T) bean.object;
			}else{
				obj = _createObj(bean.clazz);
				if(obj!=null){
					BeanInjecter.inject(this,obj);
				}
			}
		}
		return obj;
	}
	public<T> T getBean(Class<T> clazz){
		T obj = null;
		Bean bean = beanClazzMap.get(clazz);
		if(bean!=null){
			if(bean.scope == ScopeType.singleton){
				obj =  (T) bean.object;
			}else{
				obj = _createObj(bean.clazz);
				if(obj!=null){
					BeanInjecter.inject(this,obj);
				}
			}
		}
		return obj;
	}
	protected<T> void _regBean(String name,Class<?> clazz,T object){
		Scope annScop = clazz.getAnnotation(Scope.class);
		Bean bean = new Bean();
		bean.name = name;
		bean.clazz = clazz;
		bean.object = object;
		if(annScop == null || annScop.value()==ScopeType.singleton){
			bean.scope = ScopeType.singleton;
		}else{
			bean.scope = ScopeType.prototype;
		}
		beanClazzMap.put(clazz, bean);
		beanNameMap.put(name, bean);
		debug("register bean ["+name+"] "+clazz);
	}
	/**
	 * 先用preNameClazzMap修正下，真正需要获得class，再去obtain，因为有时写的是父类名
	 * @param preName
	 * @param clazz
	 * @return
	 */
	protected<T> T _obtainBean(String preName,Class<T> clazz){
		Class<?> preClazz = specifyNameClazzMap.get(preName);
		if(preClazz==null) preClazz = clazz;
		return (T) _obtainBean(preClazz);
		
	}
	/**
	 * 根据class获得bean，如果没有就_createObj并且用Component定义的名称去注册
	 * @param clazz
	 * @return
	 */
	protected<T> T _obtainBean(Class<T> clazz){
		T obj = null;
		do{
			Bean bean = beanClazzMap.get(clazz);
			if(bean==null){
				if(faultClazzSet.contains(clazz))break;
				if(Context.class.isAssignableFrom(clazz))break;
				if(clazz.isPrimitive())break;
				Component component = clazz.getAnnotation(Component.class);
				if(component!=null){
					obj = _createObj(clazz);
					if(obj!=null){
						String name = component.value();
						name = StringUtils.defaultIfEmpty(name,StringUtils.uncapitalize(clazz.getSimpleName()));
						_regBean(name,clazz,obj);
						BeanInjecter.inject(this,obj);
					}else{
						faultClazzSet.add(clazz);
					}
				}
			}else{
				if(bean.scope==ScopeType.singleton){
					obj = (T) bean.object;
				}else{
					obj = _createObj(bean.clazz);
				}
			}
		}while(false);
		return obj;
	}
	/**
	 * 创建一个object，如果有构造参数，按照构造函数的提示的bean名去obtain一个bean参数
	 * @param clazz
	 * @return
	 */
	private <T> T _createObj(Class<?> clazz){
		T obj = null;
		try {
			Constructor<T>[] cons = (Constructor<T>[]) clazz.getConstructors();
			for(Constructor<T> con : cons){
				if(con.getAnnotation(Autowired.class)!=null){
					Annotation[][] psAnns = con.getParameterAnnotations();
					Class<?>[] psClazz = con.getParameterTypes();
					Object params[]=new Object[psAnns.length];
					for(int i=0;i<psAnns.length;i++){
						Annotation[] pAnns = psAnns[i];
						Object pobj = null;
						String qname = null;
						for(Annotation pAnn : pAnns){
							if(pAnn.annotationType()==Qualifier.class){
								qname = ((Qualifier)pAnn).value();
								qname = StringUtils.defaultIfEmpty(qname,StringUtils.uncapitalize(psClazz[i].getSimpleName()));
								pobj = getBean(qname);
								break;
							}
						}
						if(pobj==null){
							Class<?> pClazz = psClazz[i];
							if(waitClazzSet.contains(pClazz)){
								throw new RuntimeException("loop inject :"+clazz+" & "+pClazz);
							}else{
								waitClazzSet.add(pClazz);
							}
							pobj = _obtainBean(qname,pClazz);
							waitClazzSet.remove(pClazz);
							if(pobj==null){
								throw new RuntimeException("can not inject ["+qname+"] "+pClazz);
							}
						}
						params[i]=pobj;
					}
					obj = con.newInstance(params);
					break;
				}
			}
			if(obj==null){
				Constructor<T> con = (Constructor<T>)clazz.getConstructor();
				obj = con.newInstance();
			}
		} catch (Exception e) {
			error(e);
		}
		return obj;
	}
	
	public Context getContext(){
		return context;
	}
    public void debug(String e) {
    	if(debug){
    		Log.d(tag,e);
    	}
    }
    public void debug(Exception e) {
    	if(debug){
    		e.printStackTrace();
    	}
    }
    public void error(String e) {
    	debug(e);
    	throw new RuntimeException(e);
    }
    public void error(Exception e) {
    	debug(e);
    	throw new RuntimeException(e);
    }
    private void _initPackages(Context context, String[] classPackages){
		DexFile dex = null;
		try {
			dex = new DexFile(((ContextWrapper) context).getPackageResourcePath());
		} catch (IOException e1) {
			error(e1);
		}
		PathClassLoader classLoader = (PathClassLoader) Thread.currentThread().getContextClassLoader();
		try {
			if(classPackages.length!=0){
				for(String classPackage : classPackages){
					if(!StringUtils.isEmpty(classPackage)){
						_initPackage(dex,classLoader,classPackage);
					}
				}
			}else{
				String classPackage = context.getPackageName();
				_initPackage(dex,classLoader,classPackage);
			}
		} catch (ClassNotFoundException e) {
			error(e);
		}
	}
	private void _initPackage(DexFile dex,ClassLoader classLoader,String classPackage) throws ClassNotFoundException {
		Enumeration<String> n=dex.entries();  
	    while(n.hasMoreElements()){  
	        String classname = n.nextElement();
//	        debug(classname);
	        if(!classname.contains("$") && classname.startsWith(classPackage) ){
		        Class<?> _clazz = null;
				try {
					_clazz = Class.forName(classname);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if(_clazz!=null){
					Component ann = _clazz.getAnnotation(Component.class);
					if(ann!=null){
						specifyNameList.add(_clazz);
						String name = ann.value();
						if(!StringUtils.isEmpty(name)){
							specifyNameClazzMap.put(name, _clazz);
						}
					}
				}
	        }
	    }  
	    for(Class<?> clazz : specifyNameList){  
			_obtainBean(clazz);
	    }  
	}
	
	private void _initConfigClass(Class<?> configClass){
		ClassInfo cfgClassInfo = ClassInfo.find(configClass);
		Object cfgObj=null;
		for(Method m : cfgClassInfo.getMethodList()){
			if(m.isAnnotationPresent(org.xsj.android.spring.core.annotation.Bean.class)){
				if(cfgObj==null){
					try {
						cfgObj = configClass.newInstance();
					} catch (Exception e) {
						error(e);
					}
				}
				Class<?> returnClass = m.getReturnType();
				if(returnClass.isPrimitive()){
					error("configuration bean cannot be primitive");
				}
				org.xsj.android.spring.core.annotation.Bean bean = m.getAnnotation(org.xsj.android.spring.core.annotation.Bean.class);
				String[] beanNames = bean.name();
				List<String> beanNameList = new ArrayList<String>();
				beanNameList.addAll(Arrays.asList(beanNames));
				if(beanNameList.size()==0){
					String beanName = StringUtils.uncapitalize(returnClass.getSimpleName());
					beanNameList.add(beanName);
				}
				try {
					Object returnBean = m.invoke(cfgObj);
					for(String beanName : beanNameList){
						_regBean(beanName,returnClass, returnBean);
					}
				} catch (Exception e) {
					error(e);
				}
			}
		};
	}
	
	public boolean hasLoad(){
		return this.hasLoadConfig;
	}
	public void load(Context context,Class<?> configClass){
		if(!this.hasLoadConfig){
			if(configClass==null || !configClass.isAnnotationPresent(Configuration.class)){
				error("the class is not a Configuration class");
			}
			Configuration configuration = configClass.getAnnotation(Configuration.class);
			this.context =  context.getApplicationContext();
			registerBean(CONTEXT, context);
			debug("springCongfig loading...");
			this.debug = configuration.debug();
			ComponentScan componentScan = configClass.getAnnotation(ComponentScan.class);
			if(componentScan!=null){
				this.basePackage = componentScan.value();
			}
			this.basePackage = new String[]{};
			_initConfigClass(configClass);
			_initPackages(context,this.basePackage);
			this.hasLoadConfig=true;
			debug("springCongfig loading success");
		}
	}
	public void reload(Context context,Class<?> configClass){
		_init();
		load(context,configClass);
	}
	public void unload(){
		_init();
		debug("springCongfig unload");
	}

}
