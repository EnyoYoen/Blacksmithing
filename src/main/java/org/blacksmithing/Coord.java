package org.blacksmithing;

public record Coord(int x, int y, int z) {

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Coord other_)) {
            return false;
        }

        return other_.x == this.x && other_.y == this.y && other_.z == this.z;
    }
}
