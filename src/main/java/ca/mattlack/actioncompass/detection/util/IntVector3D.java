package ca.mattlack.actioncompass.detection.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.annotation.Nullable;
import java.util.Objects;

@Getter
@AllArgsConstructor
public class IntVector3D implements Comparable<IntVector3D> {
    public static final IntVector3D ZERO = new IntVector3D();
    public static final IntVector3D UNIT_X = new IntVector3D(1, 0, 0);
    public static final IntVector3D UNIT_Y = new IntVector3D(0, 1, 0);
    public static final IntVector3D UNIT_Z = new IntVector3D(0, 0, 1);
    public static final IntVector3D ONE = new IntVector3D(1, 1, 1);
    protected final int x;
    protected final int y;
    protected final int z;

    public IntVector3D(IntVector3D other) {
        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
    }

    public IntVector3D() {
        this(0, 0, 0);
    }

    public static IntVector3D fromBukkitVector(org.bukkit.util.Vector vec) {
        return new IntVector3D(vec.getBlockX(), vec.getBlockY(), vec.getBlockZ());
    }

    public static IntVector3D getMinimum(IntVector3D v1, IntVector3D v2) {
        return new IntVector3D(Math.min(v1.x, v2.x), Math.min(v1.y, v2.y), Math.min(v1.z, v2.z));
    }

    public static IntVector3D getMaximum(IntVector3D v1, IntVector3D v2) {
        return new IntVector3D(Math.max(v1.x, v2.x), Math.max(v1.y, v2.y), Math.max(v1.z, v2.z));
    }

    public static IntVector3D getMidpoint(IntVector3D v1, IntVector3D v2) {
        return new IntVector3D((int) ((v1.x + v2.x) / 2.0D), (int) ((v1.y + v2.y) / 2.0D), (int) ((v1.z + v2.z) / 2.0D));
    }

    public int getArea() {
        return this.x * this.y * this.z;
    }

    public int sum() {
        return this.x + this.y + this.z;
    }

    public IntVector3D clamp(IntVector3D min, IntVector3D max) {
        return new IntVector3D(
                Math.min(Math.max(x, min.x), max.x),
                Math.min(Math.max(y, min.y), max.y),
                Math.min(Math.max(z, min.z), max.z)
        );
    }

    public org.bukkit.util.Vector toBukkitVector() {
        return new org.bukkit.util.Vector(this.x, this.y, this.z);
    }

    public IntVector3D setX(int x) {
        return new IntVector3D(x, this.y, this.z);
    }

    public IntVector3D setY(int y) {
        return new IntVector3D(this.x, y, this.z);
    }

    public IntVector3D setZ(int z) {
        return new IntVector3D(this.x, this.y, z);
    }

    public IntVector3D add(IntVector3D other) {
        return new IntVector3D(this.x + other.x, this.y + other.y, this.z + other.z);
    }

    public IntVector3D add(int x, int y, int z) {
        return new IntVector3D(this.x + x, this.y + y, this.z + z);
    }

    public IntVector3D add(IntVector3D... others) {
        int newX = this.x;
        int newY = this.y;
        int newZ = this.z;
        int var9 = others.length;

        for (IntVector3D other : others) {
            newX += other.x;
            newY += other.y;
            newZ += other.z;
        }

        return new IntVector3D(newX, newY, newZ);
    }

    public IntVector3D subtract(IntVector3D other) {
        return new IntVector3D(this.x - other.x, this.y - other.y, this.z - other.z);
    }

    public IntVector3D subtract(int x, int y, int z) {
        return new IntVector3D(this.x - x, this.y - y, this.z - z);
    }

    public IntVector3D subtract(IntVector3D... others) {
        int newX = this.x;
        int newY = this.y;
        int newZ = this.z;

        for (IntVector3D other : others) {
            newX -= other.x;
            newY -= other.y;
            newZ -= other.z;
        }

        return new IntVector3D(newX, newY, newZ);
    }

    public IntVector3D multiply(IntVector3D other) {
        return new IntVector3D(this.x * other.x, this.y * other.y, this.z * other.z);
    }

    public IntVector3D multiply(int x, int y, int z) {
        return new IntVector3D(this.x * x, this.y * y, this.z * z);
    }

    public IntVector3D multiply(IntVector3D... others) {
        int newX = this.x;
        int newY = this.y;
        int newZ = this.z;

        for (IntVector3D other : others) {
            newX *= other.x;
            newY *= other.y;
            newZ *= other.z;
        }

        return new IntVector3D(newX, newY, newZ);
    }

    public IntVector3D multiply(double n) {
        return new IntVector3D((int) (this.x * n), (int) (this.y * n), (int) (this.z * n));
    }

    public IntVector3D multiply(float n) {
        return new IntVector3D((int) (this.x * n), (int) (this.y * n), (int) (this.z * n));
    }

    public IntVector3D multiply(int n) {
        return new IntVector3D(this.x * n, this.y * n, this.z * n);
    }

    public IntVector3D divide(IntVector3D other) {
        return new IntVector3D(this.x / other.x, this.y / other.y, this.z / other.z);
    }

    public IntVector3D divide(double x, double y, double z) {
        return new IntVector3D((int) (this.x / x), (int) (this.y / y), (int) (this.z / z));
    }

    public IntVector3D divide(int x, int y, int z) {
        return divide((double) x, y, z);
    }

    public IntVector3D divide(int n) {
        return divide(n, n, n);
    }

    public IntVector3D divide(double n) {
        return divide(n, n, n);
    }

    public IntVector3D divide(float n) {
        return divide(n, n, n);
    }

    public double length() {
        return Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
    }

    public double lengthSq() {
        return this.x * this.x + this.y * this.y + this.z * this.z;
    }

    public double distance(IntVector3D other) {
        return Math.sqrt(distanceSq(other));
    }

    public double distanceSq(IntVector3D other) {
        return (other.x - this.x) * (other.x - this.x) + (other.y - this.y) * (other.y - this.y) + (other.z - this.z) * (other.z - this.z);
    }

    public IntVector3D normalize() {
        return this.divide(this.length());
    }

    public double dot(IntVector3D other) {
        return this.x * other.x + this.y * other.y + this.z * other.z;
    }

    public IntVector3D cross(IntVector3D other) {
        return new IntVector3D(this.y * other.z - this.z * other.y, this.z * other.x - this.x * other.z, this.x * other.y - this.y * other.x);
    }

    public boolean containedWithin(IntVector3D min, IntVector3D max) {
        return this.x >= min.x && this.x <= max.x && this.y >= min.y && this.y <= max.y && this.z >= min.z && this.z <= max.z;
    }

    public IntVector3D clampY(int min, int max) {
        return new IntVector3D(this.x, Math.max(min, Math.min(max, this.y)), this.z);
    }

    public IntVector3D positive() {
        return new IntVector3D(Math.abs(this.x), Math.abs(this.y), Math.abs(this.z));
    }

    public IntVector3D transform2D(double angle, double aboutX, double aboutZ, double translateX, double translateZ) {
        angle = Math.toRadians(angle);
        double x = this.x - aboutX;
        double z = this.z - aboutZ;
        double x2 = x * Math.cos(angle) - z * Math.sin(angle);
        double z2 = x * Math.sin(angle) + z * Math.cos(angle);
        return new IntVector3D((int) (x2 + aboutX + translateX), this.y, (int) (z2 + aboutZ + translateZ));
    }

    public boolean isCollinearWith(IntVector3D other) {
        if (this.x == 0.0D && this.y == 0.0D && this.z == 0.0D) {
            return true;
        } else {
            double otherX = other.x;
            double otherY = other.y;
            double otherZ = other.z;
            if (otherX == 0.0D && otherY == 0.0D && otherZ == 0.0D) {
                return true;
            } else if (this.x == 0.0D != (otherX == 0.0D)) {
                return false;
            } else if (this.y == 0.0D != (otherY == 0.0D)) {
                return false;
            } else if (this.z == 0.0D != (otherZ == 0.0D)) {
                return false;
            } else {
                double quotientX = otherX / this.x;
                if (!Double.isNaN(quotientX)) {
                    return other.equals(this.multiply(quotientX));
                } else {
                    double quotientY = otherY / this.y;
                    if (!Double.isNaN(quotientY)) {
                        return other.equals(this.multiply(quotientY));
                    } else {
                        double quotientZ = otherZ / this.z;
                        if (!Double.isNaN(quotientZ)) {
                            return other.equals(this.multiply(quotientZ));
                        } else {
                            throw new RuntimeException("This should not happen");
                        }
                    }
                }
            }
        }
    }

    public float toPitch() {
        double x = this.getX();
        double z = this.getZ();
        if (x == 0.0D && z == 0.0D) {
            return this.getY() > 0.0D ? -90.0F : 90.0F;
        } else {
            double x2 = x * x;
            double z2 = z * z;
            double xz = Math.sqrt(x2 + z2);
            return (float) Math.toDegrees(Math.atan(-this.getY() / xz));
        }
    }

    public float toYaw() {
        double x = this.getX();
        double z = this.getZ();
        double t = Math.atan2(-x, z);
        double _2pi = 6.283185307179586D;
        return (float) Math.toDegrees((t + _2pi) % _2pi);
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof IntVector3D)) {
            return false;
        } else {
            IntVector3D other = (IntVector3D) obj;
            return other.x == this.x && other.y == this.y && other.z == this.z;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    public int compareTo(@Nullable IntVector3D other) {
        if (other == null) {
            throw new IllegalArgumentException("null not supported");
        } else if (this.y != other.y) {
            return Double.compare(this.y, other.y);
        } else if (this.z != other.z) {
            return Double.compare(this.z, other.z);
        } else {
            return this.x != other.x ? Double.compare(this.x, other.x) : 0;
        }
    }

    public String toString() {
        return "(" + this.x + ", " + this.y + ", " + this.z + ")";
    }

    public IntVector3D swapXZ() {
        return new IntVector3D(z, y, x);
    }

    public Vector3D asVector() {
        return new Vector3D(getX(), getY(), getZ());
    }

    public IntVector3D addXZ(IntVector2D vec) {
        return add(vec.getX(), 0, vec.getY());
    }

    public IntVector2D getXZ() {
        return new IntVector2D(this.x, this.z);
    }
}