package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.Display;
import io.intino.alexandria.ui.displays.UserMessage;
import io.intino.alexandria.ui.displays.components.selector.SelectorOption;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command.CommandResult;
import io.quassar.editor.box.commands.LanguageCommands;
import io.quassar.editor.box.ui.types.ForgeView;
import io.quassar.editor.box.util.DisplayHelper;
import io.quassar.editor.box.util.LanguageHelper;
import io.quassar.editor.box.util.PathHelper;
import io.quassar.editor.box.util.PermissionsHelper;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;

import java.util.List;

public class LanguageForgeTemplate extends AbstractLanguageForgeTemplate<EditorBox> {
	private Language language;
	private String release;
	private ForgeView view;

	public LanguageForgeTemplate(EditorBox box) {
		super(box);
	}

	public void language(Language language) {
		this.language = language;
	}

	public void release(String release) {
		this.release = release;
	}

	public void view(ForgeView view) {
		this.view = view;
	}

	@Override
	public void init() {
		super.init();
		infoLink.onExecute(e -> showInfoBlock());
		infoDialog.onOpen(e -> refreshInfoDialog());
		infoDialog.onClose(e -> refreshProperties());
		kitBlock.onInit(e -> initKitBlock());
		kitBlock.onShow(e -> refreshKitBlock());
		executionBlock.onInit(e -> initExecutionBlock());
		executionBlock.onShow(e -> refreshExecutionBlock());
		viewSelector.select(0);
		removeLanguage.signChecker((sign, reason) -> language.key().equals(sign));
		removeLanguage.onExecute(e -> removeLanguage());
		versionSelector.onSelect(e -> updateVersion());
		refreshVersions.onExecute(e -> refreshVersions());
	}

	@Override
	public void refresh() {
		super.refresh();
		refreshProperties();
		refreshVersions();
		refreshFooter();
		refreshView();
		refreshVersions.launch();
	}

	private void showInfoBlock() {
		infoDialog.open();
	}

	private void refreshView() {
		Model model = box().modelManager().get(language.metamodel());
		viewSelector.address(path -> PathHelper.forgePath(path, model.id(), release));
		if (view != null) viewSelector.selection(view.name());
		hideViews();
		if (view == ForgeView.Kit) kitBlock.show();
		else if (view == ForgeView.Execution) executionBlock.show();
		else kitBlock.show();
	}

	private void hideViews() {
		if (kitBlock.isVisible()) kitBlock.hide();
		else if (executionBlock.isVisible()) executionBlock.hide();
	}

	private void refreshInfoDialog() {
		infoStamp.onRename(this::reload);
		infoStamp.language(language);
		infoStamp.release(release);
		infoStamp.refresh();
	}

	private void initKitBlock() {
		kitStamp.onCreateVersion(this::versionCreated);
	}

	private void refreshKitBlock() {
		kitStamp.language(language);
		kitStamp.release(release);
		kitStamp.refresh();
	}

	private void initExecutionBlock() {
		executionStamp.onCreateVersion(this::versionCreated);
	}

	private void refreshExecutionBlock() {
		executionStamp.language(language);
		executionStamp.release(release);
		executionStamp.refresh();
	}

	private void refreshProperties() {
		Model metamodel = box().modelManager().get(language.metamodel());
		logo.value(LanguageHelper.logo(language, box()));
		languageName.value(language.key().toLowerCase());
		metamodelLink.site(PathHelper.modelUrl(metamodel, release, session()));
		removeLanguage.visible(PermissionsHelper.canRemove(language, session(), box()));
	}

	private void refreshVersions() {
		Model metamodel = box().modelManager().get(language.metamodel());
		List<String> releases = box().modelManager().releases(metamodel).reversed();
		versionSelector.visible(false);
		versionSelector.address(path -> PathHelper.forgeReleasePath(path, metamodel.id(), viewSelector.selection().getFirst()));
		versionSelector.clear();
		releases.forEach(r -> versionSelector.add((SelectorOption) createReleaseOption(r)));
		if (release != null) versionSelector.selection(releaseOptionOf(release));
		versionSelector.visible(true);
		versionSelector.children().stream().filter(d -> d instanceof ReleaseSelectorOption).forEach(Display::refresh);
		versionSelector.refresh();
	}

	private String releaseOptionOf(String release) {
		return versionSelector.children().stream().filter(d -> d instanceof ReleaseSelectorOption && d.name().equals(release)).map(Display::name).findFirst().orElse(null);
	}

	private String selectedRelease() {
		String id = !versionSelector.selection().isEmpty() ? versionSelector.selection().getFirst() : null;
		if (id == null) return null;
		ReleaseSelectorOption option = versionSelector.children().stream().filter(d -> d instanceof ReleaseSelectorOption && d.name().equals(id)).map(d -> ((ReleaseSelectorOption) d)).findFirst().orElse(null);
		return option != null ? option.release() : null;
	}

	private ReleaseSelectorOption createReleaseOption(String release) {
		return new ReleaseSelectorOption(box()).language(language).release(release).onRemove(this::removeRelease);
	}

	private void refreshFooter() {
		Language parentLanguage = box().languageManager().get(language.parent());
		parentLanguageBlock.visible(parentLanguage != null);
		if (parentLanguage == null) return;
		parentLanguageImage.value(LanguageHelper.logo(parentLanguage, box()));
		parentLanguageTitle.value("%s forge".formatted(parentLanguage.name()));
		parentLanguageLink.address(path -> PathHelper.languagePath(language));
	}

	private void updateVersion() {
		List<String> selection = versionSelector.selection();
		release = !selection.isEmpty() ? selection.getFirst() : null;
		refreshView();
	}

	private void versionCreated(CommandResult result) {
		if (!result.success()) notifyUser(translate("Could not create version"), UserMessage.Type.Error);
		refresh();
	}

	private void removeRelease(String release) {
		box().commands(LanguageCommands.class).remove(language, release, username());
		refresh();
	}

	private void removeLanguage() {
		notifyUser(translate("Removing language..."), UserMessage.Type.Loading);
		box().commands(LanguageCommands.class).remove(language, username());
		notifyUser(translate("Language removed"), UserMessage.Type.Success);
		Model model = box().modelManager().get(language.metamodel());
		notifier.redirect(PathHelper.modelUrl(model, session()));
	}

	private void reload(Language newLanguage) {
		this.language = newLanguage;
	}

}