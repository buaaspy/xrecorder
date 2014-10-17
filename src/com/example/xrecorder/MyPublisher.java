package com.example.xrecorder;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smaxe.uv.client.INetConnection;
import com.smaxe.uv.client.INetStream;
import com.smaxe.uv.client.License;
import com.smaxe.uv.client.NetConnection;
import com.smaxe.uv.client.NetStream;

public class MyPublisher {
	
	private Logger log = LoggerFactory.getLogger(MyPublisher.class);
	
	private NetConnection connection = null;
	private OnConnListener mConnListener;
	private NetStream netStream = null;
	private String SERVER_URL;
	private String STREAM = "";
	private MyPublisherImpl publisherImpl = null;
	
	public void connect(String SERVER_URL, String STREAM) {
		License.setKey(Config.JUV_KEY);
		
		this.SERVER_URL = SERVER_URL;
		this.STREAM = STREAM;
		
		connection = new NetConnection();
		connection.addEventListener(new NetConnectionListener());
		connection.connect(this.SERVER_URL);
	}
	
	public void publish() {
		if (connection.connected()) {
			log.debug("connection established !");
			publisherImpl = MyPublisherImpl.getInstance();
			netStream.attachAudio(publisherImpl);
			netStream.publish(STREAM, NetStream.LIVE);
			publisherImpl.publishSpeexAudio();
		} else {
			log.debug("connection failed !!!");
		}
	}

	public void disConnect() {
		if (connection.connected()) {
			connection.close();
			publisherImpl.stop();
		}
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
			log.debug("NetConnection#onNetStatus: " + info);
			String result = info.get("code").toString();
			if (NetConnection.CONNECT_SUCCESS.equals(result)) {
				log.debug("NetConnection#onNetStatus: connection success.");
				netStream = new NetStream(source);
				netStream.addEventListener(new NetStreamListener());
				if (mConnListener != null) {
					mConnListener.onConnectSuccess();
				}
			} else if (NetConnection.CONNECT_FAILED.equals(result)) {
				log.debug("NetConnection#onNetStatus: connection fail.");
			}
		}

		private class NetStreamListener extends NetStream.ListenerAdapter {
			@Override
			public void onNetStatus(INetStream netStream,
					Map<String, Object> info) {
				String code = info.get("code").toString();
				if (NetStream.PUBLISH_START.equals(code)) {
					log.debug("NetStream#onNetStatus: PUBLISH_START");
				} else if (NetStream.RECORD_START.equals(code)) {
					log.debug("NetStream#onNetStatus: RECORD_START");
				} else if (NetStream.RECORD_STOP.equals(code)) {
					log.debug("NetStream#onNetStatus: RECORD_STOP");
				} else if (NetStream.PLAY_START.equals(code)) {
					log.debug("NetStream#onNetStatus: PLAY_START");
				} else if (NetStream.PLAY_COMPLETE.equals(code)) {
					log.debug("NetStream#onNetStatus: PLAY_COMPLETE");
				} else if (NetStream.PLAY_PUBLISH_NOTIFY.equals(code)) {
					log.debug("NetStream#onNetStatus: PLAY_PUBLISH_NOTIFY");
				} else if (NetStream.PLAY_UNPUBLISH_NOTIFY.equals(code)) {
					log.debug("NetStream#onNetStatus: PLAY_UNPUBLISH_NOTIFY");
				} else if (NetStream.PLAY_STOP.equals(code)) {
					log.debug("NetStream#onNetStatus: PLAY_STOP");
				}
			}
		}
	}
	

	public void setOnConnListener(OnConnListener onConnListener) {
		this.mConnListener = onConnListener;
	}

	public interface OnConnListener {
		public void onConnectSuccess();
	}
}
