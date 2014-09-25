package org.xsj.android.spring.system;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.xsj.android.spring.common.IOUtils;
import org.xsj.android.spring.common.Logx;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;


public class AndroidUtils {
//	private final String DIALOG_ALERT = "DIALOG_ALERT";
//	private final String DIALOG_CONFIRM = "DIALOG_CONFIRM";
//	private final String DIALOG_PROGRESS = "DIALOG_PROGRESS";
	private Map<String,Object> cacheMap;
	protected Context context;
	private SharedPreferences  sharedPreferences ;
	
	public AndroidUtils(Context context){
		cacheMap = Collections.synchronizedMap(new HashMap<String, Object>());
		this.context = context;
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		
	}
    private void _addCache(String key,Object value){
    	cacheMap.put(key, value);
    }
    private Object _getCache(String key){
    	return cacheMap.get(key);
    }
    private void _removeCache(String key){
    	cacheMap.remove(key);
    }
    
	public void testQueryUri(String uriString){
		ContentResolver resolver = context.getContentResolver();
		Uri uri = Uri.parse(uriString);
		Cursor cursor =  resolver.query(uri, null, null, null, null);
		String keys[] = cursor.getColumnNames();
		System.out.println(cursor.getColumnNames());
		while(cursor.moveToNext()){
			for(String key : keys){
				String value = "";
				try {
					value = cursor.getString(cursor.getColumnIndex(key));
				} catch (Exception e) {
					try {
						value = cursor.getInt(cursor.getColumnIndex(key))+"";
					} catch (Exception e2) {
						// TODO: handle exception
					}
				}
				System.out.print(key + ":" + value+",");
			}
			System.out.println("");
		}
		IOUtils.close(cursor);
	}
	
	public static void testShowCur(Cursor cur){
		while (cur.moveToNext()){
			String w="";
			for(String name : cur.getColumnNames()){
				try {
					w += (name+":"+cur.getString(cur.getColumnIndex(name))+",");
				} catch (Exception e) {
					w += (name+":"+cur.getLong(cur.getColumnIndex(name))+",");
				}
			}
			Logx.d(w);
		}
	}

    
	public String getRString(int... ids){
		Resources resources = context.getResources();
		StringBuffer sb = new StringBuffer();
		for(int id : ids){
			sb.append(resources.getText(id));
		}
		return sb.toString();
	}
	public AlertDialog.Builder getBuilder(Object title,Object message){
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		String titleStr=null;
		String messageStr=null;
		if(title!=null){
			if(title.getClass() == Integer.class){
				titleStr = getRString((Integer)title);
			}else{
				titleStr= title.toString();
			}
		}
		builder.setTitle(titleStr);
		if(message!=null){
			if(message.getClass() == Integer.class){
				messageStr = getRString((Integer)message);
			}else{
				messageStr = message.toString();
			}
		}
		builder.setMessage(messageStr);
		return builder;
	}
	public AlertDialog getAlert(Object title,Object message,Object okText,OnClickListener okClicklistener){
		AlertDialog.Builder builder = getBuilder(title, message);
		AlertDialog alertDialog;
		String btnStr=null;
		if(okText!=null){
			if(okText.getClass() == Integer.class){
				btnStr = getRString((Integer)okText);
			}else{
				btnStr=okText.toString();
			}
		}else{
			btnStr = "OK";
		}
		builder.setPositiveButton(btnStr, okClicklistener);
		alertDialog = builder.create();
		alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		return alertDialog;
	}
	public AlertDialog getConfirm(Object title,Object message,Object okText,OnClickListener okClicklistener,Object cancelText,OnClickListener cancelClicklistener){
		AlertDialog.Builder builder = getBuilder(title, message);
		AlertDialog alertDialog;
		String okStr=null;
		if(okText!=null){
			if(okText.getClass() == Integer.class){
				okStr = getRString((Integer)okText);
			}else{
				okStr=okText.toString();
			}
		}else{
			okStr = "OK";
		}
		builder.setPositiveButton(okStr, okClicklistener);
		String cancelStr=null;
		if(cancelText!=null){
			if(cancelText.getClass() == Integer.class){
				cancelStr = getRString((Integer)cancelText);
			}else{
				cancelStr=cancelText.toString();
			}
		}else{
			cancelStr = "CANCEL";
		}
		builder.setNegativeButton(cancelStr, cancelClicklistener);
		alertDialog = builder.create();
		alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		return alertDialog;
	}
	/**
	 * 您必须在androidManifest中添加 
	 * <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>
	 * @param title
	 * @param message
	 * @param okText
	 * @param onClickListener
	 */
	public AlertDialog alert(Object title,Object message,Object okText,OnClickListener okClicklistener){
		AlertDialog alertDialog = getAlert(title, message, okText,okClicklistener);
		alertDialog.show();
		return alertDialog;
	}
	
	public AlertDialog confirm(Object title,Object message,Object okText,OnClickListener okClicklistener,Object cancelText,OnClickListener cancelClicklistener){
		AlertDialog alertDialog = getConfirm(title, message, okText,okClicklistener,cancelText,cancelClicklistener);
		alertDialog.show();
		return alertDialog;
	}
	public ProgressDialog getWait(Object title,Object message,OnCancelListener onCancelListener){
		ProgressDialog pd = new ProgressDialog(context);
		pd.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		String titleStr=null;
		String messageStr=null;
		if(title!=null){
			if(title.getClass() == Integer.class){
				titleStr = getRString((Integer)title);
			}else{
				titleStr= title.toString();
			}
		}
		if(message!=null){
			if(message.getClass() == Integer.class){
				messageStr = getRString((Integer)message);
			}else{
				messageStr = message.toString();
			}
		}
		pd.setTitle(titleStr);
		pd.setMessage(messageStr);
		pd.setIndeterminate(true);
		pd.setCancelable(true);
		pd.setOnCancelListener(onCancelListener);
		return pd;
	}
	public ProgressDialog wait(Object title,Object message,OnCancelListener onCancelListener){
		ProgressDialog pd = getWait(title, message, onCancelListener);
		pd.show();
		return pd;
	}
	
	public Toast toast(boolean showlong,Object... msgs){
		StringBuffer sb = new StringBuffer();
		for(Object msg : msgs){
			if(msg!=null){
				String msgStr=null;
				if(msg.getClass() == Integer.class){
					msgStr = getRString((Integer)msg);
				}else{
					if(msg!=null)msgStr= msg.toString();
				}
				sb.append(msgStr);
			}
		}
		int show = showlong? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;
		Toast t = Toast.makeText(context, sb.toString(), show);
		t.show();
		return t;
	}
	
	public Toast toast(Object... msgs){
		return toast(false,msgs);
	}
	
	public SharedPreferences getSharedPreferences(){
		return sharedPreferences;
	}
	public boolean getsharedPreferences(String key,boolean defvalue){
		return sharedPreferences.getBoolean(key, defvalue);
	}
	public float getsharedPreferences(String key,float defvalue){
		return sharedPreferences.getFloat(key, defvalue);
	}
	public int getsharedPreferences(String key,int defvalue){
		return sharedPreferences.getInt(key, defvalue);
	}
	public long getsharedPreferences(String key,long defvalue){
		return sharedPreferences.getLong(key, defvalue);
	}
	public String getsharedPreferences(String key,String defvalue){
		return sharedPreferences.getString(key, defvalue);
	}
	public void setsharedPreferences(String key,boolean value){
		sharedPreferences.edit().putBoolean(key, value).commit();
	}
	public void setsharedPreferences(String key,float value){
		sharedPreferences.edit().putFloat(key, value).commit();
	}
	public void setsharedPreferences(String key,int value){
		sharedPreferences.edit().putInt(key, value).commit();
	}
	public void setsharedPreferences(String key,long value){
		sharedPreferences.edit().putLong(key, value).commit();
	}
	public void setsharedPreferences(String key,String value){
		sharedPreferences.edit().putString(key, value).commit();
	}
	public void removeSharedPreferences(String key){
		sharedPreferences.edit().remove(key).commit();
	}
	
	public void setListViewHeightBasedOnChildren(ListView listView) {   
        //获取ListView对应的Adapter   
        ListAdapter listAdapter = listView.getAdapter();    
        if (listAdapter == null) {   
        // pre-condition   
        return;   
        }   
          
        int totalHeight = 10;
        for (int i = 0, len = listAdapter.getCount(); i < len; i++) { //listAdapter.getCount()返回数据项的数目   
	        View listItem = listAdapter.getView(i, null, listView);   
	        listItem.measure(0, 0); //计算子项View 的宽高   
	        totalHeight += listItem.getMeasuredHeight(); //统计所有子项的总高度   
        }   
        ViewGroup.LayoutParams params = listView.getLayoutParams();   
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));   
        //listView.getDividerHeight()获取子项间分隔符占用的高度   
        //params.height最后得到整个ListView完整显示需要的高度 
        listView.setLayoutParams(params);   
   }
	public void setDialogAutoClose(AlertDialog dialog,boolean bool){
		try {
			Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
			field.setAccessible(true);
			field.set(dialog, bool);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

};
