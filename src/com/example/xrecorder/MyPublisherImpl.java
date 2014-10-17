package com.example.xrecorder;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.xrecorder.RtmpClient.OnConnListener;
import com.smaxe.io.ByteArray;
import com.smaxe.uv.client.INetConnection;
import com.smaxe.uv.client.INetStream;
import com.smaxe.uv.client.NetConnection;
import com.smaxe.uv.client.NetStream;
import com.smaxe.uv.client.microphone.AbstractMicrophone;
import com.smaxe.uv.stream.support.MediaDataByteArray;

public class MyPublisherImpl extends AbstractMicrophone implements Consumer {
	private Logger log = LoggerFactory.getLogger(MyPublisherImpl.class);
	
	private volatile boolean isRecording;
	
	class processedData {
		private long ts;
		private int size;
		private byte[] processed = new byte[1024];
	}
	private processedData pData;
	private List<processedData> list;
	
	private MyPublisherImpl() {
		list = Collections.synchronizedList(new LinkedList<processedData>());
	}
	
	private static MyPublisherImpl publisherImpl = null;
	
	public static MyPublisherImpl getInstance() {
		if (publisherImpl == null) {
			synchronized(MyPublisher.class) {
				if (publisherImpl == null) {
					publisherImpl = new MyPublisherImpl();
					publisherImpl.setRecording(true);
				}
			}
		}
		
		return publisherImpl; 
	}

	public void publishSpeexAudio() {
		new Thread(new Runnable() {
			
			byte[] SpeexRtmpHead = new byte[] { (byte) 0xB2 };
			private byte[] processedData;
			int len;
			
			@Override
			public void run() {
				log.debug("publish thread starts running !");
				
				while (isRecording()) {
					
					if(list.size() > 0){
						pData = list.remove(0);
						processedData = pData.processed;
						len = pData.size;
						byte[] speexData = new byte[len + 1];
						System.arraycopy(SpeexRtmpHead, 0, speexData, 0, 1);
						System.arraycopy(processedData, 0, speexData, 1, len);
						fireOnAudioData(new MediaDataByteArray(20, new ByteArray(speexData)));
						
						log.debug("fireOnAudioData");
					} else {
						try {
							Thread.sleep(20);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				
				log.debug("Publish SpeexAudio Thread Release");
				
			}
			
		}, "Publish SpeexAudio Thread").start();
	}

	@Override
	public void putData(long ts, byte[] buf, int size) {
		processedData data = new processedData();
		data.ts = ts;
		data.size = size;
		System.arraycopy(buf, 0, data.processed, 0, size);
		list.add(data);
	}

	@Override
	public void setRecording(boolean isRecording) {
		isRecording = true;
	}

	@Override
	public boolean isRecording() {
		return isRecording;
	}

	@Override
	public void stop() {
		isRecording = false;
	}
}
