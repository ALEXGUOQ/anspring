package org.xsj.android.spring.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.xsj.android.spring.common.Logx;
import org.xsj.android.spring.common.StringUtils;
import android.content.Context;
/**
 * 
 * @author jjliu
 * 不能重复load
 *	uload 时没敢删除内容，防止多线程还在使用的地方报错
 */
public class SpringContext {
	public final static String tag = "SpringContext";
	public final static String CONTEXT = "CONTEXT";
	public final static int STATUS_UNLOAD = 0;
	public final static int STATUS_LOADING = 1;
	public final static int STATUS_LOADED= 2;
	
	private Context context;
	private ConfigurationLoader configurationLoader;
	private int status;
	private String[] basePackage;
	private boolean debug;
	private boolean lazyLoad;
	
	/**
	 * 类和bean映射
	 */
	private Map<Class<?>, Bean> beanClazzMap ;
	/**
	 * bean名和bean的隐射
	 */
	private Map<String, Bean> beanNameMap ;
	
	
	public SpringContext() {
		configurationLoader=new ConfigurationLoader(this);
		beanClazzMap = new ConcurrentHashMap<Class<?>, Bean>();
		beanNameMap = new ConcurrentHashMap<String, Bean>();
		status=STATUS_UNLOAD;
		basePackage=null;
		debug=true;
	}
	public<T> void registerBean(String name,T object){
		if(!StringUtils.isEmpty(name) && object!=null){
			Bean bean = new Bean();
			bean.name = name;
			bean.clazz = object.getClass();
			bean.object = object;
			beanNameMap.put(name, bean);
			debug("register bean ["+name+"] "+bean.clazz);
		}
	}
	public<T> void unRegisterBean(String name){
		if(!StringUtils.isEmpty(name)){
			Bean bean = beanNameMap.get(name);
			if(bean!=null){
				beanNameMap.remove(name);
				debug("unregister bean ["+name+"] "+bean.clazz);
			}
		}
	}
	

	public<T> T getBean(String name){
		T obj = null;
		Bean bean = beanNameMap.get(name);
		if(bean!=null){
			obj =  (T) bean.object;
		}else{
			obj = (T) configurationLoader.obtainObject(name);
		}
		return obj;
	}
	public<T> T getBean(Class<T> clazz){
		T obj = null;
		Bean bean = beanClazzMap.get(clazz);
		if(bean!=null){
			obj =  (T) bean.object;
		}else{
			obj = (T) configurationLoader.obtainObject(clazz);
		}
		return obj;
	}
	public<T> T getProxyBean(Class<?> clazz){
		T obj = null;
		Bean bean = beanClazzMap.get(clazz);
		if(bean!=null){
			obj =  (T) bean.object;
		}else{
			obj = (T) configurationLoader.obtainObject(clazz);
		}
		return obj;
	}
	
	public Context getContext(){
		return context;
	}
    public void debug(String e) {
    	if(debug){
    		Logx.dt(tag,e);
    	}
    }
    public void debug(Exception e) {
    	if(debug){
    		Logx.dt(tag,e.getMessage());
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
	
	public Map<Class<?>, Bean> getBeanClazzMap() {
		return beanClazzMap;
	}
	public Map<String, Bean> getBeanNameMap() {
		return beanNameMap;
	}
	public String[] getBasePackage() {
		return basePackage;
	}
	public boolean hasLoad(){
		return status==STATUS_LOADED ? true:false ;
	}
	
	public int getStatus() {
		return status;
	}
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	public void setLazyLoad(boolean lazyLoad) {
		this.lazyLoad = lazyLoad;
	}
	public boolean isLazyLoad() {
		return lazyLoad;
	}
	public void setBasePackage(String[] basePackage) {
		this.basePackage = basePackage;
	}
	public ConfigurationLoader getConfigurationLoader() {
		return configurationLoader;
	}
	public void load(Context context,Class<?> configClass){
		if(this.status == STATUS_UNLOAD){
			this.status = STATUS_LOADING;
			this.context =  context.getApplicationContext();
			registerBean(CONTEXT, context);
			configurationLoader.load(context,configClass);
			this.status=STATUS_LOADED;
		}
	}

	public void unload(){
		configurationLoader.unload();
	}

}
