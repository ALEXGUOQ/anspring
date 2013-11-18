package org.xsj.android.spring.core;

import java.lang.reflect.Method;

import org.xsj.android.spring.core.Bean.ScopeType;
import org.xsj.android.spring.core.annotation.Scope;

public class PendingBean {
	public static final int InjectTypeComponent = 0;
	public static final int InjectTypeBean = 1;
	private int injectType;
	public ScopeType scope;
	private Class<?> injectClazz;
	private Method injectMethod;
	private Object configureObject;
	
	public PendingBean(Class<?> injectClazz) {
		this.injectClazz = injectClazz;
		Scope annScope = injectClazz.getAnnotation(Scope.class);
		this.scope = getScopeType(annScope);
		injectType = InjectTypeComponent;
	}
	
	public PendingBean(Method injectMethod,Object configureObject) {
		this.injectMethod = injectMethod;
		Scope annScope = injectMethod.getAnnotation(Scope.class);
		this.scope = getScopeType(annScope);
		this.configureObject = configureObject;
		injectType = InjectTypeBean;
	}
	private ScopeType getScopeType(Scope annScope){
		if(annScope==null){
			return ScopeType.singleton;
		}else{
			return annScope.value();
		}
	}
	
	public int getInjectType() {
		return injectType;
	}
	public void setInjectType(int injectType) {
		this.injectType = injectType;
	}
	public Class<?> getInjectClazz() {
		return injectClazz;
	}
	public void setInjectClazz(Class<?> injectClazz) {
		this.injectClazz = injectClazz;
	}
	public Method getInjectMethod() {
		return injectMethod;
	}
	public void setInjectMethod(Method injectMethod) {
		this.injectMethod = injectMethod;
	}

	public Object getConfigureObject() {
		return configureObject;
	}

	public void setConfigureObject(Object configureObject) {
		this.configureObject = configureObject;
	}

	public ScopeType getScope() {
		return scope;
	}

	public void setScope(ScopeType scope) {
		this.scope = scope;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((configureObject == null) ? 0 : configureObject.hashCode());
		result = prime * result
				+ ((injectClazz == null) ? 0 : injectClazz.hashCode());
		result = prime * result
				+ ((injectMethod == null) ? 0 : injectMethod.hashCode());
		result = prime * result + injectType;
		result = prime * result + ((scope == null) ? 0 : scope.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PendingBean other = (PendingBean) obj;
		if (configureObject == null) {
			if (other.configureObject != null)
				return false;
		} else if (!configureObject.equals(other.configureObject))
			return false;
		if (injectClazz == null) {
			if (other.injectClazz != null)
				return false;
		} else if (!injectClazz.equals(other.injectClazz))
			return false;
		if (injectMethod == null) {
			if (other.injectMethod != null)
				return false;
		} else if (!injectMethod.equals(other.injectMethod))
			return false;
		if (injectType != other.injectType)
			return false;
		if (scope != other.scope)
			return false;
		return true;
	}
	
}
