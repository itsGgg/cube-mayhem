package model.tools;

public enum Direction {
    NORTH,
    EAST,
    SOUTH,
    WEST;

    private final String[] names = { "Nord", "Est", "Sud", "Ouest" };
    private final double[] radians = { 0, Math.PI / 2, Math.PI, -Math.PI / 2 };

    public String getName() {
        return names[this.ordinal()];
    }

    public double toRadians() {
        return radians[this.ordinal()];
    }

    public Direction spin(int k) {
        int n = values().length;
        return values()[((this.ordinal() + k) % n + n) % n];
    }

    public Direction spin() {
        return spin(1);
    }

    public Direction opposite() {
        return spin(2);
    }

    public static Direction getfor3D(int i) {
        if (i == 1)
            return values()[3];
        else if (i == 3)
            return values()[1];
        else
            return values()[i];
    }

    public int deltaI() {
        switch (this) {
            case NORTH:
                return -1;
            case SOUTH:
                return 1;
            default:
                return 0;
        }
    }

    public int deltaJ() {
        switch (this) {
            case EAST:
                return 1;
            case WEST:
                return -1;
            default:
                return 0;
        }
    }
}
