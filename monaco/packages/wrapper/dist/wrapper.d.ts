import * as monaco from 'monaco-editor';
import { MonacoLanguageClient } from 'monaco-languageclient';
import { EditorAppExtended } from './editorAppExtended.js';
import { EditorAppClassic } from './editorAppClassic.js';
import { CodeResources, ModelRefs, TextModels } from './editorAppBase.js';
import { LanguageClientWrapper } from './languageClientWrapper.js';
import { UserConfig } from './userConfig.js';
/**
 * This class is responsible for the overall ochestration.
 * It inits, start and disposes the editor apps and the language client (if configured) and provides
 * access to all required components.
 */
export declare class MonacoEditorLanguageClientWrapper {
    private id;
    private editorApp;
    private languageClientWrapper?;
    private logger;
    private initDone;
    /**
     * Perform an isolated initialization of the user services and the languageclient wrapper (if used).
     */
    init(userConfig: UserConfig): Promise<void>;
    /**
     * Performs a full user configuration and the languageclient wrapper (if used) init and then start the application.
     */
    initAndStart(userConfig: UserConfig, htmlElement: HTMLElement | null): Promise<void>;
    /**
     * Does not perform any user configuration or other application init and just starts the application.
     */
    start(htmlElement: HTMLElement | null): Promise<void>;
    isInitDone(): boolean;
    isStarted(): boolean;
    haveLanguageClient(): boolean;
    getMonacoEditorApp(): EditorAppExtended | EditorAppClassic | undefined;
    getEditor(): monaco.editor.IStandaloneCodeEditor | undefined;
    getDiffEditor(): monaco.editor.IStandaloneDiffEditor | undefined;
    getLanguageClientWrapper(): LanguageClientWrapper | undefined;
    getLanguageClient(): MonacoLanguageClient | undefined;
    getTextModels(): TextModels | undefined;
    getModelRefs(): ModelRefs | undefined;
    getWorker(): Worker | undefined;
    updateCodeResources(codeResources?: CodeResources): Promise<void>;
    updateEditorModels(modelRefs: ModelRefs): Promise<void>;
    reportStatus(): string[];
    /**
     * Disposes all application and editor resources, plus the languageclient (if used).
     */
    dispose(): Promise<void>;
    updateLayout(): void;
    isReInitRequired(userConfig: UserConfig, previousUserConfig: UserConfig): boolean;
}
//# sourceMappingURL=wrapper.d.ts.map