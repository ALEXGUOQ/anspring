package org.xsj.android.spring.test;

import org.xsj.android.spring.common.Logx;
import org.xsj.android.spring.core.annotation.Autowired;
import org.xsj.android.spring.core.annotation.Component;
import org.xsj.android.spring.core.annotation.PostConstruct;
import org.xsj.android.spring.core.annotation.PreDestroy;
import org.xsj.android.spring.core.annotation.Value;

@Component
public class XiaoMing {
	String name;
	@Value("shadiao")
	String wahao;
	@Value("${rc.xiaoming.age}")
	int age;
	
	@Autowired
	public XiaoMing(@Value("${rc.xiaoming.name}")String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "XiaoMing [name=" + name + ", wahao=" + wahao + ", age=" + age
				+ "]";
	}
	
	public String getName(){
		return name;
	}
	
	@PostConstruct
	public void sayHello(){
		Logx.d("hello:",this.toString());
	}
	
	@PreDestroy
	public void saybye(){
		Logx.d("bye:",this.toString());
	}
	
	
}
