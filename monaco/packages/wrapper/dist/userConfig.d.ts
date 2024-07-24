import { InitializeServiceConfig } from 'monaco-languageclient/vscode/services';
import type { LoggerConfig } from 'monaco-languageclient/tools';
import { EditorAppConfigExtended } from './editorAppExtended.js';
import { EditorAppConfigClassic } from './editorAppClassic.js';
import { LanguageClientConfig } from './languageClientWrapper.js';
export type WrapperConfig = {
    serviceConfig?: InitializeServiceConfig;
    editorAppConfig: EditorAppConfigExtended | EditorAppConfigClassic;
};
export type UserConfig = {
    id?: string;
    loggerConfig?: LoggerConfig;
    wrapperConfig: WrapperConfig;
    languageClientConfig?: LanguageClientConfig;
};
//# sourceMappingURL=userConfig.d.ts.map