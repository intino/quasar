package io.intino.ime.box.commands.model;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.Command;
import io.intino.ime.model.Model;

public class SaveModelGitSettingsCommand extends Command<Boolean> {
	public Model model;
	public String url;
	public String branch;

	public SaveModelGitSettingsCommand(ImeBox box) {
		super(box);
	}

	@Override
	public Boolean execute() {
		Model.GitSettings gitSettings = model.gitSettings();
		if (gitSettings.equals(new Model.GitSettings(url, branch))) return true;
		model.gitSettings(new Model.GitSettings(url, branch));
		box.modelManager().save(model);
		box.serverManager().remove(model);
		return true;
	}

}
