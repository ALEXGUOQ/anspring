<html>
<body>
<b>anspr1ng简介</b>

android平台仿spring标记

目前实现了ioc基本功能
列子代码
<pre>
User
<code>
@Component
public class User {
	int i = 0;
	
	@Autowired
	Name name;
	
	@Autowired
	@Qualifier("wm")
	WindowManager winman;
	
	public String say(){
		return name.text;
	}
}
</code>
</pre>
<pre>
MainActivity
<code>
public class MainActivity extends ComponentActivity {
	@RId(R.id.button2)
	Button btn2;
	@RString(R.string.hello_world)
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
	@AfterStart(1000)	//view全部加载完成之后延时1000毫秒	
	private void afterViewTest(){
		//直接注入的user
		Toast.makeText(MainActivity.this, user.say(), Toast.LENGTH_SHORT).show();
	}
	@Click(R.id.button1)
	public void onClick(View v) {
		//getBean获取user
		User u = (User) SpringContext.getInstance().getBean("user");
		Toast.makeText(MainActivity.this, "结果："+u.winman.toString(), Toast.LENGTH_SHORT).show();
	}
	@LongClick({R.id.button1,R.id.button2})
	public boolean onLongClick() {
		Toast.makeText(MainActivity.this, "按下长时间", Toast.LENGTH_SHORT).show();
		return false;
	}
}
</code>
</pre>
</body>
</html>
