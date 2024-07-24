import * as vscode from 'vscode';
import { WebSocketConfigOptions, WebSocketConfigOptionsUrl } from './commonTypes.js';
import { CodePlusFileExt, CodePlusUri, CodeResources } from './editorAppBase.js';
export declare const createUrl: (config: WebSocketConfigOptions | WebSocketConfigOptionsUrl) => string;
export declare const verifyUrlOrCreateDataUrl: (input: string | URL) => string;
export declare const getEditorUri: (id: string, original: boolean, code: CodePlusUri | CodePlusFileExt, basePath?: string) => vscode.Uri;
export declare enum ModelUpdateType {
    NONE = 0,
    CODE = 1,
    MODEL = 2,
    CODE_AND_MODEL = 3
}
export declare const isCodeUpdateRequired: (codeResourcesPrevious?: CodeResources, codeResources?: CodeResources) => ModelUpdateType.NONE | ModelUpdateType.CODE;
export declare const evaluateCodeUpdate: (code?: CodePlusUri | CodePlusFileExt) => string | undefined;
export declare const isModelUpdateRequired: (codeResourcesPrevious?: CodeResources, codeResources?: CodeResources) => ModelUpdateType;
export declare const evaluateCodeModel: (code?: CodePlusUri | CodePlusFileExt) => string | undefined;
/**
 * The check for equality relies on JSON.stringify for instances of type Object.
 * Everything else is directly compared.
 * In this context, the check for equality is sufficient.
 */
export declare const isEqual: (obj1: unknown, obj2: unknown) => boolean;
//# sourceMappingURL=utils.d.ts.map