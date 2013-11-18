package org.xsj.android.spring.test;

import org.xsj.android.spring.core.ConfigurationTemplate;
import org.xsj.android.spring.core.SpringUtils;
import org.xsj.android.spring.core.annotation.Bean;
import org.xsj.android.spring.core.annotation.Configuration;
import org.xsj.android.spring.core.annotation.PropertySource;
import org.xsj.android.spring.core.db.DataSource;
import org.xsj.android.spring.core.db.SqliteTemplate;
import org.xsj.android.spring.core.db.TransactionManager;
import org.xsj.android.spring.system.AndroidUtils;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
