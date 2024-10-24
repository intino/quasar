package io.intino.ime.box.ui.displays.templates;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.ui.DisplayHelper;
import io.intino.ime.box.users.TokenProvider;

public class UserSettingsEditor extends AbstractUserSettingsEditor<ImeBox> {

	public UserSettingsEditor(ImeBox box) {
		super(box);
	}

	public boolean check() {
		if (!DisplayHelper.check(dockerHubTokenField, this::translate)) return false;
		return true;
	}

	public TokenProvider.Record tokens() {
		return new TokenProvider.Record(username()).dockerHubToken(dockerHubTokenField.value());
	}

	@Override
	public void refresh() {
		super.refresh();
		TokenProvider.Record record = box().tokenProvider().of(username());
		dockerHubImage.value(UserSettingsEditor.class.getResource("/images/docker.png"));
		githubImage.value(UserSettingsEditor.class.getResource("/images/github.png"));
		dockerHubTokenField.value(record.dockerHubToken());
	}
}