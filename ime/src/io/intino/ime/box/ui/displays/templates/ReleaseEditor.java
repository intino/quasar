package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.UserMessage;
import io.intino.alexandria.ui.displays.events.SelectionEvent;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.util.ModelHelper;
import io.intino.ime.model.LanguageLevel;
import io.intino.ime.model.Model;
import io.intino.ime.model.Release;
import io.intino.ime.model.VersionType;

import java.util.function.Consumer;

public class ReleaseEditor extends AbstractReleaseEditor<ImeBox> {
	private Model model;
	private Consumer<Release> acceptListener;

	public ReleaseEditor(ImeBox box) {
		super(box);
	}

	public void model(Model model) {
		this.model = model;
	}

	public Release release() {
		return new Release(model.modelingLanguage(), model.id(), level(), version.value());
	}

	public void onAccept(Consumer<Release> listener) {
		this.acceptListener = listener;
	}

	public boolean check() {
		Release release = release();
		if (ModelHelper.hasRelease(model, release, box())) {
			notifyUser(translate("Release already exists"), UserMessage.Type.Error);
			return false;
		}
		return true;
	}

	@Override
	public void init() {
		super.init();
		versionTypeSelector.onSelect(this::updateVersion);
		versionTypeSelector.selection("revisionOption");
		levelSelector.selection("level1Option");
	}

	private void updateVersion(SelectionEvent event) {
		version.value(ModelHelper.nextReleaseVersion(model, versionType(event), box()));
	}

	private VersionType versionType(SelectionEvent event) {
		String selected = (String) event.selection().getFirst();
		if (selected.equals("revisionOption")) return VersionType.Revision;
		if (selected.equals("minorVersionOption")) return VersionType.MinorVersion;
		return VersionType.MajorVersion;
	}

	@Override
	public void refresh() {
		super.refresh();
		String value = ModelHelper.nextReleaseVersion(model, VersionType.Revision, box());
		version.value(value);
		versionTypeSelector.selection("revisionOption");
		versionTypeSelector.readonly(value.equals("1.0.0"));
		levelSelector.selection("level1Option");
	}

	private LanguageLevel level() {
		String selected = levelSelector.selection().getFirst();
		if (selected.equals("level1Option")) return LanguageLevel.L1;
		return LanguageLevel.L2;
	}

}