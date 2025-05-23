package io.quassar.editor.box.util;

import java.net.InetAddress;
import java.net.URI;

public class UrlHelper {

	public static boolean isLocalUrl(String url) {
		try {
			URI uri = new URI(url);
			String host = uri.getHost();

			if (host == null) return false;
			if (host.equals("localhost") || host.equals("127.0.0.1")) return true;

			InetAddress address = InetAddress.getByName(host);
			if (address.isAnyLocalAddress() || address.isLoopbackAddress()) return true;

			byte[] ip = address.getAddress();
			int firstByte = ip[0] & 0xFF;
			int secondByte = ip[1] & 0xFF;

			return (
					// 10.x.x.x
					firstByte == 10 ||
							// 172.16.0.0 - 172.31.255.255
							(firstByte == 172 && secondByte >= 16 && secondByte <= 31) ||
							// 192.168.x.x
							(firstByte == 192 && secondByte == 168)
			);

		} catch (Exception e) {
			return false;
		}
	}

}
