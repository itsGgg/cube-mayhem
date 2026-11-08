package model.level.plate;

import model.tools.Constants;
import model.tools.Direction;

public enum CaseType {
    BLOCKED() {
        private Direction direction = Direction.NORTH;

        @Override
        public Direction getDirection() {
            return direction;
        }

        @Override
        public void setDirection(Direction newDirection) {
            this.direction = newDirection;
        }
    },
    PATH {
        private Direction direction = Direction.NORTH;

        @Override
        public Direction getDirection() {
            return direction;
        }

        @Override
        public void setDirection(Direction newDirection) {
            this.direction = newDirection;
        }
    },
    TRANSPARENT {
        private Direction direction = Direction.NORTH;

        @Override
        public Direction getDirection() {
            return direction;
        }

        @Override
        public void setDirection(Direction newDirection) {
            this.direction = newDirection;
        }
    },
    RESTRICT {
        private Direction direction = Direction.NORTH;

        @Override
        public Direction getDirection() {
            return direction;
        }

        @Override
        public void setDirection(Direction newDirection) {
            this.direction = newDirection;
        }
    };

    private final String[] names = { "Condamnée", "Accessible", "Transparent", "Restreint" };

    public abstract Direction getDirection();

    public abstract void setDirection(Direction newDirection);

    public int getId() {
        return this.ordinal();
    }

    public String getName() {
        return names[this.ordinal()];
    }

    public String getCompleteName() {
        return "[" + this.ordinal() + "] " + getName();
    }

    @Override
    public String toString() {
        return switch (this) {
            case BLOCKED -> Constants.TerminalCode.BLOCKED;
            case PATH -> Constants.TerminalCode.PATH;
            case TRANSPARENT -> Constants.TerminalCode.TRANSPARENT_INVISIBLE;
            case RESTRICT -> Constants.TerminalCode.RESTRICT;
        };
    }
}
