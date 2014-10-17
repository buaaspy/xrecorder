package com.example.xrecorder;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

import org.apache.mina.core.buffer.IoBuffer;

public class Utility {

	public static byte[] IoBufferToByte(Object o) {
		if (!(o instanceof IoBuffer))
			return null;
		IoBuffer ioBuffer = (IoBuffer) o;
		ioBuffer.flip();
		byte[] b = new byte[ioBuffer.limit()];
		try {
			ioBuffer.get(b);
		} catch (Exception e) {
			System.out.println("EXCEPTION" + e.toString());
		}
		return b;
	}

	public static byte[] serialize(Object object) {
		ObjectOutputStream oos = null;
		ByteArrayOutputStream baos = null;

		try {
			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			oos.writeObject(object);
			byte[] bytes = baos.toByteArray();
			return bytes;
		} catch (Exception e) {
			System.out.println("EXCEPTION" + e.toString());
		}

		return null;
	}
}
