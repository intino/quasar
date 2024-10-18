package io.intino.ime.model;

public class Operation {
	private String name;
	private Type type;

	public Operation(String name) {
		this(name, Operation.Type.Generic);
	}

	public Operation(String name, Type type) {
		this.name = name;
		this.type = type;
	}

	public String name() {
		return name;
	}

	public Operation name(String name) {
		this.name = name;
		return this;
	}

	public Type type() {
		return type;
	}

	public Operation type(Type type) {
		this.type = type;
		return this;
	}

	public enum Type {
		Build("Adb"), Run("PlayArrow"), Publish("Publish"), Generic("Build");

		private final String icon;

		Type(String icon) {
			this.icon = icon;
		}

		public String icon() {
			return icon;
		}
	}

}
