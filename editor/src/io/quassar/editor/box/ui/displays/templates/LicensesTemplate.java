package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.UserMessage;
import io.intino.alexandria.ui.displays.components.Grid;
import io.intino.alexandria.ui.displays.events.collection.CellClickEvent;
import io.intino.alexandria.ui.displays.events.collection.CellClickListener;
import io.intino.alexandria.ui.displays.events.collection.SortColumnEvent;
import io.intino.alexandria.ui.model.datasource.grid.GridColumn;
import io.intino.alexandria.ui.model.datasource.grid.GridItem;
import io.intino.alexandria.ui.model.datasource.grid.GridValue;
import io.quassar.editor.box.commands.CollectionCommands;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.ui.datasources.LicensesDatasource;
import io.quassar.editor.box.util.PermissionsHelper;
import io.quassar.editor.box.util.UserHelper;
import io.quassar.editor.model.Collection;
import io.quassar.editor.model.License;

import java.util.Collections;
import java.util.List;

public class LicensesTemplate extends AbstractLicensesTemplate<EditorBox> {
	private Collection collection;

	public LicensesTemplate(EditorBox box) {
		super(box);
	}

	public void collection(Collection collection) {
		this.collection = collection;
	}

	@Override
	public void init() {
		super.init();
		licensesGrid.onClickCell(this::copyLicenseToClipboard);
		licensesGrid.itemResolver(itemResolver());
		licensesGrid.onSortColumn(this::sort);
		licensesGrid.column("duration").formatter(value -> String.valueOf(value.asNumber().intValue()));
		addLicensesDialog.onOpen(e -> refreshLicensesDialog());
		addLicenses.onExecute(e -> addLicenses());
		countField.onChange(e -> addLicenses.readonly(!hasCredit()));
		durationField.onChange(e -> addLicenses.readonly(!hasCredit()));
	}

	@Override
	public void refresh() {
		super.refresh();
		licensesGrid.source(new LicensesDatasource(box(), session(), collection));
		addLicensesTrigger.visible(PermissionsHelper.canAddLicenses(collection, session(), box()));
		addLicensesTrigger.readonly(!hasCredit());
	}

	private Grid.ItemResolver<License> itemResolver() {
		return new Grid.ItemResolver<>() {
			@Override
			public GridItem build(License license) {
				GridItem result = new GridItem();
				result.add(license.code());
				result.add(license.createDate());
				result.add(license.duration());
				result.add(translate(license.status().name()));
				result.add(license.user());
				result.add(license.assignDate());
				result.add(license.expireDate());
				return result;
			}

			@Override
			public String address(GridColumn<License> column, License license) {
				return "";
			}
		};
	}

	private void refreshLicensesDialog() {
		int licenseTime = UserHelper.licenseTime(session(), box());
		message.value(translate("You have %s months of license time available").formatted(licenseTime));
		hint.value(translate("This can be used as one %s-month license or split into multiple shorter licenses (e.g., %s one-month licenses).").formatted(licenseTime, licenseTime));
	}

	private void addLicenses() {
		if (countField.value() == null || countField.value() <= 0) {
			notifyUser(translate("The number of licenses must be greater than zero"), UserMessage.Type.Error);
			return;
		}
		if (durationField.value() == null || durationField.value() <= 0) {
			notifyUser(translate("The duration in months for each license must be greater than zero"), UserMessage.Type.Error);
			return;
		}
		if (!hasCredit()) {
			notifyUser(translate("You don’t have enough monthly credit to add these licenses"), UserMessage.Type.Error);
			return;
		}
		addLicensesDialog.close();
		box().commands(CollectionCommands.class).addLicenses(collection, countField.value(), durationField.value(), username());
		refresh();
	}

	private boolean hasCredit() {
		return PermissionsHelper.hasCredit(Double.valueOf(countField.value()*durationField.value()).intValue(), session(), box());
	}

	private void copyLicenseToClipboard(CellClickEvent event) {
		License license = event.item();
		copyTrigger.text(license.code());
		copyTrigger.launch();
	}

	private void sort(SortColumnEvent event) {
		GridColumn<License> column = event.column();
		SortColumnEvent.Mode mode = event.mode();
		licensesGrid.sortings(mode != SortColumnEvent.Mode.None ? List.of(column.name() + "=" + (mode == SortColumnEvent.Mode.Ascendant ? "A" : "D")) : Collections.emptyList());
	}

}