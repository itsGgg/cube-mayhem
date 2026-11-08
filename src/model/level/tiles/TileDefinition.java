package model.level.tiles;

import model.tools.Direction;

public class TileDefinition {
    private TileType type;
    private final int i;
    private final int j;
    private Direction direction;
    private TileDefinition linkedTile;

    public TileDefinition(TileType type, int i, int j, Direction direction, TileDefinition linkedTile) {
        this.type = type;
        this.i = i;
        this.j = j;
        this.direction = direction;
        this.linkedTile = linkedTile;
    }

    public TileType getType() {
        return type;
    }

    public int getI() {
        return i;
    }

    public int getJ() {
        return j;
    }

    public Direction getDirection() {
        return direction;
    }

    public TileDefinition getLinkedTile() {
        return linkedTile;
    }

    public void setLinkedTile(TileDefinition linkedTile) {
        this.linkedTile = linkedTile;
    }

    public boolean isTeleportation() {
        return linkedTile != null;
    }

    public boolean isDirectional() {
        return direction != null;
    }

    public String getBasicInfo() {
        return type + " , " + i + " , " + j + " ";
    }

    @Override
    public String toString() {
        return "TileDefinition{" +
                "type=" + type +
                ", i=" + i +
                ", j=" + j +
                ", direction=" + (direction != null ? direction.toString() : "null") +
                ", linkedTile=" + (linkedTile != null ? "[linked]" : "null") +
                '}';
    }
}
