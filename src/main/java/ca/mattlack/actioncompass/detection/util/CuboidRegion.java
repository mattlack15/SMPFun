package ca.mattlack.actioncompass.detection.util;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.*;
import java.util.function.Consumer;

@Getter
@Setter
public class CuboidRegion {
    private Vector3D pos1;
    private Vector3D pos2;
    private final World world;

    public CuboidRegion(Location pos1, Location pos2) {
        this(pos1.getWorld(), new Vector3D(pos1.getX(), pos1.getY(), pos1.getZ()), new Vector3D(pos2.getX(), pos2.getY(), pos2.getZ()));
    }

    public CuboidRegion(World world, Vector3D pos1, Vector3D pos2) {
        Preconditions.checkNotNull(pos1);
        Preconditions.checkNotNull(pos2);
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.world = world;
        this.recalculate();
    }

    public CuboidRegion(World world, IntVector3D pos1, IntVector3D pos2) {
        this(world, pos1.asVector(), pos2.asVector());
    }

    public Vector3D getCenter() {
        return this.getMinimumPoint().add(this.getMaximumPoint()).divide(2);
    }

    public int getArea() {
        Vector3D min = this.getMinimumPoint();
        Vector3D max = this.getMaximumPoint();
        return (int) ((max.getX() - min.getX() + 1.0D) * (max.getY() - min.getY() + 1.0D) * (max.getZ() - min.getZ() + 1.0D));
    }

    public int getWidth() {
        Vector3D min = this.getMinimumPoint();
        Vector3D max = this.getMaximumPoint();
        return (int) (max.getX() - min.getX() + 1.0D);
    }

    public int getHeight() {
        Vector3D min = this.getMinimumPoint();
        Vector3D max = this.getMaximumPoint();
        return (int) (max.getY() - min.getY() + 1.0D);
    }

    public int getLength() {
        Vector3D min = this.getMinimumPoint();
        Vector3D max = this.getMaximumPoint();
        return (int) (max.getZ() - min.getZ() + 1.0D);
    }

    private void recalculate() {
        this.pos1 = this.pos1.clampY(0, this.world == null ? 255 : this.world.getMaxHeight());
        this.pos2 = this.pos2.clampY(0, this.world == null ? 255 : this.world.getMaxHeight());
    }

    public Vector3D getMinimumPoint() {
        return new Vector3D(Math.min(this.pos1.getX(), this.pos2.getX()), Math.min(this.pos1.getY(), this.pos2.getY()), Math.min(this.pos1.getZ(), this.pos2.getZ()));
    }

    public Vector3D getMaximumPoint() {
        return new Vector3D(Math.max(this.pos1.getX(), this.pos2.getX()), Math.max(this.pos1.getY(), this.pos2.getY()), Math.max(this.pos1.getZ(), this.pos2.getZ()));
    }

    public int getMinimumY() {
        return Math.min(this.pos1.getBlockY(), this.pos2.getBlockY());
    }

    public int getMaximumY() {
        return Math.max(this.pos1.getBlockY(), this.pos2.getBlockY());
    }

    public boolean intersectsChunk(int x, int z) {
        Vector3D max = getMaximumPoint();
        Vector3D min = getMinimumPoint();
        int minI = min.getBlockX() >> 4;
        int minJ = min.getBlockZ() >> 4;
        int maxI = max.getBlockX() >> 4;
        int maxJ = max.getBlockZ() >> 4;

        //If the no-unload region contains this chunk
        return x >= minI && x <= maxI && z >= minJ && z <= maxJ;
    }

    public void expand(Vector3D... changes) {
        Preconditions.checkNotNull(changes);

        for (Vector3D change : changes) {
            if (change.getX() > 0.0D) {
                if (Math.max(this.pos1.getX(), this.pos2.getX()) == this.pos1.getX()) {
                    this.pos1 = this.pos1.add(new Vector3D(change.getX(), 0.0D, 0.0D));
                } else {
                    this.pos2 = this.pos2.add(new Vector3D(change.getX(), 0.0D, 0.0D));
                }
            } else if (Math.min(this.pos1.getX(), this.pos2.getX()) == this.pos1.getX()) {
                this.pos1 = this.pos1.add(new Vector3D(change.getX(), 0.0D, 0.0D));
            } else {
                this.pos2 = this.pos2.add(new Vector3D(change.getX(), 0.0D, 0.0D));
            }

            if (change.getY() > 0.0D) {
                if (Math.max(this.pos1.getY(), this.pos2.getY()) == this.pos1.getY()) {
                    this.pos1 = this.pos1.add(new Vector3D(0.0D, change.getY(), 0.0D));
                } else {
                    this.pos2 = this.pos2.add(new Vector3D(0.0D, change.getY(), 0.0D));
                }
            } else if (Math.min(this.pos1.getY(), this.pos2.getY()) == this.pos1.getY()) {
                this.pos1 = this.pos1.add(new Vector3D(0.0D, change.getY(), 0.0D));
            } else {
                this.pos2 = this.pos2.add(new Vector3D(0.0D, change.getY(), 0.0D));
            }

            if (change.getZ() > 0.0D) {
                if (Math.max(this.pos1.getZ(), this.pos2.getZ()) == this.pos1.getZ()) {
                    this.pos1 = this.pos1.add(new Vector3D(0.0D, 0.0D, change.getZ()));
                } else {
                    this.pos2 = this.pos2.add(new Vector3D(0.0D, 0.0D, change.getZ()));
                }
            } else if (Math.min(this.pos1.getZ(), this.pos2.getZ()) == this.pos1.getZ()) {
                this.pos1 = this.pos1.add(new Vector3D(0.0D, 0.0D, change.getZ()));
            } else {
                this.pos2 = this.pos2.add(new Vector3D(0.0D, 0.0D, change.getZ()));
            }
        }

        this.recalculate();
    }

    public void contract(Vector3D... changes) {
        Preconditions.checkNotNull(changes);

        for (Vector3D change : changes) {
            if (change.getX() < 0.0D) {
                if (Math.max(this.pos1.getX(), this.pos2.getX()) == this.pos1.getX()) {
                    this.pos1 = this.pos1.add(new Vector3D(change.getX(), 0.0D, 0.0D));
                } else {
                    this.pos2 = this.pos2.add(new Vector3D(change.getX(), 0.0D, 0.0D));
                }
            } else if (Math.min(this.pos1.getX(), this.pos2.getX()) == this.pos1.getX()) {
                this.pos1 = this.pos1.add(new Vector3D(change.getX(), 0.0D, 0.0D));
            } else {
                this.pos2 = this.pos2.add(new Vector3D(change.getX(), 0.0D, 0.0D));
            }

            if (change.getY() < 0.0D) {
                if (Math.max(this.pos1.getY(), this.pos2.getY()) == this.pos1.getY()) {
                    this.pos1 = this.pos1.add(new Vector3D(0.0D, change.getY(), 0.0D));
                } else {
                    this.pos2 = this.pos2.add(new Vector3D(0.0D, change.getY(), 0.0D));
                }
            } else if (Math.min(this.pos1.getY(), this.pos2.getY()) == this.pos1.getY()) {
                this.pos1 = this.pos1.add(new Vector3D(0.0D, change.getY(), 0.0D));
            } else {
                this.pos2 = this.pos2.add(new Vector3D(0.0D, change.getY(), 0.0D));
            }

            if (change.getZ() < 0.0D) {
                if (Math.max(this.pos1.getZ(), this.pos2.getZ()) == this.pos1.getZ()) {
                    this.pos1 = this.pos1.add(new Vector3D(0.0D, 0.0D, change.getZ()));
                } else {
                    this.pos2 = this.pos2.add(new Vector3D(0.0D, 0.0D, change.getZ()));
                }
            } else if (Math.min(this.pos1.getZ(), this.pos2.getZ()) == this.pos1.getZ()) {
                this.pos1 = this.pos1.add(new Vector3D(0.0D, 0.0D, change.getZ()));
            } else {
                this.pos2 = this.pos2.add(new Vector3D(0.0D, 0.0D, change.getZ()));
            }
        }

        this.recalculate();
    }

    public void shift(Vector3D change) {
        this.pos1 = this.pos1.add(change);
        this.pos2 = this.pos2.add(change);
        this.recalculate();
    }

    public void clampY(double min, double max) {
        double y1 = pos1.y;
        double y2 = pos2.y;

        if (y1 >= y2) {
            pos1 = pos1.setY(Math.min(y1, max));
            pos2 = pos2.setY(Math.max(y2, min));
        } else {
            pos1 = pos1.setY(Math.max(y1, min));
            pos2 = pos2.setY(Math.min(y2, max));
        }
    }

    public Set<Vector3D> getChunkCubes() {
        Set<Vector3D> chunks = new HashSet<>();
        Vector3D min = this.getMinimumPoint();
        Vector3D max = this.getMaximumPoint();

        for (int x = min.getBlockX() >> 4; x <= max.getBlockX() >> 4; ++x) {
            for (int z = min.getBlockZ() >> 4; z <= max.getBlockZ() >> 4; ++z) {
                for (int y = min.getBlockY() >> 4; y <= max.getBlockY() >> 4; ++y) {
                    chunks.add(new Vector3D(x, y, z));
                }
            }
        }

        return chunks;
    }

    public double smallestDistance(Vector3D point) {
        return point.distance(point.clamp(this.getMinimumPoint(), this.getMaximumPoint()));
    }

    public boolean contains(Vector3D position) {
        double x = position.getX();
        double y = position.getY();
        double z = position.getZ();
        Vector3D min = this.getMinimumPoint();
        Vector3D max = this.getMaximumPoint();
        return x >= min.getX() && x <= max.getX() && y >= min.getY() && y <= max.getY() && z >= min.getZ() && z <= max.getZ();
    }

    public boolean contains(IntVector3D position) {
        return contains(position.asVector());
    }

    public Iterator<Vector3D> iterator() {
        return new Iterator<Vector3D>() {
            private final Vector3D min = CuboidRegion.this.getMinimumPoint();
            private final Vector3D max = CuboidRegion.this.getMaximumPoint();
            private int nextX;
            private int nextY;
            private int nextZ;

            {
                this.nextX = this.min.getBlockX();
                this.nextY = this.min.getBlockY();
                this.nextZ = this.min.getBlockZ();
            }

            public boolean hasNext() {
                return this.nextX != -2147483648;
            }

            public Vector3D next() {
                if (!this.hasNext()) {
                    throw new NoSuchElementException();
                } else {
                    Vector3D answer = new Vector3D(this.nextX, this.nextY, this.nextZ);
                    if (++this.nextX > this.max.getBlockX()) {
                        this.nextX = this.min.getBlockX();
                        if (++this.nextY > this.max.getBlockY()) {
                            this.nextY = this.min.getBlockY();
                            if (++this.nextZ > this.max.getBlockZ()) {
                                this.nextX = -2147483648;
                            }
                        }
                    }

                    return answer;
                }
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public String toString() {
        return this.getMinimumPoint() + " - " + this.getMaximumPoint();
    }

    public CuboidRegion clone() {
        return new CuboidRegion(this.world, this.pos1, this.pos2);
    }

    public static CuboidRegion fromCenter(World world, Vector3D origin, int apothemRadiusThingy) {
        Preconditions.checkNotNull(origin);
        Preconditions.checkArgument(apothemRadiusThingy >= 0, "radius/apothem >= 0 required");
        Vector3D size = (new Vector3D(1, 1, 1)).multiply(apothemRadiusThingy);
        return new CuboidRegion(world, origin.subtract(size), origin.add(size));
    }

    public boolean intersects(Vector3D origin, Vector3D direction) {
        return intersects(origin, direction, null, null);
    }

    public List<Vector3D> getIntersectionPoints(Vector3D origin, Vector3D direction) {
        List<Vector3D> points = new ArrayList<>();
        intersects(origin, direction, points::add, points::add);
        return points;
    }

    private boolean intersects(Vector3D origin, Vector3D direction, Consumer<Vector3D> a, Consumer<Vector3D> b) {
        direction = direction.normalize();
        Vector3D dirfrac = new Vector3D(1 / direction.x, 1 / direction.y, 1 / direction.z);

        Vector3D lb = getMinimumPoint();
        Vector3D rt = getMaximumPoint();

        double t1 = (lb.x - origin.x) * dirfrac.x;
        double t2 = (rt.x - origin.x) * dirfrac.x;
        double t3 = (lb.y - origin.y) * dirfrac.y;
        double t4 = (rt.y - origin.y) * dirfrac.y;
        double t5 = (lb.z - origin.z) * dirfrac.z;
        double t6 = (rt.z - origin.z) * dirfrac.z;

        double tmin = Math.max(Math.max(Math.min(t1, t2), Math.min(t3, t4)), Math.min(t5, t6));
        double tmax = Math.min(Math.min(Math.max(t1, t2), Math.max(t3, t4)), Math.max(t5, t6));

        double t;

        if (tmax < 0) {
            return false;
        }

        if (tmin > tmax) {
            return false;
        }

        t = tmin;
        if (a != null)
            a.accept(origin.add(direction.multiply(t)));
        return true;
    }


    public static void main(String[] args) {
        for (int i = 0; i < 10; ) {

            //

            for (int j = 0; j < 10; ) {
                System.out.println(i + " " + j);
                j++;
            }

            //
            i++;
        }
    }
}
