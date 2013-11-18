package org.xsj.android.spring.core;

import org.xsj.android.spring.core.db.DataSource;
import org.xsj.android.spring.core.db.SqliteTemplate;
import org.xsj.android.spring.core.db.TransactionManager;


public interface ConfigurationTemplate {
	
	public abstract DataSource dataSource();
	
	public abstract SqliteTemplate sqliteTemplate(DataSource dataSource);
	
	public abstract TransactionManager transactionManager(DataSource dataSource);
	
	
}
