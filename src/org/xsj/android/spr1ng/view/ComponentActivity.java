package org.xsj.android.spr1ng.view;

import org.xsj.android.spr1ng.core.BeanInjecter;
import org.xsj.android.spr1ng.core.SpringContext;
import org.xsj.android.spr1ng.core.annotation.SpringConfig;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;


public class ComponentActivity extends Activity {
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
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
		SpringContext.getInstance().load(this.getClass().getAnnotation(SpringConfig.class));
		ViewInjecter.inject(this);
		BeanInjecter.inject(this);
	}


}
