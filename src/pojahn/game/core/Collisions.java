package pojahn.game.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import pojahn.game.core.Level.Tile;
import pojahn.game.essentials.geom.Bounds;
import pojahn.game.essentials.Direction;
import pojahn.game.essentials.Image2D;

public class Collisions {
	
	public static final boolean rectanglesCollide(Rectangle rec1, Rectangle rec2){
		return !((rec1.y + rec1.height < rec2.y) ||
				(rec1.y > rec2.y + rec2.height) ||
				(rec1.x + rec1.width < rec2.x) ||
				(rec1.x > rec2.x + rec2.width));
	}
	
	public static boolean rectanglesCollide(float x1, float y1, float width1, float height1,
											float x2, float y2, float width2, float height2){
		return !((y1 + height1 < y2) ||
				(y1 > y2 + height2) ||
				(x1 + width1 < x2) ||
				(x1 > x2 + width2));
	}

	public static boolean rotatedRectanglesCollide(Bounds bounds1, Bounds bounds2){
		RotRect rr1 = new RotRect();
		rr1.C = bounds1.center();
		rr1.S = new Vector2(bounds1.size.width / 2, bounds1.size.height / 2);
		rr1.ang = (float) Math.toRadians(bounds1.rotation);
		
		RotRect rr2 = new RotRect();
		rr2.C = bounds2.center();
		rr2.S = new Vector2(bounds2.size.width / 2, bounds2.size.height / 2);
		rr2.ang = (float) Math.toRadians(bounds2.rotation);
		
		Vector2 A,B,C,BL,TR;
		
		float ang = rr1.ang - rr2.ang,
			  cosa = (float) Math.cos(ang),
			  sina = (float) Math.sin(ang),
			  t,x,a,dx,ext1,ext2;
		
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
		A.y =  rr1.S.y * cosa;
		B.y = A.y;
		t = rr1.S.x * sina;
		A.y += t; 
		B.y -= t;
		
		t = sina*cosa;
		
		if (t < 0){
			t = A.x; A.x = B.x; B.x = t;
			t = A.y; A.y = B.y; B.y = t;
		}
		if (sina < 0){
			B.x = -B.x;
			B.y = -B.y;
		}
		if (B.x > TR.x || B.x > -BL.x) 
			return false;
		
		if (t == 0){
			ext1 = A.y;
			ext2 = -ext1;
		}
		else{
			x = BL.x-A.x;
			a = TR.x-A.x;
			ext1 = A.y;
			  
			if (a*x > 0){
				dx = A.x;
				if (x < 0)
				{
					dx -= B.x;
					ext1 -= B.y;
					x = a;
				}
				else
				{
					dx += B.x;
					ext1 += B.y;
				}
				ext1 *= x;
				ext1 /= dx;
				ext1 += A.y;
			}
		
			x = BL.x+A.x;
			a = TR.x+A.x;
			ext2 = -A.y;
		
			if (a*x > 0){
				dx = -A.x;
		
				if (x < 0)
				{
					dx -= B.x;
					ext2 -= B.y;
					x = a;
				}
				else
				{
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
	
	public static boolean circleRectangleCollide(Circle circle, Rectangle rectangle){
	    float circleDistanceX = Math.abs((circle.x) - (rectangle.x + rectangle.width  / 2));
	    float circleDistanceY = Math.abs((circle.y) - (rectangle.y + rectangle.height / 2));
	    float radius = circle.radius;

	    if (circleDistanceX > (rectangle.width / 2 + radius) || (circleDistanceY > (rectangle.height / 2 + radius)))
	    	return false;
	    
	    if ((circleDistanceX <= (rectangle.width / 2)) || (circleDistanceY <= (rectangle.height / 2)))
	    	return true;

	    double cornerDistance_sq = Math.pow(circleDistanceX - rectangle.width  /2, 2) +
	                               Math.pow(circleDistanceY - rectangle.height /2, 2);

	    return (cornerDistance_sq <= (radius * radius));
	}
	
	public static boolean circleVsCircle(Circle c1, Circle c2){
	    float dx = c2.x - c1.x;
	    float dy = c2.y - c1.y;
	    float d = c1.radius + c2.radius;
	    return (dx * dx + dy * dy) < (d * d);
	}
	
	/**
	 * Performs between the two entities polygons.
	 * Precondition: offsetX and offsetY == 0, flipX and flipY == false
	 * @return True if the two polygons are colliding.
	 */
//	public static boolean polygonsCollide(Entity entity1, Entity entity2){
//		preparePolygon(entity1);
//		preparePolygon(entity2);
//		return Intersector.overlapConvexPolygons(entity1.poly, entity2.poly);
//	}
//	
//	private static void preparePolygon(Entity entity){
//		if(	entity.poly.getX()	 			!= entity.bounds.x 		||
//			entity.poly.getY() 				!= entity.bounds.y 		|| 
//			entity.poly.getRotation() 		!= entity.getRotation())
//		{
//			entity.poly.setRotation(entity.getRotation());
//			entity.poly.setPosition(entity.bounds.x, entity.bounds.y);
//			entity.poly.setOrigin(entity.bounds.x - entity.bounds.width / 2, entity.bounds.y - entity.bounds.height / 2);
//		}
//	}

	/**
	 * Performs a pixel perfect collision check.
	 * Precondition: Rotation == 0, offsetX and offsetY == 0, bounds.width and height are equal to the size of the image.
	 * @return True if the two entities are colliding.
	 */
	public static boolean pixelPerfect(	Rectangle rec1, Image2D image1, boolean flipX1, boolean flipY1,
										Rectangle rec2, Image2D image2, boolean flipX2, boolean flipY2){
		int width1  = image1.getWidth();
		int width2  = image2.getWidth();
		int height1 = image1.getHeight();
		int height2 = image2.getHeight();
		int top    = (int) Math.max(rec1.x, rec2.y);
		int bottom = (int) Math.min(rec1.y + height1, rec2.y + height2);
		int left   = (int) Math.max(rec1.x, rec2.y);
		int right  = (int) Math.min(rec1.x + width1, rec2.x + width2);
		
		for (int y = top; y < bottom; y++){
			for (int x = left; x < right; x++){
				int x1 = (int) (flipX1 ? width1  - (x - rec1.x) - 1 : x - rec1.x); //TODO: Why are there -1 on these?
				int y1 = (int) (flipY1 ? height1 - (y - rec1.y) - 1 : y - rec1.y);
				int x2 = (int) (flipX2 ? width2  - (x - rec2.x) - 1 : x - rec2.x);
				int y2 = (int) (flipY2 ? height2 - (y - rec2.y) - 1 : y - rec2.y);
				
				int alpha1 = image1.getPixel(x1, y1) & 0x000000FF;
				int alpha2 = image2.getPixel(x2, y2) & 0x000000FF;
				
				if (alpha1 != 0 && alpha2 != 0)
					return true;
			}
		}
		return false;
	}
	
	public static boolean pixelPerfectRotation(Matrix4 m1, Image2D img1, Matrix4 m2, Image2D img2){ //TODO: Not even working...
		Matrix4 AtoB = m1.mul(m2.inv());
		Matrix4 norM = AtoB.cpy().toNormalMatrix();
		
		Vector3 stepX = Vector3.X.cpy().mul(norM);
		Vector3 stepY = Vector3.Y.cpy().mul(norM);
		Vector3 yPosInB = Vector3.Zero.cpy().mul(AtoB);
		
		int widthA = img1.getWidth();
		int widthB = img2.getWidth();
		int heightA = img1.getHeight();
		int heightB = img2.getHeight();
		
		for (int yA = 0; yA < heightA; yA++){
			Vector3 posInB = yPosInB.cpy();

            for (int xA = 0; xA < widthA; xA++){
                int xB = Math.round(posInB.x);
                int yB = Math.round(posInB.y);

                if (0 <= xB && xB < widthB && 0 <= yB && yB < heightB){
                	int alpha1 = img1.getPixel(xA, yA) & 0x000000FF;
    				int alpha2 = img2.getPixel(xB, yB) & 0x000000FF;

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
	
	public static Matrix4 buildMatrix(Entity entity){
		return new Matrix4(	new Vector3(entity.x(), entity.y(), 0), 
							new Quaternion(new Vector3(0,0,1), -entity.getRotation()), 
							new Vector3(1,1,1));
		
//		return new Matrix4()
//				.idt()
//				.setToTranslation(-entity.halfWidth(), -entity.halfHeight(), 0)
//				.rotate(0, 0, 1, -entity.getRotation())
//				.scl(1, 1, 1)
//				.trn(entity.x(), entity.y(), 0);
	}
	
	public static double getAngle(float x1, float y1, float x2, float y2){
		float deltaX = x2 - x1;
		float deltaY = y2 - y1;
		
		return Math.toDegrees(Math.atan2(deltaY, deltaX));
	}
	
	public static double distance(float x1, float y1, float x2, float y2){
		return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
	}
	
	public static double distance(Entity e1, Entity e2){
		return distance(e1.x(), e1.y(), e2.x(), e2.y());
	}
	
	public static Entity findClosest(Entity watcher, Entity... targets){
		return findClosest(watcher, Arrays.asList(targets));
	}
	
	public static Entity findClosest(Entity watcher, List<? extends Entity> targets){
		if(targets.size() <= 0)
			throw new IllegalArgumentException("The target list is empty.");
		
		if(targets.size() == 1)
			return targets.get(0);
		
		int closestIndex  = -1; 
		double closestLength = 0;
		
		for (int i = 0; i < targets.size(); i++){				
			double distance = distance(watcher, targets.get(0));
			
			if (closestLength == 0){
				closestLength = distance;
				closestIndex = i;
			}
			if (distance < closestLength){
				closestLength = distance;
				closestIndex = i;
			}
		}
		return targets.get(closestIndex);
	}
	
	public static Entity findClosestSeeable(Entity watcher, Entity... targets){
		List<Entity> seeable = new ArrayList<>();
		for(Entity target : targets)
			if(watcher.canSee(target))
				seeable.add(target);
		
		if(seeable.isEmpty())
			return null;
		else
			return findClosest(watcher, seeable);
	}

	public static boolean lineIntersects(Vector2 p1, Vector2 p2, Vector2 p3, Vector2 p4){
		return lineIntersect(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y, p4.x, p4.y);
	}
	
	public static boolean lineIntersect(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4){
		float bx = x2 - x1; 
		float by = y2 - y1; 
		float dx = x4 - x3; 
		float dy = y4 - y3;
		float b_dot_d_perp = bx * dy - by * dx;
		if(b_dot_d_perp == 0) 
			return false;
		  
		float cx = x3 - x1;
		float cy = y3 - y1;
		float t = (cx * dy - cy * dx) / b_dot_d_perp;
		if(t < 0 || t > 1) 
			return false;
		  
		float u = (cx * by - cy * bx) / b_dot_d_perp;
		return !(u < 0 || u > 1);
	}
	
	/**
	 * Returns the bounding box of the (rotated) rectangle.
	 */
	public static Rectangle getBoundingBox(Bounds bounds){
		  Vector2 c = bounds.center();
		  Vector2 p1 = rotatePoint(bounds.pos.x, bounds.pos.y, c.x, c.y, bounds.rotation);
		  Vector2 p2 = rotatePoint(bounds.pos.x + bounds.size.width, bounds.pos.y, c.x, c.y, bounds.rotation);
		  Vector2 p3 = rotatePoint(bounds.pos.x + bounds.size.width, bounds.pos.y + bounds.size.height, c.x, c.y, bounds.rotation);
		  Vector2 p4 = rotatePoint(bounds.pos.x, bounds.pos.y + bounds.size.height, c.x, c.y, bounds.rotation);
		  
		  List<Float> xcords = Arrays.asList(p1.x, p2.x, p3.x, p4.x);
		  List<Float> ycords = Arrays.asList(p1.y, p2.y, p3.y, p4.y);
		  
		  float minX = Collections.min(xcords);
		  float maxX = Collections.max(xcords);
		  float minY = Collections.min(ycords);
		  float maxY = Collections.max(ycords);
		  
		  return new Rectangle(minX, minY, maxX - minX, maxY - minY);
	}
	
	public static Vector2 rotatePoint(Vector2 point, Vector2 center, float rotation){
		return rotatePoint(point.x, point.y, center.x, center.y, rotation);
	}
	
	/**
	 * Rotates the given point around the given axis and returning the new coordinate.
	 * @param x The x coordinate we want after a rotation(absolute).
	 * @param y The y coordinate we want after a rotation(absolute).
	 * @param cx The pixel to rotate around(absolute).
	 * @param cy The pixel to rotate around(absolute).
	 * @param rotation The angle in degrees.
	 * @return The point that contains the rotated coordinates.
	 */
	public static Vector2 rotatePoint(float x, float y, float cx, float cy, float rotation){
		if(rotation == 0)
			return new Vector2(x,y);
		
		float angleInRadians = (float)(rotation * (Math.PI / 180));
		float cosTheta = MathUtils.cos(angleInRadians);
		float sinTheta = MathUtils.sin(angleInRadians);
		
		return new Vector2(
				(cosTheta * (x - cx) - sinTheta * (y - cy) + cx), 
				(sinTheta * (x - cx) + cosTheta * (y - cy) + cy)
		);
	}
	
	public static Entity leftMost(Entity e1, Entity e2){
		return e2.x() > e1.x() ? e1 : e2;
	}
	
	public static Entity rightMost(Entity e1, Entity e2){
		return e2.x() + e2.width() > e1.x() + e1.width() ? e2 : e1;
	}
	
	public static Vector2 searchTile(int x0, int y0, final int x1, final int y1, Tile tile, Level level){
		return searchTile(x0, y0, x1, y1, false, tile, level);
	}
	
	public static boolean solidSpace(float x0, float y0, float x1, float y1, Level level){
		return searchTile((int)x0, (int)y0, (int)x1, (int)y1, false, Tile.SOLID, level) != null;
	}
	
	public static boolean solidSpace(int x0, int y0, int x1, int y1, Level level){
		return searchTile(x0, y0, x1, y1, false, Tile.SOLID, level) != null;
	}
	
	/**
	 * Iterates through the specified line(and continues in the specified path if set), searching for the given tile type.<br>
	 * Return a non-null value if the given tile was found between the two points.
	 * @param x0 The x position of the first point.
	 * @param y0 The y position of the first point.
	 * @param x1 The x position of the second point.
	 * @param y1 The y position of the second point.
	 * @param tile The tile to scan for.
	 * @return The point where the tile was found, or null if the tile was not found.
	 */
	public static Vector2 searchTile(int x0, int y0, final int x1, final int y1, boolean continuesly, Tile tile, Level level){
		final int dx = Math.abs(x1-x0);
		final int dy = Math.abs(y1-y0); 
		final int sx = (x0 < x1) ? 1 : -1;
		final int sy = (y0 < y1) ? 1 : -1;
		int err = dx-dy;
		
		while (true){
			if(level.outOfBounds(x0, y0) || (!continuesly && x0 == x1 && y0 == y1))
				return null;
			else if(level.tileAt(x0,y0) == tile)
				return new Vector2(x0, y0);
			
			final int e2 = 2 * err;
			if (e2 > -dy){
				err -= dy;
				x0 += sx;
			}
			if (e2 < dx){
				err += dx;
				y0 += sy;
			}
		}
	}
	
	public static Vector2 searchTile(float x0, float y0, final float x1, final float y1, Tile tile, Level level){
		return searchTile((int)x0, (int)y0, (int)x1, (int)y1, tile, level);
	}
	
	public static boolean circleVsLine(float Ax, float Ay, float Bx, float By, Circle c){
		double LAB = Math.sqrt((Bx-Ax)*(Bx-Ax) + (By-Ay)*(By-Ay));
		double Dx = (Bx-Ax)/LAB;
		double Dy = (By-Ay)/LAB;
		double t = Dx*(c.x-Ax) + Dy*(c.y-Ay);
		double Ex = t*Dx+Ax;
		double Ey = t*Dy+Ay;
		double LEC = Math.sqrt((Ex-c.x)*(Ex-c.x) + (Ey-c.y)*(Ey-c.y));

		return LEC <= c.radius;
	}
	
	/**
	 * Check if the specified GameObject appear between the two points.
	 * @return True if the specified {@code Entity} is intersecting with the given line.
	 */
	public static boolean lineRectangle(float x1, float y1, float x2, float y2, Rectangle rec){
		return 	lineIntersect(x1,y1,x2,y2, rec.x, rec.y, rec.x + rec.width, rec.y) 							||
				lineIntersect(x1,y1,x2,y2, rec.x, rec.y, rec.x, rec.y + rec.height) 						||
				lineIntersect(x1,y1,x2,y2, rec.x + rec.width, rec.y, rec.x + rec.width, rec.y + rec.height) ||
				lineIntersect(x1,y1,x2,y2, rec.x, rec.y + rec.height, rec.x + rec.width, rec.y + rec.height);
	}
	
	public static Vector2 findEdgePoint(float obsX, float obsY, float tarX, float tarY, Level level){
		int width  = level.getWidth();
		int height = level.getHeight();
		
		Vector2 obs = new Vector2(obsX, obsY);
		Vector2 tar = new Vector2(tarX, tarY);
		
		float vTime = 1.0e20f;
		if 		(tar.x > obs.x) vTime = (width - obs.x) / (tar.x - obs.x);
		else if (tar.x < obs.x) vTime = (0     - obs.x) / (tar.x - obs.x);
		 
		float hTime = 1.0e20f;
		if      (tar.y > obs.y) hTime = (height - obs.y) / (tar.y - obs.y);
		else if (tar.y < obs.y) hTime = (0      - obs.y) / (tar.y - obs.y);
		 
		float time = Math.min(hTime, vTime);
		
		float newX = obs.x + time * (tar.x - obs.x);
		float newY = obs.y + time * (tar.y - obs.y);
		
		return new Vector2(newX, newY);
	}
	
	public static Vector2 findEdgePoint(Entity observer, Entity target, Level level){
		return findEdgePoint(observer.centerX(), observer.centerY(), target.centerX(), target.centerY(), level);
	}
	
	public static void rotateTowards(Entity source, Entity target, float speed){
		source.setRotation(rotateTowardsPoint(source.centerX(), source.centerY(), target.centerX(), target.centerY(), source.getRotation(), speed));
	}
	
	/**
	 * Tries to rotate the abstract source so that it faces the abstract target.<br>
	 * Like an turret in a 2D environment rotates so it faces its target.
	 * @param srcX The X position of the source.
	 * @param srcY The Y position of the source.
	 * @param targetX The X position of the target.
	 * @param targetY The Y position of the target.
	 * @param currRotation The current rotation of the source.
	 * @param speed The speed of the rotation. You may want to put a low value such as 0.01.
	 * @return The new and updated rotation value(angle) of the source.
	 */
	public static float rotateTowardsPoint(float srcX, float srcY, float targetX, float targetY, float currRotation, float speed){
		float destinationRotation = (float) (Math.atan2(srcY - targetY, srcX - targetX) + Math.PI);
		currRotation = (float) Math.toRadians(currRotation);
	
		if(Math.abs((currRotation + 180 - destinationRotation) % 360 - 180) < speed)
			currRotation = destinationRotation;
		else{
		    if (destinationRotation > currRotation){
		        if (currRotation < destinationRotation - Math.PI)
		        	currRotation -= speed;
		        else
		        	currRotation += speed;
		    } else if (destinationRotation  < currRotation){
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
	
	public static Vector2 normalize(float x1, float y1, float x2, float y2){
		float dx = x1 - x2;
		float dy = y1 - y2;
		double length = Math.sqrt( dx*dx + dy*dy );
		dx /= length;
		dy /= length;
		
		return new Vector2(Float.isNaN(dx) ? 0 : dx, Float.isNaN(dy) ? 0 : dy);
	}

	public static Vector2 normalize(Entity entity1, Entity entity2){
		return normalize(entity1.x(), entity1.y(), entity2.x(), entity2.y());
	}
	
	/**
	 * Continues from the start point {@code x} and {@code y} with the given direction until the edge of the stage has been reached, which is the point returned.
	 * @param x The x coordinate to start at.
	 * @param y The y coordinate to start at.
	 * @param dir The direction to move in.
	 * @return The point.
	 */
	public static Vector2 getEdgePoint(int x, int y, Direction dir, Level level){
		int targetX, targetY;
		
		switch (dir){
			case NW:
				targetX = x - 1;
				targetY = y - 1;
				return searchTile(x, y, targetX, targetY, true, Tile.SOLID, level);
				
			case N:
				targetX = x;
				targetY = y - 1;
				return searchTile(x, y, targetX, targetY, true, Tile.SOLID, level);
			
			case NE:
				targetX = x + 1;
				targetY = y - 1;
				return searchTile(x, y, targetX, targetY, true, Tile.SOLID, level);
			
			case E:
				targetX = x + 1;
				targetY = y;
				return searchTile(x, y, targetX, targetY, true, Tile.SOLID, level);
			
			case SE:
				targetX = x + 1;
				targetY = y + 1;
				return searchTile(x, y, targetX, targetY, true, Tile.SOLID, level);
			
			case S:
				targetX = x;
				targetY = y + 1;
				return searchTile(x, y, targetX, targetY, true, Tile.SOLID, level);
			
			case SW:
				targetX = x - 1;
				targetY = y + 1;
				return searchTile(x, y, targetX, targetY, true, Tile.SOLID, level);
			
			case W:
				targetX = x - 1;
				targetY = y;
				return searchTile(x, y, targetX, targetY, true, Tile.SOLID, level);
		
			default:
				return null;
		}
	}
	
	public static Direction getDirection(float currX, float currY, float prevX, float prevY){
		if(currX == prevX && currY == prevY)
			return null;
		
		Vector2 normalized = normalize(prevX, prevY, currX, currY);
		final double fThreshold = Math.cos(Math.PI / 8);
		 
		if (normalized.x > fThreshold)
		    return Direction.W;
		else if (normalized.x < -fThreshold)
		    return Direction.E; 
		else if (normalized.y > fThreshold)
		    return Direction.N;
		else if (normalized.y < -fThreshold)
		    return Direction.S;
		else if (normalized.x > 0 && normalized.y > 0)
		    return Direction.NW;
		else if (normalized.x > 0 && normalized.y < 0)
		    return Direction.SW;
		else if (normalized.x < 0 && normalized.y > 0)
		    return Direction.NE;
		else if (normalized.x < 0 && normalized.y < 0)
		    return Direction.SE;
		
		return null;
	}

	public static Direction getDirection(MobileEntity mobile){
		return Collisions.getDirection(mobile.x(), mobile.y(), mobile.prevX(), mobile.prevY());
	}
	
	private static void addVectors2D(Vector2 v1, Vector2 v2){
		v1.x += v2.x;
		v1.y += v2.y;
	}

	private static void subVectors2D(Vector2 v1, Vector2 v2){
		v1.x -= v2.x;
		v1.y -= v2.y;
	}

	private static void rotateVector2DClockwise(Vector2 v, float ang){
		float cosa = (float) Math.cos(ang),
			  sina = (float) Math.sin(ang),
			  t = v.x;
		 
		v.x =  t * cosa + v.y * sina;
		v.y = -t * sina + v.y * cosa;
	}
	
	private static class RotRect{
		Vector2 C, S;
		float ang;
	}
}
