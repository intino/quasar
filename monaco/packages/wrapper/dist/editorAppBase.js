/* --------------------------------------------------------------------------------------------
 * Copyright (c) 2024 TypeFox and others.
 * Licensed under the MIT License. See LICENSE in the package root for license information.
 * ------------------------------------------------------------------------------------------ */
import * as monaco from 'monaco-editor';
import { createModelReference } from 'vscode/monaco';
import { getUserConfiguration, updateUserConfiguration as vscodeUpdateUserConfiguration } from '@codingame/monaco-vscode-configuration-service-override';
import { getEditorUri, isModelUpdateRequired, ModelUpdateType } from './utils.js';
/**
 * This is the base class for both Monaco Ediotor Apps:
 * - EditorAppClassic
 * - EditorAppExtended
 *
 * It provides the generic functionality for both implementations.
 */
export class EditorAppBase {
    id;
    logger;
    editor;
    diffEditor;
    modelRef;
    modelRefOriginal;
    constructor(id, logger) {
        this.id = id;
        this.logger = logger;
    }
    buildConfig(userAppConfig) {
        const config = {
            $type: userAppConfig.$type,
            codeResources: userAppConfig.codeResources,
            useDiffEditor: userAppConfig.useDiffEditor ?? false,
            readOnly: userAppConfig.readOnly ?? false,
            domReadOnly: userAppConfig.domReadOnly ?? false,
            overrideAutomaticLayout: userAppConfig.overrideAutomaticLayout ?? true,
            awaitExtensionReadiness: userAppConfig.awaitExtensionReadiness ?? undefined,
        };
        config.editorOptions = {
            ...userAppConfig.editorOptions,
            automaticLayout: userAppConfig.overrideAutomaticLayout ?? true
        };
        config.diffEditorOptions = {
            ...userAppConfig.diffEditorOptions,
            automaticLayout: userAppConfig.overrideAutomaticLayout ?? true
        };
        return config;
    }
    haveEditor() {
        return this.editor !== undefined || this.diffEditor !== undefined;
    }
    getEditor() {
        return this.editor;
    }
    getDiffEditor() {
        return this.diffEditor;
    }
    async createEditors(container) {
        if (this.getConfig().useDiffEditor ?? false) {
            this.diffEditor = monaco.editor.createDiffEditor(container, this.getConfig().diffEditorOptions);
        }
        else {
            this.editor = monaco.editor.create(container, this.getConfig().editorOptions);
        }
        const modelRefs = await this.buildModelRefs(this.getConfig().codeResources);
        await this.updateEditorModels(modelRefs);
    }
    disposeEditors() {
        if (this.editor) {
            this.modelRef?.dispose();
            this.editor.dispose();
            this.editor = undefined;
        }
        if (this.diffEditor) {
            this.modelRef?.dispose();
            this.modelRefOriginal?.dispose();
            this.diffEditor.dispose();
            this.diffEditor = undefined;
        }
    }
    getTextModels() {
        const modelRefs = this.getModelRefs();
        return {
            text: modelRefs.modelRef?.object.textEditorModel ?? undefined,
            textOriginal: modelRefs.modelRefOriginal?.object.textEditorModel ?? undefined
        };
    }
    getModelRefs() {
        return {
            modelRef: this.modelRef,
            modelRefOriginal: this.modelRefOriginal
        };
    }
    async updateCodeResources(codeResources) {
        if (!this.editor && !this.diffEditor) {
            return Promise.reject(new Error('You cannot update the code resources as neither editor or diff editor is available.'));
        }
        const modelUpdateType = isModelUpdateRequired(this.getConfig().codeResources, codeResources);
        if (modelUpdateType !== ModelUpdateType.NONE) {
            this.updateCodeResourcesConfig(codeResources);
        }
        if (modelUpdateType === ModelUpdateType.CODE) {
            if (this.editor) {
                this.editor.setValue(codeResources?.main?.text ?? '');
            }
            else {
                this.diffEditor?.getModifiedEditor().setValue(codeResources?.main?.text ?? '');
                this.diffEditor?.getOriginalEditor().setValue(codeResources?.original?.text ?? '');
            }
        }
        else if (modelUpdateType === ModelUpdateType.MODEL || modelUpdateType === ModelUpdateType.CODE_AND_MODEL) {
            const modelRefs = await this.buildModelRefs(codeResources);
            this.updateEditorModels(modelRefs);
        }
    }
    async buildModelRefs(codeResources) {
        const modelRef = await this.buildModelRef(codeResources?.main, false);
        const modelRefOriginal = await this.buildModelRef(codeResources?.original, true);
        return {
            modelRef,
            modelRefOriginal
        };
    }
    async buildModelRef(code, original) {
        if (code) {
            const uri = getEditorUri(this.id, original ?? false, code);
            const modelRef = await createModelReference(uri, code.text);
            this.checkEnforceLanguageId(modelRef, code.enforceLanguageId);
            return modelRef;
        }
        return undefined;
    }
    async updateEditorModels(modelRefs) {
        if (!this.editor && !this.diffEditor) {
            return Promise.reject(new Error('You cannot update models as neither editor nor diff editor is available.'));
        }
        let updateMain = false;
        let updateOriginal = false;
        if (modelRefs.modelRef) {
            this.modelRef?.dispose();
            this.modelRef = modelRefs.modelRef;
            updateMain = true;
        }
        if (modelRefs.modelRefOriginal) {
            this.modelRefOriginal?.dispose();
            this.modelRefOriginal = modelRefs.modelRefOriginal;
            updateOriginal = true;
        }
        if (this.editor) {
            if (updateMain && this.modelRef && this.modelRef.object.textEditorModel !== null) {
                this.editor.setModel(this.modelRef.object.textEditorModel);
            }
        }
        else if (this.diffEditor) {
            if ((updateMain || updateOriginal) &&
                this.modelRef && this.modelRefOriginal &&
                this.modelRef.object.textEditorModel !== null && this.modelRefOriginal.object.textEditorModel !== null) {
                this.diffEditor.setModel({
                    original: this.modelRefOriginal.object.textEditorModel,
                    modified: this.modelRef.object.textEditorModel
                });
            }
            else {
                return Promise.reject(new Error('You cannot update models, because original model ref is not contained, but required for DiffEditor.'));
            }
        }
    }
    checkEnforceLanguageId(modelRef, enforceLanguageId) {
        if (enforceLanguageId !== undefined) {
            modelRef.object.setLanguageId(enforceLanguageId);
            this.logger?.info(`Main languageId is enforced: ${enforceLanguageId}`);
        }
    }
    updateCodeResourcesConfig(codeResources) {
        const config = this.getConfig();
        // reset first, if the passed resources are empty, then the new resources will be empty as well
        config.codeResources = {};
        if (codeResources?.main) {
            config.codeResources.main = codeResources.main;
        }
        if (codeResources?.original) {
            config.codeResources.original = codeResources.original;
        }
    }
    updateLayout() {
        if (this.getConfig().useDiffEditor ?? false) {
            this.diffEditor?.layout();
        }
        else {
            this.editor?.layout();
        }
    }
    async awaitReadiness(awaitExtensionReadiness) {
        if (awaitExtensionReadiness) {
            const allPromises = [];
            for (const awaitReadiness of awaitExtensionReadiness) {
                allPromises.push(awaitReadiness());
            }
            return Promise.all(allPromises);
        }
        return Promise.resolve();
    }
    updateMonacoEditorOptions(options) {
        this.getEditor()?.updateOptions(options);
    }
    async updateUserConfiguration(json) {
        if (json !== undefined) {
            return vscodeUpdateUserConfiguration(json);
        }
        return Promise.resolve();
    }
    getUserConfiguration() {
        return getUserConfiguration();
    }
}
//# sourceMappingURL=editorAppBase.js.map