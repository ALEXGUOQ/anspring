package org.xsj.android.spr1ng.test;

import org.xsj.android.spr1ng.core.annotation.Autowired;
import org.xsj.android.spr1ng.core.annotation.Component;
import org.xsj.android.spr1ng.core.annotation.Qualifier;

import android.view.WindowManager;

@Component
public class User {
	int i = 0;
	Name name;
	@Autowired
	MainActivity actvity;
	@Autowired
	@Qualifier("wm")
	WindowManager winman;
	public User() {
	}
	public String say(){
		return actvity.hello;
	}
	@Autowired
	public void setName(Name name) {
		this.name = name;
	}
}
