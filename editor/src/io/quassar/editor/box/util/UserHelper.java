package io.quassar.editor.box.util;

import io.intino.alexandria.ui.services.push.UISession;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.models.File;
import io.quassar.editor.box.models.ModelContainer;
import io.quassar.editor.box.ui.types.VersionType;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;
import io.quassar.editor.model.ModelRelease;
import io.quassar.editor.model.User;
import org.eclipse.jgit.diff.Edit;

import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

public class UserHelper {

	public static int licenseTime(String username, EditorBox box) {
		return licenseTime(user(username, box), box);
	}

	public static int licenseTime(UISession session, EditorBox box) {
		return licenseTime(user(session, box), box);
	}

	public static int licenseTime(User user, EditorBox box) {
		return user != null && user.licenseTime() != User.DefaultLicenseTime ? user.licenseTime() : Integer.parseInt(box.configuration().newUserLicenseTime());
	}

	public static User user(UISession session, EditorBox box) {
		return user(session.user() != null ? session.user().username() : null, box);
	}

	public static User user(String username, EditorBox box) {
		return username != null ? box.userManager().get(username) : null;
	}

}
