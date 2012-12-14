package org.xsj.android.spr1ng.test;

import org.xsj.android.spr1ng.R;
import org.xsj.android.spr1ng.core.SpringContext;
import org.xsj.android.spr1ng.core.annotation.Autowired;
import org.xsj.android.spr1ng.core.annotation.SpringConfig;
import org.xsj.android.spr1ng.view.ComponentActivity;
import org.xsj.android.spr1ng.view.annotation.Click;
import org.xsj.android.spr1ng.view.annotation.RId;
import org.xsj.android.spr1ng.view.annotation.RString;
import org.xsj.android.spr1ng.view.annotation.RegisterBean;
import org.xsj.android.spr1ng.view.annotation.SystemService;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

@SpringConfig(allowInjectFault=true)
public class MainActivity extends ComponentActivity {
//	@RId(R.id.button1)
	Button btn2;
//	@RString(R.string.hello_world)
	String hello;
	
	@Autowired
	User user;
	
	@SystemService
	@RegisterBean("wm")
	WindowManager windowMananger;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}
//	@Click(R.id.button1)
	public void onClick(View v) {
		User u = (User) SpringContext.getInstance().getBean("user");
		Toast.makeText(MainActivity.this, "结果："+u.winman.toString(), Toast.LENGTH_SHORT).show();
	}
	public boolean onLongClick() {
		Toast.makeText(MainActivity.this, "按下长时间", Toast.LENGTH_SHORT).show();
		return false;
	}
//	@AfterStart
	private void afterViewTest(){
		Toast.makeText(MainActivity.this, user.say(), Toast.LENGTH_SHORT).show();
	}
}
