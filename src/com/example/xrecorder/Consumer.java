package com.example.xrecorder;

public interface Consumer {
	
	public void putData(long ts, byte[] buf, int size);
	
	public void setRecording(boolean isRecording);
	
	public boolean isRecording();	
	
	public void stop();
}
