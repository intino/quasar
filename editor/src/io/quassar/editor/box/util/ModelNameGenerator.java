package io.quassar.editor.box.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

public class ModelNameGenerator {
	private static final String[] Adjectives = load("/generators/model-name-generator/adjectives");
	private static final String[] Nouns = load("/generators/model-name-generator/nouns");
	private static final Random Random = new Random();

	public static String generate() {
		return String.join("-", randomAdjective(), randomNoun());
	}

	private static String randomAdjective() {
		return Adjectives[Random.nextInt(Adjectives.length)];
	}

	private static String randomNoun() {
		return Nouns[Random.nextInt(Nouns.length)];
	}

	private static String[] load(String resource) {
		try (InputStream is = ModelNameGenerator.class.getResourceAsStream(resource)) {
			assert is != null;
			return new String(is.readAllBytes()).split("\n");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) {
		for (int i = 0; i < 10; i++) {
			System.out.println(generate());
		}
	}
}
