/******************************************************************************
 * This file was generated by langium-cli 3.0.3.
 * DO NOT EDIT MANUALLY!
 ******************************************************************************/
import { StatemachineAstReflection } from './ast.js';
import { StatemachineGrammar } from './grammar.js';
export const StatemachineLanguageMetaData = {
    languageId: 'statemachine',
    fileExtensions: ['.statemachine'],
    caseInsensitive: false
};
export const StatemachineGeneratedSharedModule = {
    AstReflection: () => new StatemachineAstReflection()
};
export const StatemachineGeneratedModule = {
    Grammar: () => StatemachineGrammar(),
    LanguageMetaData: () => StatemachineLanguageMetaData,
    parser: {}
};
//# sourceMappingURL=module.js.map