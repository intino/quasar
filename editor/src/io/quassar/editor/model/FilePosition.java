package io.quassar.editor.model;

public record FilePosition(int line, int column) {
	public static FilePosition from(String position) {
		return new FilePosition(Integer.parseInt(position.split("-")[0]), position.split("-").length > 1 ? Integer.parseInt(position.split("-")[1]) : 1);
	}
}
