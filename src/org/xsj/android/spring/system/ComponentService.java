package org.xsj.android.spring.system;

import org.xsj.android.spring.core.BeanInjecter;
import org.xsj.android.spring.core.SpringUtils;
import org.xsj.android.spring.core.annotation.Configuration;
import android.app.Service;

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
		SystemInjecter.inject(this,this);
		BeanInjecter.inject(SpringUtils.getSpringContext(),this);
	}

}
