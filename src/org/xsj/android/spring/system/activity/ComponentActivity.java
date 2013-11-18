package org.xsj.android.spring.system.activity;

import org.xsj.android.spring.core.BeanInjecter;
import org.xsj.android.spring.core.SpringUtils;
import org.xsj.android.spring.core.annotation.Configuration;
import android.app.Activity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;


public class ComponentActivity extends Activity {

	@Override
	public void setContentView(int layoutResID) {
		super.setContentView(layoutResID);
		__inject__();
	}

	@Override
	public void setContentView(View view, LayoutParams params) {
		super.setContentView(view, params);
		__inject__();
	}

	@Override
	public void setContentView(View view) {
		super.setContentView(view);
		__inject__();
	}

	private void __inject__() {
		if(this.getClass().isAnnotationPresent(Configuration.class)){
			SpringUtils.load(this,this.getClass());
		}
		ActivityInjecter.inject(this);
		BeanInjecter.inject(SpringUtils.getSpringContext(),this);
	}


}
