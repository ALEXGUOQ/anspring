package org.xsj.android.spring.system;

import org.xsj.android.spring.core.SpringUtils;
import org.xsj.android.spring.core.annotation.Configuration;
import org.xsj.android.spring.system.activity.ActivityInjecter;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public abstract class ComponentService extends Service {

	@Override
	public void onCreate() {
		super.onCreate();
		__inject__();
	}
	
	private void __inject__() {
		if(this.getClass().isAnnotationPresent(Configuration.class)){
			SpringUtils.load(this,this.getClass());
		}
		if(SpringUtils.hasLoad()){
			SystemInjecter.inject(SpringUtils.getSpringContext(),this);
		}
	}

}
