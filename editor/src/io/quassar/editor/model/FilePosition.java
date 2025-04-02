package io.quassar.editor.model;

public record FilePosition(int line, int column) {
	public static FilePosition from(String position) {
		try {
			return new FilePosition(Integer.parseInt(position.split("-")[0]), position.split("-").length > 1 ? Integer.parseInt(position.split("-")[1]) : 1);
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
