package io.intino.ls;

import org.eclipse.lsp4j.SemanticTokenModifiers;
import org.eclipse.lsp4j.SemanticTokenTypes;

import java.util.ArrayList;
import java.util.List;

public class IntinoSemanticTokens {
	static final List<String> tokenTypes = new ArrayList<>();
	static final List<String> tokenModifiers = new ArrayList<>();

	static {
		tokenTypes.add(SemanticTokenTypes.Class);
		tokenTypes.add(SemanticTokenTypes.Comment);
		tokenTypes.add(SemanticTokenTypes.Keyword);
		tokenTypes.add(SemanticTokenTypes.Type);
		tokenTypes.add(SemanticTokenTypes.Modifier);
		tokenTypes.add(SemanticTokenTypes.String);
		tokenTypes.add(SemanticTokenTypes.Number);
		tokenModifiers.add(SemanticTokenModifiers.Abstract);
		tokenModifiers.add(SemanticTokenModifiers.Documentation);
		tokenModifiers.add(SemanticTokenModifiers.Modification);
		tokenModifiers.add(SemanticTokenModifiers.Declaration);
	}
}
