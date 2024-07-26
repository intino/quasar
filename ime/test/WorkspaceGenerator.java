import io.intino.alexandria.logger.Logger;

import java.io.File;
import java.nio.file.Files;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class WorkspaceGenerator {
	private static final File WorkspacesDir = new File("/Users/mcaballero/Proyectos/quasar/temp/workspaces");

	private static List<String> WorkspaceLabels = List.of(
			"Feedforward Neural Network (FNN)",
			"Multilayer Perceptron",
			"Radial Basis Function Neural Network",
			"Convolutional Neural Network (CNN)",
			"Recurrent Neural Network (RNN)",
			"Long Short-Term Memory (LSTM)",
			"Gated Recurrent Units",
			"Autoencoders",
			"Variational Autoencoder",
			"Generative Adversarial Networks",
			"Self Organizing Maps",
			"Neural Architecture Search",
			"Siamese Networks",
			"Transformers",
			"Modular Neural Networks",
			"Sequence-to-Sequence Models",
			"Cascade-Correlation Neural Networks",
			"Time delay Neural Networks",
			"Deep Belief Networks",
			"Liquid State Machines",
			"Hopfield Networks",
			"Echo State Networks",
			"Bidirectional Recurrent Neural Networks",
			"Restricted Boltzmann Machines",
			"Extreme Learning Machines",
			"Neural Turing Machines",
			"Capsule Networks",
			"Spike Neural Networks",
			"Probabilistic Neural Networks",
			"Neuro-Fuzzy Networks",
			"Deep Residual Networks"
	);

	private static List<String> WorkspaceAuthors = List.of(
		"jhernandez#Jose Juan Hernández",
		"mcaballero#Mario Caballero",
		"jevora#Jose Évora",
		"oroncal#Octavio Roncal",
		"cherrera#Cristian Herrera",
		"jhernandezgalvez#Jose Juan Hernández Galvez"
	);

	private static int WorkspaceIndexOffset = 4;

	public static void main(String[] args) {
		int index = WorkspaceIndexOffset;
		for (String label : WorkspaceLabels) {
			createWorkspace(index, label, randomAuthor());
			index++;
		}
	}

	private static void createWorkspace(int index, String label, String author) {
		try {
			String name = "w" + index;
			File parent = new File(WorkspacesDir, name);
			parent.mkdirs();
			File destiny = new File(parent, "definition.json");
			Files.writeString(destiny.toPath(), definition(name, label, author));
		} catch (Exception e) {
			Logger.error(e);
		}
	}

	private static final String WorkspaceDefinition = "{\n" +
			"\t\"name\" : \"%s\",\n" +
			"\t\"title\": \"%s\",\n" +
			"\t\"owner\": { \"name\": \"%s\", \"fullName\": \"%s\" },\n" +
			"\t\"lastModifyDate\": %d,\n" +
			"\t\"dsl\": \"flogo:1.0.0-SNAPSHOT\",\n" +
			"\t\"executionsCount\": %d\n" +
			"}";
	private static String definition(String name, String label, String author) {
		Instant instant = Instant.now().plus(random(1, 365), ChronoUnit.DAYS);
		long executionsCount = random(0, 10000000);
		return String.format(WorkspaceDefinition, name, label, author.split("#")[0], author.split("#")[1], instant.toEpochMilli(), executionsCount);
	}

	private static String randomAuthor() {
		int random = random(0, WorkspaceAuthors.size() - 1);
		return WorkspaceAuthors.get(random);
	}

	private static int random(int min, int max) {
		return new Random().nextInt(max - min + 1) + min;
	}
}
