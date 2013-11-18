package org.xsj.android.spring.test;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xsj.android.spring.R;
import org.xsj.android.spring.common.Logx;
import org.xsj.android.spring.core.BeanInjecter;
import org.xsj.android.spring.core.ConfigurationTemplate;
import org.xsj.android.spring.core.SpringContext;
import org.xsj.android.spring.core.SpringUtils;
import org.xsj.android.spring.core.annotation.Autowired;
import org.xsj.android.spring.core.annotation.Bean;
import org.xsj.android.spring.core.annotation.Configuration;
import org.xsj.android.spring.core.annotation.Import;
import org.xsj.android.spring.core.annotation.PropertySource;
import org.xsj.android.spring.core.annotation.Qualifier;
import org.xsj.android.spring.core.annotation.Value;
import org.xsj.android.spring.core.db.DataSource;
import org.xsj.android.spring.core.db.SqliteTemplate;
import org.xsj.android.spring.core.db.TransactionManager;
import org.xsj.android.spring.system.AndroidUtils;
import org.xsj.android.spring.system.activity.ActivityInjecter;
import org.xsj.android.spring.system.activity.ComponentActivity;
import org.xsj.android.spring.system.activity.annotaion.AfterStart;
import org.xsj.android.spring.system.activity.annotaion.OnClick;
import org.xsj.android.spring.system.activity.annotaion.OnLongClick;
import org.xsj.android.spring.system.activity.annotaion.R_Id;
import org.xsj.android.spring.system.annotation.R_String;
import org.xsj.android.spring.system.annotation.SystemService;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity{
	@R_Id(R.id.button1)
	Button btn2;

	@Autowired
	@Qualifier("manServiceImp1")
	ManService manService;
	
	@Autowired
	AndroidUtils androidUtils;
	
	@Autowired
	XiaoMing xm;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		SpringUtils.load(this,MyConfigure.class);
		BeanInjecter.inject(SpringUtils.getSpringContext(),this);
		ActivityInjecter.inject(this);
	}
	@Override
	protected void onDestroy() {
		SpringUtils.unload();
		super.onDestroy();
	}
	@OnClick({R.id.button1,R.id.button2})
	public void onClick(View v) {
		androidUtils.alert("消息", xm.getName(), "确定", null);
	}
	@OnLongClick(R.id.button2)
	public boolean onLongClick() {
		androidUtils.toast("长按");
		return false;
	}
	@AfterStart(1000)
	private void afterViewTest(){
		androidUtils.toast("延时一秒");
	}
}
