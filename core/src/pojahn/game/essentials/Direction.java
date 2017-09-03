package pojahn.game.essentials;

public enum Direction {
    N,
    NE,
    E,
    SE,
    S,
    SW,
    W,
    NW;

    public boolean isNorth() {
        return this == Direction.N;
    }

    public boolean isSouth() {
        return this == Direction.S;
    }

    public boolean isWest() {
        return this == Direction.W;
    }

    public boolean isEast() {
        return this == Direction.E;
    }

    public boolean isNorthWest() {
        return this == NW;
    }

    public boolean isSouthWest() {
        return this == SW;
    }

    public boolean isNorthEast() {
        return this == Direction.NE;
    }

    public boolean isSouthEast() {
        return this == Direction.SE;
    }

    public boolean isEastSide() {
        return isEast() || isNorthEast() || isSouthEast();
    }

    public boolean isWestSide() {
        return isWest() || isNorthWest() || isSouthWest();
    }

    public boolean isNorthSide() {
        return isNorth() || isNorthEast() || isNorthWest();
    }

    public boolean isSouthSide() {
        return isSouth() || isSouthEast() || isSouthWest();
    }

    public boolean isStraight() {
        return this == Direction.E || this == Direction.W;
    }

    public boolean isDiagonal() {
        switch (this) {
            case NE:
            case SE:
            case SW:
            case NW:
                return true;
            default:
                return false;
        }
    }

    public Direction invert() {
        switch (this) {
            case N:
                return Direction.S;
            case NE:
                return Direction.SW;
            case E:
                return Direction.W;
            case SE:
                return Direction.NW;
            case S:
                return Direction.N;
            case SW:
                return Direction.NE;
            case W:
                return Direction.E;
            case NW:
                return Direction.SE;
            default:
                throw new RuntimeException();
        }
    }
}
