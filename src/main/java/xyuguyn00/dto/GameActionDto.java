package xyuguyn00.dto;

import xyuguyn00.common.Position;
import xyuguyn00.common.UnitType;

public class GameActionDto {
    private final GameActionType type;
    private final Position from;
    private final Position to;
    private final Position target;
    private final UnitType unitType;

    private GameActionDto(Builder builder)
    {
        this.type = builder.type;
        this.from = builder.from;
        this.to = builder.to;
        this.target = builder.target;
        this.unitType = builder.unitType;
    }

    public static Builder builder(GameActionType type) {
        return new Builder(type);
    }

    public GameActionType getType() {
        return type;
    }

    public Position getFrom() {
        return from;
    }

    public Position getTo() {
        return to;
    }

    public Position getTarget() {
        return target;
    }

    public UnitType getUnitType() {
        return unitType;
    }

    public static class Builder {
        private final GameActionType type;
        private Position from;
        private Position to;
        private Position target;
        private UnitType unitType;

        private Builder(GameActionType type) {
            this.type = type;
        }

        public Builder from(Position from) {
            this.from = from;
            return this;
        }

        public Builder to(Position to) {
            this.to = to;
            return this;
        }

        public Builder target(Position target) {
            this.target = target;
            return this;
        }

        public Builder unitType(UnitType unitType) {
            this.unitType = unitType;
            return this;
        }

        public GameActionDto build() {
            return new GameActionDto(this);
        }
    }
}