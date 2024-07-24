import * as monaco from 'monaco-editor';
import { OpenEditor } from '@codingame/monaco-vscode-editor-service-override';
import { InitializeServiceConfig } from 'monaco-languageclient/vscode/services';
import { Logger } from 'monaco-languageclient/tools';
export type VscodeServicesConfig = {
    serviceConfig?: InitializeServiceConfig;
    specificServices?: monaco.editor.IEditorOverrideServices;
    logger?: Logger;
};
/**
 * Child classes are allow to override the services configuration implementation.
 */
export declare const configureServices: (config: VscodeServicesConfig) => Promise<InitializeServiceConfig>;
export declare const useOpenEditorStub: OpenEditor;
export declare const checkServiceConsistency: (userServices?: monaco.editor.IEditorOverrideServices) => boolean;
//# sourceMappingURL=services.d.ts.map