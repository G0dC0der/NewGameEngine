package pojahn.game.core;

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import pojahn.game.core.Level.Tile;
import pojahn.game.essentials.Direction;
import pojahn.game.essentials.Image2D;
import pojahn.game.essentials.geom.Bounds;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BaseLogic {

    public static boolean pointRectangleOverlap(final float x, final float y, final float w, final float h, final int px, final float py) {
        return px >= x && px <= x + w && py >= y && py <= y + h;
    }

    public static boolean rectanglesCollide(final Rectangle rec, final float x, final float y, final float width, final float height) {
        return !((rec.y + rec.height < y)
            || (rec.y > y + height)
            || (rec.x + width < x)
            || (rec.x > x + width));
    }

    public static boolean rectanglesCollide(final Rectangle rec1, final Rectangle rec2) {
        return !((rec1.y + rec1.height < rec2.y)
            || (rec1.y > rec2.y + rec2.height)
            || (rec1.x + rec1.width < rec2.x)
            || (rec1.x > rec2.x + rec2.width));
    }

    public static boolean rectanglesCollide(final float x1, final float y1, final float width1, final float height1,
                                            final float x2, final float y2, final float width2, final float height2) {
        return !((y1 + height1 < y2)
            || (y1 > y2 + height2)
            || (x1 + width1 < x2)
            || (x1 > x2 + width2));
    }

    public static boolean rotatedRectanglesCollide(final Bounds bounds1, final Bounds bounds2) {
        final RotRect rr1 = new RotRect();
        rr1.C = bounds1.center();
        rr1.S = new Vector2(bounds1.size.width / 2, bounds1.size.height / 2);
        rr1.ang = (float) Math.toRadians(bounds1.rotation);

        final RotRect rr2 = new RotRect();
        rr2.C = bounds2.center();
        rr2.S = new Vector2(bounds2.size.width / 2, bounds2.size.height / 2);
        rr2.ang = (float) Math.toRadians(bounds2.rotation);

        final Vector2 A;
        Vector2 B;
        Vector2 C;
        Vector2 BL;
        final Vector2 TR;

        final float ang = rr1.ang - rr2.ang;
        float cosa = (float) Math.cos(ang);
        final float sina = (float) Math.sin(ang);
        float t;
        float x;
        float a;
        float dx;
        float ext1;
        float ext2;

        C = new Vector2(rr2.C);
        subVectors2D(C, rr1.C);

        rotateVector2DClockwise(C, rr2.ang);

        BL = new Vector2(C);
        TR = new Vector2(C);
        subVectors2D(BL, rr2.S);
        addVectors2D(TR, rr2.S);

        A = new Vector2();
        B = new Vector2();
        A.x = -rr1.S.y * sina;
        B.x = A.x;
        t = rr1.S.x * cosa;
        A.x += t;
        B.x -= t;
        A.y = rr1.S.y * cosa;
        B.y = A.y;
        t = rr1.S.x * sina;
        A.y += t;
        B.y -= t;

        t = sina * cosa;

        if (t < 0) {
            t = A.x;
            A.x = B.x;
            B.x = t;
            t = A.y;
            A.y = B.y;
            B.y = t;
        }
        if (sina < 0) {
            B.x = -B.x;
            B.y = -B.y;
        }
        if (B.x > TR.x || B.x > -BL.x)
            return false;

        if (t == 0) {
            ext1 = A.y;
            ext2 = -ext1;
        } else {
            x = BL.x - A.x;
            a = TR.x - A.x;
            ext1 = A.y;

            if (a * x > 0) {
                dx = A.x;
                if (x < 0) {
                    dx -= B.x;
                    ext1 -= B.y;
                    x = a;
                } else {
                    dx += B.x;
                    ext1 += B.y;
                }
                ext1 *= x;
                ext1 /= dx;
                ext1 += A.y;
            }

            x = BL.x + A.x;
            a = TR.x + A.x;
            ext2 = -A.y;

            if (a * x > 0) {
                dx = -A.x;

                if (x < 0) {
                    dx -= B.x;
                    ext2 -= B.y;
                    x = a;
                } else {
                    dx += B.x;
                    ext2 += B.y;
                }

                ext2 *= x;
                ext2 /= dx;
                ext2 -= A.y;
            }
        }
        return !((ext1 < BL.y && ext2 < BL.y) || (ext1 > TR.y && ext2 > TR.y));
    }

    public static boolean circleRectangleCollide(final Circle circle, final Rectangle rectangle) {
        final float circleDistanceX = Math.abs((circle.x) - (rectangle.x + rectangle.width / 2));
        final float circleDistanceY = Math.abs((circle.y) - (rectangle.y + rectangle.height / 2));
        final float radius = circle.radius;

        if (circleDistanceX > (rectangle.width / 2 + radius) || (circleDistanceY > (rectangle.height / 2 + radius)))
            return false;

        if ((circleDistanceX <= (rectangle.width / 2)) || (circleDistanceY <= (rectangle.height / 2)))
            return true;

        final double cornerDistance_sq = Math.pow(circleDistanceX - rectangle.width / 2, 2) +
                Math.pow(circleDistanceY - rectangle.height / 2, 2);

        return (cornerDistance_sq <= (radius * radius));
    }

    public static boolean circleVsCircle(final Circle c1, final Circle c2) {
        final float dx = c2.x - c1.x;
        final float dy = c2.y - c1.y;
        final float d = c1.radius + c2.radius;
        return (dx * dx + dy * dy) < (d * d);
    }

    /**
     * Performs a pixel perfect collision check.
     * Precondition: Rotation == 0, offsetX and offsetY == 0, bounds.width and height are equal to the size of the image.
     *
     * @return True if the two entities are colliding.
     */
    public static boolean pixelPerfect(
        final Rectangle rec1, final Image2D image1, final boolean flipX1, final boolean flipY1,
        final Rectangle rec2, final Image2D image2, final boolean flipX2, final boolean flipY2) {

        final int width1 = image1.getWidth();
        final int width2 = image2.getWidth();
        final int height1 = image1.getHeight();
        final int height2 = image2.getHeight();
        final int top = (int) Math.max(rec1.y, rec2.y);
        final int bottom = (int) Math.min(rec1.y + height1, rec2.y + height2);
        final int left = (int) Math.max(rec1.x, rec2.x);
        final int right = (int) Math.min(rec1.x + width1, rec2.x + width2);

        for (int y = top; y < bottom; y++) {
            for (int x = left; x < right; x++) {
                final int x1 = (int) (flipX1 ? width1 - (x - rec1.x) - 1 : x - rec1.x);
                final int y1 = (int) (flipY1 ? height1 - (y - rec1.y) - 1 : y - rec1.y);
                final int x2 = (int) (flipX2 ? width2 - (x - rec2.x) - 1 : x - rec2.x);
                final int y2 = (int) (flipY2 ? height2 - (y - rec2.y) - 1 : y - rec2.y);

                final int alpha1 = image1.getPixel(x1, y1) & 0x000000FF;
                final int alpha2 = image2.getPixel(x2, y2) & 0x000000FF;

                if (alpha1 != 0 && alpha2 != 0)
                    return true;
            }
        }
        return false;
    }

    public static boolean pixelPerfectRotation(final Matrix4 m1, final Image2D img1, final Matrix4 m2, final Image2D img2) { //TODO: Not even working...
        final Matrix4 AtoB = m1.mul(m2.inv());
        final Matrix4 norM = AtoB.cpy().toNormalMatrix();

        final Vector3 stepX = Vector3.X.cpy().mul(norM);
        final Vector3 stepY = Vector3.Y.cpy().mul(norM);
        final Vector3 yPosInB = Vector3.Zero.cpy().mul(AtoB);

        final int widthA = img1.getWidth();
        final int widthB = img2.getWidth();
        final int heightA = img1.getHeight();
        final int heightB = img2.getHeight();

        for (int yA = 0; yA < heightA; yA++) {
            final Vector3 posInB = yPosInB.cpy();

            for (int xA = 0; xA < widthA; xA++) {
                final int xB = Math.round(posInB.x);
                final int yB = Math.round(posInB.y);

                if (0 <= xB && xB < widthB && 0 <= yB && yB < heightB) {
                    final int alpha1 = img1.getPixel(xA, yA) & 0x000000FF;
                    final int alpha2 = img2.getPixel(xB, yB) & 0x000000FF;

                    if (alpha1 != 0 && alpha2 != 0)
                        return true;
                }
                posInB.x += stepX.x;
                posInB.y += stepX.y;
            }
            yPosInB.x += stepY.x;
            yPosInB.y += stepY.y;
        }
        return false;
    }

    public static Matrix4 buildMatrix(final Entity entity) {
        return new Matrix4(new Vector3(entity.x(), entity.y(), 0),
                new Quaternion(new Vector3(0, 0, 1), -entity.getRotation()),
                new Vector3(1, 1, 1));

//		return new Matrix4()
//				.idt()
//				.setToTranslation(-entity.halfWidth(), -entity.halfHeight(), 0)
//				.rotate(0, 0, 1, -entity.getRotation())
//				.scl(1, 1, 1)
//				.trn(entity.x(), entity.y(), 0);
    }

    public static double getAngle(final float x1, final float y1, final float x2, final float y2) {
        final float deltaX = x2 - x1;
        final float deltaY = y2 - y1;

        return Math.toDegrees(Math.atan2(deltaY, deltaX));
    }

    public static double distance(final float x1, final float y1, final float x2, final float y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    public static double distance(final Entity e1, final Entity e2) {
        return distance(e1.x(), e1.y(), e2.x(), e2.y());
    }

    public static Entity findClosest(final Entity watcher, final List<? extends Entity> targets) {
        return targets.stream()
            .min(Comparator.comparing(watcher::dist))
            .orElse(null);
    }

    public static Entity findClosestSeeable(final Entity watcher, final List<? extends Entity> targets) {
        return targets.stream()
            .filter(watcher::canSee)
            .min(Comparator.comparing(watcher::dist))
            .orElse(null);
    }

    public static boolean lineIntersects(final Vector2 p1, final Vector2 p2, final Vector2 p3, final Vector2 p4) {
        return lineIntersect(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y, p4.x, p4.y);
    }

    public static boolean lineIntersect(
        final float x1, final float y1,
        final float x2, final float y2,
        final float x3, final float y3,
        final float x4, final float y4) {

        final float bx = x2 - x1;
        final float by = y2 - y1;
        final float dx = x4 - x3;
        final float dy = y4 - y3;
        final float b_dot_d_perp = bx * dy - by * dx;
        if (b_dot_d_perp == 0)
            return false;

        final float cx = x3 - x1;
        final float cy = y3 - y1;
        final float t = (cx * dy - cy * dx) / b_dot_d_perp;
        if (t < 0 || t > 1)
            return false;

        final float u = (cx * by - cy * bx) / b_dot_d_perp;
        return !(u < 0 || u > 1);
    }

    /**
     * Returns the bounding box of the (rotated) rectangle.
     */
    public static Rectangle getBoundingBox(final Bounds bounds) {
        final Vector2 c = bounds.center();
        final Vector2 p1 = rotatePoint(bounds.pos.x, bounds.pos.y, c.x, c.y, bounds.rotation);
        final Vector2 p2 = rotatePoint(bounds.pos.x + bounds.size.width, bounds.pos.y, c.x, c.y, bounds.rotation);
        final Vector2 p3 = rotatePoint(bounds.pos.x + bounds.size.width, bounds.pos.y + bounds.size.height, c.x, c.y, bounds.rotation);
        final Vector2 p4 = rotatePoint(bounds.pos.x, bounds.pos.y + bounds.size.height, c.x, c.y, bounds.rotation);

        final List<Float> xcords = Arrays.asList(p1.x, p2.x, p3.x, p4.x);
        final List<Float> ycords = Arrays.asList(p1.y, p2.y, p3.y, p4.y);

        final float minX = Collections.min(xcords);
        final float maxX = Collections.max(xcords);
        final float minY = Collections.min(ycords);
        final float maxY = Collections.max(ycords);

        return new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }

    public static Vector2 rotatePoint(final Vector2 point, final Vector2 center, final float rotation) {
        return rotatePoint(point.x, point.y, center.x, center.y, rotation);
    }

    /**
     * Rotates the given point around the given axis and returning the new coordinate.
     *
     * @param x        The x coordinate we want after a rotation(absolute).
     * @param y        The y coordinate we want after a rotation(absolute).
     * @param cx       The pixel to rotate around(absolute).
     * @param cy       The pixel to rotate around(absolute).
     * @param rotation The angle in degrees.
     * @return The point that contains the rotated coordinates.
     */
    public static Vector2 rotatePoint(final float x, final float y, final float cx, final float cy, final float rotation) {
        if (rotation % 360 == 0)
            return new Vector2(x, y);

        final float angleInRadians = (float) (rotation * (Math.PI / 180));
        final float cosTheta = MathUtils.cos(angleInRadians);
        final float sinTheta = MathUtils.sin(angleInRadians);

        return new Vector2(
                (cosTheta * (x - cx) - sinTheta * (y - cy) + cx),
                (sinTheta * (x - cx) + cosTheta * (y - cy) + cy));
    }

    public static Entity leftMost(final Entity e1, final Entity e2) {
        return e2.x() > e1.x() ? e1 : e2;
    }

    public static Entity rightMost(final Entity e1, final Entity e2) {
        return e2.x() + e2.width() > e1.x() + e1.width() ? e2 : e1;
    }

    public static Vector2 searchTile(final int x0, final int y0, final int x1, final int y1, final Tile tile, final Level level) {
        return searchTile(x0, y0, x1, y1, false, tile, level);
    }

    public static boolean solidSpace(final float x0, final float y0, final float x1, final float y1, final Level level) {
        return searchTile((int) x0, (int) y0, (int) x1, (int) y1, false, Tile.SOLID, level) != null;
    }

    public static boolean solidSpace(final int x0, final int y0, final int x1, final int y1, final Level level) {
        return searchTile(x0, y0, x1, y1, false, Tile.SOLID, level) != null;
    }

    /**
     * Iterates through the specified line(and continues in the specified path if set), searching for the given tile type.<br>
     * Return a non-null value if the given tile was found between the two points.
     *
     * @param x0          The x position of the first point.
     * @param y0          The y position of the first point.
     * @param x1          The x position of the second point.
     * @param y1          The y position of the second point.
     * @param continuesly Whether or not to continue the given path if the given tile was not found when reached the second point(x1,y1).
     * @param tile        The tile to scan for.
     * @return The point where the tile was found, or null if the tile was not found.
     */
    public static Vector2 searchTile(int x0, int y0, final int x1, final int y1, final boolean continuesly, final Tile tile, final Level level) {
        final int dx = Math.abs(x1 - x0);
        final int dy = Math.abs(y1 - y0);
        final int sx = (x0 < x1) ? 1 : -1;
        final int sy = (y0 < y1) ? 1 : -1;
        int err = dx - dy;

        while (true) {
            if (level.tileAt(x0, y0) == tile)
                return new Vector2(x0, y0);
            if (level.outOfBounds(x0, y0) || (!continuesly && x0 == x1 && y0 == y1))
                return null;

            final int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x0 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y0 += sy;
            }
        }
    }

    public static Vector2 searchTile(final float x0, final float y0, final float x1, final float y1, final Tile tile, final Level level) {
        return searchTile((int) x0, (int) y0, (int) x1, (int) y1, tile, level);
    }

    public static boolean circleVsLine(final float Ax, final float Ay, final float Bx, final float By, final Circle c) {
        final double LAB = Math.sqrt((Bx - Ax) * (Bx - Ax) + (By - Ay) * (By - Ay));
        final double Dx = (Bx - Ax) / LAB;
        final double Dy = (By - Ay) / LAB;
        final double t = Dx * (c.x - Ax) + Dy * (c.y - Ay);
        final double Ex = t * Dx + Ax;
        final double Ey = t * Dy + Ay;
        final double LEC = Math.sqrt((Ex - c.x) * (Ex - c.x) + (Ey - c.y) * (Ey - c.y));

        return LEC <= c.radius;
    }

    /**
     * Check if the specified GameObject appear between the two points.
     *
     * @return True if the specified {@code Entity} is intersecting with the given line.
     */
    public static boolean lineRectangle(final float x1, final float y1, final float x2, final float y2, final Rectangle rec) {
        return lineIntersect(x1, y1, x2, y2, rec.x, rec.y, rec.x + rec.width, rec.y)
            || lineIntersect(x1, y1, x2, y2, rec.x, rec.y, rec.x, rec.y + rec.height)
            || lineIntersect(x1, y1, x2, y2, rec.x + rec.width, rec.y, rec.x + rec.width, rec.y + rec.height)
            || lineIntersect(x1, y1, x2, y2, rec.x, rec.y + rec.height, rec.x + rec.width, rec.y + rec.height);
    }

    public static Vector2 findEdgePoint(final float obsX, final float obsY, final float tarX, final float tarY, final Level level) {
        final int width = level.getWidth();
        final int height = level.getHeight();

        final Vector2 obs = new Vector2(obsX, obsY);
        final Vector2 tar = new Vector2(tarX, tarY);

        float vTime = 1.0e20f;
        if (tar.x > obs.x) vTime = (width - obs.x) / (tar.x - obs.x);
        else if (tar.x < obs.x) vTime = (0 - obs.x) / (tar.x - obs.x);

        float hTime = 1.0e20f;
        if (tar.y > obs.y) hTime = (height - obs.y) / (tar.y - obs.y);
        else if (tar.y < obs.y) hTime = (0 - obs.y) / (tar.y - obs.y);

        final float time = Math.min(hTime, vTime);

        final float newX = obs.x + time * (tar.x - obs.x);
        final float newY = obs.y + time * (tar.y - obs.y);

        return new Vector2(newX, newY);
    }

    public static Vector2 findEdgePoint(final Entity observer, final Entity target, final Level level) {
        return findEdgePoint(observer.centerX(), observer.centerY(), target.centerX(), target.centerY(), level);
    }

    public static void rotateTowards(final Entity source, final Entity target, final float speed) {
        source.setRotation(rotateTowardsPoint(source.centerX(), source.centerY(), target.centerX(), target.centerY(), source.getRotation(), speed));
    }

    /**
     * Tries to rotate the abstract source so that it faces the abstract target.<br>
     * Like an turret in a 2D environment rotates so it faces its target.
     *
     * @param srcX         The X position of the source.
     * @param srcY         The Y position of the source.
     * @param targetX      The X position of the target.
     * @param targetY      The Y position of the target.
     * @param currRotation The current rotation of the source.
     * @param speed        The speed of the rotation. You may want to put a low value such as 0.01.
     * @return The new and updated rotation value(angle) of the source.
     */
    public static float rotateTowardsPoint(final float srcX, final float srcY, final float targetX, final float targetY, float currRotation, final float speed) {
        final float destinationRotation = (float) (Math.atan2(srcY - targetY, srcX - targetX) + Math.PI);
        currRotation = (float) Math.toRadians(currRotation);

        if (Math.abs((currRotation + 180 - destinationRotation) % 360 - 180) < speed)
            currRotation = destinationRotation;
        else {
            if (destinationRotation > currRotation) {
                if (currRotation < destinationRotation - Math.PI)
                    currRotation -= speed;
                else
                    currRotation += speed;
            } else if (destinationRotation < currRotation) {
                if (currRotation > destinationRotation + Math.PI)
                    currRotation += speed;
                else
                    currRotation -= speed;
            }
            if (currRotation > Math.PI * 2.0f) currRotation = 0;
            if (currRotation < 0) currRotation = (float) (Math.PI * 2.0f);
        }
        return (float) Math.toDegrees(currRotation);
    }

    public static Vector2 normalize(final float x1, final float y1, final float x2, final float y2) {
        float dx = x1 - x2;
        float dy = y1 - y2;
        final double length = Math.sqrt(dx * dx + dy * dy);
        dx /= length;
        dy /= length;

        return new Vector2(Float.isNaN(dx) ? 0 : dx, Float.isNaN(dy) ? 0 : dy);
    }

    public static Vector2 normalize(final Vector2 v1, final Vector2 v2) {
        return normalize(v1.x, v1.y, v2.x, v2.y);
    }

    public static Vector2 normalize(final Entity entity1, final Entity entity2) {
        return normalize(entity1.x(), entity1.y(), entity2.x(), entity2.y());
    }

    /**
     * Continues from the start point {@code x} and {@code y} with the given direction until the edge of the stage has been reached, which is the point returned.
     *
     * @param x   The x coordinate to start at.
     * @param y   The y coordinate to start at.
     * @param dir The direction to move in.
     * @return The point.
     */
    public static Vector2 getEdgePoint(final int x, final int y, final Direction dir, final Level level) {
        final int targetX;
        final int targetY;

        switch (dir) {
            case NW:
                targetX = x - 1;
                targetY = y - 1;
                break;
            case N:
                targetX = x;
                targetY = y - 1;
                break;
            case NE:
                targetX = x + 1;
                targetY = y - 1;
                break;
            case E:
                targetX = x + 1;
                targetY = y;
                break;
            case SE:
                targetX = x + 1;
                targetY = y + 1;
                break;
            case S:
                targetX = x;
                targetY = y + 1;
                break;
            case SW:
                targetX = x - 1;
                targetY = y + 1;
                break;
            case W:
                targetX = x - 1;
                targetY = y;
                break;
            default:
                return null;
        }

        return findEdgePoint(x, y, targetX, targetY, level);
    }

    public static Direction getDirection(final Vector2 normalizedPoint) {
        final double x = normalizedPoint.x;
        final double y = normalizedPoint.y;

        final double fThreshold = Math.cos(Math.PI / 8);

        if (x > fThreshold)
            return Direction.W;
        else if (x < -fThreshold)
            return Direction.E;
        else if (y > fThreshold)
            return Direction.N;
        else if (y < -fThreshold)
            return Direction.S;
        else if (x > 0 && y > 0)
            return Direction.NW;
        else if (x > 0 && y < 0)
            return Direction.SW;
        else if (x < 0 && y > 0)
            return Direction.NE;
        else if (x < 0 && y < 0)
            return Direction.SE;

        return null;
    }

    public static Direction getDirection(final float currX, final float currY, final float prevX, final float prevY) {
        if (currX == prevX && currY == prevY)
            return null;

        final Vector2 normalized = normalize(prevX, prevY, currX, currY);
        return getDirection(normalized);
    }

    public static Direction getDirection(final MobileEntity mobile) {
        return getDirection(mobile.x(), mobile.y(), mobile.prevX(), mobile.prevY());
    }

    private static void addVectors2D(final Vector2 v1, final Vector2 v2) {
        v1.x += v2.x;
        v1.y += v2.y;
    }

    private static void subVectors2D(final Vector2 v1, final Vector2 v2) {
        v1.x -= v2.x;
        v1.y -= v2.y;
    }

    private static void rotateVector2DClockwise(final Vector2 v, final float ang) {
        final float cosa = (float) Math.cos(ang);
        float sina = (float) Math.sin(ang);
        final float t = v.x;

        v.x = t * cosa + v.y * sina;
        v.y = -t * sina + v.y * cosa;
    }

    private static class RotRect {
        Vector2 C, S;
        float ang;
    }
}
