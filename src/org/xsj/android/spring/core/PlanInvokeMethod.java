package org.xsj.android.spring.core;

import java.lang.reflect.Method;

import org.xsj.android.spring.common.Logx;

public class PlanInvokeMethod {
	private Object object;
	private Method method;
	public PlanInvokeMethod(Object object, Method method) {
		this.object = object;
		this.method = method;
	}
	public void exec(){
		try {
			method.invoke(object);
		} catch (Exception e) {
			Logx.e(e.getMessage());
			throw new RuntimeException(e);
		}
	}
}
