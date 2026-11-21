package de.joernott.c0depaint.core.draw;

import de.joernott.c0depaint.core.PaintError;
import de.joernott.c0depaint.core.PaintException;
import de.joernott.c0depaint.core.canvas.Point;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * The rasterization of lines and flat shapes (docs/concept.md#rules-for-all-shapes): a voxel belongs to a shape when
 * its center lies inside; lines and faces are one voxel thick; lines connect through edges and corners.
 */
public final class Raster {
    /** The tolerance of the inside tests, so that voxels on the border of a shape belong to it. */
    static final double EPSILON = 1e-6;

    /** The most voxels a shape may have, so that a typo in a coordinate can't stall the server. */
    static final long MAX_VOXELS = 64L * 1024 * 1024;

    private Raster() {}

    /** The voxels of a straight line, like the Bresenham algorithm in 3D: one voxel per step on the longest axis. */
    public static List<Point> line(Point from, Point to) {
        int steps = Math.max(
                Math.abs(to.x() - from.x()), Math.max(Math.abs(to.y() - from.y()), Math.abs(to.z() - from.z())));
        List<Point> voxels = new ArrayList<>(steps + 1);
        if (steps == 0) {
            voxels.add(from);
            return voxels;
        }
        Vec start = Vec.of(from);
        Vec delta = Vec.of(to).minus(start);
        for (int i = 0; i <= steps; i++) {
            voxels.add(start.plus(delta.times((double) i / steps)).round());
        }
        return voxels;
    }

    /** The voxels of a line through points in space, closed or open, connected with straight lines. */
    public static Set<Point> path(List<Vec> points, boolean closed) {
        Set<Point> voxels = new LinkedHashSet<>();
        int count = points.size();
        for (int i = 0; i < (closed ? count : count - 1); i++) {
            voxels.addAll(
                    line(points.get(i).round(), points.get((i + 1) % count).round()));
        }
        if (count == 1) {
            voxels.add(points.getFirst().round());
        }
        return voxels;
    }

    /**
     * A plane through a point, spanned by two directions. The voxels of a flat shape in the plane are found column by
     * column along the axis on which the plane is steepest: every column gets the voxel nearest to the plane, so the
     * face is one voxel thick and has no holes.
     *
     * @param origin a point of the plane
     * @param u the first direction in the plane, a unit vector
     * @param v the second direction in the plane, a unit vector perpendicular to u
     */
    public record Plane(Vec origin, Vec u, Vec v) {
        /** The plane through three points; the first two give the direction u. NOT_A_PLANE if they lie on one line. */
        public static Plane of(Vec a, Vec b, Vec c) {
            Vec ab = b.minus(a);
            Vec ac = c.minus(a);
            Vec normal = ab.cross(ac);
            if (ab.length() < EPSILON || normal.length() < EPSILON * Math.max(1, ab.length() * ac.length())) {
                throw new PaintException(
                        PaintError.NOT_A_PLANE, "The points " + a + ", " + b + " and " + c + " lie on one line");
            }
            Vec u = ab.unit();
            Vec v = normal.unit().cross(u);
            return new Plane(a, u, v);
        }

        public Vec normal() {
            return u.cross(v);
        }

        /** The point of the plane at the coordinates s along u and t along v. */
        public Vec at(double s, double t) {
            return origin.plus(u.times(s)).plus(v.times(t));
        }

        /** The coordinates along u and v of a point, projected onto the plane. */
        public double[] coordinates(Vec point) {
            Vec relative = point.minus(origin);
            return new double[] {relative.dot(u), relative.dot(v)};
        }

        /** The point projected onto the plane. */
        public Vec project(Vec point) {
            double[] st = coordinates(point);
            return at(st[0], st[1]);
        }

        /**
         * The voxels of the region of the plane for which {@code inside} holds, given in the coordinates along u and v.
         * {@code corners} are points of the plane that enclose the region.
         */
        public Set<Point> fill(List<Vec> corners, Predicate<double[]> inside) {
            Vec normal = normal();
            int axis = 0;
            for (int a = 1; a < 3; a++) {
                if (Math.abs(normal.get(a)) > Math.abs(normal.get(axis))) {
                    axis = a;
                }
            }
            int first = axis == 0 ? 1 : 0;
            int second = axis == 2 ? 1 : 2;
            double[] min = {Double.MAX_VALUE, Double.MAX_VALUE};
            double[] max = {-Double.MAX_VALUE, -Double.MAX_VALUE};
            for (Vec corner : corners) {
                min[0] = Math.min(min[0], corner.get(first));
                min[1] = Math.min(min[1], corner.get(second));
                max[0] = Math.max(max[0], corner.get(first));
                max[1] = Math.max(max[1], corner.get(second));
            }
            long columns = (long) (Math.ceil(max[0]) - Math.floor(min[0]) + 1)
                    * (long) (Math.ceil(max[1]) - Math.floor(min[1]) + 1);
            check(columns);
            Set<Point> voxels = new LinkedHashSet<>();
            for (int i = (int) Math.floor(min[0]); i <= (int) Math.ceil(max[0]); i++) {
                for (int j = (int) Math.floor(min[1]); j <= (int) Math.ceil(max[1]); j++) {
                    // The point of the plane in the column: solve normal · (p - origin) = 0 for the steep axis
                    double[] p = new double[3];
                    p[first] = i;
                    p[second] = j;
                    p[axis] = origin.get(axis)
                            - (normal.get(first) * (i - origin.get(first))
                                            + normal.get(second) * (j - origin.get(second)))
                                    / normal.get(axis);
                    Vec point = new Vec(p[0], p[1], p[2]);
                    if (inside.test(coordinates(point))) {
                        voxels.add(point.round());
                    }
                }
            }
            return voxels;
        }
    }

    /** The voxels of a body: the voxels whose centers are inside, within the box from min to max. */
    public static Set<Point> body(Point min, Point max, Predicate<Vec> inside) {
        check((long) (max.x() - min.x() + 1) * (max.y() - min.y() + 1) * (max.z() - min.z() + 1));
        Set<Point> voxels = new LinkedHashSet<>();
        for (int x = min.x(); x <= max.x(); x++) {
            for (int y = min.y(); y <= max.y(); y++) {
                for (int z = min.z(); z <= max.z(); z++) {
                    if (inside.test(new Vec(x, y, z))) {
                        voxels.add(new Point(x, y, z));
                    }
                }
            }
        }
        return voxels;
    }

    /** The voxels of a body that have a neighbour through a face outside of it: its surface, one voxel thick. */
    public static Set<Point> surface(Set<Point> body) {
        Set<Point> surface = new LinkedHashSet<>();
        int[][] neighbours = {{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};
        for (Point voxel : body) {
            for (int[] n : neighbours) {
                if (!body.contains(new Point(voxel.x() + n[0], voxel.y() + n[1], voxel.z() + n[2]))) {
                    surface.add(voxel);
                    break;
                }
            }
        }
        return surface;
    }

    private static void check(long voxels) {
        if (voxels > MAX_VOXELS || voxels < 0) {
            throw new PaintException(PaintError.CANVAS_LIMIT, "The shape is too large");
        }
    }
}
