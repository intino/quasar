/* --------------------------------------------------------------------------------------------
 * Copyright (c) 2024 TypeFox and others.
 * Licensed under the MIT License. See LICENSE in the package root for license information.
 * ------------------------------------------------------------------------------------------ */
import { EditorAppBase } from './editorAppBase.js';
import { registerExtension } from 'vscode/extensions';
import { verifyUrlOrCreateDataUrl, ModelUpdateType, isEqual, isModelUpdateRequired } from './utils.js';
import { DisposableStore } from 'vscode/monaco';
/**
 * The vscode-apo monaco-editor app uses vscode user and extension configuration for monaco-editor.
 */
export class EditorAppExtended extends EditorAppBase {
    config;
    extensionRegisterResults = new Map();
    subscriptions = new DisposableStore();
    constructor(id, userConfig, logger) {
        super(id);
        this.logger = logger;
        const userAppConfig = userConfig.wrapperConfig.editorAppConfig;
        this.config = this.buildConfig(userAppConfig);
        this.config.extensions = userAppConfig.extensions ?? undefined;
        this.config.userConfiguration = userAppConfig.userConfiguration ?? undefined;
    }
    getConfig() {
        return this.config;
    }
    getExtensionRegisterResult(extensionName) {
        return this.extensionRegisterResults.get(extensionName);
    }
    async specifyServices() {
        const getTextmateServiceOverride = (await import('@codingame/monaco-vscode-textmate-service-override')).default;
        const getThemeServiceOverride = (await import('@codingame/monaco-vscode-theme-service-override')).default;
        return {
            ...getTextmateServiceOverride(),
            ...getThemeServiceOverride()
        };
    }
    async init() {
        // await all extensions that should be ready beforehand
        // always await theme extension
        const whenReadyTheme = (await import('@codingame/monaco-vscode-theme-defaults-default-extension')).whenReady;
        const awaitReadiness = (this.config.awaitExtensionReadiness ?? []).concat(whenReadyTheme);
        await this.awaitReadiness(awaitReadiness);
        if (this.config.extensions) {
            const allPromises = [];
            for (const extensionConfig of this.config.extensions) {
                const manifest = extensionConfig.config;
                const extRegResult = registerExtension(manifest, 1 /* ExtensionHostKind.LocalProcess */);
                this.extensionRegisterResults.set(manifest.name, extRegResult);
                if (extensionConfig.filesOrContents && Object.hasOwn(extRegResult, 'registerFileUrl')) {
                    for (const entry of extensionConfig.filesOrContents) {
                        const registerFileUrlResult = extRegResult.registerFileUrl(entry[0], verifyUrlOrCreateDataUrl(entry[1]));
                        this.subscriptions.add(registerFileUrlResult);
                    }
                }
                allPromises.push(extRegResult.whenReady());
            }
            await Promise.all(allPromises);
        }
        // buildConfig ensures userConfiguration is available
        await this.updateUserConfiguration(this.config.userConfiguration?.json);
        this.logger?.info('Init of Extended App was completed.');
    }
    disposeApp() {
        this.disposeEditors();
        this.extensionRegisterResults.forEach((k) => k?.dispose());
        this.subscriptions.dispose();
    }
    isAppConfigDifferent(orgConfig, config, includeModelData) {
        let different = false;
        if (includeModelData) {
            different = isModelUpdateRequired(orgConfig.codeResources, config.codeResources) !== ModelUpdateType.NONE;
        }
        const propsExtended = [
            // model required changes are not taken into account in this list
            'useDiffEditor',
            'domReadOnly',
            'readOnly',
            'awaitExtensionReadiness',
            'overrideAutomaticLayout',
            'editorOptions',
            'diffEditorOptions',
            'extensions',
            'userConfiguration'
        ];
        const propCompareExtended = (name) => {
            return !isEqual(orgConfig[name], config[name]);
        };
        different = different || propsExtended.some(propCompareExtended);
        return different;
    }
}
//# sourceMappingURL=editorAppExtended.js.map