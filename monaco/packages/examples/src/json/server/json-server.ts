/* --------------------------------------------------------------------------------------------
 * Copyright (c) 2024 TypeFox and others.
 * Licensed under the MIT License. See LICENSE in the package root for license information.
 * ------------------------------------------------------------------------------------------ */
import { readFile } from 'fs';
import requestLight from 'request-light';
import * as URI from 'vscode-uri';
import 'vscode-ws-jsonrpc';
import { createConnection, _Connection, TextDocuments, DocumentSymbolParams, ProposedFeatures } from 'vscode-languageserver/lib/node/main.js';
import {
    Diagnostic, Command, CompletionList, CompletionItem, Hover,
    SymbolInformation, TextEdit, FoldingRange, ColorInformation, ColorPresentation
} from 'vscode-languageserver-types';
import { TextDocumentPositionParams, DocumentRangeFormattingParams, ExecuteCommandParams, CodeActionParams, FoldingRangeParams, DocumentColorParams, ColorPresentationParams, TextDocumentSyncKind } from 'vscode-languageserver-protocol';
import { getLanguageService, LanguageService, JSONDocument } from 'vscode-json-languageservice';
import { TextDocument } from 'vscode-languageserver-textdocument';

export class JsonServer {
    protected readonly connection: _Connection;
    protected workspaceRoot: URI.URI | undefined;
    protected readonly documents = new TextDocuments(TextDocument);

    protected readonly jsonService: LanguageService = getLanguageService({
        schemaRequestService: this.resolveSchema.bind(this)
    });

    protected readonly pendingValidationRequests = new Map<string, NodeJS.Timeout>();

    constructor(connection: _Connection) {
        this.connection = connection;
        this.documents.listen(this.connection);
        this.documents.onDidChangeContent(change =>
            this.validate(change.document)
        );
        this.documents.onDidClose(event => {
            this.cleanPendingValidation(event.document);
            this.cleanDiagnostics(event.document);
        });

        this.connection.onInitialize(params => {
            if (params.rootPath !== null && params.rootPath !== undefined) {
                this.workspaceRoot = URI.URI.file(params.rootPath);
            } else if (params.rootUri !== null) {
                this.workspaceRoot = URI.URI.parse(params.rootUri);
            }
            this.connection.console.log('The server is initialized.');
            return {
                capabilities: {
                    textDocumentSync: TextDocumentSyncKind.Incremental,
                    codeActionProvider: true,
                    completionProvider: {
                        resolveProvider: true,
                        triggerCharacters: ['"', ':']
                    },
                    hoverProvider: true,
                    documentSymbolProvider: true,
                    documentRangeFormattingProvider: true,
                    executeCommandProvider: {
                        commands: ['json.documentUpper']
                    },
                    colorProvider: true,
                    foldingRangeProvider: true
                }
            };
        });
        this.connection.onCodeAction(params =>
            this.codeAction(params)
        );
        this.connection.onCompletion(params =>
            this.completion(params)
        );
        this.connection.onCompletionResolve(item =>
            this.resolveCompletion(item)
        );
        this.connection.onExecuteCommand(params =>
            this.executeCommand(params)
        );
        this.connection.onHover(params =>
            this.hover(params)
        );
        this.connection.onDocumentSymbol(params =>
            this.findDocumentSymbols(params)
        );
        this.connection.onDocumentRangeFormatting(params =>
            this.format(params)
        );
        this.connection.onDocumentColor(params =>
            this.findDocumentColors(params)
        );
        this.connection.onColorPresentation(params =>
            this.getColorPresentations(params)
        );
        this.connection.onFoldingRanges(params =>
            this.getFoldingRanges(params)
        );
    }

    start() {
        this.connection.listen();
    }

    protected getFoldingRanges(params: FoldingRangeParams): FoldingRange[] {
        const document = this.documents.get(params.textDocument.uri);
        if (!document) {
            return [];
        }
        return this.jsonService.getFoldingRanges(document);
    }

    protected findDocumentColors(params: DocumentColorParams): Thenable<ColorInformation[]> {
        const document = this.documents.get(params.textDocument.uri);
        if (!document) {
            return Promise.resolve([]);
        }
        const jsonDocument = this.getJSONDocument(document);
        return this.jsonService.findDocumentColors(document, jsonDocument);
    }

    protected getColorPresentations(params: ColorPresentationParams): ColorPresentation[] {
        const document = this.documents.get(params.textDocument.uri);
        if (!document) {
            return [];
        }
        const jsonDocument = this.getJSONDocument(document);
        return this.jsonService.getColorPresentations(document, jsonDocument, params.color, params.range);
    }

    protected codeAction(params: CodeActionParams): Command[] {
        const document = this.documents.get(params.textDocument.uri);
        if (!document) {
            return [];
        }
        return [{
            title: 'Upper Case Document',
            command: 'json.documentUpper',
            // Send a VersionedTextDocumentIdentifier
            arguments: [{
                ...params.textDocument,
                version: document.version
            }]
        }];
    }

    protected format(params: DocumentRangeFormattingParams): TextEdit[] {
        const document = this.documents.get(params.textDocument.uri);
        return document ? this.jsonService.format(document, params.range, params.options) : [];
    }

    protected findDocumentSymbols(params: DocumentSymbolParams): SymbolInformation[] {
        const document = this.documents.get(params.textDocument.uri);
        if (!document) {
            return [];
        }
        const jsonDocument = this.getJSONDocument(document);
        return this.jsonService.findDocumentSymbols(document, jsonDocument);
    }

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    protected executeCommand(params: ExecuteCommandParams): any {
        if (params.command === 'json.documentUpper' && params.arguments) {
            const versionedTextDocumentIdentifier = params.arguments[0];
            const document = this.documents.get(versionedTextDocumentIdentifier.uri);
            if (document) {
                this.connection.model.applyEdit({
                    documentChanges: [{
                        textDocument: versionedTextDocumentIdentifier,
                        edits: [{
                            range: {
                                start: { line: 0, character: 0 },
                                end: { line: Number.MAX_SAFE_INTEGER, character: Number.MAX_SAFE_INTEGER }
                            },
                            newText: document.getText().toUpperCase()
                        }]
                    }]
                });
            }
        }
    }

    protected hover(params: TextDocumentPositionParams): Thenable<Hover | null> {
        const document = this.documents.get(params.textDocument.uri);
        if (!document) {
            return Promise.resolve(null);
        }
        const jsonDocument = this.getJSONDocument(document);
        return this.jsonService.doHover(document, params.position, jsonDocument);
    }

    protected async resolveSchema(url: string): Promise<string> {
        const uri = URI.URI.parse(url);
        if (uri.scheme === 'file') {
            return new Promise<string>((resolve, reject) => {
                readFile(uri.fsPath, { encoding: 'utf8' }, (err, result) => {
                    err ? reject(err) : resolve(result.toString());
                });
            });
        }
        try {
            const response = await requestLight.xhr({ url, followRedirects: 5 });
            return response.responseText;
        } catch (error: unknown) {
            const err = error as Record<string, unknown>;
            return Promise.reject(err.responseText !== undefined || requestLight.getErrorStatusDescription(err.status as number) || err.toString());
        }
    }

    protected resolveCompletion(item: CompletionItem): Thenable<CompletionItem> {
        return this.jsonService.doResolve(item);
    }

    protected completion(params: TextDocumentPositionParams): Thenable<CompletionList | null> {
        const document = this.documents.get(params.textDocument.uri);
        if (!document) {
            return Promise.resolve(null);
        }
        const jsonDocument = this.getJSONDocument(document);
        return this.jsonService.doComplete(document, params.position, jsonDocument);
    }

    protected validate(document: TextDocument): void {
        this.cleanPendingValidation(document);
        this.pendingValidationRequests.set(document.uri, setTimeout(() => {
            this.pendingValidationRequests.delete(document.uri);
            this.doValidate(document);
        }));
    }

    protected cleanPendingValidation(document: TextDocument): void {
        const request = this.pendingValidationRequests.get(document.uri);
        if (request !== undefined) {
            clearTimeout(request);
            this.pendingValidationRequests.delete(document.uri);
        }
    }

    protected doValidate(document: TextDocument): void {
        if (document.getText().length === 0) {
            this.cleanDiagnostics(document);
            return;
        }
        const jsonDocument = this.getJSONDocument(document);
        this.jsonService.doValidation(document, jsonDocument).then(diagnostics =>
            this.sendDiagnostics(document, diagnostics)
        );
    }

    protected cleanDiagnostics(document: TextDocument): void {
        this.sendDiagnostics(document, []);
    }

    protected sendDiagnostics(document: TextDocument, diagnostics: Diagnostic[]): void {
        this.connection.sendDiagnostics({
            uri: document.uri, diagnostics
        });
    }

    protected getJSONDocument(document: TextDocument): JSONDocument {
        return this.jsonService.parseJSONDocument(document);
    }
}

const scriptExec = process.argv[2];
if (scriptExec === '--stdio') {
    const connection = createConnection(ProposedFeatures.all);
    const ls = new JsonServer(connection);
    ls.start();
}
