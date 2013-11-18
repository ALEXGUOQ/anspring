package org.xsj.android.spring.core;

import java.util.ArrayList;
import java.util.List;

public class PendingConfiguration {
	private Class<?> configClass;
	private Object configObject;
	private List<PendingConfiguration> childrenList;
	
	public PendingConfiguration(Class<?> configClass) throws IllegalAccessException, InstantiationException {
		this.configClass = configClass;
		this.configObject = configClass.newInstance();
		this.childrenList = new ArrayList<PendingConfiguration>();
	}

	public Class<?> getConfigClass() {
		return configClass;
	}


	public void setConfigClass(Class<?> configClass) {
		this.configClass = configClass;
	}


	public Object getConfigObject() {
		return configObject;
	}


	public void setConfigObject(Object configObject) {
		this.configObject = configObject;
	}


	public List<PendingConfiguration> getChildrenList() {
		return childrenList;
	}


	public void setChildrenList(List<PendingConfiguration> childrenList) {
		this.childrenList = childrenList;
	}


	public void addChildren(PendingConfiguration pendingConfiguration){
		childrenList.add(pendingConfiguration);
	}
	
}
