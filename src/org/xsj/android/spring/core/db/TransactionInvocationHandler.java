package org.xsj.android.spring.core.db;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.xsj.android.spring.common.BeanUtils;
import org.xsj.android.spring.common.Logx;
import org.xsj.android.spring.core.SpringContext;
import org.xsj.android.spring.core.SpringUtils;
import org.xsj.android.spring.core.db.annotation.Transactional;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TransactionInvocationHandler implements InvocationHandler {
	
	private TransactionManager transactionManager;
	private Class<?> clazz;
	private Transactional clazzTransactional;
	private Object obj;
	
	public TransactionInvocationHandler(TransactionManager transactionManager, Object obj) {
		this.transactionManager = transactionManager;
		this.obj = obj;
		this.clazz = obj.getClass();
		this.clazzTransactional = clazz.getAnnotation(Transactional.class);
	}
	
	public boolean isTransFn(Transactional transactional){
		Transactional curTransaction = clazzTransactional;
		if(transactional!=null){
			curTransaction = transactional;
		}
		if(curTransaction!=null && !curTransaction.readOnly()){
			return true;
		}
		return false;
	}
	
	public boolean ignoreException(Transactional transactional,Class<?> eclazz){
		Transactional curTransaction = clazzTransactional;
		if(transactional!=null){
			curTransaction = transactional;
		}
		if(curTransaction!=null){
			if(!BeanUtils.containSubClass(curTransaction.rollbackFor(), eclazz) 
				|| BeanUtils.containSubClass(curTransaction.noRollbackFor(), eclazz) )
			{
				return false;
			}
		}
		return true;
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		Object res=null;
		Integer numObj = transactionManager.getThreadLocalTranNum().get();
		int num = 0;
		if(numObj!=null){
			num = numObj.intValue();
		}
		SQLiteDatabase db = transactionManager.getDb();
		Transactional transactional=null;
		method.getParameterTypes();
		Method _method =  obj.getClass().getMethod(method.getName(), method.getParameterTypes());
		if(_method!=null){
			transactional = _method.getAnnotation(Transactional.class);
		}
		boolean _isTransFn = isTransFn(transactional);
		if(num==0 && _isTransFn){
			db.beginTransaction();
			transactionManager.getThreadLocalTranNum().set(++num);
		}
		try {
			res = method.invoke(obj, args);
			if(num==1 && _isTransFn){
				db.setTransactionSuccessful();
			}
		} catch (Exception e) {
			Class<?> eclazz =  e.getClass();
			if(num==1 && _isTransFn){
				if(ignoreException(transactional,eclazz)){
					db.setTransactionSuccessful();
				}
			}
			throw e;
		}finally{
			if(num==1 && _isTransFn){
				transactionManager.getThreadLocalTranNum().set(--num);
				db.endTransaction();
			}
		}
		return res;
	}

}
