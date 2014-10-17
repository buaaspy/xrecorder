package com.example.xrecorder;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.red5.server.messaging.IMessage;

public class Publisher implements Consumer, Runnable{
	
	private Logger log = LoggerFactory.getLogger(Publisher.class);
	private final Object mutex = new Object();
	private PublishClient client = new PublishClient();
	private volatile boolean isRecording;
	private processedData pData;
	private List<processedData> list;
	private String publishName = "test";
	private String host = Config.SERVER_IP;
	private int port = Config.SERVER_PORT;
	private String app = Config.APP_NAME;
	
	IMessage msg = null;
	int timestamp = 0;
	int lastTS = 0;

	public Publisher() {
		super();
		list = Collections.synchronizedList(new LinkedList<processedData>());
		client.setHost(host);
		client.setPort(port);
		client.setApp(app);	
		client.setChannle(1);
		client.setSampleRate(8000);
		client.start(publishName, "live", null);
	}

	public void run() {
		log.debug("publish thread runing");
		while (this.isRecording()) {							
			if(list.size() > 0){
				pData = list.remove(0);
				client.writeTag(pData.processed, pData.size, pData.ts);
			} else {
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}			
		}
		stop();
	}

	public void putData(long ts, byte[] buf, int size) {
		processedData data = new processedData();
		data.ts = ts;
		data.size = size;
		System.arraycopy(buf, 0, data.processed, 0, size);
		list.add(data);
	}

	public void stop() {
		client.stop();
	}

	public void setRecording(boolean isRecording) {
		synchronized (mutex) {
			this.isRecording = isRecording;
			if (this.isRecording) {
				mutex.notify();
			}
		}
	}

	public boolean isRecording() {
		synchronized (mutex) {
			return isRecording;
		}
	}
	
	class processedData {
		private long ts;
		private int size;
		private byte[] processed = new byte[256];
	}
}