package ca.mattlack.actioncompass.detection.util;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Vector3D implements Comparable<Vector3D> {
    public static final Vector3D ZERO = new Vector3D(0, 0, 0);
    public static final Vector3D UNIT_X = new Vector3D(1, 0, 0);
    public static final Vector3D UNIT_Y = new Vector3D(0, 1, 0);
    public static final Vector3D UNIT_Z = new Vector3D(0, 0, 1);
    public static final Vector3D ONE = new Vector3D(1, 1, 1);
    protected final double x;
    protected final double y;
    protected final double z;

    public Vector3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3D(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3D(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3D(Vector3D other) {
        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
    }

    public Vector3D() {
        this.x = 0.0D;
        this.y = 0.0D;
        this.z = 0.0D;
    }

    public Vector3D(IntVector3D intVector3D) {
        this.x = intVector3D.getX();
        this.y = intVector3D.getY();
        this.z = intVector3D.getZ();
    }

    public static Vector3D fromBukkitVector(org.bukkit.util.Vector vec) {
        return new Vector3D(vec.getX(), vec.getY(), vec.getZ());
    }

    public static Vector3D getMinimum(Vector3D v1, Vector3D v2) {
        return new Vector3D(Math.min(v1.x, v2.x), Math.min(v1.y, v2.y), Math.min(v1.z, v2.z));
    }

    public static Vector3D getMaximum(Vector3D v1, Vector3D v2) {
        return new Vector3D(Math.max(v1.x, v2.x), Math.max(v1.y, v2.y), Math.max(v1.z, v2.z));
    }

    public static Vector3D getMidpoint(Vector3D v1, Vector3D v2) {
        return new Vector3D((v1.x + v2.x) / 2.0D, (v1.y + v2.y) / 2.0D, (v1.z + v2.z) / 2.0D);
    }

    public Vector3D clamp(Vector3D min, Vector3D max) {
        return new Vector3D(
                Math.min(Math.max(x, min.x), max.x),
                Math.min(Math.max(y, min.y), max.y),
                Math.min(Math.max(z, min.z), max.z)
        );
    }

    public org.bukkit.util.Vector toBukkitVector() {
        return new org.bukkit.util.Vector(this.x, this.y, this.z);
    }

    public double getX() {
        return this.x;
    }

    public Vector3D setX(double x) {
        return new Vector3D(x, this.y, this.z);
    }

    public Vector3D setX(int x) {
        return new Vector3D((double) x, this.y, this.z);
    }

    public int getBlockX() {
        return (int) Math.round(this.x);
    }

    public double getY() {
        return this.y;
    }

    public Vector3D setY(double y) {
        return new Vector3D(this.x, y, this.z);
    }

    public Vector3D setY(int y) {
        return new Vector3D(this.x, (double) y, this.z);
    }

    public int getBlockY() {
        return (int) Math.round(this.y);
    }

    public double getZ() {
        return this.z;
    }

    public Vector3D setZ(double z) {
        return new Vector3D(this.x, this.y, z);
    }

    public Vector3D setZ(int z) {
        return new Vector3D(this.x, this.y, (double) z);
    }

    public int getBlockZ() {
        return (int) Math.round(this.z);
    }

    public Vector3D add(Vector3D other) {
        return new Vector3D(this.x + other.x, this.y + other.y, this.z + other.z);
    }

    public Vector3D add(double x, double y, double z) {
        return new Vector3D(this.x + x, this.y + y, this.z + z);
    }

    public Vector3D add(int x, int y, int z) {
        return new Vector3D(this.x + (double) x, this.y + (double) y, this.z + (double) z);
    }

    public Vector3D add(Vector3D... others) {
        double newX = this.x;
        double newY = this.y;
        double newZ = this.z;
        Vector3D[] var8 = others;
        int var9 = others.length;

        for (int var10 = 0; var10 < var9; ++var10) {
            Vector3D other = var8[var10];
            newX += other.x;
            newY += other.y;
            newZ += other.z;
        }

        return new Vector3D(newX, newY, newZ);
    }

    public Vector3D subtract(Vector3D other) {
        return new Vector3D(this.x - other.x, this.y - other.y, this.z - other.z);
    }

    public Vector3D subtract(double x, double y, double z) {
        return new Vector3D(this.x - x, this.y - y, this.z - z);
    }

    public Vector3D subtract(int x, int y, int z) {
        return new Vector3D(this.x - (double) x, this.y - (double) y, this.z - (double) z);
    }

    public Vector3D subtract(Vector3D... others) {
        double newX = this.x;
        double newY = this.y;
        double newZ = this.z;
        Vector3D[] var8 = others;
        int var9 = others.length;

        for (int var10 = 0; var10 < var9; ++var10) {
            Vector3D other = var8[var10];
            newX -= other.x;
            newY -= other.y;
            newZ -= other.z;
        }

        return new Vector3D(newX, newY, newZ);
    }

    public Vector3D multiply(Vector3D other) {
        return new Vector3D(this.x * other.x, this.y * other.y, this.z * other.z);
    }

    public Vector3D multiply(double x, double y, double z) {
        return new Vector3D(this.x * x, this.y * y, this.z * z);
    }

    public Vector3D multiply(int x, int y, int z) {
        return new Vector3D(this.x * (double) x, this.y * (double) y, this.z * (double) z);
    }

    public Vector3D multiply(Vector3D... others) {
        double newX = this.x;
        double newY = this.y;
        double newZ = this.z;
        int var9 = others.length;

        for (int var10 = 0; var10 < var9; ++var10) {
            Vector3D other = others[var10];
            newX *= other.x;
            newY *= other.y;
            newZ *= other.z;
        }

        return new Vector3D(newX, newY, newZ);
    }

    public Vector3D multiply(double n) {
        return new Vector3D(this.x * n, this.y * n, this.z * n);
    }

    public Vector3D multiply(float n) {
        return new Vector3D(this.x * (double) n, this.y * (double) n, this.z * (double) n);
    }

    public Vector3D multiply(int n) {
        return new Vector3D(this.x * (double) n, this.y * (double) n, this.z * (double) n);
    }

    public Vector3D divide(Vector3D other) {
        return new Vector3D(this.x / other.x, this.y / other.y, this.z / other.z);
    }

    public Vector3D divide(double x, double y, double z) {
        return new Vector3D(this.x / x, this.y / y, this.z / z);
    }

    public Vector3D divide(int x, int y, int z) {
        return new Vector3D(this.x / (double) x, this.y / (double) y, this.z / (double) z);
    }

    public Vector3D divide(int n) {
        return new Vector3D(this.x / (double) n, this.y / (double) n, this.z / (double) n);
    }

    public Vector3D divide(double n) {
        return new Vector3D(this.x / n, this.y / n, this.z / n);
    }

    public Vector3D divide(float n) {
        return new Vector3D(this.x / (double) n, this.y / (double) n, this.z / (double) n);
    }

    public double length() {
        return Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
    }

    public double lengthSq() {
        return this.x * this.x + this.y * this.y + this.z * this.z;
    }

    public double distance(Vector3D other) {
        return Math.sqrt(distanceSq(other));
    }

    public double distanceSq(Vector3D other) {
        return (other.x - this.x) * (other.x - this.x) + (other.y - this.y) * (other.y - this.y) + (other.z - this.z) * (other.z - this.z);
    }

    public Vector3D normalize() {
        return this.divide(this.length());
    }

    public double dot(Vector3D other) {
        return this.x * other.x + this.y * other.y + this.z * other.z;
    }

    public Vector3D cross(Vector3D other) {
        return new Vector3D(this.y * other.z - this.z * other.y, this.z * other.x - this.x * other.z, this.x * other.y - this.y * other.x);
    }

    public boolean containedWithin(Vector3D min, Vector3D max) {
        return this.x >= min.x && this.x <= max.x && this.y >= min.y && this.y <= max.y && this.z >= min.z && this.z <= max.z;
    }

    public boolean containedWithinBlock(Vector3D min, Vector3D max) {
        return this.getBlockX() >= min.getBlockX() && this.getBlockX() <= max.getBlockX() && this.getBlockY() >= min.getBlockY() && this.getBlockY() <= max.getBlockY() && this.getBlockZ() >= min.getBlockZ() && this.getBlockZ() <= max.getBlockZ();
    }

    public Vector3D clampY(int min, int max) {
        return new Vector3D(this.x, Math.max(min, Math.min(max, this.y)), this.z);
    }

    public Vector3D floor() {
        return new Vector3D(Math.floor(this.x), Math.floor(this.y), Math.floor(this.z));
    }

    public Vector3D ceil() {
        return new Vector3D(Math.ceil(this.x), Math.ceil(this.y), Math.ceil(this.z));
    }

    public Vector3D round() {
        return new Vector3D(Math.floor(this.x + 0.5D), Math.floor(this.y + 0.5D), Math.floor(this.z + 0.5D));
    }

    public Vector3D positive() {
        return new Vector3D(Math.abs(this.x), Math.abs(this.y), Math.abs(this.z));
    }

    public Vector3D transform2D(double angle, double aboutX, double aboutZ, double translateX, double translateZ) {
        angle = Math.toRadians(angle);
        double x = this.x - aboutX;
        double z = this.z - aboutZ;
        double x2 = x * Math.cos(angle) - z * Math.sin(angle);
        double z2 = x * Math.sin(angle) + z * Math.cos(angle);
        return new Vector3D(x2 + aboutX + translateX, this.y, z2 + aboutZ + translateZ);
    }

    public boolean isCollinearWith(Vector3D other) {
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
        if (!(obj instanceof Vector3D)) {
            return false;
        } else {
            Vector3D other = (Vector3D) obj;
            return other.x == this.x && other.y == this.y && other.z == this.z;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    public int compareTo(@NotNull Vector3D other) {
        if (this.y != other.y) {
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

    public Vector3D swapXZ() {
        return new Vector3D(z, y, x);
    }

    public IntVector3D asIntVector() {
        return new IntVector3D(getBlockX(), getBlockY(), getBlockZ());
    }

}