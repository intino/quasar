package io.intino.ls.codeinsight.completion;

import io.intino.tara.Language;
import io.intino.tara.language.semantics.Constraint;
import io.intino.tara.model.*;
import io.intino.tara.model.rules.Size;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.intino.ls.codeinsight.completion.CompletionService.TARA_FAKE_TOKEN;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class CompletionUtils {
	private final CompletionContext context;
	private final Language language;

	public CompletionUtils(CompletionContext context) {
		this.context = context;
		this.language = context.language();
	}

	List<CompletionItem> collectAllowedFacets(Mogram mogram) {
		if (language == null) return List.of();
		String type = mogram.types().get(0);
		if (type == null) return List.of();
		List<Constraint> constraints = context.language().constraints(type);
		if (constraints == null) return List.of();
		return buildCompletionForFacets(constraints, mogram);
	}

	List<CompletionItem> collectAllowedComponents(ElementContainer container) {
		String type = container instanceof Mogram m ? m.types().get(0) : "";
		if (type == null) return List.of();
		final List<Constraint> mogramConstraints = language.constraints(type);
		if (mogramConstraints == null) return List.of();
		List<Constraint> constraints = new ArrayList<>(mogramConstraints);
		if (container instanceof Mogram m)
			constraints.addAll(constraintsOf(facetConstraints(mogramConstraints, m.appliedFacets())));
		List<Constraint.Component> components = new ArrayList<>(componentConstraints(constraints).stream().filter(c -> isSizeAccepted(c, container)).toList());
		if (container instanceof Mogram m && m.level().equals(Level.M2)) {
			components.addAll(constraints.stream()
					.filter(c -> c instanceof Constraint.Facet && hasFacet(container, (Constraint.Facet) c))
					.flatMap(c -> ((Constraint.Facet) c).constraints().stream().filter(cs -> cs instanceof Constraint.Component).map(cst -> (Constraint.Component) cst))
					.filter(c -> isSizeAccepted(c, container)).toList());
		}
		if (components.isEmpty()) return List.of();
		return createCompletionForComponents(components);
	}

	private static List<Constraint.Component> componentConstraints(List<Constraint> constraints) {
		return constraints.stream().filter(c -> c instanceof Constraint.Component).map(c -> (Constraint.Component) c).collect(toList());
	}

	private List<Constraint> constraintsOf(List<Constraint> constraints) {
		return constraints.stream()
				.flatMap(constraint -> ((Constraint.Facet) constraint).constraints().stream())
				.collect(Collectors.toList());
	}

	List<CompletionItem> collectBodyParameters(Mogram mogram) {
		if (language == null || mogram == null) return null;
		return buildCompletionForParameters(parameterConstraintsOf(mogram), mogram.parameters());
	}

	List<CompletionItem> collectSignatureParameters(Mogram mogram) {
		if (language == null || mogram == null) return null;
		List<Constraint.Property> constraints = isInFacet() ? facetParameterConstraintsOf(mogram) : parameterConstraintsOf(mogram);
		return buildCompletionForParameters(constraints, removeFakes(mogram));
	}

	private static List<PropertyDescription> removeFakes(Mogram mogram) {
		return mogram.parameters().stream()
				.filter(p -> !p.values().isEmpty() && !p.values().get(0).toString().equals(TARA_FAKE_TOKEN)).toList();
	}

	private boolean isInFacet() {
		//TODO
		return false;
	}

	private List<Constraint.Property> parameterConstraintsOf(Mogram mogram) {
		final List<Constraint> constraints = language.constraints(mogram.types().get(0));
		if (constraints == null) return emptyList();
		List<Constraint.Property> parameters = new ArrayList<>();
		for (Constraint constraint : constraints)
			if (constraint instanceof Constraint.Property) parameters.add((Constraint.Property) constraint);
			else if (constraint instanceof Constraint.Facet && hasFacet(mogram, (Constraint.Facet) constraint))
				parameters.addAll(((Constraint.Facet) constraint).constraints().stream().filter(c -> c instanceof Constraint.Property).map(c -> (Constraint.Property) c).toList());
		return parameters;
	}

	private List<Constraint.Property> facetParameterConstraintsOf(Mogram mogram) {
		final List<Constraint> cs = language.constraints(mogram.types().get(0));
		if (cs == null) return emptyList();
		return cs.stream()
				.filter(constraint -> constraint instanceof Constraint.Facet && hasFacet(mogram, (Constraint.Facet) constraint))
				.flatMap(constraint -> ((Constraint.Facet) constraint).constraints().stream().filter(c -> c instanceof Constraint.Property).map(c -> (Constraint.Property) c))
				.collect(toList());
	}


	private boolean isSizeAccepted(Constraint.Component component, ElementContainer container) {
		long count = container.components().stream()
				.filter(c -> component.type().equals(c.types().get(0)) || shortType(component.type()).equals(c.types().get(0)))
				.count();
		return component.rules().stream()
				.filter(r -> r instanceof Size)
				.allMatch(r -> ((Size) r).max() > count);
	}

	private List<Constraint> facetConstraints(List<Constraint> mogramConstraints, List<Facet> facets) {
		List<String> facetTypes = facets.stream().map(Facet::fullType).toList();
		List<Constraint> list = new ArrayList<>();
		if (mogramConstraints == null) return list;
		for (Constraint constraint : mogramConstraints)
			if (constraint instanceof Constraint.Facet f && facetTypes.contains(f.type()))
				list.add(constraint);
		return list;
	}

	private List<CompletionItem> createCompletionForComponents(List<Constraint.Component> constraints) {
		Set<String> added = new HashSet<>();
		List<CompletionItem> items = new ArrayList<>();
		for (Constraint.Component constraint : constraints)
			if (constraint instanceof Constraint.OneOf)
				items.addAll(createCompletionItem((Constraint.OneOf) constraint));
			else items.add(createCompletionItem(constraint));
		return items.stream().filter(c -> added.add(c.getInsertText())).collect(toList());
	}

	private List<CompletionItem> buildCompletionForFacets(List<Constraint> constraints, Mogram mogram) {
		Set<String> added = new HashSet<>();
		return constraints.stream().
				filter(c -> c instanceof Constraint.Facet && !hasFacet(mogram, (Constraint.Facet) c)).
				map(c -> createCompletionItem((Constraint.Facet) c)).filter(l -> added.add(l.getInsertText())). //TODO pasar el container
						collect(toList());
	}

	private List<CompletionItem> buildCompletionForParameters(List<Constraint.Property> constraints, List<PropertyDescription> props) {
		Set<String> added = new HashSet<>();
		return constraints.stream()
				.filter(c -> c != null && !contains(props, c.name()))
				.map(this::createCompletionItem)
				.filter(l -> added.add(l.getInsertText())).
				collect(toList());
	}

	private boolean hasFacet(ElementContainer container, Constraint.Facet f) {
		return (container instanceof Mogram m) && m.appliedFacets().stream().anyMatch(facet -> Objects.equals(facet.type(), f.type()) || Objects.equals(facet.fullType(), f.type()));
	}

	private List<CompletionItem> createCompletionItem(Constraint.OneOf constraint) {
		return constraint.components().stream().map(this::createCompletionItem).collect(toList());
	}

	private String lastTypeOf(String fullType) {
		String[] splittedType = fullType.split(":");
		String type = splittedType[splittedType.length - 1];
		return type.contains(".") ? type.substring(type.lastIndexOf('.') + 1) : type;
	}

	private CompletionItem createCompletionItem(Constraint.Component constraint) {
		CompletionItem item = new CompletionItem(constraint.type());
		item.setKind(CompletionItemKind.Class);
		item.setInsertText(lastTypeOf(constraint.type()) + " ");
		return item;
	}

	private CompletionItem createCompletionItem(Constraint.Facet facet) {
		String simpleType = simpleType(facet.type());
		CompletionItem item = new CompletionItem("facet " + simpleType);
		item.setKind(CompletionItemKind.Interface);
		item.setInsertText(simpleType);
		return item;
	}

	private String simpleType(String type) {
		return type.contains(".") ? type.substring(type.lastIndexOf('.') + 1) : type;
	}

	private boolean contains(List<PropertyDescription> parameters, String name) {
		return parameters.stream().anyMatch(p -> name.equals(p.name()));
	}

	private CompletionItem createCompletionItem(Constraint.Property c) {
		CompletionItem item = new CompletionItem(c.type() + " " + c.name());
		item.setInsertText(c.name());
		item.setKind(CompletionItemKind.Constructor);
		return item;
	}

	private static String shortType(String type) {
		final String[] s = type.split("\\.");
		return s[s.length - 1];
	}
}