package org.xsj.android.spr1ng.test;

public class Bridge {
	static{
		System.loadLibrary("bridge");
	}
	public native int calculate(int i,int j);
}
