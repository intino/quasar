export type WorkerOverrides = {
    rootPath?: string | URL;
    basePath?: string | URL;
    workerLoaders?: Partial<Record<string, WorkerConfigSupplier | WorkerLoader>>;
    ignoreMapping?: boolean;
    userDefinedMapping?: (label: string) => string;
};
export type WorkerConfig = {
    rootPath: string | URL;
    basePath?: string | URL;
    workerFile: string | URL;
    options?: WorkerOptions;
};
export type WorkerConfigSupplier = () => WorkerConfig;
export type WorkerLoader = () => Worker;
export declare const defaultWorkerLoaders: Partial<Record<string, WorkerConfigSupplier | WorkerLoader>>;
/**
 * Cross origin workers don't work
 * The workaround used by vscode is to start a worker on a blob url containing a short script calling 'importScripts'
 * importScripts accepts to load the code inside the blob worker
 */
export declare const buildWorker: (config: WorkerConfig, workerOverrides?: WorkerOverrides) => Worker;
export declare const useWorkerFactory: (workerOverrides?: WorkerOverrides) => void;
export declare const useDefaultWorkerMapping: (label: string) => string;
//# sourceMappingURL=workerFactory.d.ts.map