import type * as vscode from 'vscode';
import * as monaco from 'monaco-editor';
import { EditorAppBase, EditorAppConfigBase } from './editorAppBase.js';
import { IExtensionManifest } from 'vscode/extensions';
import { Logger } from 'monaco-languageclient/tools';
import { UserConfig } from './userConfig.js';
export type ExtensionConfig = {
    config: IExtensionManifest | object;
    filesOrContents?: Map<string, string | URL>;
};
export type UserConfiguration = {
    json?: string;
};
export type EditorAppConfigExtended = EditorAppConfigBase & {
    $type: 'extended';
    extensions?: ExtensionConfig[];
    userConfiguration?: UserConfiguration;
};
export type RegisterExtensionResult = {
    id: string;
    dispose(): Promise<void>;
    whenReady(): Promise<void>;
};
interface RegisterLocalExtensionResult extends RegisterExtensionResult {
    registerFileUrl: (path: string, url: string) => monaco.IDisposable;
}
export type RegisterLocalProcessExtensionResult = RegisterLocalExtensionResult & {
    getApi(): Promise<typeof vscode>;
    setAsDefaultApi(): Promise<void>;
};
/**
 * The vscode-apo monaco-editor app uses vscode user and extension configuration for monaco-editor.
 */
export declare class EditorAppExtended extends EditorAppBase {
    private config;
    private extensionRegisterResults;
    private subscriptions;
    constructor(id: string, userConfig: UserConfig, logger?: Logger);
    getConfig(): EditorAppConfigExtended;
    getExtensionRegisterResult(extensionName: string): RegisterExtensionResult | RegisterLocalProcessExtensionResult | undefined;
    specifyServices(): Promise<monaco.editor.IEditorOverrideServices>;
    init(): Promise<void>;
    disposeApp(): void;
    isAppConfigDifferent(orgConfig: EditorAppConfigExtended, config: EditorAppConfigExtended, includeModelData: boolean): boolean;
}
export {};
//# sourceMappingURL=editorAppExtended.d.ts.map