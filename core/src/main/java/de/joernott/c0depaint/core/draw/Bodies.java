package de.joernott.c0depaint.core.draw;

import de.joernott.c0depaint.core.canvas.Point;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * The bodies (docs/concept.md#functions): their edges or rims, faces and inside as voxels. Bodies are axis-aligned or
 * have one of the six directions; the Platonic solids have a fixed orientation.
 */
public final class Bodies {
    private static final double EPSILON = Raster.EPSILON;

    private Bodies() {}

    /** An axis-aligned box between two opposite corners: the 12 edges, the six sides and the inside. */
    public static Shapes.Voxels box(Point from, Point to) {
        Point min = new Point(Math.min(from.x(), to.x()), Math.min(from.y(), to.y()), Math.min(from.z(), to.z()));
        Point max = new Point(Math.max(from.x(), to.x()), Math.max(from.y(), to.y()), Math.max(from.z(), to.z()));
        Set<Point> inside = Raster.body(min, max, voxel -> true);
        Set<Point> edges = new LinkedHashSet<>();
        int[] xs = {min.x(), max.x()};
        int[] ys = {min.y(), max.y()};
        int[] zs = {min.z(), max.z()};
        for (int y : ys) {
            for (int z : zs) {
                edges.addAll(Raster.line(new Point(min.x(), y, z), new Point(max.x(), y, z)));
            }
        }
        for (int x : xs) {
            for (int z : zs) {
                edges.addAll(Raster.line(new Point(x, min.y(), z), new Point(x, max.y(), z)));
            }
            for (int y : ys) {
                edges.addAll(Raster.line(new Point(x, y, min.z()), new Point(x, y, max.z())));
            }
        }
        return new Shapes.Voxels(edges, Raster.surface(inside), inside);
    }

    /** An axis-aligned ellipsoid; a sphere when the radii are equal. A radius of 0 makes it flat on that axis. */
    public static Shapes.Voxels ellipsoid(Point center, int rx, int ry, int rz) {
        positive("rx", rx, 0);
        positive("ry", ry, 0);
        positive("rz", rz, 0);
        Set<Point> inside = Raster.body(
                new Point(center.x() - rx, center.y() - ry, center.z() - rz),
                new Point(center.x() + rx, center.y() + ry, center.z() + rz),
                voxel -> term(voxel.x() - center.x(), rx)
                                + term(voxel.y() - center.y(), ry)
                                + term(voxel.z() - center.z(), rz)
                        <= 1 + EPSILON);
        return new Shapes.Voxels(Set.of(), Raster.surface(inside), inside);
    }

    private static double term(double distance, int radius) {
        if (radius == 0) {
            return Math.abs(distance) < EPSILON ? 0 : Double.MAX_VALUE;
        }
        return (distance / radius) * (distance / radius);
    }

    /**
     * One of the six directions, "+x", "-x", "+y", "-y", "+z" or "-z".
     *
     * @param axis 0 for x, 1 for y, 2 for z
     * @param sign 1 or -1
     */
    public record Direction(int axis, int sign) {
        public static Direction of(String text) {
            if (text.length() == 2
                    && (text.charAt(0) == '+' || text.charAt(0) == '-')
                    && "xyz".indexOf(text.charAt(1)) >= 0) {
                return new Direction("xyz".indexOf(text.charAt(1)), text.charAt(0) == '+' ? 1 : -1);
            }
            throw new IllegalArgumentException("The direction is '" + text + "', not +x, -x, +y, -y, +z or -z");
        }

        /** The coordinates of a voxel relative to the base: t along the direction, u and v across it. */
        double[] local(Vec voxel, Point base) {
            double[] d = {voxel.x() - base.x(), voxel.y() - base.y(), voxel.z() - base.z()};
            int u = axis == 0 ? 1 : 0;
            int v = axis == 2 ? 1 : 2;
            return new double[] {d[axis] * sign, d[u], d[v]};
        }

        /** The point at the distance t along the direction and u, v across it. */
        Point point(Point base, int t, int u, int v) {
            int[] d = new int[3];
            d[axis] = t * sign;
            d[axis == 0 ? 1 : 0] = u;
            d[axis == 2 ? 1 : 2] = v;
            return new Point(base.x() + d[0], base.y() + d[1], base.z() + d[2]);
        }

        /** The box of a body with the radius r across and the length h along the direction. */
        Point[] box(Point base, int r, int h) {
            Point a = point(base, 0, -r, -r);
            Point b = point(base, h, r, r);
            return new Point[] {
                new Point(Math.min(a.x(), b.x()), Math.min(a.y(), b.y()), Math.min(a.z(), b.z())),
                new Point(Math.max(a.x(), b.x()), Math.max(a.y(), b.y()), Math.max(a.z(), b.z()))
            };
        }
    }

    /** A cylinder: a disc of radius r around the center, grown by h voxels in the direction; the rims are lines. */
    public static Shapes.Voxels cylinder(Point center, int r, int h, Direction direction) {
        positive("r", r, 0);
        positive("h", h, 1);
        Point[] box = direction.box(center, r, h - 1);
        Set<Point> inside = Raster.body(box[0], box[1], voxel -> {
            double[] l = direction.local(voxel, center);
            return l[0] >= -EPSILON && l[0] <= h - 1 + EPSILON && l[1] * l[1] + l[2] * l[2] <= r * r + EPSILON;
        });
        Set<Point> rims = new LinkedHashSet<>(rim(center, r, 0, direction, inside));
        rims.addAll(rim(center, r, h - 1, direction, inside));
        return new Shapes.Voxels(rims, Raster.surface(inside), inside);
    }

    /** A cone with the base of a cylinder; its tip lies h voxels from the base. The rim of the base is a line. */
    public static Shapes.Voxels cone(Point center, int r, int h, Direction direction) {
        positive("r", r, 0);
        positive("h", h, 1);
        Point[] box = direction.box(center, r, h);
        Set<Point> inside = Raster.body(box[0], box[1], voxel -> {
            double[] l = direction.local(voxel, center);
            double radius = r * (1 - l[0] / h);
            return l[0] >= -EPSILON && l[0] <= h + EPSILON && l[1] * l[1] + l[2] * l[2] <= radius * radius + EPSILON;
        });
        return new Shapes.Voxels(rim(center, r, 0, direction, inside), Raster.surface(inside), inside);
    }

    /**
     * A pyramid with a square base, its sides parallel to the axes: half the length of a side is r, the tip lies h
     * voxels from the base. The edges are the four sides of the base and the four lines to the tip.
     */
    public static Shapes.Voxels pyramid(Point center, int r, int h, Direction direction) {
        positive("r", r, 0);
        positive("h", h, 1);
        Point[] box = direction.box(center, r, h);
        Set<Point> inside = Raster.body(box[0], box[1], voxel -> {
            double[] l = direction.local(voxel, center);
            double half = r * (1 - l[0] / h);
            return l[0] >= -EPSILON
                    && l[0] <= h + EPSILON
                    && Math.abs(l[1]) <= half + EPSILON
                    && Math.abs(l[2]) <= half + EPSILON;
        });
        Point tip = direction.point(center, h, 0, 0);
        Point[] corners = {
            direction.point(center, 0, -r, -r),
            direction.point(center, 0, r, -r),
            direction.point(center, 0, r, r),
            direction.point(center, 0, -r, r)
        };
        Set<Point> edges = new LinkedHashSet<>();
        for (int i = 0; i < 4; i++) {
            edges.addAll(Raster.line(corners[i], corners[(i + 1) % 4]));
            edges.addAll(Raster.line(corners[i], tip));
        }
        return new Shapes.Voxels(edges, Raster.surface(inside), inside);
    }

    /** The voxels of a layer of a body that touch a voxel outside of it in that layer: the outline of the layer. */
    private static Set<Point> rim(Point center, int r, int t, Direction direction, Set<Point> body) {
        Set<Point> rim = new LinkedHashSet<>();
        for (int u = -r; u <= r; u++) {
            for (int v = -r; v <= r; v++) {
                Point voxel = direction.point(center, t, u, v);
                if (body.contains(voxel)
                        && (!body.contains(direction.point(center, t, u + 1, v))
                                || !body.contains(direction.point(center, t, u - 1, v))
                                || !body.contains(direction.point(center, t, u, v + 1))
                                || !body.contains(direction.point(center, t, u, v - 1)))) {
                    rim.add(voxel);
                }
            }
        }
        return rim;
    }

    /** The Platonic solids with their corners at the distance 1 from the center, in their fixed orientation. */
    public enum Solid {
        TETRAHEDRON(scaled(1 / Math.sqrt(3), new double[][] {{1, 1, 1}, {1, -1, -1}, {-1, 1, -1}, {-1, -1, 1}})),
        OCTAHEDRON(new double[][] {{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}}),
        DODECAHEDRON(scaled(1 / Math.sqrt(3), dodecahedron())),
        ICOSAHEDRON(scaled(1 / Math.sqrt(1 + PHI() * PHI()), icosahedron()));

        private final double[][] corners;

        Solid(double[][] corners) {
            this.corners = corners;
        }

        private static double PHI() {
            return (1 + Math.sqrt(5)) / 2;
        }

        private static double[][] scaled(double factor, double[][] points) {
            double[][] result = new double[points.length][];
            for (int i = 0; i < points.length; i++) {
                result[i] = new double[] {points[i][0] * factor, points[i][1] * factor, points[i][2] * factor};
            }
            return result;
        }

        private static double[][] icosahedron() {
            double phi = PHI();
            List<double[]> points = new ArrayList<>();
            for (int a : new int[] {1, -1}) {
                for (int b : new int[] {1, -1}) {
                    points.add(new double[] {0, a, b * phi});
                    points.add(new double[] {a, b * phi, 0});
                    points.add(new double[] {b * phi, 0, a});
                }
            }
            return points.toArray(double[][]::new);
        }

        private static double[][] dodecahedron() {
            double phi = PHI();
            List<double[]> points = new ArrayList<>();
            for (int a : new int[] {1, -1}) {
                for (int b : new int[] {1, -1}) {
                    for (int c : new int[] {1, -1}) {
                        points.add(new double[] {a, b, c});
                    }
                    points.add(new double[] {0, a / phi, b * phi});
                    points.add(new double[] {a / phi, b * phi, 0});
                    points.add(new double[] {a * phi, 0, b / phi});
                }
            }
            return points.toArray(double[][]::new);
        }

        /** The corners around the center at the distance r. */
        public List<Vec> corners(Point center, int r) {
            List<Vec> result = new ArrayList<>();
            for (double[] corner : corners) {
                result.add(Vec.of(center).plus(new Vec(corner[0], corner[1], corner[2]).times(r)));
            }
            return result;
        }
    }

    /** A Platonic solid around the center, with all corners at the distance r. */
    public static Shapes.Voxels solid(Solid solid, Point center, int r) {
        positive("r", r, 1);
        List<Vec> corners = solid.corners(center, r);
        // The planes of the faces: the planes through three corners that have all corners on one side. A face is
        // its unit normal and its distance from the center.
        List<Vec> normals = new ArrayList<>();
        List<Double> distances = new ArrayList<>();
        Vec middle = Vec.of(center);
        for (int i = 0; i < corners.size(); i++) {
            for (int j = i + 1; j < corners.size(); j++) {
                for (int k = j + 1; k < corners.size(); k++) {
                    Vec normal = corners.get(j)
                            .minus(corners.get(i))
                            .cross(corners.get(k).minus(corners.get(i)));
                    if (normal.length() < EPSILON) {
                        continue;
                    }
                    normal = normal.unit();
                    if (normal.dot(corners.get(i).minus(middle)) < 0) {
                        normal = normal.times(-1);
                    }
                    double distance = normal.dot(corners.get(i).minus(middle));
                    if (supporting(normal, distance, corners, middle, r) && !known(normals, normal)) {
                        normals.add(normal);
                        distances.add(distance);
                    }
                }
            }
        }
        Predicate<Vec> inside = voxel -> {
            Vec relative = voxel.minus(middle);
            for (int f = 0; f < normals.size(); f++) {
                if (normals.get(f).dot(relative) > distances.get(f) + EPSILON) {
                    return false;
                }
            }
            return true;
        };
        Set<Point> body = Raster.body(
                new Point(center.x() - r, center.y() - r, center.z() - r),
                new Point(center.x() + r, center.y() + r, center.z() + r),
                inside);
        // The edges join the corners at the shortest distance between two corners
        double shortest = Double.MAX_VALUE;
        for (int i = 0; i < corners.size(); i++) {
            for (int j = i + 1; j < corners.size(); j++) {
                shortest =
                        Math.min(shortest, corners.get(i).minus(corners.get(j)).length());
            }
        }
        Set<Point> surface = Raster.surface(body);
        Set<Point> edges = new LinkedHashSet<>();
        for (int i = 0; i < corners.size(); i++) {
            for (int j = i + 1; j < corners.size(); j++) {
                if (corners.get(i).minus(corners.get(j)).length() < shortest * (1 + 1e-6)) {
                    // The line between the rounded corners can leave the body of a small solid; such voxels move
                    // onto the nearest voxel of its surface (task 9.1.5)
                    for (Point voxel : Raster.line(corners.get(i).round(), corners.get(j).round())) {
                        Point onBody = body.contains(voxel) ? voxel : nearest(surface, voxel);
                        if (onBody != null) {
                            edges.add(onBody);
                        }
                    }
                }
            }
        }
        return new Shapes.Voxels(edges, surface, body);
    }

    /** The voxel of the surface nearest to a voxel outside of it, searched in growing cubes; null if there is none. */
    private static Point nearest(Set<Point> surface, Point voxel) {
        for (int d = 1; d <= 3; d++) {
            Point best = null;
            int bestDistance = Integer.MAX_VALUE;
            for (int x = -d; x <= d; x++) {
                for (int y = -d; y <= d; y++) {
                    for (int z = -d; z <= d; z++) {
                        Point candidate = new Point(voxel.x() + x, voxel.y() + y, voxel.z() + z);
                        int distance = x * x + y * y + z * z;
                        if (distance < bestDistance && surface.contains(candidate)) {
                            best = candidate;
                            bestDistance = distance;
                        }
                    }
                }
            }
            if (best != null) {
                return best;
            }
        }
        return null;
    }

    /** True if all corners lie on the inner side of the plane. */
    private static boolean supporting(Vec normal, double distance, List<Vec> corners, Vec middle, int r) {
        for (Vec corner : corners) {
            if (normal.dot(corner.minus(middle)) > distance + 1e-6 * r) {
                return false;
            }
        }
        return true;
    }

    private static boolean known(List<Vec> normals, Vec normal) {
        return normals.stream().anyMatch(other -> other.minus(normal).length() < 1e-6);
    }

    private static void positive(String name, int value, int min) {
        if (value < min) {
            throw new IllegalArgumentException(name + " is " + value + ", not " + min + " or more");
        }
    }
}
