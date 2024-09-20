package io.intino.ime.model;

public class Operation {
	private String name;
	private Type type;
	private String url;

	public Operation(String name, Type type, String url) {
		this.name = name;
		this.type = type;
		this.url = url;
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

	public String url() {
		return url;
	}

	public Operation url(String url) {
		this.url = url;
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
