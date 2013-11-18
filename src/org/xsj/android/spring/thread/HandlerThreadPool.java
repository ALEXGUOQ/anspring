package org.xsj.android.spring.thread;

import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.xsj.android.spring.core.annotation.Component;

import android.os.HandlerThread;
public class HandlerThreadPool {
	private static int __threadId__=0;
	int maxNum;
	ConcurrentLinkedQueue<HandlerThread> unusedList = new ConcurrentLinkedQueue<HandlerThread>();
	ConcurrentLinkedQueue<HandlerThread> usedList = new ConcurrentLinkedQueue<HandlerThread>();
	
	private static synchronized int getThreadId() {
		if(__threadId__==Integer.MAX_VALUE){
			__threadId__ = 0;
		}else{
			__threadId__++;
		}
		return __threadId__;
	}
	public HandlerThreadPool(int num,int maxNum) {
		this.maxNum = maxNum;
		while(num>0){
			num--;
			HandlerThread handlerThread = _createHandlerThread();
			unusedList.add(handlerThread);
		}
	}
	public HandlerThread _createHandlerThread(){
		HandlerThread handlerThread = new HandlerThread(getThreadId()+"");
		handlerThread.start();
		return handlerThread;
	}
	public void close(){
		for(HandlerThread thread : unusedList){
			thread.getLooper().quit();
		}
		unusedList.clear();
		for(HandlerThread thread : usedList){
			thread.getLooper().quit();
		}
		usedList.clear();
	}
	public void close(HandlerThread thread){
		
	}
	public void reclaim(HandlerThread thread){
		System.out.println("reclain thread:"+thread.getId());
		usedList.remove(thread);
		if(thread.isInterrupted())
		unusedList.add(thread);
	}
	public HandlerThread getThread(){
		HandlerThread handlerThread = unusedList.poll();
		if(handlerThread!=null){
			usedList.add(handlerThread);
		}else{
			if(usedList.size() < maxNum){
				handlerThread = _createHandlerThread();
				usedList.add(handlerThread);
			}
		}
		return handlerThread;
	}
	
	public int getCountUsed(){
		return usedList.size();
	}
	public int getCountUnUsed(){
		return unusedList.size();
	}
}
