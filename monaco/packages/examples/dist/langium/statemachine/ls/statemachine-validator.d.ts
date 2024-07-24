import type { ValidationAcceptor } from 'langium';
import type { State, Statemachine } from './generated/ast.js';
import type { StatemachineServices } from './statemachine-module.js';
export declare function registerValidationChecks(services: StatemachineServices): void;
export declare class StatemachineValidator {
    /**
     * Checks if the state name starts with a capital letter.
     * @param state the state to check
     * @param accept the acceptor to report errors
     */
    checkStateNameStartsWithCapital(state: State, accept: ValidationAcceptor): void;
    /**
     * Checks if there are duplicate state and event names.
     * @param statemachine the statemachine to check
     * @param accept the acceptor to report errors
     */
    checkUniqueStatesAndEvents(statemachine: Statemachine, accept: ValidationAcceptor): void;
}
//# sourceMappingURL=statemachine-validator.d.ts.map