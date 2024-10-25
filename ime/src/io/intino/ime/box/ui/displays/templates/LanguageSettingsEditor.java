package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.Resource;
import io.intino.alexandria.exceptions.Conflict;
import io.intino.alexandria.exceptions.InternalServerError;
import io.intino.alexandria.ui.displays.UserMessage;
import io.intino.alexandria.ui.displays.components.Text;
import io.intino.alexandria.ui.displays.events.actionable.ToggleEvent;
import io.intino.builderservice.schemas.BuilderInfo;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.ui.DisplayHelper;
import io.intino.ime.box.util.LanguageHelper;
import io.intino.ime.model.Language;
import io.intino.ime.model.Operation;
import io.intino.ime.model.Release;

import java.util.*;

public class LanguageSettingsEditor extends AbstractLanguageSettingsEditor<ImeBox> {
	private Language language;
	private Resource logo;
	private List<String> programmingLanguages;
	private Map<String, Operation> operationMap;
	private Set<String> tagSet;
	private BuilderInfo info;
	private String error;

	public LanguageSettingsEditor(ImeBox box) {
		super(box);
	}

	public void language(Language language) {
		this.language = language;
		this.tagSet = new HashSet<>(language.tags());
	}

	public boolean check() {
		if (!DisplayHelper.check(descriptionField, this::translate)) return false;
		if (builderField == null) return true;
		String builder = builderField.value();
		if (info == null && builder != null && !builder.isEmpty()) {
			notifyUser(translate("Docker image name is not valid"), UserMessage.Type.Error);
			return false;
		}
		return true;
	}

	public Resource logo() {
		return logo;
	}

	public String description() {
		return descriptionField.value();
	}

	public boolean isPrivate() {
		return accessTypeField.state() == ToggleEvent.State.On;
	}

	public String builder() {
		return builderField != null ? builderField.value() : language.builder();
	}

	public List<Operation> operations() {
		return new ArrayList<>(operationMap.values());
	}

	public List<String> programmingLanguages() {
		return programmingLanguages;
	}

	public List<String> tags() {
		return new ArrayList<>(tagSet);
	}

	@Override
	public void init() {
		super.init();
		propertiesBlock.onInit(e -> initProperties());
		propertiesBlock.onShow(e -> refreshProperties());
		builderBlock.onInit(e -> initBuilder());
		builderBlock.onShow(e -> refreshBuilder());
		addTagDialog.onOpen(e -> refreshTagDialog());
	}

	@Override
	public void refresh() {
		super.refresh();
		tabSelector.select("propertiesOption");
	}

	private void initProperties() {
		logoField.onChange(e -> logo = e.value());
		addTag.onExecute(e -> addTag());
		tagField.onEnterPress(e -> addTag());
	}

	private void refreshProperties() {
		logoField.value(LanguageHelper.logo(language, box()));
		nameField.value(language.name());
		descriptionField.value(language.description());
		accessTypeField.state(language.isPrivate() ? ToggleEvent.State.On : ToggleEvent.State.Off);
		refreshTags();
	}

	private void refreshTags() {
		tags.clear();
		tagSet.stream().sorted(Comparator.naturalOrder()).forEach(o -> fill(o, tags.add()));
	}

	private void initBuilder() {
		builderField.onEnterPress(e -> checkBuilder());
		checkBuilder.onExecute(e -> checkBuilder());
	}

	private void refreshBuilder() {
		builderField.value(language.builder());
		loadBuilder(language.builder());
		refreshBuilder(info);
	}

	private Release lastRelease() {
		return box().languageManager().lastRelease(language);
	}

	private void fill(Map.Entry<String, Operation> operationEntry, OperationEditor display) {
		display.operation(operationEntry.getValue());
		display.onChange(o -> operationMap.put(operationEntry.getKey(), o));
		display.refresh();
	}

	private void fill(String tag, TagEditor display) {
		display.tag(tag);
		display.onRemove(o -> removeTag(tag));
		display.refresh();
	}

	private void removeTag(String tag) {
		tagSet.remove(tag);
		refreshTags();
	}

	private void addTag() {
		if (!DisplayHelper.check(tagField, this::translate)) return;
		addTagDialog.close();
		tagSet.add(tagField.value());
		refreshTags();
	}

	private void refreshTagDialog() {
		tagField.value(null);
	}

	private void refreshBuilder(BuilderInfo info) {
		refreshBuilderInfo(info);
		refreshDetails(info);
		refreshOperations(info);
	}

	private void refreshBuilderInfo(BuilderInfo info) {
		String name = builderField.value();
		validBuilderIcon.visible(isValidBuilder(info));
		noBuilderInfoIcon.visible(hasNoBuilderInformation(info));
		invalidBuilderIcon.visible(isInvalidBuilder(info));
		checkBuilder.readonly(name == null || name.isEmpty());
	}

	private void refreshDetails(BuilderInfo info) {
		BuilderBlock.BuilderInfoBlock.BuilderDetailsBlock.BuilderPropertiesBlock builderPropertiesBlock = builderBlock.builderInfoBlock.builderDetailsBlock.builderPropertiesBlock;
		Text<?, ?> errorMessageComponent = builderBlock.builderInfoBlock.builderDetailsBlock.errorMessage;
		builderPropertiesBlock.visible(isValidBuilder(info));
		errorMessageComponent.visible(isInvalidBuilder(info));
		errorMessageComponent.value(error);
		if (!builderPropertiesBlock.isVisible()) return;
		builderPropertiesBlock.builderProperties.clear();
		info.properties().forEach((key, value) -> fill(key, value, builderPropertiesBlock.builderProperties.add()));
	}

	private void fill(String property, String value, BuilderPropertyView display) {
		display.property(property, value);
		display.refresh();
	}

	private void refreshOperations(BuilderInfo info) {
		builderBlock.builderInfoBlock.operationsBlock.visible(info != null);
		if (info == null) return;
		operations.clear();
		operationMap.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(o -> fill(o, operations.add()));
	}

	private void loadBuilder(String builder) {
		try {
			showLoading();
			checkBuilder.readonly(true);
			this.error = null;
			this.info = box().builderService().getBuilderInfo(builder, box().tokenProvider().of(username()).dockerHubToken());
			this.operationMap = loadOperations();
			this.programmingLanguages = info.targetLanguages();
		} catch (InternalServerError | Conflict e) {
			this.error = builderField.value() != null && !builderField.value().isEmpty() ? e.getMessage().isEmpty() ? translate("No information") : e.getMessage() : null;
			this.info = null;
			this.operationMap = new HashMap<>();
			this.programmingLanguages = new ArrayList<>();
		}
		finally {
			hideLoading();
			checkBuilder.readonly(false);
		}
	}

	private void showLoading() {
		builderBlock.loadingBlock.visible(true);
		builderBlock.builderInfoBlock.visible(false);
	}

	private void hideLoading() {
		builderBlock.loadingBlock.visible(false);
		builderBlock.builderInfoBlock.visible(true);
	}

	private void checkBuilder() {
		refreshBuilder(null);
		loadBuilder(builderField.value());
		refreshBuilder(info);
	}

	private boolean isValidBuilder(BuilderInfo info) {
		return info != null;
	}

	private boolean hasNoBuilderInformation(BuilderInfo info) {
		return info == null && error == null;
	}

	private boolean isInvalidBuilder(BuilderInfo info) {
		return info == null && error != null;
	}

	private Map<String, Operation> loadOperations() {
		Map<String, Operation> result = new HashMap<>();
		info.operations().forEach(o -> result.putIfAbsent(o, languageOperationOf(o)));
		return result;
	}

	private Operation languageOperationOf(String name) {
		Operation operation = language.operation(name);
		return operation != null ? operation : new Operation(name);
	}

}