package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.UserMessage;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.util.VersionNumberComparator;
import io.intino.ime.model.Language;
import io.intino.ime.model.LanguageInfo;
import io.intino.ime.model.Model;

import java.util.function.Consumer;

public class VersionEditor extends AbstractVersionEditor<ImeBox> {
	private Model model;
	private boolean createMode = false;
	private Consumer<Model.Version> acceptListener;

	public VersionEditor(ImeBox box) {
		super(box);
	}

	public void model(Model model) {
		this.model = model;
	}

	public void createMode(boolean value) {
		this.createMode = value;
	}

	public Model.Version version() {
		return new Model.Version(versionField.value(), metamodelVersionField.selection().getFirst(), builderUrlField.value());
	}

	public void onAccept(Consumer<Model.Version> listener) {
		this.acceptListener = listener;
	}

	public boolean check() {
		Model.Version version = version();
		if (createMode && (version.id() == null || version.id().isEmpty())) {
			notifyUser(translate("Invalid version number"), UserMessage.Type.Error);
			return false;
		}
		if (createMode && model.versionMap().containsKey(version.id())) {
			notifyUser(translate("Version already exists"), UserMessage.Type.Error);
			return false;
		}
		return true;
	}

	@Override
	public void init() {
		super.init();
		builderUrlField.onEnterPress(e -> {
			if (acceptListener != null) acceptListener.accept(version());
		});
	}

	@Override
	public void refresh() {
		super.refresh();
		Language metaLanguage = box().languageManager().get(model.language());
		versionField.value(model.version());
		versionField.readonly(!createMode);
		metamomdelVersionTitle.value(String.format(translate("%s language version"), metaLanguage.name()));
		metamodelVersionField.clear();
		metamodelVersionField.addAll(box().languageManager().versions(metaLanguage.name()).stream().map(Language::version).sorted((o1, o2) -> VersionNumberComparator.getInstance().compare(o2, o1)).toList());
		metamodelVersionField.selection(metaLanguage.version());
		builderUrlField.value(model.versionMap().get(model.version()).builderUrl());
		builderUrlField.visible(metaLanguage.level() != LanguageInfo.Level.L1 && user() != null && model.isPrivate());
	}
}