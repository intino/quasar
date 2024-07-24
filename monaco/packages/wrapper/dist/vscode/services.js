/* --------------------------------------------------------------------------------------------
 * Copyright (c) 2024 TypeFox and others.
 * Licensed under the MIT License. See LICENSE in the package root for license information.
 * ------------------------------------------------------------------------------------------ */
import * as vscode from 'vscode';
import getConfigurationServiceOverride from '@codingame/monaco-vscode-configuration-service-override';
import { mergeServices } from 'monaco-languageclient/vscode/services';
/**
 * Child classes are allow to override the services configuration implementation.
 */
export const configureServices = async (config) => {
    const serviceConfig = config.serviceConfig ?? {};
    // configure log level
    serviceConfig.debugLogging = config.logger?.isEnabled() === true && (serviceConfig.debugLogging === true || config.logger.isDebugEnabled() === true);
    // always set required services if not configured
    serviceConfig.userServices = serviceConfig.userServices ?? {};
    const configureService = serviceConfig.userServices.configurationService ?? undefined;
    const workspaceConfig = serviceConfig.workspaceConfig ?? undefined;
    if (configureService === undefined) {
        if (workspaceConfig) {
            throw new Error('You provided a workspaceConfig without using the configurationServiceOverride');
        }
        const mlcDefautServices = {
            ...getConfigurationServiceOverride()
        };
        mergeServices(mlcDefautServices, serviceConfig.userServices);
    }
    // adding the default workspace config if not provided
    if (!workspaceConfig) {
        serviceConfig.workspaceConfig = {
            workspaceProvider: {
                trusted: true,
                workspace: {
                    workspaceUri: vscode.Uri.file('/workspace')
                },
                async open() {
                    window.open(window.location.href);
                    return true;
                }
            }
        };
    }
    mergeServices(config.specificServices ?? {}, serviceConfig.userServices);
    return serviceConfig;
};
export const useOpenEditorStub = async (modelRef, options, sideBySide) => {
    console.log('Received open editor call with parameters: ', modelRef, options, sideBySide);
    return undefined;
};
export const checkServiceConsistency = (userServices) => {
    const haveThemeService = Object.keys(userServices ?? {}).includes('themeService');
    const haveTextmateService = Object.keys(userServices ?? {}).includes('textMateTokenizationFeature');
    const haveMarkersService = Object.keys(userServices ?? {}).includes('markersService');
    const haveViewsService = Object.keys(userServices ?? {}).includes('viewsService');
    // theme requires textmate
    if (haveThemeService && !haveTextmateService) {
        throw new Error('"theme" service requires "textmate" service. Please add it to the "userServices".');
    }
    // markers service requires views service
    if (haveMarkersService && !haveViewsService) {
        throw new Error('"markers" service requires "views" service. Please add it to the "userServices".');
    }
    // we end up here if no exceptions were thrown
    return true;
};
//# sourceMappingURL=services.js.map