package org.xsj.android.spring.core;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xsj.android.spring.common.IOUtils;
import org.xsj.android.spring.common.StringUtils;
import org.xsj.android.spring.core.Bean.ScopeType;
import org.xsj.android.spring.core.annotation.Autowired;
import org.xsj.android.spring.core.annotation.Component;
import org.xsj.android.spring.core.annotation.ComponentScan;
import org.xsj.android.spring.core.annotation.Configuration;
import org.xsj.android.spring.core.annotation.DefaultConfigure;
import org.xsj.android.spring.core.annotation.Import;
import org.xsj.android.spring.core.annotation.PostConstruct;
import org.xsj.android.spring.core.annotation.PreDestroy;
import org.xsj.android.spring.core.annotation.PropertySource;
import org.xsj.android.spring.core.annotation.Qualifier;
import org.xsj.android.spring.core.annotation.Scope;
import org.xsj.android.spring.core.annotation.Value;
import org.xsj.android.spring.core.db.TransactionInvocationHandler;
import org.xsj.android.spring.core.db.TransactionManager;
import org.xsj.android.spring.core.db.annotation.Transactional;

import dalvik.system.DexFile;
import dalvik.system.PathClassLoader;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;

public class ConfigurationLoader {
	private SpringContext springContext;
	private Pattern patternValue = Pattern.compile("\\$\\{(.+)\\}");
	private Map<Class<?>, PendingBean> pendingBeanClazzMap ;
	private Map<String, PendingBean> pendingBeanNameMap ;
	private List<PendingBean> pendingBeanUnInitList;
	private Set<PendingBean> pendingBeanInitedSet;
	private Set<PendingBean> waitPendingBeanSet ;
	private Set<PendingBean> faultPendingBeanSet ;
	private PendingConfiguration rootPendingConfiguration;	
	private Set<Class<?>> uniqueConfigurationSet;	
	private List<Properties> propertiesList;	
	private List<PlanInvokeMethod> postConstructList;
	private List<PlanInvokeMethod> preDestroyList;
	
	public ConfigurationLoader(SpringContext springContext) {
		this.springContext = springContext;
		pendingBeanClazzMap = new HashMap<Class<?>, PendingBean>();
		pendingBeanNameMap = new HashMap<String, PendingBean>();
		pendingBeanUnInitList = new ArrayList<PendingBean>();
		pendingBeanInitedSet = new HashSet<PendingBean>();
		faultPendingBeanSet = new HashSet<PendingBean>();
		waitPendingBeanSet = new HashSet<PendingBean>();
		uniqueConfigurationSet = new HashSet<Class<?>>();
		propertiesList = new ArrayList<Properties>();
		postConstructList = new ArrayList<PlanInvokeMethod>();
		preDestroyList = new ArrayList<PlanInvokeMethod>();
	}
	
	public void  addPostConstruct(PlanInvokeMethod planInvokeMethod){
		postConstructList.add(planInvokeMethod);
	}
	public void  addPreDestroy(PlanInvokeMethod planInvokeMethod){
		preDestroyList.add(planInvokeMethod);
	}
	
	public void load(Context context,Class<?> configClass){
		springContext.debug("springCongfig loading...");
		_initConfigClass(configClass);
		if(!springContext.isLazyLoad()){
			_initPackages(context,springContext.getBasePackage());
			invokeConfiguration(rootPendingConfiguration);
		    for(PendingBean pb : pendingBeanUnInitList){  
				initPendingBean(pb);
		    }
		    springContext.debug("springCongfig loading success");
		    ClassInfo.clear();
		    for(PlanInvokeMethod im : postConstructList){
		    	im.exec();
		    }
		}else{
		    springContext.debug("springCongfig lazy loading");
		}
	};

	
	public void unload(){
	    for(PlanInvokeMethod im : preDestroyList){
	    	im.exec();
	    }
	    springContext.debug("springCongfig unload");
	};
	
	private void invokeConfiguration(PendingConfiguration pendingConfiguration){
		for(PendingConfiguration children : pendingConfiguration.getChildrenList()){
			invokeConfiguration(children);
		}
		Object configObject = pendingConfiguration.getConfigObject();
		BeanInjecter.inject(springContext, configObject);
	}
	
	public void regBean(String name,Class<?> clazz,Object object){
		Bean bean = new Bean();
		bean.name = name;
		bean.clazz = clazz;
		bean.object = object;
		springContext.getBeanClazzMap().put(clazz, bean);
		springContext.getBeanNameMap().put(name, bean);
		springContext.debug("register bean ["+name+"] "+clazz);
	}

	protected Object obtainObject(String name){
		Object obj = null;
		Bean bean = springContext.getBeanNameMap().get(name);
		if(bean==null){
			PendingBean pb = pendingBeanNameMap.get(name);
			if(pb!=null){
				if(pb.getScope()==ScopeType.prototype){
					obj = _createObject(pb);
				}else if(pb.getScope()==ScopeType.singleton){
					if(springContext.getStatus() == SpringContext.STATUS_LOADING){
						obj = _createObject(pb);
					}
				}
			}
		}else{
			obj = bean.object;
		}
		return obj;
	}
	protected Object obtainObject(Class<?> clazz){
		Object obj = null;
		Bean bean = springContext.getBeanClazzMap().get(clazz);
		if(bean==null){
			if(springContext.isLazyLoad()){
				PendingBean pb = new PendingBean(clazz);
				obj = _createObject(pb);
			}else{
				PendingBean pb = pendingBeanClazzMap.get(clazz);
				if(pb!=null){
					if(pb.getScope()==ScopeType.prototype){
						obj = _createObject(pb);
					}else if(pb.getScope()==ScopeType.singleton){
						if(springContext.getStatus() == SpringContext.STATUS_LOADING){
							obj = _createObject(pb);
						}
					}
				}
			}
		}else{
			obj = bean.object;
		}
		return obj;
	}
	protected void initPendingBean(PendingBean pendingBean){
		if(!pendingBeanInitedSet.contains(pendingBean)){
			 _createObject(pendingBean);
		}
	}

	protected Object _createObject(PendingBean pb){
		if(pb==null)return null;
		if(pb.getInjectType() == PendingBean.InjectTypeComponent){
			return _createObjectByComponent(pb);
		}else{
			return _createObjectByBean(pb);
		}
	}
	protected Object _createObjectByBean(PendingBean pb){
		Method method = pb.getInjectMethod();
		Class<?> clazz = method.getReturnType();
		if(faultPendingBeanSet.contains(pb) || Context.class.isAssignableFrom(clazz) || clazz.isPrimitive()){
			return null;
		};
		Object obj = null;
		org.xsj.android.spring.core.annotation.Bean beanAnn = method.getAnnotation(org.xsj.android.spring.core.annotation.Bean.class);
		if(beanAnn!=null){
			obj = _createPureObjByBean(pb);
			if(obj!=null){
				String[] names = beanAnn.name();
				String name = null;
				if(names.length>0){
					name = names[1];
				}else{
					name = StringUtils.uncapitalize(method.getName());
				}
				if(pb.getScope()==ScopeType.singleton){
					regBean(name,clazz,obj);
				}
				pendingBeanInitedSet.add(pb);
				
				String postConstruct = beanAnn.initMethod();
				if(!StringUtils.isEmpty(postConstruct)){
					Method postConstructMethod = ClassInfo.find(clazz).getMethod(postConstruct);
					if(postConstructMethod!=null){
						if(postConstructMethod.getReturnType() != Void.TYPE){
							springContext.error("PostConstruct must return void:"+postConstructMethod);
						}else if(postConstructMethod.getParameterTypes().length > 0){
							springContext.error("PostConstruct must no param:"+postConstructMethod);
						}
						addPostConstruct(new PlanInvokeMethod(obj, postConstructMethod));
					}
				}
				String preDestroy = beanAnn.destroyMethod();
				if(!StringUtils.isEmpty(preDestroy)){
					Method preDestroyMethod = ClassInfo.find(clazz).getMethod(preDestroy);
					if(preDestroyMethod!=null){
						if(preDestroyMethod.getReturnType() != Void.TYPE){
							springContext.error("preDestroy must return void:"+preDestroyMethod);
						}else if(method.getParameterTypes().length > 0){
							springContext.error("preDestroy must no param:"+preDestroyMethod);
						}
						addPreDestroy(new PlanInvokeMethod(obj, preDestroyMethod));
					}
				}
				
			}else{
				faultPendingBeanSet.add(pb);
			}
		}
		return obj;
	}

	protected Object _createObjectByComponent(PendingBean pb){
		Class<?> clazz = pb.getInjectClazz();
		if(faultPendingBeanSet.contains(pb) || Context.class.isAssignableFrom(clazz) || clazz.isPrimitive()){
			return null;
		};
		Object proxyObj = null;
		Object obj = null;
		Component component = clazz.getAnnotation(Component.class);
		if(component!=null){
			obj = _createPureObjByComponent(pb);
			if(obj!=null){
				proxyObj = _handleTransaction(obj, clazz);
				String name = component.value();
				name = StringUtils.defaultIfEmpty(name,StringUtils.uncapitalize(clazz.getSimpleName()));
				if(pb.getScope()==ScopeType.singleton){
					if(proxyObj==null){
						regBean(name,clazz,obj);
					}else{
						regBean(name,clazz,proxyObj);
					}
				}
				pendingBeanInitedSet.add(pb);
				BeanInjecter.inject(springContext,obj);
			}else{
				faultPendingBeanSet.add(pb);
			}
		}
		if(proxyObj!=null){
			return proxyObj;
		}else{
			return obj;
		}
	}

	private Object _handleTransaction(Object obj, Class<?> clazz) {
		boolean inTrans=false;
		if(clazz.getAnnotation(Transactional.class)!=null){
			inTrans = true;
		}else{
			Collection<Method> mlist = ClassInfo.find(clazz).getMethodList();
			for(Method m : mlist){
				if(m.getAnnotation(Transactional.class)!=null){
					inTrans = true;
					break;
				}
			}
		}
		Object proxyObj = null;
		if(inTrans){
			Class<?>[] clazzInterfaces = clazz.getInterfaces();
			if(clazzInterfaces.length==0){
				throw new RuntimeException("transactional class must implement a interface");
			}
			TransactionManager transactionManager = (TransactionManager) obtainObject(TransactionManager.class);
			if(transactionManager==null){
				throw new RuntimeException("not found bean TransactionManager");
			}
			proxyObj =  Proxy.newProxyInstance(clazz.getClassLoader(), clazz.getInterfaces(), new TransactionInvocationHandler(transactionManager, obj));
		}
		return proxyObj;
	}
	
	protected void assignParam(Class<?> clazz, Annotation[][] psAnns, Class<?>[] psClazz, Object[] params) {
		for(int i=0;i<psAnns.length;i++){
			Annotation[] pAnns = psAnns[i];
			Class<?> pClazz = psClazz[i];
			Object pobj = null;
			String pname = null;
			String pvalueKey=null;
			int pinjectType = 0;//0 autoware 1 qualifier  2 value
			for(Annotation pAnn : pAnns){
				Class<?> annClazz = pAnn.annotationType();
				if(annClazz==Qualifier.class){
					pname = ((Qualifier)pAnn).value();
					pname = StringUtils.defaultIfEmpty(pname,StringUtils.uncapitalize(psClazz[i].getSimpleName()));
					pinjectType = 1;
					break;
				}else if(annClazz==Value.class){
					pvalueKey = ((Value)pAnn).value().trim();
					pinjectType = 2;
					break;
				}
			}
			if(pinjectType==0){
				pobj = obtainObject(pClazz);
			}else if(pinjectType==1){
				pobj = obtainObject(pname);
			}else if(pinjectType==2){
				pobj = getAnnValueObject(psClazz[i],pvalueKey);
			}
			if(pobj==null){
				throw new RuntimeException("can not inject ["+pname+"] "+pClazz);
			}
			params[i]=pobj;
		}
	}

	public Object getAnnValueObject(Class<?> pClazz, String pvalueKey) {
		String pvalueStr = null;
		Matcher matcher = patternValue.matcher(pvalueKey);
		if(matcher.find()){
			pvalueKey = matcher.group(1);
			for(Properties pro : propertiesList){
				String v = pro.getProperty(pvalueKey);
				if(v!=null){
					pvalueStr = v;
					break;
				}
			}
		}else{
			pvalueStr = pvalueKey;
		}
		Object pobj=null;
		if(pvalueStr != null){
		try {
			pobj = StringUtils.fromString(pvalueStr, pClazz);
		} catch (Exception e) {
			springContext.error(e);
		}
		}
		return pobj;
	}
	
	private Object _createPureObjByBean(PendingBean pb){
		Method method = pb.getInjectMethod();
		Class<?> clazz = method.getReturnType();
		if(waitPendingBeanSet.contains(pb)){
			throw new RuntimeException("loop inject :"+clazz+" ");
		}else{
			waitPendingBeanSet.add(pb);
		}
		Object obj = null;
		try {
			Annotation[][] psAnns = method.getParameterAnnotations();
			Class<?>[] psClazz = method.getParameterTypes();
			Object params[]=new Object[psAnns.length];
			assignParam(clazz, psAnns, psClazz, params);
			obj = method.invoke(pb.getConfigureObject(), params);
		} catch (Exception e) {
//			springContext.error("fault create: "+clazz);
			Exception ex = new Exception("fault create: "+clazz);
			ex.setStackTrace(e.getStackTrace());
			springContext.error(ex);
		}
		waitPendingBeanSet.remove(pb);
		return obj;
	}

	private Object _createPureObjByComponent(PendingBean pb){
		Class<?> clazz = pb.getInjectClazz();
		if(waitPendingBeanSet.contains(pb)){
			throw new RuntimeException("loop inject :"+clazz+" ");
		}else{
			waitPendingBeanSet.add(pb);
		}
		Object obj = null;
		try {
			Constructor<?>[] cons = (Constructor<?>[]) clazz.getConstructors();
			for(Constructor<?> con : cons){
				if(con.getAnnotation(Autowired.class)!=null){
					Annotation[][] psAnns = con.getParameterAnnotations();
					Class<?>[] psClazz = con.getParameterTypes();
					Object params[]=new Object[psAnns.length];
					assignParam(clazz, psAnns, psClazz, params);
					obj = con.newInstance(params);
					break;
				}
			}
			if(obj==null){
				Constructor<?> con = (Constructor<?>)clazz.getConstructor();
				obj = con.newInstance();
			}
			
		} catch (Exception e) {
//			springContext.error("fault create: "+pb.getInjectClazz());
			Exception ex = new Exception("fault create: "+pb.getInjectClazz());
			ex.setStackTrace(e.getStackTrace());
			springContext.error(e);
		}
		waitPendingBeanSet.remove(pb);
		return obj;
	}
	
    private void _initPackages(Context context, String[] classPackages){
		DexFile dex = null;
		try {
			dex = new DexFile(((ContextWrapper) context).getPackageResourcePath());
		} catch (IOException e1) {
			springContext.error(e1);
		}
		PathClassLoader classLoader = (PathClassLoader) Thread.currentThread().getContextClassLoader();
		try {
			if(classPackages!=null && classPackages.length!=0){
				for(String classPackage : classPackages){
					if(!StringUtils.isEmpty(classPackage)){
						_initPackage(dex,classLoader,classPackage.replaceAll("\\*$", ""));
					}
				}
			}else{
				String classPackage = context.getPackageName();
				_initPackage(dex,classLoader,classPackage);
			}

		} catch (ClassNotFoundException e) {
			springContext.error(e);
		}
	}
	private void _initPackage(DexFile dex,ClassLoader classLoader,String classPackage) throws ClassNotFoundException {
		Enumeration<String> n=dex.entries();  
	    while(n.hasMoreElements()){  
	        String classname = n.nextElement();
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
						PendingBean pb = new PendingBean(_clazz);
						pendingBeanUnInitList.add(pb);
						pendingBeanClazzMap.put(_clazz,pb);
						String name = ann.value();
						name = StringUtils.defaultIfEmpty(name,StringUtils.uncapitalize(_clazz.getSimpleName()));
						if(pendingBeanNameMap.containsKey(name)){
							springContext.error("bean name defined repeat :"+_clazz+" -"+name);
						}
						pendingBeanNameMap.put(name,pb);
					}
				}
	        }
	    }  
	}
	
	private void _initConfigClass(Class<?> configClass){
		if(configClass==null || !configClass.isAnnotationPresent(Configuration.class)){
			springContext.error("the class is not a Configuration class:"+configClass);
		}
		if(uniqueConfigurationSet.contains(configClass)){
			return;
		}else{
			uniqueConfigurationSet.add(configClass);
		}
		PendingConfiguration pendingConfiguration = null;
		try {
			pendingConfiguration = new PendingConfiguration(configClass);
		} catch (Exception e) {
			springContext.error(e);
		}
		Configuration configuration = configClass.getAnnotation(Configuration.class);
		if(rootPendingConfiguration==null){
			rootPendingConfiguration = pendingConfiguration;
			springContext.setDebug(configuration.debug());
			springContext.setLazyLoad(configuration.lazyLoad());
			ComponentScan componentScan = configClass.getAnnotation(ComponentScan.class);
			if(componentScan!=null){
				springContext.setBasePackage(componentScan.value());
			}else{
				springContext.setBasePackage(new String[]{});
			}
		}
		PropertySource propertySource = configClass.getAnnotation(PropertySource.class);
		if(propertySource!=null){
			for(String propath : propertySource.value()){
				try {
					Properties properties = new Properties();
					if(propath.startsWith("assets:")){
						propath = propath.replace("assets:", "");
						InputStream ism = springContext.getContext().getAssets().open(propath);
						properties.load(ism);
						propertiesList.add(properties);
						IOUtils.close(ism);
					}else{
						FileInputStream fism = new FileInputStream(propath);
						properties.load(fism);
						propertiesList.add(properties);
						IOUtils.close(fism);
					}
				} catch (Exception e) {
					springContext.error(e);
				}
			}
		}
		
		Import importAnn = configClass.getAnnotation(Import.class);
		if(importAnn!=null){
			for(Class<?> childrenConfigClass : importAnn.value()){
				_initConfigClass(childrenConfigClass);
			}
		}
		ClassInfo cfgClassInfo = ClassInfo.find(configClass);
		for(Method m : cfgClassInfo.getMethodList()){
			if(m.isAnnotationPresent(org.xsj.android.spring.core.annotation.Bean.class)){
				Class<?> returnClass = m.getReturnType();
				if(returnClass.isPrimitive()){
					springContext.error("configuration bean cannot be primitive");
				}
				org.xsj.android.spring.core.annotation.Bean bean = m.getAnnotation(org.xsj.android.spring.core.annotation.Bean.class);
				if(bean!=null){
					String[] beanNames = bean.name();
					List<String> beanNameList = new ArrayList<String>();
					beanNameList.addAll(Arrays.asList(beanNames));
					if(beanNameList.size()==0){
						String beanName = StringUtils.uncapitalize(m.getName());
						beanNameList.add(beanName);
					}
					PendingBean pb = new PendingBean(m,pendingConfiguration.getConfigObject());
					pendingBeanUnInitList.add(pb);
					pendingBeanClazzMap.put(returnClass, pb);
					for(String beanName : beanNameList){
						if(pendingBeanNameMap.containsKey(beanName)){
							springContext.error("bean name defined repeat :"+returnClass+" -"+beanName);
						}
						pendingBeanNameMap.put(beanName, pb);
					}
				}
			}
		};
	}
	
	
	
	
	
	
}
