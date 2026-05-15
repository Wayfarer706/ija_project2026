/**
 * Project: Advance Wars Clone
 * Authors: Mariia Zhdaniuk
 * Description: The contract for the Command Pattern. Every specific game action 
 * (Move, Attack, Purchase) must implement this interface to define its own 
 * validation and execution logic.
 */
package xyuguyn00.handler;

import xyuguyn00.common.Result;
import xyuguyn00.model.dto.GameActionDto;

public interface GameActionHandler {
    // Determines if this specific handler is responsible for the given DTO
    boolean canHandle(GameActionDto action);

    // Executes the action and returns a success/failure Result to the Dispatcher
    Result handle(GameActionDto action);
}