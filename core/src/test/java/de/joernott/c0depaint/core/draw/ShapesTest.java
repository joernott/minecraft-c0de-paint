package de.joernott.c0depaint.core.draw;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.joernott.c0depaint.core.PaintError;
import de.joernott.c0depaint.core.PaintException;
import de.joernott.c0depaint.core.canvas.Point;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** The rasterization of lines and flat shapes (task 4.1 to 4.4). */
class ShapesTest {
    private static Point p(int x, int y, int z) {
        return new Point(x, y, z);
    }

    /** Every voxel touches the next one through a face, an edge or a corner, and no step is a double. */
    private static void assertThinAndConnected(List<Point> line) {
        for (int i = 1; i < line.size(); i++) {
            Point a = line.get(i - 1);
            Point b = line.get(i);
            int dx = Math.abs(a.x() - b.x());
            int dy = Math.abs(a.y() - b.y());
            int dz = Math.abs(a.z() - b.z());
            assertTrue(dx <= 1 && dy <= 1 && dz <= 1 && dx + dy + dz > 0, a + " -> " + b);
        }
    }

    @Test
    void linesConnectThroughEdgesAndCorners() {
        List<Point> line = Raster.line(p(0, 0, 0), p(9, 3, -2));
        assertEquals(10, line.size());
        assertEquals(p(0, 0, 0), line.getFirst());
        assertEquals(p(9, 3, -2), line.getLast());
        assertThinAndConnected(line);
        assertEquals(List.of(p(4, 4, 4)), Raster.line(p(4, 4, 4), p(4, 4, 4)));
    }

    @Test
    void rectanglesOfTheConcept() {
        Shapes.Voxels rectangle = Shapes.rectangle(p(0, 0, 0), p(9, 0, 0), p(0, 0, 4));
        assertEquals(50, rectangle.inside().size());
        assertEquals(26, rectangle.lines().size());
        assertTrue(rectangle.inside().stream()
                .allMatch(v -> v.y() == 0 && v.x() >= 0 && v.x() <= 9 && v.z() >= 0 && v.z() <= 4));
        // c only gives the distance: a point beside the end of a-b gives the same rectangle
        assertEquals(rectangle, Shapes.rectangle(p(0, 0, 0), p(9, 0, 0), p(30, 0, 4)));
    }

    @Test
    void pointsOnALineAreNotAPlane() {
        PaintException error =
                assertThrows(PaintException.class, () -> Shapes.rectangle(p(0, 0, 0), p(9, 0, 0), p(20, 0, 0)));
        assertEquals(PaintError.NOT_A_PLANE, error.error());
        assertThrows(PaintException.class, () -> Shapes.polygon(List.of(p(0, 0, 0), p(1, 1, 1), p(2, 2, 2))));
        assertThrows(PaintException.class, () -> Shapes.ellipse(p(0, 0, 0), p(5, 0, 0), p(-5, 0, 0)));
    }

    @Test
    void polygonsAreClosedAndUseTheFirstPlane() {
        // The second point lies on the line of the first and the third, so the fourth point defines the plane
        Shapes.Voxels triangle = Shapes.polygon(List.of(p(0, 0, 0), p(10, 0, 0), p(20, 0, 0), p(0, 0, 10)));
        assertTrue(triangle.lines().contains(p(10, 0, 5)), "the closing edge from 20,0,0 to 0,0,10");
        assertTrue(triangle.inside().stream().allMatch(v -> v.y() == 0));
        // A point beside the plane is projected onto it
        Shapes.Voxels projected = Shapes.polygon(List.of(p(0, 0, 0), p(10, 0, 0), p(0, 0, 10), p(10, 7, 10)));
        assertTrue(projected.inside().stream().allMatch(v -> v.y() == 0));
        assertTrue(projected.inside().contains(p(10, 0, 10)));
    }

    @Test
    void circlesHaveTheAreaOfTheirCenters() {
        Shapes.Voxels circle = Shapes.ellipse(p(0, 0, 0), p(5, 0, 0), p(0, 0, 5));
        // The voxels whose centers lie within the radius 5
        int expected = 0;
        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                if (x * x + z * z <= 25) {
                    expected++;
                }
            }
        }
        assertEquals(expected, circle.inside().size());
        assertTrue(circle.lines().stream().allMatch(v -> {
            double r = Math.hypot(v.x(), v.z());
            return v.y() == 0 && r > 4.0 && r < 5.8;
        }));
    }

    @Test
    void circlesAndEllipsesAreMirrorSymmetric() {
        // Found for c0de.at: the outlines of r = 3 and 5 weren't symmetric (task 9.1.4)
        for (int rx = 1; rx <= 12; rx++) {
            for (int rz : new int[] {rx, rx + 2}) {
                Shapes.Voxels ellipse = Shapes.ellipse(p(0, 0, 0), p(rx, 0, 0), p(0, 0, rz));
                Set<Point> lines = ellipse.lines();
                for (Point v : lines) {
                    String shape = "rx " + rx + ", rz " + rz + ": " + v;
                    assertTrue(lines.contains(p(-v.x(), v.y(), v.z())), shape + " has no mirror in x");
                    assertTrue(lines.contains(p(v.x(), v.y(), -v.z())), shape + " has no mirror in z");
                }
                // The outline closes: every voxel touches two others (also over an edge of the grid); a shape one
                // voxel wide is a line with two ends
                for (Point v : rx < 2 ? Set.<Point>of() : lines) {
                    long neighbours = lines.stream()
                            .filter(w -> !w.equals(v) && Math.abs(w.x() - v.x()) <= 1 && Math.abs(w.z() - v.z()) <= 1)
                            .count();
                    assertTrue(neighbours >= 2, "rx " + rx + ", rz " + rz + ": " + v + " is an open end");
                }
            }
        }
    }

    @Test
    void tiltedCirclesAreOneVoxelThickWithoutHoles() {
        // A circle of radius 20, tilted by about 10° about the x axis
        Shapes.Voxels circle = Shapes.ellipse(p(0, 0, 0), p(20, 0, 0), p(0, 4, 20));
        Set<String> columns = new HashSet<>();
        for (Point voxel : circle.inside()) {
            assertTrue(columns.add(voxel.x() + "," + voxel.z()), "two voxels in the column of " + voxel);
        }
        // Without holes: every column in the projected ellipse has its voxel
        for (int x = -18; x <= 18; x++) {
            for (int z = -15; z <= 15; z++) {
                double ry = 20 * Math.cos(Math.atan2(4, 20));
                if ((x / 20.0) * (x / 20.0) + (z / ry) * (z / ry) < 0.9) {
                    assertTrue(columns.contains(x + "," + z), "hole at " + x + "," + z);
                }
            }
        }
    }

    @Test
    void arcsAndPieSlices() {
        Shapes.Voxels quarter = Shapes.arc(p(0, 0, 0), p(10, 0, 0), p(0, 0, 10), 0, 90);
        assertTrue(
                quarter.inside().stream().allMatch(v -> v.x() >= 0 && v.z() >= 0),
                quarter.inside().toString());
        assertTrue(quarter.inside().contains(p(5, 0, 5)));
        assertTrue(quarter.lines().contains(p(10, 0, 0)));
        assertTrue(quarter.lines().contains(p(0, 0, 10)));
        assertTrue(quarter.lines().stream().allMatch(v -> v.x() >= 0 && v.z() >= 0));
        Shapes.Voxels full = Shapes.arc(p(0, 0, 0), p(10, 0, 0), p(0, 0, 10), -90, 270);
        assertEquals(Shapes.ellipse(p(0, 0, 0), p(10, 0, 0), p(0, 0, 10)).inside(), full.inside());
    }

    /**
     * A spiral rises one layer per voxel by default, every second voxel with the elevation 0.5, and stops below the
     * height; its line is connected, and its ramp reaches from the axis to the line in every layer (DRAW_SPIRAL).
     */
    @Test
    void spiralsRiseByTheElevation() {
        Bodies.Direction up = Bodies.Direction.of("+y");
        Point center = p(0, 0, 0);
        Shapes.Voxels circle = Shapes.ellipse(center, p(5, 0, 0), p(0, 0, 5));
        // The voxels of one turn: the path along the curve of the ellipse, which the spiral follows
        int perTurn = Raster.path(
                        Shapes.Ellipse.of(center, p(5, 0, 0), p(0, 0, 5)).curve(0, 2 * Math.PI), true)
                .size();
        Shapes.Voxels steep = Shapes.spiral(center, p(5, 0, 0), p(0, 0, 5), up, 1, 10);
        List<Point> line = List.copyOf(steep.lines());
        assertEquals(10, line.size());
        for (int i = 0; i < line.size(); i++) {
            assertEquals(i, line.get(i).y(), "layer of voxel " + i);
        }
        assertEquals(p(5, 0, 0), line.getFirst());
        assertThinAndConnected(line);
        Shapes.Voxels flat = Shapes.spiral(center, p(5, 0, 0), p(0, 0, 5), up, 0.5, 2 * perTurn);
        List<Point> slow = List.copyOf(flat.lines());
        assertEquals(4 * perTurn, slow.size());
        assertEquals(0, slow.get(1).y());
        assertEquals(1, slow.get(2).y());
        assertThinAndConnected(slow);
        // Two turns with one layer per turn: every layer of the ramp has a part of the disc
        Shapes.Voxels ramp = Shapes.spiral(center, p(5, 0, 0), p(0, 0, 5), up, 1.0 / perTurn, 2);
        Set<Point> disc = new HashSet<>(circle.inside());
        for (int layer = 0; layer < 2; layer++) {
            int y = layer;
            assertTrue(ramp.inside().contains(p(0, y, 0)), "axis in layer " + y);
            long count = ramp.inside().stream().filter(v -> v.y() == y).count();
            assertEquals(disc.size(), count, "layer " + y);
        }
        // Downwards along x, in a plane across the direction
        Shapes.Voxels sideways = Shapes.spiral(center, p(0, 3, 0), p(0, 0, 3), Bodies.Direction.of("-x"), 2, 6);
        assertEquals(List.of(0, -2, -4), sideways.lines().stream().map(Point::x).toList());
        assertThrows(IllegalArgumentException.class, () -> Shapes.spiral(center, p(5, 0, 0), p(0, 0, 5), up, 0, 3));
        assertThrows(IllegalArgumentException.class, () -> Shapes.spiral(center, p(5, 0, 0), p(0, 0, 5), up, 1, 0));
    }
}
