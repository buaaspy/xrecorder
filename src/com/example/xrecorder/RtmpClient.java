package com.example.xrecorder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.xrecorder.RtmpMediaData.OnMediaDataListener;
import com.smaxe.uv.client.INetConnection;
import com.smaxe.uv.client.INetStream;
import com.smaxe.uv.client.License;
import com.smaxe.uv.client.NetConnection;
import com.smaxe.uv.client.NetStream;
import com.smaxe.uv.stream.MediaData;

public class RtmpClient {

	private Logger log = LoggerFactory.getLogger(RtmpClient.class);

	private final String subTAG = "RtmpClient";
	private String SERVER_URL;
	private String STREAM = "";
	private NetConnection connection = null;
	private NetStream netStream = null;
	private RtmpMediaData rtmpMediaData = null;
	private AudioCenter audioCenter;
	private OnConnListener mConnListener;

	public RtmpClient() {
		License.setKey(Config.JUV_KEY);

		audioCenter = new AudioCenter();
		rtmpMediaData = new RtmpMediaData();

		rtmpMediaData.setDataChangeListener(new OnMediaDataListener() {
			public void onAudioData(MediaData mediaData) {
				try {
					InputStream is = mediaData.read();
					byte[] audioData = new byte[is.available()];
					int re = is.read(audioData);
					if (re != 0) {
						byte[] realAudioData = new byte[re - 1];
						System.arraycopy(audioData, 1, realAudioData, 0, re - 1);
						audioCenter.putData(0, realAudioData, re - 1);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void connect(String SERVER_URL, String STREAM) {
		this.SERVER_URL = SERVER_URL;
		this.STREAM = STREAM;
		connection = new NetConnection();
		connection.addEventListener(new NetConnectionListener());
		connection.connect(this.SERVER_URL);
	}

	private class NetConnectionListener extends NetConnection.ListenerAdapter {
		@Override
		public void onAsyncError(INetConnection arg0, String arg1,
				Exception arg2) {
			super.onAsyncError(arg0, arg1, arg2);
		}

		@Override
		public void onIOError(INetConnection arg0, String arg1) {
			super.onIOError(arg0, arg1);
		}

		@Override
		public void onNetStatus(INetConnection source, Map<String, Object> info) {
			log.debug("NetConnection#onNetStatus: " + info, subTAG);
			String result = info.get("code").toString();
			if (NetConnection.CONNECT_SUCCESS.equals(result)) {
				log.debug("NetConnection#onNetStatus: connection success.",
						subTAG);
				netStream = new NetStream(source);
				netStream.addEventListener(new NetStreamListener());
				if (mConnListener != null) {
					mConnListener.onConnectSuccess();
				}
			} else if (NetConnection.CONNECT_FAILED.equals(result)) {
				log.debug("NetConnection#onNetStatus: connection fail.", subTAG);
			}
		}

		private class NetStreamListener extends NetStream.ListenerAdapter {
			@Override
			public void onNetStatus(INetStream netStream,
					Map<String, Object> info) {
				String code = info.get("code").toString();
				if (NetStream.PUBLISH_START.equals(code)) {
					log.debug("NetStream#onNetStatus: PUBLISH_START", subTAG);
				} else if (NetStream.RECORD_START.equals(code)) {
					log.debug("NetStream#onNetStatus: RECORD_START", subTAG);
				} else if (NetStream.RECORD_STOP.equals(code)) {
					log.debug("NetStream#onNetStatus: RECORD_STOP", subTAG);
				} else if (NetStream.PLAY_START.equals(code)) {
					log.debug("NetStream#onNetStatus: PLAY_START", subTAG);
				} else if (NetStream.PLAY_COMPLETE.equals(code)) {
					log.debug("NetStream#onNetStatus: PLAY_COMPLETE", subTAG);
				} else if (NetStream.PLAY_PUBLISH_NOTIFY.equals(code)) {
					log.debug("NetStream#onNetStatus: PLAY_PUBLISH_NOTIFY",
							subTAG);
				} else if (NetStream.PLAY_UNPUBLISH_NOTIFY.equals(code)) {
					log.debug("NetStream#onNetStatus: PLAY_UNPUBLISH_NOTIFY",
							subTAG);
				} else if (NetStream.PLAY_STOP.equals(code)) {
					log.debug("NetStream#onNetStatus: PLAY_STOP", subTAG);
				}
			}
		}
	}

	public void publish() {
		if (connection.connected()) {
			netStream.attachAudio(audioCenter);
			audioCenter.publishSpeexAudio();
			netStream.publish(STREAM, NetStream.LIVE);
		}
	}

	public void disConnect() {
		if (connection.connected()) {
			connection.close();
			audioCenter.stop();
		}
	}

	public void setOnConnListener(OnConnListener onConnListener) {
		this.mConnListener = onConnListener;
	}

	public interface OnConnListener {
		public void onConnectSuccess();
	}
}
