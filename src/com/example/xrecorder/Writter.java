package com.example.xrecorder;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Writter implements Consumer, Runnable {

	private Logger log = LoggerFactory.getLogger(Writter.class);
	private final Object mutex = new Object();
	private FlvWriteClient client = new FlvWriteClient();
	private volatile boolean isRecording;
	private processedData pData;
	private List<processedData> list;

	public Writter() {
		super();
		list = Collections.synchronizedList(new LinkedList<processedData>());
		client.setChannle(1);
		client.setSampleRate(8000);
		client.start("/mnt/sdcard/xrecorder/test.flv");
	}

	public void run() {
		log.error("write thread runing");
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