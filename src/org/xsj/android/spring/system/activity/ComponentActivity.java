package org.xsj.android.spring.system.activity;

import org.xsj.android.spring.core.BeanInjecter;
import org.xsj.android.spring.core.SpringUtils;
import org.xsj.android.spring.core.annotation.Configuration;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;


public class ComponentActivity extends Activity {

	private void __inject__() {
		ActivityInjecter.inject(this);
		BeanInjecter.inject(SpringUtils.getSpringContext(),this);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		onBeforeCreate(savedInstanceState);
		super.onCreate(savedInstanceState);
		onBeforeInject(savedInstanceState);
		__inject__();
		onAfterInject(savedInstanceState);
	}
	
	protected void onBeforeCreate(Bundle savedInstanceState) {
	}
	
	protected void onBeforeInject(Bundle savedInstanceState) {
	}
	
	protected void onAfterInject(Bundle savedInstanceState) {
	}
	

}
