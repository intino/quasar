package io.intino.builderservice.konos.actions;

import io.intino.builderservice.konos.BuilderServiceBox;
import io.intino.alexandria.exceptions.*;
import io.intino.alexandria.http.pushservice.Client;
import io.intino.alexandria.http.server.AlexandriaHttpNotifier;

public class SubscribeOperationAction {
	public BuilderServiceBox box;
	public io.intino.alexandria.http.server.AlexandriaHttpContext context;
	public String ticket;

	public void onOpen(Client client, AlexandriaHttpNotifier notifier) {
		//register listener
	}

	public void onClose(Client client) {
		//unregister listener
	}

	public void execute() {

	}
}