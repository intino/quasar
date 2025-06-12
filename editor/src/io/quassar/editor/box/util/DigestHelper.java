package io.quassar.editor.box.util;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DigestHelper {

	public static String sha1Of(File file) throws NoSuchAlgorithmException, IOException {
		MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
		try (InputStream input = new FileInputStream(file)) {
			byte[] buffer = new byte[8192];
			int len;
			while ((len = input.read(buffer)) != -1) {
				sha1.update(buffer, 0, len);
			}
		}
		return bytesAHex(sha1.digest());
	}

	private static String bytesAHex(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (byte b : bytes) {
			sb.append(String.format("%02x", b));
		}
		return sb.toString();
	}

}
