package ca.mattlack.actioncompass.detection.util;

import lombok.Getter;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.Objects;

@Getter
public class IntVector2D {
    public static final IntVector2D ONE = new IntVector2D(1, 1);
    public static final Object ZERO = new IntVector2D(0, 0);
    private int x;
    private int y;

    public IntVector2D(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public IntVector2D add(IntVector2D location) {
        return new IntVector2D(this.x + location.x, this.y + location.y);
    }

    public IntVector2D subtract(IntVector2D location) {
        return new IntVector2D(this.x - location.x, this.y - location.y);
    }

    public IntVector2D subtract(int x, int y) {
        return new IntVector2D(this.x - x, this.y - y);
    }

    public IntVector2D multiply(int num) {
        return new IntVector2D(this.x * num, this.y * num);
    }

    public Location toLocation(World world, int yValue) {
        return new Location(world, x, 0, y);
    }


    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (o instanceof IntVector2D)
            return ((IntVector2D) o).x == this.x && ((IntVector2D) o).y == this.y;
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    public IntVector3D toIntVector3D(int y) {
        return new IntVector3D(this.x, y, this.y);
    }
}
