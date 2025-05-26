package io.quassar.editor.box.actions;

import io.intino.alexandria.exceptions.AlexandriaException;
import io.intino.alexandria.exceptions.BadRequest;
import io.quassar.editor.box.EditorBox;

public class QuassarAction implements io.intino.alexandria.rest.RequestErrorHandler {
	public EditorBox box;
	public io.intino.alexandria.http.server.AlexandriaHttpContext context;
	public String token;

	public void onMalformedRequest(Throwable e) throws AlexandriaException {
		throw new BadRequest("Malformed request");
	}

}
