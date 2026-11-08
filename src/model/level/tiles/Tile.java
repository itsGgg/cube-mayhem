package model.level.tiles;

import model.tools.Direction;

public class Tile {
    private final TileType tileType;
    private Direction direction;

    public Tile(TileType tileType) {
        this(tileType, Direction.NORTH);
    }

    public Tile(TileType tileType, Direction direction) {
        this.tileType = tileType;
        this.direction = direction;
    }

    /*
     * Constructeur permettant de créer une copie d'une autre tuile
     */
    public Tile(Tile original) {
        this.tileType = original.getTileType();
        this.direction = original.getDirection();
    }

    public TileType getTileType() {
        return tileType;
    }

    public boolean isType(TileType tileType) {
        return this.tileType == tileType;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public Direction rotate(int k) {
        this.direction = this.direction.spin(k);
        return this.direction;
    }

    public Direction rotate() {
        return rotate(1);
    }

    public boolean isDirectional() {
        return this.tileType.isDirectional();
    }

    @Override
    public String toString() {
        return "Tile { type=" + (tileType != null ? tileType : "null") +
                ", direction=" + (direction != null ? direction : "null") + " }";
    }
}
