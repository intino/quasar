package io.quassar.editor.box.ui.displays;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.ui.services.push.User;
import io.intino.alexandria.ui.utils.UrlUtil;
import io.quassar.editor.box.EditorBox;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.function.Consumer;

public class GoogleLoginDisplay extends AbstractGoogleLoginDisplay<EditorBox> {
	private Consumer<User> successListener;
	private Consumer<String> failureListener;

	public GoogleLoginDisplay(EditorBox box) {
		super(box);
	}

	public void onSuccess(Consumer<User> listener) {
		this.successListener = listener;
	}

	public void onFailure(Consumer<String> listener) {
		this.failureListener = listener;
	}

	@Override
	public void refresh() {
		super.refresh();
		notifier.refresh(box().configuration().googleClientId());
	}

	public void success(String credential) {
		User user = verify(credential);
		if (user == null) failure();
		else successListener.accept(user);
	}

	public void failure() {
		failureListener.accept("Could not log in with google");
	}

	private User verify(String credential) {
		try {
			GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
					.setAudience(Collections.singletonList(box().configuration().googleClientId()))
					.build();

			GoogleIdToken idToken = verifier.verify(credential);
			if (idToken == null) return null;

			GoogleIdToken.Payload payload = idToken.getPayload();

			String email = payload.getEmail();
			String name = (String) payload.get("name");
			String pictureUrl = (String) payload.get("picture");

			return new User().username(email).fullName(name).email(email).photo(UrlUtil.toURL(pictureUrl));
		} catch (GeneralSecurityException | IOException e) {
			Logger.error(e);
			return null;
		}
	}

}