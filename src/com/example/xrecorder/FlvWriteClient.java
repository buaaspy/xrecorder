package com.example.xrecorder;

import java.io.File;
import java.io.IOException;

import org.apache.mina.core.buffer.IoBuffer;
import org.red5.io.IStreamableFile;
import org.red5.io.ITagWriter;
import org.red5.io.IoConstants;
import org.red5.io.flv.FLVService;
import org.red5.io.flv.Tag;
import org.red5.server.messaging.IMessage;
import org.red5.server.net.rtmp.event.AudioData;
import org.red5.server.net.rtmp.event.FlexStreamSend;
import org.red5.server.net.rtmp.event.IRTMPEvent;
import org.red5.server.net.rtmp.event.Invoke;
import org.red5.server.net.rtmp.event.Notify;
import org.red5.server.net.rtmp.event.Unknown;
import org.red5.server.net.rtmp.event.VideoData;
import org.red5.server.net.rtmp.message.Constants;
import org.red5.server.stream.message.RTMPMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlvWriteClient implements Constants {
	private static Logger log = LoggerFactory.getLogger(FlvWriteClient.class);

	private String saveAsFileName;
	private ITagWriter tagWriter;
	private int prevSize = 0;
	private Tag tag;
	private int currentTime = 0;
	private long timeBase = 0;
	private int sampleRate = 0;
	private int channle;
	private MyPublisherImpl publisherImpl = null;

	public FlvWriteClient() {

	}

	public void start(String saveAsFileName) {
		this.saveAsFileName = saveAsFileName;
		init();
	}

	private void init() {
		File file = new File(saveAsFileName);
		FLVService flvService = new FLVService();
		flvService.setGenerateMetadata(true);
		try {
			IStreamableFile flv = flvService.getStreamableFile(file);
			tagWriter = flv.getWriter();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		publisherImpl = MyPublisherImpl.getInstance();
	}

	public void stop() {
		if (tagWriter != null) {
			tagWriter.close();
			tagWriter = null;
		}
		log.debug("writer closed!");
	}

	public void writeTag(byte[] buf, int size, long ts) {	
		if (timeBase == 0) {
			timeBase = ts;
		}
		currentTime = (int) (ts - timeBase);
		
		tag = new Tag(IoConstants.TYPE_AUDIO, currentTime, size + 1, null,
				prevSize);
		prevSize = size + 1;
		
		byte tagType = (byte) ((IoConstants.FLAG_FORMAT_SPEEX << 4))
				| (IoConstants.FLAG_SIZE_16_BIT << 1);

		switch (sampleRate) {
		case 44100:
			tagType |= IoConstants.FLAG_RATE_44_KHZ << 2;
			break;
		case 22050:
			tagType |= IoConstants.FLAG_RATE_22_KHZ << 2;
			break;
		case 11025:
			tagType |= IoConstants.FLAG_RATE_11_KHZ << 2;
			break;
		default:
			tagType |= IoConstants.FLAG_RATE_5_5_KHZ << 2;
		}

		tagType |= (channle == 2 ? IoConstants.FLAG_TYPE_STEREO
				: IoConstants.FLAG_TYPE_MONO);

		IoBuffer body = IoBuffer.allocate(tag.getBodySize());
		body.setAutoExpand(true);
		body.put(tagType);
		body.put(buf);
		body.flip();
		body.limit(size + 1);
		tag.setBody(body);
		
		/*
		if (publisherImpl == null) {
			log.debug("publisherImpl is NULL!");
		} else if (tag == null) {
			log.debug("tag is NULL!");			
		} else if (Utility.serialize(tag) == null) {
			log.debug("serialize tag is NULL!");
		} else {
			publisherImpl.putData(0, Utility.serialize(tag), Utility.serialize(tag).length);
		}
		*/
		
		try {
			tagWriter.writeTag(tag);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void setSampleRate(int sampleRate) {
		this.sampleRate = sampleRate;
	}

	public void setChannle(int channle) {
		this.channle = channle;
	}
}