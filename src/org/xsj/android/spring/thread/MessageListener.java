package org.xsj.android.spring.thread;

import android.os.Message;

public abstract class MessageListener {
	public int key;
	public MessageListener(int key){
		this.key = key;
	}
	public abstract void  handleMessage(Message msg) throws Exception;

}
