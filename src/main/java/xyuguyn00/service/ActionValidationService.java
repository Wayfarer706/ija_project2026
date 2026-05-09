package xyuguyn00.service;

import java.util.ArrayList;
import java.util.List;

import xyuguyn00.common.Position;
import xyuguyn00.common.Result;
import xyuguyn00.common.UnitType;
import xyuguyn00.dto.AvailableActionsDto;
import xyuguyn00.game.Building;
import xyuguyn00.game.Game;
import xyuguyn00.game.Unit;

public class ActionValidationService {
    private final Game game;

    public ActionValidationService(Game game) {
        this.game = game;
    }

    public Result canMove(Position from, Position to) {
        if (from == null || to == null) {
            return Result.failure("Move requires from and to positions.");
        }

        Unit unit = game.getUnitAt(from);

        if (unit == null) {
            return Result.failure("No unit found at source position.");
        }

        if (!unit.getPlayer().equals(game.getCurrentPlayer())) {
            return Result.failure("Selected unit does not belong to current player.");
        }

        if (unit.hasMoved()) {
            return Result.failure("Selected unit has already moved this turn.");
        }

        if (!game.getReachableTiles(from).contains(to)) {
            return Result.failure("Target position is not reachable.");
        }

        return Result.success();
    }

    public Result canAttack(Position attackerPosition, Position targetPosition) {
        if (attackerPosition == null || targetPosition == null) {
            return Result.failure("Attack requires attacker and target positions.");
        }

        Unit attacker = game.getUnitAt(attackerPosition);
        Unit target = game.getUnitAt(targetPosition);

        if (attacker == null) {
            return Result.failure("No attacker found.");
        }

        if (target == null) {
            return Result.failure("No derfender found.");
        }

        if (attacker.getPlayer().equals(target.getPlayer())) {
            return Result.failure("Cannot attack friendly unit.");
        }

        int distance = getDistance(attackerPosition, targetPosition);

        if (distance < attacker.getMinAttackRange() || distance > attacker.getMaxAttackRange()) {
            return Result.failure("Target is out of attack range.");
        }

        return Result.success();
    }

    public Result canMoveAndAttack(Position from, Position to, Position target) {
        Result moveResult = canMove(from, to);

        if (moveResult.isFailure()) {
            return moveResult;
        }

        Unit attacker = game.getUnitAt(from);

        if (attacker.getUnitType() == UnitType.ARTILLERY && !from.equals(to)) {
            return Result.failure("Artillery cannot move and attack in the same turn.");
        }

        return canAttackFromVirtualPosition(attacker, to, target);
    }

    public Result canCapture(Position position) {
        if (position == null) {
            return Result.failure("Capture requires position.");
        }

        Unit unit = game.getUnitAt(position);
        Building building = game.getBuildingAt(position);

        if (unit == null) {
            return Result.failure("No unit found on capture position.");
        }

        if (building == null) {
            return Result.failure("No building found on capture position.");
        }

        if (unit.getUnitType() != UnitType.INFANTRY) {
            return Result.failure("Only infantry can capture buildings.");
        }

        if (building.getOwner().equals(unit.getPlayer())) {
            return Result.failure("Cannot capture own building.");
        }

        return Result.success();
    }

    public Result canPurchase(UnitType unitType, Position position) {
        if (unitType == null || position == null) {
            return Result.failure("Purchase requires unit type and position.");
        }

        Building building = game.getBuildingAt(position);

        if (building == null) {
            return Result.failure("No building found at purchase position.");
        }

        if (!"Továrna".equals(building.getType())) {
            return Result.failure("Units can only be purchased on factory.");
        }

        if (!building.getOwner().equals(game.getCurrentPlayer())) {
            return Result.failure("Current player does not own this factory.");
        }

        if (game.getUnitAt(position) != null) {
            return Result.failure("Factory is blocked by unit.");
        }

        int cost = game.getUnitCost(unitType);

        if (game.getPlayerFunds(game.getCurrentPlayer()) < cost) {
            return Result.failure("Not enough funds to purchase unit.");
        }

        return Result.success();
    }

    public AvailableActionsDto getAvailableActions(Position from, Position afterMovePosition) {
        if (from == null || afterMovePosition == null) {
            return new AvailableActionsDto(false, false, List.of());
        }

        Unit unit = game.getUnitAt(from);

        if (unit == null) {
            return new AvailableActionsDto(false, false, List.of());
        }

        boolean canWait = game.getReachableTiles(from).contains(afterMovePosition);
        boolean canCapture = canCaptureFromVirtualPosition(unit, afterMovePosition);

        List<Position> attackTargets = List.of();

        if (!(unit.getUnitType() == UnitType.ARTILLERY && !from.equals(afterMovePosition))) {
            attackTargets = getAttackTargetsFromVirtualPosition(unit, afterMovePosition);
        }

        return new AvailableActionsDto(canWait, canCapture, attackTargets);
    }

    private boolean canCaptureFromVirtualPosition(Unit unit, Position position) {
        Building building = game.getBuildingAt(position);

        if (building == null) {
            return false;
        }

        if (unit.getUnitType() != UnitType.INFANTRY) {
            return false;
        }

        return !building.getOwner().equals(unit.getPlayer());
    }

    private Result canAttackFromVirtualPosition(Unit attacker, Position attackerPosition, Position targetPosition) {
        Unit defender = game.getUnitAt(targetPosition);

        if (defender == null) {
            return Result.failure("No defender found.");
        }

        if (attacker.getPlayer().equals(defender.getPlayer())) {
            return Result.failure("Cannot attack friendly unit.");
        }

        int distance = getDistance(attackerPosition, targetPosition);

        if (distance < attacker.getMinAttackRange() || distance > attacker.getMaxAttackRange()) {
            return Result.failure("Target is out of attack range.");
        }

        return Result.success();
    }

    private List<Position> getAttackTargetsFromVirtualPosition(Unit attacker, Position attackerPosition) {
        List<Position> targets = new ArrayList<>();

        for (Position position : game.getUnitPositions()) {
            Unit possibleTarget = game.getUnitAt(position);

            if (possibleTarget == null) {
                continue;
            }

            if (possibleTarget.getPlayer().equals(attacker.getPlayer())) {
                continue;
            }

            int distance = getDistance(attackerPosition, position);

            if (distance >= attacker.getMinAttackRange() && distance <= attacker.getMaxAttackRange()) {
                targets.add(position);
            }
        }

        return targets;
    }

    private int getDistance(Position first, Position second) {
        return Math.abs(first.getX() - second.getX()) + Math.abs(first.getY() - second.getY());
    }
}