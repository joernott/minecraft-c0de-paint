package de.joernott.c0depaint.core.draw;

import de.joernott.c0depaint.core.PaintError;
import de.joernott.c0depaint.core.PaintException;
import de.joernott.c0depaint.core.canvas.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
