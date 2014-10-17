package com.example.xrecorder;

import com.smaxe.uv.client.video.AbstractVideo;
import com.smaxe.uv.stream.MediaData;

public class RtmpMediaData extends AbstractVideo {
	private OnMediaDataListener mediaDataListener;

	public void setDataChangeListener(OnMediaDataListener dataChangeListener) {
		this.mediaDataListener = dataChangeListener;
	}

	@Override
	public void onMetaData(Object arg0) {
		super.onMetaData(arg0);
	}

	@Override
	public void onAudioData(MediaData data) {
		mediaDataListener.onAudioData(data);
		super.onAudioData(data);
	}

	@Override
	public void onFlvData(MediaData data) {

		super.onFlvData(data);
	}

	@Override
	public void onVideoData(MediaData data) {
		super.onVideoData(data);
	}

	public interface OnMediaDataListener {
		public void onAudioData(MediaData mediaData);
	}

}