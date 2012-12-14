package org.xsj.android.spr1ng.core;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;

import org.xsj.android.spr1ng.core.Bean.ScopeType;
import org.xsj.android.spr1ng.core.annotation.Autowired;
import org.xsj.android.spr1ng.core.annotation.Component;
import org.xsj.android.spr1ng.core.annotation.Qualifier;
import org.xsj.android.spr1ng.core.annotation.Scope;
import org.xsj.android.spr1ng.core.annotation.SpringConfig;

//import android.content.Context;
import android.util.Log;




public class SpringContext {
	public final static String tag = "SpringContext";
	
	public boolean hasLoadConfig;
	@Deprecated
	public String[] basePackage;
	public boolean allowInjectFault;
	public boolean threadSafe;
	public boolean debug;
	
	private static SpringContext springContext;
	
	private Map<Class, Bean> beanClazzMap ;
	private Map<String, Bean> beanNameMap ;

	private Set<Class> waitClazzSet ;
	private Set<Class> faultClazzSet ;
	
	private SpringContext() {
		beanClazzMap = new WeakHashMap<Class, Bean>();
		beanNameMap = new WeakHashMap<String, Bean>();
		faultClazzSet = new HashSet<Class>();
		waitClazzSet = new HashSet<Class>();
		_init();
	}
	private void _init() {
		beanClazzMap.clear();
		beanNameMap.clear();
		faultClazzSet.clear();
		waitClazzSet.clear();
		hasLoadConfig=false;
		basePackage=null;
		allowInjectFault=true;
		threadSafe=true;
		debug=true;
	}
	public static SpringContext getInstance(){
		if(springContext==null){
			springContext = new SpringContext();
		}
		return springContext;
	}
	public<T> void registerBean(String name,T object){
		if(!StringUtils.isEmpty(name) && object!=null){
			if(threadSafe){
				synchronized (this) {
					__regBean__(name,object.getClass(),object);
				}
			}else{
				__regBean__(name,object.getClass(),object);
			}
		}
	}
	public<T> void unRegisterBean(String name){
		if(!StringUtils.isEmpty(name)){
			if(threadSafe){
				synchronized (this) {
					Bean bean = beanNameMap.get(name);
					if(bean!=null){
						beanClazzMap.remove(bean.clazz);
						beanNameMap.remove(name);
					}
				}
			}else{
				Bean bean = beanNameMap.get(name);
				if(bean!=null){
					beanClazzMap.remove(bean.clazz);
					beanNameMap.remove(name);
				}
			}
		}
	}
	public Object getBean(String name){
		if(threadSafe){
			synchronized (this) {
				Object obj = null;
				Bean bean = beanNameMap.get(name);
				if(bean!=null){
					if(bean.scope == ScopeType.singleton){
						obj = bean.object;
					}else{
						obj = _createObj(bean.clazz);
						if(obj!=null){
							BeanInjecter.inject(obj);
						}
					}
				}
				return obj;
			}
		}else{
			Object obj = null;
			Bean bean = beanNameMap.get(name);
			if(bean!=null){
				if(bean.scope == ScopeType.singleton){
					obj = bean.object;
				}else{
					obj = _createObj(bean.clazz);
					if(obj!=null){
						BeanInjecter.inject(obj);
					}
				}
			}
			return obj;
		}
	}
	public<T> T getBean(String name,Class<T> clazz){
		if(threadSafe){
			synchronized (this) {
				T obj = null;
				Bean bean = beanNameMap.get(name);
				if(bean!=null){
					if(bean.scope == ScopeType.singleton){
						obj =  (T) bean.object;
					}else{
						obj = _createObj(bean.clazz);
						if(obj!=null){
							BeanInjecter.inject(obj);
						}
					}
				}
				return obj;
			}
		}else{
			T obj = null;
			Bean bean = beanNameMap.get(name);
			if(bean!=null){
				if(bean.scope == ScopeType.singleton){
					obj =  (T) bean.object;
				}else{
					obj = _createObj(bean.clazz);
					if(obj!=null){
						BeanInjecter.inject(obj);
					}
				}
			}
			return obj;
		}
	}
	protected<T> void __regBean__(String name,Class<?> clazz,T object){
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
	protected<T> T obtainBean(Class<T> clazz){
		if(threadSafe){
			synchronized (this) {
				return __obtainBean__(clazz);
			}
		}else{
			return __obtainBean__(clazz);
		}
		
	}
	protected<T> T __obtainBean__(Class<T> clazz){
		T obj = null;
		do{
			Bean bean = beanClazzMap.get(clazz);
			if(bean==null){
				if(faultClazzSet.contains(clazz))break;
//				if(Context.class.isAssignableFrom(clazz))break;
				if(clazz.isPrimitive())break;
				Component component = clazz.getAnnotation(Component.class);
				if(component!=null){
					obj = _createObj(clazz);
					if(obj!=null){
						String name = component.value();
						name = StringUtils.defaultIfEmpty(name,StringUtils.uncapitalize(clazz.getSimpleName()));
						__regBean__(name,clazz,obj);
						BeanInjecter.inject(obj);
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
								throw new Exception("loop inject :"+clazz+" & "+pClazz);
							}else{
								waitClazzSet.add(pClazz);
							}
							pobj = __obtainBean__(pClazz);
							waitClazzSet.remove(pClazz);
							if(pobj==null){
								throw new Exception("can not inject ["+qname+"] "+pClazz);
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
    	if(!allowInjectFault){
    		throw new Error(e);
    	}
    }
    public void error(Exception e) {
    	debug(e);
    	if(!allowInjectFault){
    		throw new Error(e);
    	}
    }
	public void _initPackages(String[] classPackages)  throws ClassNotFoundException {
		try {
			for(String classPackage : classPackages){
				if(!StringUtils.isEmpty(classPackage)){
					_initPackage(classPackage);
				}
			}
		} catch (ClassNotFoundException e) {
		}
	}
	private void _initPackage(String classPackage) throws ClassNotFoundException {
		String classPath = "/"+classPackage.replace(".", "/");
		URL classUrl = BeanInjecter.class.getResource(classPath);
		File root = new File(classUrl.getPath());
		for(File cfile : root.listFiles()){
			try {
				_initClazz(cfile,classPackage,"");
			} catch (ClassNotFoundException e) {
			}
		}
	}
	private void _initClazz(File file,String packageName,String relativelyPackageName) throws ClassNotFoundException {
		if(file.isDirectory()){
			for(File cfile : file.listFiles()){
				_initClazz(cfile,packageName,relativelyPackageName+(("".equals(relativelyPackageName)? "" : "."))+file.getName());
			}
		}else{
			String fileName = file.getName();
			if(fileName.toLowerCase().endsWith(".class") && fileName.indexOf('$')== -1){
				String clazzName = StringUtils.substringBefore(fileName,".class");
				String clazzFullName = packageName+(("".equals(relativelyPackageName)? "" : "."))+relativelyPackageName+"."+clazzName;
				Class<?> _clazz = null;
				try {
					_clazz = Class.forName(clazzFullName);
					if(_clazz.getAnnotation(Component.class)!=null){
						obtainBean(_clazz);
					}
				} catch (ClassNotFoundException e) {
					error(e);
				}
			}
		}
	}
	public void load(SpringConfig springConfig){
		if(springConfig!=null && !springContext.hasLoadConfig){
			debug("springCongfig loading...");
			springContext.allowInjectFault = springConfig.allowInjectFault();
			springContext.basePackage = springConfig.basePackage();
			springContext.threadSafe = springConfig.threadSafe();
			springContext.debug = springConfig.debug();
			if(springContext.basePackage!=null){
				try {
					_initPackages(springContext.basePackage);
				} catch (Exception e) {
					debug(e);
				}
			}
			springContext.hasLoadConfig=true;
			debug("springCongfig loading success");
		}
	}
	public void reload(SpringConfig springConfig){
		_init();
		load(springConfig);
	}
	public void unload(){
		_init();
		debug("springCongfig unload");
	}

}
