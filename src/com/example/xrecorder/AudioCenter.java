package com.example.xrecorder;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smaxe.io.ByteArray;
import com.smaxe.uv.client.microphone.AbstractMicrophone;
import com.smaxe.uv.stream.support.MediaDataByteArray;

public class AudioCenter extends AbstractMicrophone implements Consumer {
	private Logger log = LoggerFactory.getLogger(AudioCenter.class);
	
	private volatile boolean isRecording;
	
	class processedData {
		private long ts;
		private int size;
		private byte[] processed = new byte[256];
	}
	private processedData pData;
	private List<processedData> list;
	
	public void AudioCenter() {
		list = Collections.synchronizedList(new LinkedList<processedData>());
	}

	public void publishSpeexAudio() {
		new Thread(new Runnable() {
			
			byte[] SpeexRtmpHead = new byte[] { (byte) 0xB2 };
			private byte[] processedData;
			int len;
			
			@Override
			public void run() {
				while (isRecording()) {
					
					if(list.size() > 0){
						pData = list.remove(0);
						processedData = pData.processed;
						len = pData.size;
						byte[] speexData = new byte[len + 1];
						System.arraycopy(SpeexRtmpHead, 0, speexData, 0, 1);
						System.arraycopy(processedData, 0, speexData, 1, len);
						fireOnAudioData(new MediaDataByteArray(20, new ByteArray(speexData)));
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
