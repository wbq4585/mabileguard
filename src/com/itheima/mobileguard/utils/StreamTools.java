package com.itheima.mobileguard.utils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class StreamTools {
	/**
	 * ��һ�������������ת���� һ���ַ���
	 * 
	 * @param is
	 * @return �����ַ��� null����ʧ��
	 */
	public static String readStream(InputStream is) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int len = -1;
			while ((len = is.read(buffer)) != -1) {
				baos.write(buffer, 0, len);
			}
			is.close();
			String temp = new String(baos.toByteArray());
			if (temp.contains("charset=gb2312")) {
				return new String(baos.toByteArray(), "gb2312");
			} else {
				return new String(baos.toByteArray());
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
