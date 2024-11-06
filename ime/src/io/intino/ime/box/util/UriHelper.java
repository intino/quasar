package io.intino.ime.box.util;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class UriHelper {

	public static URL urlOf(String value) {
		try {
			return new URI(value).toURL();
		} catch (MalformedURLException | URISyntaxException e) {
			return null;
		}
	}

}
