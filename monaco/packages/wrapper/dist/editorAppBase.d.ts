import * as monaco from 'monaco-editor';
import { ITextFileEditorModel } from 'vscode/monaco';
import { IReference } from '@codingame/monaco-vscode-editor-service-override';
import { Logger } from 'monaco-languageclient/tools';
export type CodeContent = {
    text: string;
    enforceLanguageId?: string;
};
export type CodePlusUri = CodeContent & {
    uri: string;
};
export type CodePlusFileExt = CodeContent & {
    fileExt: string;
};
export type CodeResources = {
    main?: CodePlusUri | CodePlusFileExt;
    original?: CodePlusUri | CodePlusFileExt;
};
export type EditorAppType = 'extended' | 'classic';
export type EditorAppConfigBase = {
    $type: EditorAppType;
    codeResources?: CodeResources;
    useDiffEditor?: boolean;
    domReadOnly?: boolean;
    readOnly?: boolean;
    awaitExtensionReadiness?: Array<() => Promise<void>>;
    overrideAutomaticLayout?: boolean;
    editorOptions?: monaco.editor.IStandaloneEditorConstructionOptions;
    diffEditorOptions?: monaco.editor.IStandaloneDiffEditorConstructionOptions;
};
export type ModelRefs = {
    modelRef?: IReference<ITextFileEditorModel>;
    modelRefOriginal?: IReference<ITextFileEditorModel>;
};
export type TextModels = {
    text?: monaco.editor.ITextModel;
    textOriginal?: monaco.editor.ITextModel;
};
/**
 * This is the base class for both Monaco Ediotor Apps:
 * - EditorAppClassic
 * - EditorAppExtended
 *
 * It provides the generic functionality for both implementations.
 */
export declare abstract class EditorAppBase {
    private id;
    protected logger: Logger | undefined;
    private editor;
    private diffEditor;
    private modelRef;
    private modelRefOriginal;
    constructor(id: string, logger?: Logger);
    protected buildConfig(userAppConfig: EditorAppConfigBase): EditorAppConfigBase;
    haveEditor(): boolean;
    getEditor(): monaco.editor.IStandaloneCodeEditor | undefined;
    getDiffEditor(): monaco.editor.IStandaloneDiffEditor | undefined;
    createEditors(container: HTMLElement): Promise<void>;
    protected disposeEditors(): void;
    getTextModels(): TextModels;
    getModelRefs(): ModelRefs;
    updateCodeResources(codeResources?: CodeResources): Promise<void>;
    buildModelRefs(codeResources?: CodeResources): Promise<ModelRefs>;
    private buildModelRef;
    updateEditorModels(modelRefs: ModelRefs): Promise<void>;
    private checkEnforceLanguageId;
    private updateCodeResourcesConfig;
    updateLayout(): void;
    awaitReadiness(awaitExtensionReadiness?: Array<() => Promise<void>>): Promise<void | void[]>;
    updateMonacoEditorOptions(options: monaco.editor.IEditorOptions & monaco.editor.IGlobalEditorOptions): void;
    updateUserConfiguration(json?: string): Promise<void>;
    getUserConfiguration(): Promise<string>;
    abstract init(): Promise<void>;
    abstract specifyServices(): Promise<monaco.editor.IEditorOverrideServices>;
    abstract getConfig(): EditorAppConfigBase;
    abstract disposeApp(): void;
    abstract isAppConfigDifferent(orgConfig: EditorAppConfigBase, config: EditorAppConfigBase, includeModelData: boolean): boolean;
}
//# sourceMappingURL=editorAppBase.d.ts.map