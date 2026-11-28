package de.joernott.c0depaint.core.draw;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.joernott.c0depaint.core.canvas.Point;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** The bodies (tasks 5.1 to 5.5). */
class BodiesTest {
    private static Point p(int x, int y, int z) {
        return new Point(x, y, z);
    }

    private static int disc(int r) {
        int count = 0;
        for (int u = -r; u <= r; u++) {
            for (int v = -r; v <= r; v++) {
                if (u * u + v * v <= r * r) {
                    count++;
                }
            }
        }
        return count;
    }

    @Test
    void boxesHaveEdgesSidesAndInside() {
        Shapes.Voxels box = Bodies.box(p(9, 4, 2), p(0, 0, 0));
        assertEquals(150, box.inside().size());
        assertEquals(150 - 8 * 3 * 1, box.faces().size());
        assertEquals(4 * 10 + 4 * 5 + 4 * 3 - 16, box.lines().size());
        assertTrue(box.lines().contains(p(9, 4, 2)));
    }

    @Test
    void spheresAndEllipsoids() {
        Shapes.Voxels sphere = Bodies.ellipsoid(p(0, 0, 0), 5, 5, 5);
        int expected = 0;
        for (int x = -5; x <= 5; x++) {
            for (int y = -5; y <= 5; y++) {
                for (int z = -5; z <= 5; z++) {
                    if (x * x + y * y + z * z <= 25) {
                        expected++;
                    }
                }
            }
        }
        assertEquals(expected, sphere.inside().size());
        assertTrue(sphere.inside().containsAll(sphere.faces()));
        assertTrue(!sphere.faces().contains(p(0, 0, 0)));
        assertTrue(sphere.faces().contains(p(5, 0, 0)));
        assertTrue(sphere.lines().isEmpty());
        // A radius of 0 makes a flat ellipse
        assertTrue(Bodies.ellipsoid(p(0, 0, 0), 4, 0, 2).inside().stream().allMatch(v -> v.y() == 0));
    }

    @Test
    void cylindersGrowInTheirDirection() {
        Shapes.Voxels up = Bodies.cylinder(p(0, 0, 0), 3, 5, Bodies.Direction.of("+y"));
        assertEquals(5 * disc(3), up.inside().size());
        assertTrue(up.inside().stream().allMatch(v -> v.y() >= 0 && v.y() <= 4));
        assertTrue(up.lines().stream().allMatch(v -> v.y() == 0 || v.y() == 4));
        assertTrue(up.lines().contains(p(3, 0, 0)) && up.lines().contains(p(0, 4, -3)));
        Shapes.Voxels west = Bodies.cylinder(p(0, 0, 0), 2, 3, Bodies.Direction.of("-x"));
        assertTrue(west.inside().stream().allMatch(v -> v.x() <= 0 && v.x() >= -2));
        assertThrows(IllegalArgumentException.class, () -> Bodies.Direction.of("up"));
        assertThrows(
                IllegalArgumentException.class, () -> Bodies.cylinder(p(0, 0, 0), 3, 0, Bodies.Direction.of("+y")));
    }

    @Test
    void conesAndPyramidsEndInATip() {
        Shapes.Voxels cone = Bodies.cone(p(0, 0, 0), 4, 8, Bodies.Direction.of("+y"));
        assertTrue(cone.inside().contains(p(0, 8, 0)));
        assertEquals(disc(4), cone.inside().stream().filter(v -> v.y() == 0).count());
        assertTrue(cone.lines().stream().allMatch(v -> v.y() == 0));
        Shapes.Voxels pyramid = Bodies.pyramid(p(0, 0, 0), 3, 6, Bodies.Direction.of("-z"));
        assertEquals(49, pyramid.inside().stream().filter(v -> v.z() == 0).count());
        assertTrue(pyramid.inside().contains(p(0, 0, -6)));
        assertTrue(pyramid.lines().contains(p(-3, 3, 0)));
        assertTrue(pyramid.lines().contains(p(0, 0, -6)));
    }

    @Test
    void theEdgesOfSmallSolidsStayOnTheirSurface() {
        // Found for c0de.at: the edges of a dodecahedron of r = 3 stuck out of its faces (task 9.1.5)
        for (Bodies.Solid solid : Bodies.Solid.values()) {
            for (int r = 1; r <= 8; r++) {
                Shapes.Voxels voxels = Bodies.solid(solid, p(0, 0, 0), r);
                for (Point edge : voxels.lines()) {
                    assertTrue(voxels.inside().contains(edge), solid + " r " + r + ": the edge voxel " + edge
                            + " lies outside of the body");
                }
                assertTrue(!voxels.lines().isEmpty(), solid + " r " + r + " has no edges");
            }
        }
    }

    @Test
    void platonicSolidsHaveTheirCornersAtTheRadius() {
        for (Bodies.Solid solid : Bodies.Solid.values()) {
            Shapes.Voxels voxels = Bodies.solid(solid, p(0, 0, 0), 10);
            for (Vec corner : solid.corners(p(0, 0, 0), 10)) {
                assertEquals(10, corner.length(), 1e-9, solid.name());
                // A corner that rounds to a voxel outside of the body has an edge voxel next to it
                assertTrue(
                        voxels.lines().stream().anyMatch(v -> Vec.of(v).minus(corner).length() <= Math.sqrt(3) + 1e-9),
                        solid + " lacks the corner " + corner);
            }
            assertTrue(voxels.inside().contains(p(0, 0, 0)), solid.name());
            assertTrue(voxels.inside().containsAll(voxels.faces()), solid.name());
        }
        // The octahedron: |x| + |y| + |z| <= r
        Set<Point> octahedron =
                Bodies.solid(Bodies.Solid.OCTAHEDRON, p(0, 0, 0), 6).inside();
        int expected = 0;
        for (int x = -6; x <= 6; x++) {
            for (int y = -6; y <= 6; y++) {
                for (int z = -6; z <= 6; z++) {
                    if (Math.abs(x) + Math.abs(y) + Math.abs(z) <= 6) {
                        expected++;
                    }
                }
            }
        }
        assertEquals(expected, octahedron.size());
        // The volumes grow from the tetrahedron to the icosahedron and dodecahedron
        int tetrahedron =
                Bodies.solid(Bodies.Solid.TETRAHEDRON, p(0, 0, 0), 12).inside().size();
        int icosahedron =
                Bodies.solid(Bodies.Solid.ICOSAHEDRON, p(0, 0, 0), 12).inside().size();
        assertTrue(tetrahedron < icosahedron, tetrahedron + " < " + icosahedron);
    }
}
