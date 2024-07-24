/* --------------------------------------------------------------------------------------------
 * Copyright (c) 2018-2022 TypeFox and others.
 * Licensed under the MIT License. See LICENSE in the package root for license information.
 * ------------------------------------------------------------------------------------------ */
/// <reference lib="WebWorker" />
import { EmptyFileSystem } from 'langium';
import { startLanguageServer } from 'langium/lsp';
import { createLangiumGrammarServices } from 'langium/grammar';
import { BrowserMessageReader, BrowserMessageWriter, createConnection } from 'vscode-languageserver/browser.js';
/* browser specific setup code */
const messageReader = new BrowserMessageReader(self);
const messageWriter = new BrowserMessageWriter(self);
// Inject the shared services and language-specific services
const context = {
    connection: createConnection(messageReader, messageWriter),
    ...EmptyFileSystem
};
const { shared } = createLangiumGrammarServices(context);
// Start the language server with the shared services
startLanguageServer(shared);
//# sourceMappingURL=langium-server.js.map