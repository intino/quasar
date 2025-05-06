package io.quassar.builder;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.intino.tara.builder.core.CompilationUnit;
import io.intino.tara.builder.core.errorcollection.CompilationFailedException;
import io.intino.tara.builder.core.operation.model.ModelOperation;
import io.intino.tara.model.Facet;
import io.intino.tara.model.Mogram;
import io.intino.tara.model.Property;
import io.intino.tara.model.PropertyDescription;
import io.intino.tara.processors.model.Model;
import io.intino.tara.processors.model.MogramImpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Logger;

public class GenerateGraphOperation extends ModelOperation {
	private static final Logger LOG = Logger.getGlobal();

	public GenerateGraphOperation(CompilationUnit unit) {
		super(unit);
	}

	@Override
	public void call(Model model) throws CompilationFailedException {
		Gson gson = new GsonBuilder().registerTypeAdapter(Model.class, modelTypeAdapter()).registerTypeAdapter(MogramImpl.class, mogramTypeAdapter()).create();
		String json = gson.toJson(model);
		try {
			File out = unit.configuration().outDirectory();
			out.mkdirs();
			File build = new File(out.getParentFile(), "build/");
			build.mkdirs();
			Files.writeString(new File(build, "graph.json").toPath(), json);
		} catch (IOException e) {
			LOG.severe(e.getMessage());
		}
	}

	private static TypeAdapter<MogramImpl> mogramTypeAdapter() {
		return new TypeAdapter<>() {
			private final Gson gson = new Gson();

			@Override
			public void write(JsonWriter jsonWriter, MogramImpl mogram) {
				gson.toJson(jsonObject(mogram), jsonWriter);
			}

			@Override
			public MogramImpl read(JsonReader jsonReader) {
				return null;
			}
		};
	}

	private static JsonObject jsonObject(Mogram mogram) {
		JsonObject object = new JsonObject();
		object.add("name", new JsonPrimitive(mogram.name()));
		object.add("types", mogram.types().stream().collect(JsonArray::new, (array, item) -> array.add(new JsonPrimitive(item)), JsonArray::addAll));
		if (mogram.parent() != null) object.add("parent", new JsonPrimitive(mogram.parent().reference()));
		object.add("components", mogram.components().stream().collect(JsonArray::new, (array, item) -> array.add(jsonObject(item)), JsonArray::addAll));
		object.add("propertyDefinitions", mogram.properties().stream().collect(JsonArray::new, (array, item) -> array.add(jsonObject(item)), JsonArray::addAll));
		object.add("propertyDescriptions", mogram.parameters().stream().collect(JsonArray::new, (array, item) -> array.add(jsonObject(item)), JsonArray::addAll));
		object.add("facets", mogram.facets().stream().collect(JsonArray::new, (array, item) -> array.add(jsonObject(item)), JsonArray::addAll));
		object.add("appliedFacets", mogram.appliedFacets().stream().collect(JsonArray::new, (array, item) -> array.add(jsonObject(item)), JsonArray::addAll));
		object.add("annotations", mogram.annotations().stream().collect(JsonArray::new, (array, item) -> array.add(new JsonPrimitive(item.name())), JsonArray::addAll));
		if (mogram.facetPrescription() != null)
			object.add("facetPrescription", new JsonPrimitive(mogram.facetPrescription().get().qualifiedName()));
		return object;
	}

	private static JsonObject jsonObject(Facet facet) {
		JsonObject object = new JsonObject();
		object.add("type", new JsonPrimitive(facet.fullType()));
		object.add("propertyDescriptions", facet.parameters().stream().collect(JsonArray::new, (array, item) -> array.add(jsonObject(item)), JsonArray::addAll));
		return object;
	}

	private static JsonObject jsonObject(PropertyDescription prop) {
		JsonObject object = new JsonObject();
		object.add("type", new JsonPrimitive(prop.type().name()));
		object.add("name", new JsonPrimitive(prop.name()));
		if (prop.metric() != null) object.add("metric", new JsonPrimitive(prop.metric()));
		object.add("values", prop.values().stream().collect(JsonArray::new, (array, item) -> array.add(new JsonPrimitive(item.toString())), JsonArray::addAll));
		return object;
	}

	private static JsonObject jsonObject(Property prop) {
		JsonObject object = new JsonObject();
		object.add("type", new JsonPrimitive(prop.type().name()));
		object.add("name", new JsonPrimitive(prop.name()));
		if (prop.metric() != null) object.add("metric", new JsonPrimitive(prop.metric()));
		object.add("defaultValues", prop.values().stream().collect(JsonArray::new, (array, item) -> array.add(new JsonPrimitive(item.toString())), JsonArray::addAll));
		object.add("annotations", prop.annotations().stream().collect(JsonArray::new, (array, item) -> array.add(new JsonPrimitive(item.name())), JsonArray::addAll));
		return object;
	}

	private static TypeAdapter<Model> modelTypeAdapter() {
		return new TypeAdapter<>() {
			private final Gson gson = new Gson();

			@Override
			public void write(JsonWriter jsonWriter, Model model) {
				JsonObject object = new JsonObject();
				object.add("name", new JsonPrimitive(model.name()));
				object.add("uri", new JsonPrimitive(model.source().getPath()));
				object.add("components", model.mograms().stream().collect(JsonArray::new, (array, item) -> array.add(jsonObject(item)), JsonArray::addAll));
				gson.toJson(object, jsonWriter);
			}

			@Override
			public Model read(JsonReader jsonReader) {
				return null;
			}
		};
	}
}
