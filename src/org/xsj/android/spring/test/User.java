package org.xsj.android.spring.test;

import org.xsj.android.spring.core.annotation.Autowired;
import org.xsj.android.spring.core.annotation.Component;
import org.xsj.android.spring.core.annotation.Qualifier;
import org.xsj.android.spring.db.CompontPo;

import android.view.WindowManager;

@Component
public class User extends CompontPo{
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
