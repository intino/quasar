package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.Resource;
import io.intino.alexandria.ui.displays.events.ChangeEvent;
import io.intino.alexandria.ui.displays.events.actionable.ToggleEvent;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.ui.DisplayHelper;
import io.intino.ime.box.util.LanguageHelper;
import io.intino.ime.model.Language;
import io.intino.ime.model.Operation;
import io.intino.ime.model.Release;

import java.util.*;

import static java.util.stream.Collectors.toMap;

public class LanguageSettingsEditor extends AbstractLanguageSettingsEditor<ImeBox> {
	private Language language;
	private Resource logo;
	private Map<String, Operation> operationMap;

	public LanguageSettingsEditor(ImeBox box) {
		super(box);
	}

	public void language(Language language) {
		this.language = language;
		this.operationMap = language.operations().stream().collect(toMap(o -> UUID.randomUUID().toString(), o -> o, (a,b) -> b, LinkedHashMap::new));
	}

	public boolean check() {
		return DisplayHelper.check(descriptionField, this::translate);
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

	public String dockerImageUrl() {
		return dockerImageUrlField.value();
	}

	public List<Operation> operations() {
		return new ArrayList<>(operationMap.values());
	}

	@Override
	public void init() {
		super.init();
		propertiesBlock.onInit(e -> initProperties());
		propertiesBlock.onShow(e -> refreshProperties());
		operationsBlock.onInit(e -> initOperations());
		operationsBlock.onShow(e -> refreshOperations());
	}

	@Override
	public void refresh() {
		super.refresh();
		if (!LanguageHelper.canEditOperations(language, lastRelease())) tabSelector.hideOption("operationsOption");
		tabSelector.select("propertiesOption");
	}

	private void initProperties() {
		logoField.onChange(e -> logo = e.value());
	}

	private void refreshProperties() {
		logoField.value(LanguageHelper.logo(language, box()));
		nameField.value(language.name());
		descriptionField.value(language.description());
		accessTypeField.state(language.isPrivate() ? ToggleEvent.State.On : ToggleEvent.State.Off);
		dockerImageUrlField.value(language.dockerImageUrl());
	}

	private void initOperations() {
		addOperation.onExecute(e -> addOperation());
	}

	private void refreshOperations() {
		operations.clear();
		addOperation.readonly(!LanguageHelper.canEditOperations(language, lastRelease()));
		operationMap.entrySet().forEach(o -> fill(o, operations.add()));
	}

	private Release lastRelease() {
		return box().languageManager().lastRelease(language);
	}

	private void fill(Map.Entry<String, Operation> operationEntry, OperationEditor display) {
		display.operation(operationEntry.getValue());
		display.onChange(o -> operationMap.put(operationEntry.getKey(), o));
		display.onRemove(o -> removeOperation(operationEntry.getKey()));
		display.refresh();
	}

	private void addOperation() {
		operationMap.put(UUID.randomUUID().toString(), new Operation("", null, ""));
		refreshOperations();
	}

	private void removeOperation(String id) {
		operationMap.remove(id);
		refreshOperations();
	}

}