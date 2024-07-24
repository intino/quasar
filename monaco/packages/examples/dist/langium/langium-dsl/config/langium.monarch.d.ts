export declare const LangiumMonarchContent: {
    keywords: string[];
    operators: string[];
    symbols: RegExp;
    tokenizer: {
        initial: ({
            regex: RegExp;
            action: {
                token: string;
                cases?: undefined;
            };
            include?: undefined;
        } | {
            regex: RegExp;
            action: {
                cases: {
                    '@keywords': {
                        token: string;
                    };
                    '@default': {
                        token: string;
                    };
                    '@operators'?: undefined;
                };
                token?: undefined;
            };
            include?: undefined;
        } | {
            include: string;
            regex?: undefined;
            action?: undefined;
        } | {
            regex: RegExp;
            action: {
                cases: {
                    '@operators': {
                        token: string;
                    };
                    '@default': {
                        token: string;
                    };
                    '@keywords'?: undefined;
                };
                token?: undefined;
            };
            include?: undefined;
        })[];
        whitespace: ({
            regex: RegExp;
            action: {
                token: string;
                next?: undefined;
            };
        } | {
            regex: RegExp;
            action: {
                token: string;
                next: string;
            };
        })[];
        comment: ({
            regex: RegExp;
            action: {
                token: string;
                next?: undefined;
            };
        } | {
            regex: RegExp;
            action: {
                token: string;
                next: string;
            };
        })[];
    };
};
//# sourceMappingURL=langium.monarch.d.ts.map