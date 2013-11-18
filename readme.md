<html>
<body>
<b>anspring简介</b>

android平台仿spring 轻量级框架
内容包含：

1，外围辅助工具集
1.1，一个轻量级 Logx框架，去掉了讨厌的tag，直接Logx.d(...),tag自动生成（类名_方法名_行数），
并支持单个日志文件/日期划分日志文件/体积划分日志文件
1.2，ActivityInject/SystemInject 对R.id 和按钮事件 等的注入，下个版本拟支持自定义解析器
1.3，AndroidUtils 简化alert()等android相关常用操作

2，核心spring 
2.1，ioc的基本功能，但只提供了annotation定义。
2.2，由于android对cglib的不支持（也许认识有误），目前实现不完善的transaction，aop没有实现。


列子代码
<pre>
MainActivity
<code>
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
		Logx.d("加载成功");
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
</code>
</pre>

<pre>
MyConfigure
<code>
@Configuration
@PropertySource("assets:test.ini")
public class MyConfigure implements ConfigurationTemplate {
	@Override
	@Bean
	public DataSource dataSource() {
		DataSource dataSource = new DataSource(){
			@Override
			protected SQLiteDatabase injectSQLiteDatabase() {
				SQLiteOpenHelper helper = new SQLiteOpenHelper(SpringUtils.getSpringContext().getContext(), "spring.db", null, 1) {
					@Override
					public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
						// TODO Auto-generated method stub
					}
					@Override
					public void onCreate(SQLiteDatabase db) {
						// TODO Auto-generated method stub
					}
				};
				SQLiteDatabase db = helper.getWritableDatabase();
				return db;
			}
		};
		return dataSource;
	}
	@Override
	@Bean
	public SqliteTemplate sqliteTemplate(DataSource dataSource) {
		SqliteTemplate template = new SqliteTemplate(dataSource);
		return template;
	}
	@Override
	@Bean
	public TransactionManager transactionManager(DataSource dataSource) {
		TransactionManager tm = new TransactionManager(dataSource);
		return tm;
	}
	@Bean
	public AndroidUtils androidUtils(){
		return new AndroidUtils(SpringUtils.getSpringContext().getContext());
	}
}
</code>
</pre>

<pre>
XiaoMing
<code>
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
	@PostConstruct
	public void sayHello(){
		Logx.d("hello:",this.toString());
	}
	
	@PreDestroy
	public void saybye(){
		Logx.d("bye:",this.toString());
	}
}
</code>
</pre>


</body>
</html>
