/* --------------------------------------------------------------------------------------------
 * Copyright (c) 2024 TypeFox and others.
 * Licensed under the MIT License. See LICENSE in the package root for license information.
 * ------------------------------------------------------------------------------------------ */
import { initServices } from 'monaco-languageclient/vscode/services';
import { Logger } from 'monaco-languageclient/tools';
import { checkServiceConsistency, configureServices } from './vscode/services.js';
import { EditorAppExtended } from './editorAppExtended.js';
import { EditorAppClassic } from './editorAppClassic.js';
import { LanguageClientWrapper } from './languageClientWrapper.js';
/**
 * This class is responsible for the overall ochestration.
 * It inits, start and disposes the editor apps and the language client (if configured) and provides
 * access to all required components.
 */
export class MonacoEditorLanguageClientWrapper {
    id;
    editorApp;
    languageClientWrapper;
    logger = new Logger();
    initDone = false;
    /**
     * Perform an isolated initialization of the user services and the languageclient wrapper (if used).
     */
    async init(userConfig) {
        if (this.initDone) {
            throw new Error('init was already performed. Please call dispose first if you want to re-start.');
        }
        const editorAppConfig = userConfig.wrapperConfig.editorAppConfig;
        if ((editorAppConfig.useDiffEditor ?? false) && !editorAppConfig.codeResources?.original) {
            throw new Error(`Use diff editor was used without a valid config. code: ${editorAppConfig.codeResources?.main} codeOriginal: ${editorAppConfig.codeResources?.original}`);
        }
        // Always dispose old instances before start
        this.editorApp?.disposeApp();
        this.id = userConfig.id ?? Math.floor(Math.random() * 101).toString();
        this.logger.updateConfig(userConfig.loggerConfig);
        if (editorAppConfig.$type === 'classic') {
            this.editorApp = new EditorAppClassic(this.id, userConfig, this.logger);
        }
        else {
            this.editorApp = new EditorAppExtended(this.id, userConfig, this.logger);
        }
        // editorApps init their own service thats why they have to be created first
        const specificServices = await this.editorApp.specifyServices();
        const serviceConfig = await configureServices({
            serviceConfig: userConfig.wrapperConfig.serviceConfig,
            specificServices,
            logger: this.logger
        });
        await initServices({
            serviceConfig,
            caller: `monaco-editor (${this.id})`,
            performChecks: checkServiceConsistency,
            logger: this.logger
        });
        if (userConfig.languageClientConfig) {
            this.languageClientWrapper = new LanguageClientWrapper();
            await this.languageClientWrapper.init({
                languageClientConfig: userConfig.languageClientConfig,
                logger: this.logger
            });
        }
        this.initDone = true;
    }
    /**
     * Performs a full user configuration and the languageclient wrapper (if used) init and then start the application.
     */
    async initAndStart(userConfig, htmlElement) {
        await this.init(userConfig);
        await this.start(htmlElement);
    }
    /**
     * Does not perform any user configuration or other application init and just starts the application.
     */
    async start(htmlElement) {
        if (!this.initDone) {
            throw new Error('No init was performed. Please call init() before start()');
        }
        if (!htmlElement) {
            throw new Error('No HTMLElement provided for monaco-editor.');
        }
        this.logger.info(`Starting monaco-editor (${this.id})`);
        await this.editorApp?.init();
        await this.editorApp?.createEditors(htmlElement);
        if (this.languageClientWrapper?.haveLanguageClientConfig() ?? false) {
            await this.languageClientWrapper?.start();
        }
    }
    isInitDone() {
        return this.initDone;
    }
    isStarted() {
        // fast-fail
        if (!(this.editorApp?.haveEditor() ?? false)) {
            return false;
        }
        if (this.languageClientWrapper?.haveLanguageClient() ?? false) {
            return this.languageClientWrapper?.isStarted() ?? false;
        }
        return true;
    }
    haveLanguageClient() {
        return this.languageClientWrapper !== undefined;
    }
    getMonacoEditorApp() {
        return this.editorApp;
    }
    getEditor() {
        return this.editorApp?.getEditor();
    }
    getDiffEditor() {
        return this.editorApp?.getDiffEditor();
    }
    getLanguageClientWrapper() {
        return this.languageClientWrapper;
    }
    getLanguageClient() {
        return this.languageClientWrapper?.getLanguageClient();
    }
    getTextModels() {
        return this.editorApp?.getTextModels();
    }
    getModelRefs() {
        return this.editorApp?.getModelRefs();
    }
    getWorker() {
        return this.languageClientWrapper?.getWorker();
    }
    async updateCodeResources(codeResources) {
        return this.editorApp?.updateCodeResources(codeResources);
    }
    async updateEditorModels(modelRefs) {
        return this.editorApp?.updateEditorModels(modelRefs);
    }
    reportStatus() {
        const status = [];
        status.push('Wrapper status:');
        status.push(`Editor: ${this.editorApp?.getEditor()?.getId()}`);
        status.push(`DiffEditor: ${this.editorApp?.getDiffEditor()?.getId()}`);
        return status;
    }
    /**
     * Disposes all application and editor resources, plus the languageclient (if used).
     */
    async dispose() {
        this.editorApp?.disposeApp();
        if (this.languageClientWrapper?.haveLanguageClient() ?? false) {
            await this.languageClientWrapper?.disposeLanguageClient(false);
            this.editorApp = undefined;
            await Promise.resolve('Monaco editor and languageclient completed disposed.');
        }
        else {
            await Promise.resolve('Monaco editor has been disposed.');
        }
        this.initDone = false;
    }
    updateLayout() {
        this.editorApp?.updateLayout();
    }
    isReInitRequired(userConfig, previousUserConfig) {
        let mustReInit = false;
        const config = userConfig.wrapperConfig.editorAppConfig;
        const prevConfig = previousUserConfig.wrapperConfig.editorAppConfig;
        const prevWorkerOptions = previousUserConfig.languageClientConfig?.options;
        const currentWorkerOptions = userConfig.languageClientConfig?.options;
        const prevIsWorker = (prevWorkerOptions?.$type === 'WorkerDirect');
        const currentIsWorker = (currentWorkerOptions?.$type === 'WorkerDirect');
        const prevIsWorkerConfig = (prevWorkerOptions?.$type === 'WorkerConfig');
        const currentIsWorkerConfig = (currentWorkerOptions?.$type === 'WorkerConfig');
        // check if both are configs and the workers are both undefined
        if (prevIsWorkerConfig && !prevIsWorker && currentIsWorkerConfig && !currentIsWorker) {
            mustReInit = prevWorkerOptions.url !== currentWorkerOptions.url;
            // check if both are workers and configs are both undefined
        }
        else if (!prevIsWorkerConfig && prevIsWorker && !currentIsWorkerConfig && currentIsWorker) {
            mustReInit = prevWorkerOptions.worker !== currentWorkerOptions.worker;
            // previous was worker and current config is not or the other way around
        }
        else if (prevIsWorker && currentIsWorkerConfig || prevIsWorkerConfig && currentIsWorker) {
            mustReInit = true;
        }
        if (prevConfig.$type !== config.$type) {
            mustReInit = true;
        }
        else if (prevConfig.$type === 'classic' && config.$type === 'classic') {
            mustReInit = this.getMonacoEditorApp().isAppConfigDifferent(prevConfig, config, false) === true;
        }
        else if (prevConfig.$type === 'extended' && config.$type === 'extended') {
            mustReInit = this.getMonacoEditorApp().isAppConfigDifferent(prevConfig, config, false) === true;
        }
        return mustReInit;
    }
}
//# sourceMappingURL=wrapper.js.map