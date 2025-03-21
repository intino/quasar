/* --------------------------------------------------------------------------------------------
 * Copyright (c) 2024 TypeFox and others.
 * Licensed under the MIT License. See LICENSE in the package root for license information.
 * ------------------------------------------------------------------------------------------ */

// solve: __dirname is not defined in ES module scope
import { fileURLToPath } from 'url';
import { dirname, resolve } from 'path';
const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

const config = {
    entry: resolve(__dirname, 'src', 'client', 'main.ts'),
    module: {
        rules: [
            {
                test: /\.css$/,
                use: ['style-loader', 'css-loader']
            },
            {
                test: /\.ts?$/,
                use: ['ts-loader']
            },
            {
                test: /\.js$/,
                enforce: 'pre',
                use: ['source-map-loader'],
                // These modules seems to have broken sourcemaps, exclude them to prevent an error flood in the logs
                exclude: [/vscode-jsonrpc/, /vscode-languageclient/, /vscode-languageserver/, /vscode-languageserver-protocol/]
            }
        ]
    },
    experiments: {
        outputModule: true
    },
    output: {
        filename: 'main.js',
        path: resolve(__dirname, '../editor-elements/res/js', 'monaco-client'),
        module: true
    },
    target: 'web',
    resolve: {
        extensions: ['.ts', '.js', '.json', '.ttf']
    },
    mode: 'production',
    devtool: 'source-map'
};

export default config;
