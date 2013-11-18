<html>
<body>
<b>anspr1ng简介</b>

android平台仿spring标记

目前实现了spring ioc的基本功能
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
