package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.events.SelectionEvent;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.ui.types.LanguageTab;
import io.quassar.editor.box.util.LanguageHelper;
import io.quassar.editor.box.util.PathHelper;
import io.quassar.editor.box.util.PermissionsHelper;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LanguageRelease;

import java.util.function.Consumer;

import static io.quassar.editor.box.util.DisplayHelper.valueOrDefault;

public class LanguageExplorer extends AbstractLanguageExplorer<EditorBox> {
	private Language language;
	private LanguageTab tab;
	private String release;
	private boolean refreshRequired = false;
	private boolean expanded = true;
	private Consumer<Boolean> expandListener;
	private Consumer<Boolean> collapseListener;

	public LanguageExplorer(EditorBox box) {
		super(box);
	}

	public void invalidateCache(boolean value) {
		this.refreshRequired = value;
	}

	public void language(Language language) {
		this.refreshRequired = refreshRequired || this.language == null || !language.name().equals(this.language.name());
		this.language = language;
	}

	public void release(String release) {
		this.refreshRequired = refreshRequired || !release.equals(this.release);
		this.release = release;
	}

	public void tab(LanguageTab tab) {
		this.refreshRequired = refreshRequired || !tab.equals(this.tab);
		this.tab = tab;
	}

	public void onExpand(Consumer<Boolean> listener) {
		this.expandListener = listener;
	}

	public void onCollapse(Consumer<Boolean> listener) {
		this.collapseListener = listener;
	}

	@Override
	public void init() {
		super.init();
		initToolbar();
		releaseSelector.onSelect(this::updateRelease);
		aboutBlock.onShow(e -> refreshAbout());
		versionsBlock.onShow(e -> refreshHelp());
		examplesBlock.onShow(e -> refreshExamples());
	}

	@Override
	public void refresh() {
		super.refresh();
		if (!refreshRequired) return;
		refreshRequired = false;
		refreshSimpleTitle();
		refreshReleaseTitle();
		if (!expanded) expand();
		else refreshBlocks();
	}

	private void initToolbar() {
		expandExplorer.onExecute(e -> expand());
		expandExplorerFull.onExecute(e -> expand());
		collapseExplorer.onExecute(e -> collapse());
	}

	private void refreshSimpleTitle() {
		simpleTitle.visible(tab == LanguageTab.About);
		if (!simpleTitle.isVisible()) return;
		title.value(translate(LanguageHelper.title(tab)).formatted(language.key().toLowerCase(), release));
	}

	private void refreshReleaseTitle() {
		releaseTitle.visible(tab != LanguageTab.About);
		if (!releaseTitle.isVisible()) return;
		titlePrefix.value(language.key().toLowerCase());
		refreshReleaseSelector();
		titleSuffix.value(translate(tab.name().toLowerCase()));
	}

	private void refreshReleaseSelector() {
		releaseSelector.visible(tab != LanguageTab.About);
		if (!releaseSelector.isVisible()) return;
		releaseSelector.clear();
		releaseSelector.addAll(language.releases().reversed().stream().map(LanguageRelease::version).toList());
		releaseSelector.selection(release);
	}

	private void refreshBlocks() {
		collapsedBlock.visible(!expanded);
		refreshExpandedBlock();
	}

	private void refreshExpandedBlock() {
		expandedBlock.visible(expanded);
		if(!expandedBlock.isVisible()) return;
		aboutBlock.hide();
		versionsBlock.hide();
		examplesBlock.hide();
		if (tab == LanguageTab.Help) versionsBlock.show();
		else if (tab == LanguageTab.Examples) examplesBlock.show();
		else aboutBlock.show();
	}

	private void refreshAbout() {
		refreshMetamodel();
		refreshForge();
		aboutTitle.value(valueOrDefault(language.title()));
		aboutDescription.value(valueOrDefault(language.description()).replace("\n", "<br/>"));
		aboutCitation.value(valueOrDefault(language.citation()).replace("\n", "<br/>"));
		aboutCitationLink.visible(!language.citationLink().isEmpty());
		if (aboutCitationLink.isVisible()) aboutCitationLink.text(language.citationLink());
		aboutLicense.value(valueOrDefault(language.license()).replace("\n", "<br/>"));
	}

	private void refreshHelp() {
		String content = box().languageManager().loadHelp(language, release);
		releaseHelpStamp.content("<div class='help'>" + content + "</div>");
		releaseHelpStamp.refresh();
	}

	private void refreshMetamodel() {
		aboutBlock.metamodelBlock.visible(PermissionsHelper.canEdit(language, session(), box()));
		if (!aboutBlock.metamodelBlock.isVisible()) return;
		metamodelLink.address(path -> PathHelper.modelPath(box().modelManager().get(language.metamodel())));
	}

	private void refreshForge() {
		String metamodel = language.metamodel();
		aboutBlock.forgeBlock.visible(metamodel != null && PermissionsHelper.canEdit(language, session(), box()));
		if (!aboutBlock.forgeBlock.isVisible()) return;
		forgeLink.site(PathHelper.forgeUrl(box().modelManager().get(metamodel), release, session()));
	}

	private void refreshExamples() {
		examplesDialog.visible(tab == LanguageTab.Examples);
		examplesStamp.language(language);
		examplesStamp.release(language.release(release));
		examplesStamp.mode(ModelsTemplate.Mode.Embedded);
		examplesStamp.tab(LanguageTab.Examples);
		examplesStamp.bindTo(examplesDialog);
		examplesStamp.refresh();
	}

	private void updateRelease(SelectionEvent event) {
		if (event.selection().isEmpty()) return;
		String selected = (String) event.selection().getFirst();
		release(selected);
		refresh();
	}

	private void expand() {
		collapsedBlock.hide();
		expandListener.accept(true);
		expanded = true;
		refreshBlocks();
		expandedBlock.show();
	}

	private void collapse() {
		expandedBlock.hide();
		collapsedBlock.show();
		collapseListener.accept(true);
		expanded = false;
		refreshBlocks();
	}

}