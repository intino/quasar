package io.intino.builderservice.konos;

public class BuilderServiceBox extends AbstractBox {

	public BuilderServiceBox(String[] args) {
		this(new BuilderServiceConfiguration(args));
	}

	public BuilderServiceBox(BuilderServiceConfiguration configuration) {
		super(configuration);
	}

	@Override
	public io.intino.alexandria.core.Box put(Object o) {
		super.put(o);
		return this;
	}

	public void beforeStart() {

	}

	public void afterStart() {

	}

	public void beforeStop() {

	}

	public void afterStop() {

	}
}