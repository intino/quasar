import { IConnectionProvider } from 'monaco-languageclient';
import { UserConfig } from 'monaco-editor-wrapper';
export declare const createLangiumGlobalConfig: (params: {
    languageServerId: string;
    useLanguageClient: boolean;
    text?: string;
    worker?: Worker;
    messagePort?: MessagePort;
    connectionProvider?: IConnectionProvider;
}) => Promise<UserConfig>;
//# sourceMappingURL=wrapperStatemachineConfig.d.ts.map