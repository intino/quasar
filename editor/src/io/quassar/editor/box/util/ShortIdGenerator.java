package io.quassar.editor.box.util;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class ShortIdGenerator {
	private static final SecureRandom Random = new SecureRandom();
	private static final char[] Base = "ABCDEFGHIJKLMNOPQRSTUVWXYZ-abcdefghijklmnopqrstuvwxyz~0123456789".toCharArray();
	private static final long EpochSeconds = LocalDateTime.of(2025, 1, 1, 0, 0).toEpochSecond(ZoneOffset.UTC);

	public static String generate() {
		return generate(value());
	}

	private static String generate(long value) {
		char[] chars = new char[8];
		for (int i = 7; i >= 0; i--) {
			chars[i] = Base[(int)(value & 0b111111)];
			value >>>= 6;
		}
		return mix(chars);
	}

	private static final int[] order = {6, 3, 0, 1, 7, 2, 4, 5};
	private static String mix(char[] chars) {
		char[] result = new char[8];
		for (int i = 0; i < 8; i++) result[i] = chars[order[i]];
		return new String(result);
	}

	private static long value() {
		return (seconds() << 18) | random();
	}

	private static long random() {
		return Random.nextInt(1 << 18);
	}

	private static long seconds() {
		return Instant.now().getEpochSecond() - EpochSeconds;
	}

}
