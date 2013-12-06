package org.xsj.android.spring.thread;

import java.util.concurrent.ConcurrentHashMap;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

public class HandlerThreadReactor{
	
	HandlerThreadPool handlerThreadPool;
	ConcurrentHashMap<Integer, MessageListener> messageListenerMap; 
	public Looper loop;
	public Handler handler;
	private  boolean alive;
	
	public Handler getHandler() {
		return handler;
	}
	public HandlerThreadReactor(){
		messageListenerMap = new ConcurrentHashMap<Integer, MessageListener>();
	}
	public HandlerThreadReactor(HandlerThreadPool handlerThreadPool){
		messageListenerMap = new ConcurrentHashMap<Integer, MessageListener>();
		this.handlerThreadPool = handlerThreadPool;
	}

	public boolean addMessageListener(MessageListener messageListener){
		if(messageListenerMap.contains(messageListener.key)){
			return false;
		}else{
			messageListenerMap.put(messageListener.key, messageListener);
			return true;
		}
	}
	public void setMessageListener(MessageListener messageListener) {
		messageListenerMap.put(messageListener.key, messageListener);
		
	}
	public void removeMessageListener(MessageListener messageListener) {
		messageListenerMap.remove(messageListener.key);		
	}
	public void removeAllMessageListener() {
		messageListenerMap.clear();
	}
	
	public void  start(){
		HandlerThread thread = null;
		if(handlerThreadPool==null){
			thread = new HandlerThread(this.getClass().getSimpleName());
			thread.start();
		}else{
			thread = handlerThreadPool.getThread();
		}
		loop = thread.getLooper();
		handler = new Handler(loop){
			@Override
			public void handleMessage(Message msg){
				try {
					MessageListener messageListener = messageListenerMap.get(msg.what);
					if(messageListener!=null){
						messageListener.handleMessage(msg);
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		};
		alive=true;
	};
	public void restart(){
		stop();
		start();
	};
	public void stop(){
		alive=false;
		if(handlerThreadPool==null){
			loop.quit();
		}else{
			handlerThreadPool.reclaim((HandlerThread) loop.getThread());
		}
		handler = null;
	}
	public boolean isAlive(){
		return alive;
	}
	public void dispatch(Message message){
		message.setTarget(handler);
		handler.sendMessage(message);
	}
	public void dispatch(Message message,long delayMillis){
		message.setTarget(handler);
		handler.sendMessageDelayed(message, delayMillis);
	}
	public void dispatch(int what, Object obj){
		Message message = handler.obtainMessage();
		message.what = what;
		message.obj = obj;
		message.sendToTarget();
	}
	public void dispatch(int what, Object obj,long delayMillis ){
		Message message = handler.obtainMessage();
		message.what = what;
		message.obj = obj;
		handler.sendMessageDelayed(message, delayMillis);
	}
}
