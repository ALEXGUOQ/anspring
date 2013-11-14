package org.xsj.android.spring.test;

import org.xsj.android.spring.R;
import org.xsj.android.spring.core.SpringContext;
import org.xsj.android.spring.core.SpringUtils;
import org.xsj.android.spring.core.annotation.Autowired;
import org.xsj.android.spring.core.annotation.Configuration;
import org.xsj.android.spring.db.CompontPo;
import org.xsj.android.spring.db.Database;
import org.xsj.android.spring.system.activity.ComponentActivity;
import org.xsj.android.spring.system.activity.annotaion.OnClick;
import org.xsj.android.spring.system.activity.annotaion.R_Id;
import org.xsj.android.spring.system.annotation.R_String;
import org.xsj.android.spring.system.annotation.RegisterBean;
import org.xsj.android.spring.system.annotation.SystemService;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

@Configuration()
public class MainActivity extends ComponentActivity {
//	@R_Id(R.id.button1)
	Button btn2;
//	@R_String(R.string.hello_world)
	String hello;
	
	@Autowired
	User user;
	
	@SystemService
	@RegisterBean("wm")
	WindowManager windowMananger;
	@Autowired
	Database db;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}
//	@Click(R.id.button1)
	public void onClick(View v) {
		User u = (User) SpringUtils.getBean("user");
		Toast.makeText(MainActivity.this, "结果："+u.winman.toString(), Toast.LENGTH_SHORT).show();
	}
	public boolean onLongClick() {
		Toast.makeText(MainActivity.this, "按下长时间", Toast.LENGTH_SHORT).show();
		return false;
	}
//	@AfterStart
	private void afterViewTest(){
		User user = new User();
		user.save(db);
		
		Toast.makeText(MainActivity.this, user.say(), Toast.LENGTH_SHORT).show();
	}
}
