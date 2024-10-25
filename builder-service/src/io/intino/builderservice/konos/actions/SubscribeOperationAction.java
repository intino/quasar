package io.intino.builderservice.konos.actions;

import io.intino.builderservice.konos.BuilderServiceBox;
import io.intino.alexandria.http.pushservice.Client;
import io.intino.alexandria.http.spark.SparkNotifier;

public class SubscribeOperationAction {
	public BuilderServiceBox box;
	public io.intino.alexandria.http.spark.SparkContext context;
	public String ticket;

	public void onOpen(Client client, SparkNotifier notifier) {
	}

	public void onClose(Client client) {
	}

	public void execute() {

	}
}