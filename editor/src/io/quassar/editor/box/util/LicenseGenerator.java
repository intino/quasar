package io.quassar.editor.box.util;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class LicenseGenerator {
	private static final SecureRandom Random = new SecureRandom();
	private static final char[] Base = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
	private static final long EpochSeconds = LocalDateTime.of(2025, 1, 1, 0, 0).toEpochSecond(ZoneOffset.UTC);
	private static final int SIZE = 9;

	public static String generate() {
		return generate(value());
	}

	public static int size() {
		return SIZE + 1;
	}

	private static String generate(long value) {
		char[] chars = new char[SIZE];
		for (int i = SIZE-1; i >= 0; i--) {
			chars[i] = Base[(int) (value % Base.length)];
			value = value / Base.length;
		}
		return addChecksumTo(mix(chars));
	}

	private static String addChecksumTo(String license) {
		return license + checksum(license);
	}

	private static long value() {
		return (seconds() << 18) | random();
	}

	private static char checksum(String license) {
		int sum = 0;
		for (int i = 0; i < license.length(); i++)
			sum += license.charAt(i);
		return Base[sum % Base.length];
	}

	private static final int[] order = {6, 3, 0, 1, 7, 2, 8, 4, 5};
	private static String mix(char[] chars) {
		char[] result = new char[SIZE];
		for (int i = 0; i < SIZE; i++) result[i] = chars[order[i]];
		return new String(result);
	}

	private static long random() {
		return Random.nextInt(1 << 18);
	}

	private static long seconds() {
		return Instant.now().getEpochSecond() - EpochSeconds;
	}

	public static void main(String[] args) {
		System.out.println(generate());
	}
}