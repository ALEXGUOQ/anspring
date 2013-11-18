package org.xsj.android.spring.common;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


import android.util.Log;

public class Logx {
	public static final int V=0;
	public static final int D=1;
	public static final int I=2;
	public static final int W=3;
	public static final int E=4;
	
	public static final int FileNull=0;
	public static final int FileSingle=1;
	public static final int FileDaily=2;
	public static final int FileSize=3;
	
	private static Logx logx = new Logx();
	private SimpleDateFormat yyyyMMddHHmmss = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyy-MM-dd");
	/**
	 * 设置所有日志级别
	 * @param allLevel
	 */
	public static void setLevel(int allLevel){
		logx._setLevel(allLevel);
	}
	/**
	 * 分别设置logcat和文件的日志级别
	 * @param fileLevel
	 * @param consoleLevel
	 */
    public static void setLevels(int fileLevel,int consoleLevel){
		logx._setLevels(fileLevel,consoleLevel);
    };

    /**
     * 设置日志文件为单文件
     * @param path 文件路径
     */
    public static void setFileSingle(String path){
		logx._setFileSingle(path);
    };

    /**
     * 设置日志文件为按日期，自动增加
     * @param dir 日志文件目录
     */
    public static void setFileDaily(String dir){
		logx._setFileDaily(dir);
    }

    /**
     * 设置日志文件为按数据限量，自动增加
     * @param dir 日志文件目录
     * @param size 日志文件大小限制 单位byte
     */
    public static void setFileSize(String dir,int size){
		logx._setFileSize(dir,size);
    }
    /**
     * 设置没有日志文件
     * 默认为此
     */
	public static void setFileNull(){
		logx._setFileNull();
	}
    
    
	public static void v(Object... objects){
		logx._v(null,objects);
	}
	
	public static void d(Object... objects){
		logx._d(null,objects);
	}
	
	public static void i(Object... objects){
		logx._i(null,objects);
	}
	
	public static void w(Object... objects){
		logx._w(null,objects);
	}
	
	public static void e(Object... objects){
		logx._e(null,objects);
	}
	
	public static void vt(String tag, Object... objects){
		logx._v(tag,objects);
	}
	
	public static void dt(String tag, Object... objects){
		logx._d(tag,objects);
	}
	
	public static void it(String tag, Object... objects){
		logx._i(tag,objects);
	}
	
	public static void wt(String tag, Object... objects){
		logx._w(tag,objects);
	}
	
	public static void et(String tag, Object... objects){
		logx._e(tag,objects);
	}
	
	
	
    private int consoleLevel;
    private int fileLevel;
    private int fileType;
    private String fileDir;
    private String filePath;
    private Date fileTimePre;
    private long fileSizeCur;
    private long fileSizeMax;
    private FileOutputStream fosm;

	public void _v(String tag,Object... objects){
		if(consoleLevel <= V){
			if(tag==null){
				tag = getTag(3);
			}
			Log.v(tag, getMsg(objects));
		}
		if(fosm!=null && fileLevel <= V){
			if(tag==null){
				tag = getTag(3);
			}
			writeFileLog("[V]",tag,objects);
		}
	}
	public void _d(String tag,Object... objects){
		if(consoleLevel <= D){
			if(tag==null){
				tag = getTag(3);
			}
			Log.d(tag, getMsg(objects));
		}
		if(fosm!=null && fileLevel <= D){
			if(tag==null){
				tag = getTag(3);
			}
			writeFileLog("[D]",tag,objects);
		}
	}
	public void _i(String tag,Object... objects){
		if(consoleLevel <= I){
			if(tag==null){
				tag = getTag(3);
			}
			Log.i(tag, getMsg(objects));
		}
		if(fosm!=null && fileLevel <= I){
			if(tag==null){
				tag = getTag(3);
			}
			writeFileLog("[I]",tag,objects);
		}
	}
	public void _w(String tag,Object... objects){
		if(consoleLevel <= W){
			if(tag==null){
				tag = getTag(3);
			}
			Log.w(tag, getMsg(objects));
		}
		if(fosm!=null && fileLevel <= W){
			if(tag==null){
				tag = getTag(3);
			}
			writeFileLog("[W]",tag,objects);
		}
	}
	public void _e(String tag,Object... objects){
		if(consoleLevel <= E){
			if(tag==null){
				tag = getTag(3);
			}
			Log.e(tag, getMsg(objects));
		}
		if(fosm!=null && fileLevel <= E){
			if(tag==null){
				tag = getTag(3);
			}
			writeFileLog("[E]",tag,objects);
		}
	}
	
	
	
    public void _setLevel(int allLevel) {
    	this.consoleLevel = allLevel;
    	this.fileLevel = allLevel;
    };
    public void _setLevels(int fileLevel,int consoleLevel){
    	this.consoleLevel = consoleLevel;
    	this.fileLevel = fileLevel;
    };
    public void _setFileSingle(String path){
    	this.fileType = FileSingle;
    	IOUtils.close(fosm);
    	filePath = path.replace('\\', '/');
    	new File(filePath).getParentFile().mkdirs();
    	try {
			fosm = new FileOutputStream(path,true);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    };
    public void _setFileDaily(String dir){
    	this.fileType = FileDaily;
    	IOUtils.close(fosm);
    	dir = dir.replace('\\', '/');
    	if(!dir.endsWith("/")){
    		dir += "/";
    	}
    	new File(dir).mkdirs();
    	fileDir = dir;
    	fileTimePre = getTodayDate(new Date());
    	_createFileDaily();
    };
    public void _setFileSize(String dir,int size){
    	this.fileType = FileSize;
    	IOUtils.close(fosm);
    	dir = dir.replace("\\", "/");
    	if(!dir.endsWith("/")){
    		dir += "/";
    	}
    	new File(dir).mkdirs();
    	fileDir = dir;
    	fileSizeMax = size;
    	_createFileSize();
    };
    public void _setFileNull(){
    	this.fileType = FileNull;
    	IOUtils.close(fosm);
    };
    
    
    private void _createFileDaily() {
    	IOUtils.close(fosm);
    	String path = fileDir + yyyyMMdd.format(new Date()) + ".log";
    	try {
			fosm = new FileOutputStream(path,true);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    }
    private void _createFileSize(){
    	IOUtils.close(fosm);
    	File dir = new File(fileDir);
    	File curfile = null;
    	for(File file: dir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File elem) {
					try {
						if(elem.isFile()){
							String ename = elem.getName();
							String[] enames = ename.split("\\.");
							yyyyMMddHHmmss.parse(enames[0]);
							return true;
						}
					} catch (ParseException e) {
					}
				return false;
			}
		})){
    		if(curfile==null || curfile.lastModified() < file.lastModified()){
    			curfile = file;
    		}
    	}
    	if(curfile==null){
        	String path = fileDir + yyyyMMddHHmmss.format(new Date()) + ".log";
        	try {
				fosm = new FileOutputStream(path,true);
				fileSizeCur=0;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
    	}else{
    		RandomAccessFile fism=null;
    		try {
				fism = new RandomAccessFile(curfile,"r");
				long len = fism.length();
				if(len < fileSizeMax){
					fosm = new FileOutputStream(curfile,true);
					fileSizeCur=len;
				}else{
		        	String path = fileDir + yyyyMMddHHmmss.format(new Date()) + ".log";
					fosm = new FileOutputStream(path,true);
					fileSizeCur=0;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				if(fism!=null)
					try {
						fism.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
    	}
    }
    private String getTag(int stackIndex){
		StackTraceElement e  = new Throwable().getStackTrace()[stackIndex];
		String clazz = e.getClassName();
		String method = e.getMethodName();
		int line = e.getLineNumber();
		String ps = clazz.substring(clazz.lastIndexOf('.')+1, clazz.length());
		StringBuffer sb = new StringBuffer(ps);
		sb.append("_").append(method).append("_").append(line);
		return sb.toString();
	}
	private String getMsg(Object... objects) {
		StringBuffer sb = new StringBuffer();
		for(Object obj : objects){
			if(obj==null){
				sb.append("null");
			}else{
				sb.append(obj.toString());
			}
		}
		return sb.toString();
	}
	
	private void writeFileLog(String levelstr,String tag,Object[] objects) {
		Date now = new Date();
		Date today = getTodayDate(now);
		StringBuffer sb = new StringBuffer();
		sb.append(yyyyMMddHHmmss.format(now)).append(" [V] ")
		.append(" ").append(tag).append("\r\n")
		.append(getMsg(objects)).append("\r\n");
		byte[] dat = sb.toString().getBytes();
		try {
			fosm.write(dat);
			fosm.flush();
			if(fileType == FileDaily){
				if(today.after(fileTimePre)){
					fileTimePre = today;
					_createFileDaily();
				}
			}else if(fileType == FileSize){
				fileSizeCur+=dat.length;
				if(fileSizeCur >= fileSizeMax){
					_createFileSize();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private Date getTodayDate(Date date){
		Date today = new Date(date.getYear(), date.getMonth(), date.getDate());
		return today;
	}


}
