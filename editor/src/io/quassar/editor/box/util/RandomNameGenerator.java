package io.quassar.editor.box.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

public class RandomNameGenerator {
	private static final String[] Adjectives = load("/random-name-generator/adjectives");
	private static final String[] Nouns = load("/random-name-generator/nouns");
	private static final Random Random = new Random();

	public static String generate() {
		return String.join("-", randomAdjective(), randomNoun(), randomNumber());
	}

	private static String randomAdjective() {
		return Adjectives[Random.nextInt(Adjectives.length)];
	}

	private static String randomNoun() {
		return Nouns[Random.nextInt(Nouns.length)];
	}

	private static String randomNumber() {
		return String.valueOf(100 + Random.nextInt(900));
	}

	private static String[] load(String resource) {
		try (InputStream is = RandomNameGenerator.class.getResourceAsStream(resource)) {
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
