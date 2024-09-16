package io.intino.ime.box.util;

import io.intino.alexandria.Scale;
import io.intino.alexandria.Timetag;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.models.ModelContainer;
import io.intino.ime.model.Language;
import io.intino.ime.model.Model;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ModelHelper {

	public static String proposeName() {
		String uuid = UUID.randomUUID().toString();
		return uuid.substring(uuid.lastIndexOf("-")+1) + new Timetag(Instant.now(), Scale.Month).value();
	}

	public static boolean validName(String name) {
		return name.matches("^[a-zA-Z0-9_-]*$");
	}

	public static boolean nameInUse(String value, ImeBox box) {
		return box.modelManager().exists(value);
	}

	public static Language language(Model model, ImeBox box) {
		return box.languageManager().get(model.language());
	}

}
