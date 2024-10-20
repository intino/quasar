package io.intino.builderservice.konos.actions;

import io.intino.alexandria.exceptions.AlexandriaException;
import io.intino.alexandria.exceptions.BadRequest;
import io.intino.alexandria.logger.Logger;
import io.intino.builderservice.konos.BuilderServiceBox;
import io.intino.builderservice.konos.schemas.BuilderInfo;
import io.intino.builderservice.konos.utils.DockerManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GetBuildersAction implements io.intino.alexandria.rest.RequestErrorHandler {
	public String imageURL;
	public BuilderServiceBox box;
	public io.intino.alexandria.http.spark.SparkContext context;

	public List<io.intino.builderservice.konos.schemas.BuilderInfo> execute() {
		Collection<BuilderInfo> all = box.builderStore().all();
		if (imageURL == null) return new ArrayList<>(all);
		List<BuilderInfo> list = all.stream()
				.filter(b -> b.imageURL().equalsIgnoreCase(imageURL))
				.toList();
		if (!list.isEmpty()) return new ArrayList<>(all);
		return findBuilder();
	}

	private List<BuilderInfo> findBuilder() {
		try {
			DockerManager.download(imageURL);
			BuilderInfo builderInfo = DockerManager.builderInfo(imageURL);
			return builderInfo == null ? List.of() : List.of(builderInfo);
		} catch (InterruptedException | IOException e) {
			Logger.error(e);
			return List.of();
		}
	}

	public void onMalformedRequest(Throwable e) throws AlexandriaException {
		throw new BadRequest("Malformed request");
	}
}