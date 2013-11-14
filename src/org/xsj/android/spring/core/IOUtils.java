package org.xsj.android.spring.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class IOUtils {
	public static void close(InputStream ism){
		if(ism!=null){
			try {
				ism.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public static void close(OutputStream osm){
		if(osm!=null){
			try {
				osm.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public static void close(Cursor cursor, SQLiteDatabase db){
		if(cursor!=null && !cursor.isClosed()){
			cursor.close();
		}
		if(db!=null && db.isOpen()){
			db.close();
		}
	}
	public static void close(SQLiteDatabase db){
		if(db!=null && db.isOpen()){
			db.close();
		}
	}
	public static void close(Cursor cursor){
		if(cursor!=null && !cursor.isClosed()){
			cursor.close();
		}
	}
	public static void copy(File is,File os){
		FileInputStream fism = null;
		FileOutputStream fosm = null;
		try {
			fism = new FileInputStream(is);
			fosm = new FileOutputStream(os);
			copy(fism,fosm);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			close(fism);
			close(fosm);
		}
	}

	public static void copy(InputStream is,OutputStream os) throws IOException{
		byte[] buffer = new byte[1024];
		int len = 0;
		while ((len = is.read(buffer)) > 0){
			os.write(buffer, 0, len);
		}
		os.flush();
	}
	
	public static void write(byte[] data, File output) throws IOException{
		FileOutputStream fosm = null;
		try {
			 fosm = new FileOutputStream(output);
			 fosm.write(data);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}finally{
			close(fosm);
		}
	}
}
