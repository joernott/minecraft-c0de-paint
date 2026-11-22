package de.joernott.c0depaint.core.draw;

import de.joernott.c0depaint.core.PaintError;
import de.joernott.c0depaint.core.PaintException;
import de.joernott.c0depaint.core.canvas.Point;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/** The flat shapes (docs/concept.md#functions): their outlines and areas as voxels, in any plane. */
public final class Shapes {
    private Shapes() {}

    /**
     * The voxels of a shape, by part.
     *
     * @param lines the outline or the edges
     * @param faces the surface of a body; empty for flat shapes
     * @param inside the area of a flat shape or the volume of a body
     */
    public record Voxels(Set<Point> lines, Set<Point> faces, Set<Point> inside) {}

    /** A rectangle: a and b are the ends of one side, the other side runs perpendicular up to the distance of c. */
    public static Voxels rectangle(Point a, Point b, Point c) {
        Vec va = Vec.of(a);
        Vec vb = Vec.of(b);
        Raster.Plane plane = Raster.Plane.of(va, vb, Vec.of(c));
        double height = plane.coordinates(Vec.of(c))[1];
        Vec up = plane.v().times(height);
        return polygon(plane, List.of(va, vb, vb.plus(up), va.plus(up)));
    }

    /**
     * A polygon through the points, closed automatically. The first three points that don't lie on one line define
     * the plane; the other points are projected onto it.
     */
    public static Voxels polygon(List<Point> points) {
        if (points.size() < 3) {
            throw new PaintException(PaintError.NOT_A_PLANE, "A polygon needs at least three points");
        }
        List<Vec> corners = new ArrayList<>();
        points.forEach(point -> corners.add(Vec.of(point)));
        Raster.Plane plane = null;
        for (int i = 2; i < corners.size() && plane == null; i++) {
            try {
                plane = Raster.Plane.of(corners.get(0), corners.get(1), corners.get(i));
            } catch (PaintException e) {
                // The point lies on the line of the first two; try the next one
            }
        }
        if (plane == null) {
            throw new PaintException(PaintError.NOT_A_PLANE, "The points of the polygon lie on one line");
        }
        List<Vec> projected = new ArrayList<>();
        for (Vec corner : corners) {
            projected.add(plane.project(corner));
        }
        return polygon(plane, projected);
    }

    private static Voxels polygon(Raster.Plane plane, List<Vec> corners) {
        List<double[]> outline = new ArrayList<>();
        corners.forEach(corner -> outline.add(plane.coordinates(corner)));
        Set<Point> inside = plane.fill(corners, point -> insidePolygon(outline, point));
        return new Voxels(Raster.path(corners, true), Set.of(), inside);
    }

    /** True if the point lies inside the polygon or on its border (even-odd rule). */
    static boolean insidePolygon(List<double[]> polygon, double[] point) {
        boolean inside = false;
        int count = polygon.size();
        for (int i = 0, j = count - 1; i < count; j = i++) {
            double[] a = polygon.get(i);
            double[] b = polygon.get(j);
            if (onSegment(a, b, point)) {
                return true;
            }
            if ((a[1] > point[1]) != (b[1] > point[1])
                    && point[0] < (b[0] - a[0]) * (point[1] - a[1]) / (b[1] - a[1]) + a[0]) {
                inside = !inside;
            }
        }
        return inside;
    }

    private static boolean onSegment(double[] a, double[] b, double[] p) {
        double cross = (b[0] - a[0]) * (p[1] - a[1]) - (b[1] - a[1]) * (p[0] - a[0]);
        double length = Math.hypot(b[0] - a[0], b[1] - a[1]);
        if (Math.abs(cross) > 1e-4 * Math.max(1, length)) {
            return false;
        }
        double dot = (p[0] - a[0]) * (b[0] - a[0]) + (p[1] - a[1]) * (b[1] - a[1]);
        return dot >= -1e-6 && dot <= length * length + 1e-6;
    }

    /**
     * The ellipse of DRAW_ELLIPSE and DRAW_ARC: {@code a} is the end of the first semi-axis, {@code b} lies in the plane
     * and its distance from the first axis is the second semi-axis.
     */
    public record Ellipse(Raster.Plane plane, double rx, double ry) {
        public static Ellipse of(Point center, Point a, Point b) {
            Vec c = Vec.of(center);
            Raster.Plane plane = Raster.Plane.of(c, Vec.of(a), Vec.of(b));
            double rx = Vec.of(a).minus(c).length();
            double ry = Math.abs(plane.coordinates(Vec.of(b))[1]);
            return new Ellipse(plane, rx, ry);
        }

        /** The point of the ellipse at the angle t (radians), counted from a towards b. */
        Vec at(double t) {
            return plane.at(rx * Math.cos(t), ry * Math.sin(t));
        }

        /** The points of the curve from start to end (radians), close enough to connect them with lines. */
        List<Vec> curve(double start, double end) {
            int steps = (int) Math.ceil(Math.abs(end - start) * Math.max(rx, ry) * 2) + 4;
            List<Vec> points = new ArrayList<>();
            for (int i = 0; i <= steps; i++) {
                points.add(at(start + (end - start) * i / steps));
            }
            return points;
        }

        boolean inside(double[] st) {
            double x = st[0] / rx;
            double y = st[1] / ry;
            return x * x + y * y <= 1 + Raster.EPSILON;
        }

        List<Vec> corners() {
            return List.of(plane.at(rx, ry), plane.at(-rx, ry), plane.at(rx, -ry), plane.at(-rx, -ry));
        }
    }

    /** An ellipse. */
    public static Voxels ellipse(Point center, Point a, Point b) {
        Ellipse ellipse = Ellipse.of(center, a, b);
        Set<Point> inside = ellipse.plane().fill(ellipse.corners(), ellipse::inside);
        return new Voxels(Raster.path(ellipse.curve(0, 2 * Math.PI), true), Set.of(), inside);
    }

    /**
     * A spiral on the ellipse of DRAW_ELLIPSE that rises in a direction: it starts at a and turns towards b; every voxel
     * of the line lies {@code elevation} layers higher than the one before (rounded down, so 0.5 rises every second
     * voxel), up to the layer h - 1. The inside is the ramp between the axis through the center and the line, like the
     * steps of a spiral staircase: every voxel of the ellipse takes the layer of the nearest voxel of the line in each
     * turn, and the axis is drawn in every layer.
     *
     * @throws IllegalArgumentException if the elevation is not more than 0 or h is less than 1
     */
    public static Voxels spiral(Point center, Point a, Point b, Bodies.Direction direction, double elevation, int h) {
        if (!(elevation > 0) || Double.isInfinite(elevation)) {
            throw new IllegalArgumentException("The elevation is " + elevation + ", not more than 0");
        }
        if (h < 1) {
            throw new IllegalArgumentException("The height is " + h + ", not 1 or more");
        }
        Ellipse ellipse = Ellipse.of(center, a, b);
        List<Point> turn = new ArrayList<>(Raster.path(ellipse.curve(0, 2 * Math.PI), true));
        int perTurn = turn.size();
        // The voxels of the line: one per step, until the layer h is reached
        long steps = (long) Math.ceil(h / elevation) + 1;
        Raster.check(steps);
        Set<Point> line = new LinkedHashSet<>();
        for (long k = 0; ; k++) {
            int layer = layer(k, elevation);
            if (layer >= h) {
                break;
            }
            line.add(direction.point(turn.get((int) (k % perTurn)), layer, 0, 0));
        }
        // The angles of the voxels of one turn, to find the step of a voxel of the ramp
        TreeMap<Double, Integer> angles = new TreeMap<>();
        for (int j = 0; j < perTurn; j++) {
            angles.putIfAbsent(angle(ellipse, Vec.of(turn.get(j))), j);
        }
        Set<Point> disc = ellipse.plane().fill(ellipse.corners(), ellipse::inside);
        long turns = steps / perTurn + 2;
        Raster.check(turns * disc.size());
        Set<Point> inside = new LinkedHashSet<>();
        for (int layer = 0; layer < h; layer++) {
            inside.add(direction.point(center, layer, 0, 0));
        }
        for (Point voxel : disc) {
            if (voxel.equals(center)) {
                continue;
            }
            int j = nearest(angles, angle(ellipse, Vec.of(voxel)));
            for (long m = 0; m < turns; m++) {
                int layer = layer(j + m * perTurn, elevation);
                if (layer >= h) {
                    break;
                }
                inside.add(direction.point(voxel, layer, 0, 0));
            }
        }
        return new Voxels(line, Set.of(), inside);
    }

    /** The layer of the step k of a spiral: k times the elevation, rounded down. */
    private static int layer(long k, double elevation) {
        return (int) Math.min(Integer.MAX_VALUE, Math.floor(k * elevation + 1e-9));
    }

    /** The angle of the parameter of the ellipse at a point, from 0 to 2π, counted from a towards b. */
    private static double angle(Ellipse ellipse, Vec point) {
        double[] st = ellipse.plane().coordinates(point);
        double angle = Math.atan2(st[1] / ellipse.ry(), st[0] / ellipse.rx());
        return angle < 0 ? angle + 2 * Math.PI : angle;
    }

    /** The index of the voxel of the turn whose angle is nearest, across the start of the turn. */
    private static int nearest(TreeMap<Double, Integer> angles, double angle) {
        Map.Entry<Double, Integer> below = angles.floorEntry(angle);
        Map.Entry<Double, Integer> above = angles.ceilingEntry(angle);
        if (below == null) {
            below = angles.lastEntry();
        }
        if (above == null) {
            above = angles.firstEntry();
        }
        double toBelow = (angle - below.getKey() + 2 * Math.PI) % (2 * Math.PI);
        double toAbove = (above.getKey() - angle + 2 * Math.PI) % (2 * Math.PI);
        return toBelow <= toAbove ? below.getValue() : above.getValue();
    }

    /**
     * The part of the ellipse from the angle start to end (degrees, counted from a towards b): the line is the arc, the
     * inside the pie slice.
     */
    public static Voxels arc(Point center, Point a, Point b, double start, double end) {
        Ellipse ellipse = Ellipse.of(center, a, b);
        if (end < start) {
            double swap = start;
            start = end;
            end = swap;
        }
        if (end - start >= 360) {
            return ellipse(center, a, b);
        }
        double from = Math.toRadians(start);
        double to = Math.toRadians(end);
        Set<Point> inside = ellipse.plane().fill(ellipse.corners(), st -> {
            if (!ellipse.inside(st)) {
                return false;
            }
            if (Math.abs(st[0]) < Raster.EPSILON && Math.abs(st[1]) < Raster.EPSILON) {
                return true;
            }
            // The angle of the parameter of the ellipse, so that the slice ends on the points of the arc
            double angle = Math.atan2(st[1] / ellipse.ry(), st[0] / ellipse.rx());
            double turns = Math.floor((angle - from) / (2 * Math.PI));
            angle -= turns * 2 * Math.PI;
            return angle <= to + 1e-9;
        });
        return new Voxels(Raster.path(ellipse.curve(from, to), false), Set.of(), inside);
    }
}
