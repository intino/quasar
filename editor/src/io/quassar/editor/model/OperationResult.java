package io.quassar.editor.model;

public class OperationResult {
	private boolean success;
	private String message;

	public OperationResult(boolean success, String message) {
		this.success = success;
		this.message = message;
	}

	public boolean success() {
		return success;
	}

	public String message() {
		return message;
	}

	public static OperationResult Success() {
		return new OperationResult(true, null);
	}

	public static OperationResult Error(String message) {
		return new OperationResult(false, message);
	}

}
