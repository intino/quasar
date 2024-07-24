import { BrowserMessageReader, BrowserMessageWriter } from 'vscode-languageserver/browser.js';
export declare let messageReader: BrowserMessageReader | undefined;
export declare let messageWriter: BrowserMessageWriter | undefined;
export declare const start: (port: MessagePort | DedicatedWorkerGlobalScope, name: string) => void;
//# sourceMappingURL=statemachine-server-start.d.ts.map