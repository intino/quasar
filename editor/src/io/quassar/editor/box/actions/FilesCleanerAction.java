package io.quassar.editor.box.actions;

import io.intino.alexandria.Scale;
import io.intino.alexandria.Timetag;
import io.intino.alexandria.logger.Logger;
import io.quassar.editor.box.EditorBox;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static java.util.Collections.emptyList;

public class FilesCleanerAction {
	public EditorBox box;

	public void execute() {
		temporalDirectories().forEach(this::clean);
	}

	private List<File> temporalDirectories() {
		File[] files = box.archetype().tmp().root().listFiles();
		if (files == null) return emptyList();
		return Arrays.stream(files).filter(File::isDirectory).toList();
	}

	private void clean(File root) {
		File[] files = root.listFiles();
		if (files == null) return;
		Arrays.stream(files).filter(this::isOld).forEach(this::delete);
		Logger.info("Cleaned " + root.getAbsolutePath() + " files before " + borderDay());
	}

	private boolean isOld(File file) {
		try {
			FileTime creationTime = (FileTime) Files.getAttribute(file.toPath(), "creationTime");
			Timetag date = Timetag.of(creationTime.toInstant(), Scale.Day);
			return date.isBefore(borderDay());
		} catch (IOException e) {
			return false;
		}
	}

	private Timetag borderDay() {
		DayOfWeek day = WeekFields.of(Locale.getDefault()).getFirstDayOfWeek();
		LocalDate date = LocalDate.now().with(TemporalAdjusters.previousOrSame(day)).minusDays(1);
		return Timetag.of(date, Scale.Day);
	}

	private void delete(File file) {
		try {
			if (file.isDirectory()) FileUtils.deleteDirectory(file);
			else file.delete();
		} catch (IOException e) {
			Logger.error(e);
		}
	}

}