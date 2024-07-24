import * as monaco from 'monaco-editor';
import { Logger } from 'monaco-languageclient/tools';
import { EditorAppBase, EditorAppConfigBase } from './editorAppBase.js';
import { UserConfig } from './userConfig.js';
export type EditorAppConfigClassic = EditorAppConfigBase & {
    $type: 'classic';
    languageDef?: {
        languageExtensionConfig: monaco.languages.ILanguageExtensionPoint;
        monarchLanguage?: monaco.languages.IMonarchLanguage;
        theme?: {
            name: monaco.editor.BuiltinTheme | string;
            data: monaco.editor.IStandaloneThemeData;
        };
    };
};
/**
 * The classic monaco-editor app uses the classic monaco-editor configuration.
 */
export declare class EditorAppClassic extends EditorAppBase {
    private config;
    constructor(id: string, userConfig: UserConfig, logger?: Logger);
    getConfig(): EditorAppConfigClassic;
    specifyServices(): Promise<monaco.editor.IEditorOverrideServices>;
    init(): Promise<void>;
    disposeApp(): void;
    isAppConfigDifferent(orgConfig: EditorAppConfigClassic, config: EditorAppConfigClassic, includeModelData: boolean): boolean;
}
//# sourceMappingURL=editorAppClassic.d.ts.map