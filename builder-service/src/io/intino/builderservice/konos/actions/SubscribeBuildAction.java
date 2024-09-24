package io.intino.builderservice.konos.actions;

import io.intino.builderservice.konos.BuilderServiceBox;
import io.intino.alexandria.exceptions.*;
import io.intino.alexandria.http.pushservice.Client;
import io.intino.alexandria.http.spark.SparkNotifier;

public class SubscribeBuildAction {
	public BuilderServiceBox box;
	public io.intino.alexandria.http.spark.SparkContext context;
	public String buildTicket;

	public void onOpen(Client client, SparkNotifier notifier) {
		//register listener
	}

	public void onClose(Client client) {
		//unregister listener
	}

	public void execute() {

	}
}