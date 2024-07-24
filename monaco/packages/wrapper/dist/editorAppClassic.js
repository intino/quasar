/* --------------------------------------------------------------------------------------------
 * Copyright (c) 2024 TypeFox and others.
 * Licensed under the MIT License. See LICENSE in the package root for license information.
 * ------------------------------------------------------------------------------------------ */
import * as monaco from 'monaco-editor';
import { EditorAppBase } from './editorAppBase.js';
import { ModelUpdateType, isEqual, isModelUpdateRequired } from './utils.js';
/**
 * The classic monaco-editor app uses the classic monaco-editor configuration.
 */
export class EditorAppClassic extends EditorAppBase {
    config;
    constructor(id, userConfig, logger) {
        super(id, logger);
        const userAppConfig = userConfig.wrapperConfig.editorAppConfig;
        this.config = this.buildConfig(userAppConfig);
        this.config.languageDef = userAppConfig.languageDef;
    }
    getConfig() {
        return this.config;
    }
    async specifyServices() {
        const getMonarchServiceOverride = (await import('@codingame/monaco-vscode-monarch-service-override')).default;
        return {
            ...getMonarchServiceOverride()
        };
    }
    async init() {
        // await all extenson that should be ready beforehand
        await this.awaitReadiness(this.config.awaitExtensionReadiness);
        const languageDef = this.config.languageDef;
        if (languageDef) {
            // register own language first
            monaco.languages.register(languageDef.languageExtensionConfig);
            const languageRegistered = monaco.languages.getLanguages().filter(x => x.id === languageDef.languageExtensionConfig.id);
            if (languageRegistered.length === 0) {
                // this is only meaningful for languages supported by monaco out of the box
                monaco.languages.register({
                    id: languageDef.languageExtensionConfig.id
                });
            }
            // apply monarch definitions
            if (languageDef.monarchLanguage) {
                monaco.languages.setMonarchTokensProvider(languageDef.languageExtensionConfig.id, languageDef.monarchLanguage);
            }
            if (languageDef.theme) {
                monaco.editor.defineTheme(languageDef.theme.name, languageDef.theme.data);
                monaco.editor.setTheme(languageDef.theme.name);
            }
        }
        if (this.config.editorOptions?.['semanticHighlighting.enabled'] !== undefined) {
            // use updateConfiguration here as otherwise semantic highlighting will not work
            const json = JSON.stringify({
                'editor.semanticHighlighting.enabled': this.config.editorOptions['semanticHighlighting.enabled']
            });
            await this.updateUserConfiguration(json);
        }
        this.logger?.info('Init of Classic App was completed.');
    }
    disposeApp() {
        this.disposeEditors();
    }
    isAppConfigDifferent(orgConfig, config, includeModelData) {
        let different = false;
        if (includeModelData) {
            different = isModelUpdateRequired(orgConfig.codeResources, config.codeResources) !== ModelUpdateType.NONE;
        }
        const propsClassic = [
            // model required changes are not taken into account in this list
            'useDiffEditor',
            'domReadOnly',
            'readOnly',
            'awaitExtensionReadiness',
            'overrideAutomaticLayout',
            'editorOptions',
            'diffEditorOptions',
            'theme',
            'languageDef',
            'languageExtensionConfig',
            'themeData'
        ];
        const propCompareClassic = (name) => {
            return !isEqual(orgConfig[name], config[name]);
        };
        different = different || propsClassic.some(propCompareClassic);
        return different;
    }
}
//# sourceMappingURL=editorAppClassic.js.map