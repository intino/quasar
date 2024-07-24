/* --------------------------------------------------------------------------------------------
 * Copyright (c) 2024 TypeFox and others.
 * Licensed under the MIT License. See LICENSE in the package root for license information.
 * ------------------------------------------------------------------------------------------ */
import * as vscode from 'vscode';
export const createUrl = (config) => {
    let buildUrl = '';
    if (config.url) {
        const options = config;
        if (!options.url.startsWith('ws://') && !options.url.startsWith('wss://')) {
            throw new Error(`This is not a proper websocket url: ${options.url}`);
        }
        buildUrl = options.url;
    }
    else {
        const options = config;
        const protocol = options.secured ? 'wss' : 'ws';
        buildUrl = `${protocol}://${options.host}`;
        if (options.port !== undefined) {
            if (options.port !== 80) {
                buildUrl += `:${options.port}`;
            }
        }
        if (options.path !== undefined) {
            buildUrl += `/${options.path}`;
        }
        if (options.extraParams) {
            const url = new URL(buildUrl);
            for (const [key, value] of Object.entries(options.extraParams)) {
                url.searchParams.set(key, value instanceof Array ? value.join(',') : value.toString());
            }
            buildUrl = url.toString();
        }
    }
    return buildUrl;
};
export const verifyUrlOrCreateDataUrl = (input) => {
    return (input instanceof URL) ? input.href : new URL(`data:text/plain;base64,${btoa(input)}`).href;
};
export const getEditorUri = (id, original, code, basePath) => {
    if (Object.hasOwn(code, 'uri')) {
        return vscode.Uri.parse(code.uri);
    }
    else {
        return vscode.Uri.parse(`${basePath ?? '/workspace'}/model${original ? 'Original' : ''}${id}.${code.fileExt}`);
    }
};
export var ModelUpdateType;
(function (ModelUpdateType) {
    ModelUpdateType[ModelUpdateType["NONE"] = 0] = "NONE";
    ModelUpdateType[ModelUpdateType["CODE"] = 1] = "CODE";
    ModelUpdateType[ModelUpdateType["MODEL"] = 2] = "MODEL";
    ModelUpdateType[ModelUpdateType["CODE_AND_MODEL"] = 3] = "CODE_AND_MODEL";
})(ModelUpdateType || (ModelUpdateType = {}));
export const isCodeUpdateRequired = (codeResourcesPrevious, codeResources) => {
    const a = evaluateCodeUpdate(codeResourcesPrevious?.main);
    const b = evaluateCodeUpdate(codeResources?.main);
    const c = evaluateCodeUpdate(codeResourcesPrevious?.original);
    const d = evaluateCodeUpdate(codeResources?.original);
    return a !== b || c !== d ? ModelUpdateType.CODE : ModelUpdateType.NONE;
};
export const evaluateCodeUpdate = (code) => {
    return code && Object.hasOwn(code, 'text') ? code.text : undefined;
};
export const isModelUpdateRequired = (codeResourcesPrevious, codeResources) => {
    const codeUpdateType = isCodeUpdateRequired(codeResourcesPrevious, codeResources);
    const codeChanged = codeUpdateType === ModelUpdateType.CODE;
    const a = evaluateCodeModel(codeResourcesPrevious?.main);
    const b = evaluateCodeModel(codeResources?.main);
    const c = evaluateCodeModel(codeResourcesPrevious?.original);
    const d = evaluateCodeModel(codeResources?.original);
    const modelChanged = a !== b || c !== d;
    return (modelChanged && codeChanged) ? ModelUpdateType.CODE_AND_MODEL : (modelChanged ? ModelUpdateType.MODEL : (codeChanged ? ModelUpdateType.CODE : ModelUpdateType.NONE));
};
export const evaluateCodeModel = (code) => {
    if (code) {
        return Object.hasOwn(code, 'uri') ? code.uri : (Object.hasOwn(code, 'fileExt') ? code.fileExt : undefined);
    }
    else {
        return undefined;
    }
};
/**
 * The check for equality relies on JSON.stringify for instances of type Object.
 * Everything else is directly compared.
 * In this context, the check for equality is sufficient.
 */
export const isEqual = (obj1, obj2) => {
    if (obj1 instanceof Object && obj2 instanceof Object) {
        return JSON.stringify(obj1) === JSON.stringify(obj2);
    }
    else {
        return obj1 === obj2;
    }
};
//# sourceMappingURL=utils.js.map