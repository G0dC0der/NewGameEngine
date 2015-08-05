package game.core;

import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import game.core.Level.Tile;
import game.essentials.Direction;
import game.essentials.Hitbox;
import game.essentials.Image2D;

public class Collisions {

	public static boolean rectanglesCollide(Entity rec1, Entity rec2){
		if ((rec1.bounds.x + rec1.bounds.height < rec2.bounds.y) ||
	        (rec1.bounds.y > rec2.bounds.y + rec2.bounds.height) ||
	        (rec1.bounds.x + rec1.bounds.width < rec2.bounds.x)  ||
	        (rec1.bounds.x > rec2.bounds.x + rec2.bounds.width))
	        return false;
			
			return true;
	}
	
	public static boolean rotatedRectanglesCollide(Entity rec1, Entity rec2){
		RotRect rr1 = new RotRect();
		rr1.C = new Vector2(rec1.bounds.x + rec1.bounds.width / 2, rec1.bounds.y + rec1.bounds.height / 2);
		rr1.S = new Vector2(rec1.bounds.width / 2, rec1.bounds.height / 2);
		rr1.ang = (float) Math.toRadians(rec1.getRotation());
		
		RotRect rr2 = new RotRect();
		rr2.C = new Vector2(rec2.bounds.x + rec2.bounds.width / 2, rec2.bounds.y + rec2.bounds.height / 2);
		rr2.S = new Vector2(rec2.bounds.width / 2, rec2.bounds.height / 2);
		rr2.ang = (float) Math.toRadians(rec2.getRotation());
		
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
	
	public static boolean circleRectangleCollide(Entity circle, Entity rect){
	    float circleDistanceX = Math.abs((circle.bounds.x + circle.width()  / 2) - (rect.bounds.x + rect.width()  / 2));
	    float circleDistanceY = Math.abs((circle.bounds.y + circle.height() / 2) - (rect.bounds.y + rect.height() / 2));
	    float radius = circle.width() / 2;

	    if (circleDistanceX > (rect.width() / 2 + radius) || (circleDistanceY > (rect.height() / 2 + radius)))
	    	return false;
	    
	    if ((circleDistanceX <= (rect.width() / 2)) || (circleDistanceY <= (rect.height() / 2)))
	    	return true;

	    double cornerDistance_sq = Math.pow(circleDistanceX - rect.width() /2, 2) +
	                               Math.pow(circleDistanceY - rect.height()/2, 2);

	    return (cornerDistance_sq <= (radius * radius));
	}
	
	public static boolean circleVsCircle(Entity c1, Entity c2){
		float x1 = c1.bounds.x + c1.width()  / 2,
			  y1 = c1.bounds.y + c1.height() / 2,
			  x2 = c2.bounds.x + c2.width()  / 2,
			  y2 = c2.bounds.y + c2.height() / 2,
			  r1 = c1.width() / 2,
			  r2 = c2.height() / 2;

	    float dx = x2 - x1;
	    float dy = y2 - y1;
	    float d = r1 + r2;
	    return (dx * dx + dy * dy) < (d * d);
	}
	
	/**
	 * Performs between the two entities polygons.
	 * Precondition: offsetX and offsetY == 0, flipX and flipY == false
	 * @return True if the two polygons are colliding.
	 */
	public static boolean polygonsCollide(Entity entity1, Entity entity2){
		preparePolygon(entity1);
		preparePolygon(entity2);
		return Intersector.overlapConvexPolygons(entity1.poly, entity2.poly);
	}
	
	private static void preparePolygon(Entity entity){
		if(	entity.poly.getX()	 			!= entity.bounds.x 		||
			entity.poly.getY() 				!= entity.bounds.y 		|| 
			entity.poly.getRotation() 		!= entity.getRotation()	|| 
			entity.poly.getScaleX() 		!= entity.scaleX		|| 
			entity.poly.getScaleY() 		!= entity.scaleY)
		{
			entity.poly.setRotation(entity.getRotation());
			entity.poly.setScale(entity.scaleX, entity.scaleY);
			entity.poly.setPosition(entity.bounds.x, entity.bounds.y);
			entity.poly.setOrigin(entity.bounds.x - entity.bounds.width / 2, entity.bounds.y - entity.bounds.height / 2);
		}
	}

	/**
	 * Performs a pixel perfect collision check.
	 * Precondition: Rotation == 0, scaleX and scaleY == 1, offsetX and offsetY == 0
	 * @return True if the two entities are colliding.
	 */
	public static boolean pixelPerfect(Entity entity1, Entity entity2){	
		Image2D image1 = entity1.getImage().getCurrentObject();
		Image2D image2 = entity2.getImage().getCurrentObject();
				
		float width1  = image1.getWidth();
		float width2  = image2.getWidth();
		float height1 = image1.getHeight();
		float height2 = image2.getHeight();
		int top    = (int) Math.max(entity1.bounds.y, entity2.bounds.y);
		int bottom = (int) Math.min(entity1.bounds.y + height1, entity2.bounds.y + height2);
		int left   = (int) Math.max(entity1.bounds.x, entity2.bounds.x);
		int right  = (int) Math.min(entity1.bounds.x + width1, entity2.bounds.x + width2);
		
		for (int y = top; y < bottom; y++){
			for (int x = left; x < right; x++){
				int x1 = (int) ((entity1.flipX) ? width1  - (x - entity1.bounds.x) - 1 : x - entity1.bounds.x);
				int y1 = (int) ((entity1.flipY) ? height1 - (y - entity1.bounds.y) - 1 : y - entity1.bounds.y);
				int x2 = (int) ((entity2.flipX) ? width2  - (x - entity2.bounds.x) - 1 : x - entity2.bounds.x);
				int y2 = (int) ((entity2.flipY) ? height2 - (y - entity2.bounds.y) - 1 : y - entity2.bounds.y);
				
				//TODO: Test this! - If this works, remove polygon
				if(entity1.getRotation() != 0){
					Vector2 rotated = rotatePoint(x1, y1, entity1.centerX(), entity1.centerY(), entity1.getRotation());
					x1 = (int) rotated.x;
					y1 = (int) rotated.y;
				}
				if(entity2.getRotation() != 0){
					Vector2 rotated = rotatePoint(x2, y2, entity2.centerX(), entity2.centerY(), entity2.getRotation());
					x2 = (int) rotated.x;
					y2 = (int) rotated.y;
				}
				
				if (image1.getPixel(x1, y1) != 0 && image2.getPixel(x2, y2) != 0)
					return true;
			}
		}
		return false;
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
	
	public static boolean lineItersects(Vector2 p1, Vector2 p2, Vector2 p3, Vector2 p4){
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
		if(u < 0 || u > 1)
			return false;
		  
		return true;
	}
	
	/**
	 * Returns the bounding box of the (rotated) rectangle.
	 * @param entity The {@code GameObject} to calculate the bounding box on.
	 * @return The bounding box.
	 */
	public static Rectangle getBoundingBox(Entity entity){ //TODO: Take scale into account.
		if(entity.getRotation() == 0)
			return new Rectangle(entity.x(), entity.y(), entity.width(), entity.height());
		
		ArrayList<Vector2> points = new ArrayList<>(4);
		float[] arr = new float[8];
		
		for(int i = 0; i < 4; i++){
			float 	x = 0,
					y = 0;
			
			switch(i){
				case 0:
					x = entity.x();
					y = entity.y();
					break;
				case 1:
					x = entity.x() + entity.width();
					y = entity.y();
					break;
				case 2:
					x = entity.x();
					y = entity.y();
					break;
				case 3:
					x = entity.x() + entity.width();
					y = entity.y() + entity.height();
					break;
			}

			arr[0] = x;
			arr[1] = y;
			arr[2] = arr[3] = arr[4] = arr[5] = arr[6] = arr[7] = 0;
			
			AffineTransform at = new AffineTransform();
			at.rotate(Math.toRadians(entity.getRotation()), entity.centerX(), entity.centerY());
			at.transform(arr, 0, arr, 0, 4);
			
			points.add(new Vector2(arr[0], arr[1]));
		}
		
		float minX, maxX, minY, maxY, value1, value2;
		
		value1 = Math.min(points.get(0).x, points.get(1).x);
		value2 = Math.min(points.get(2).x, points.get(3).x);
		minX = Math.min(value1, value2);
		
		value1 = Math.max(points.get(0).x, points.get(1).x);
		value2 = Math.max(points.get(2).x, points.get(3).x);
		maxX = Math.max(value1, value2);
		
		value1 = Math.min(points.get(0).y, points.get(1).y);
		value2 = Math.min(points.get(2).y, points.get(3).y);
		minY = Math.min(value1, value2);
		
		value1 = Math.max(points.get(0).y, points.get(1).y);
		value2 = Math.max(points.get(2).y, points.get(3).y);
		maxY = Math.max(value1, value2);
		
		return new Rectangle(minX, minY, maxX - minX, maxY - minY);
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
		
//		final float[] arr = {x, y, 0, 0, 0, 0, 0, 0};
//		
//		AffineTransform at = new AffineTransform();
//		at.rotate(Math.toRadians(rotation), cx, cy);
//		at.transform(arr, 0, arr, 0, 4);
//		
//		return new Vector2(arr[0], arr[1]);
		Vector2 p = new Vector2(x,y);
		float s = MathUtils.sin(rotation);
		float c = MathUtils.cos(rotation);

		p.x -= cx;
		p.y -= cy;

		float xnew = p.x * c - p.y * s;
		float ynew = p.x * s + p.y * c;

		p.x = xnew + cx;
		p.y = ynew + cy;
		return p;
	}
	
	public static Vector2 searchTile(int x0, int y0, final int x1, final int y1, Tile tile, Level level){
		return searchTile(x0, y0, x1, y1, false, tile, level);
	}
	
	public static boolean solidSpace(int x0, int y0, final int x1, final int y1, Level level){
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
			if(outOfBounds(x0, y0, level) || (!continuesly && x0 == x1 && y0 == y1))
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
	
	public static boolean outOfBounds(float x, float y, Level level){
		if(x >= level.getWidth()  ||
		   y >= level.getHeight() || 
		   x < 0 ||
		   y < 0)
			return true;
		
		return false;
	}
	
	public static boolean circleVsLine(float Ax, float Ay, float Bx, float By, Entity circle){
		float r = circle.width() / 2;
		float Cx = circle.x() + r;
		float Cy = circle.y() + circle.height() / 2;
		
		double LAB = Math.sqrt((Bx-Ax)*(Bx-Ax) + (By-Ay)*(By-Ay));
		double Dx = (Bx-Ax)/LAB;
		double Dy = (By-Ay)/LAB;
		double t = Dx*(Cx-Ax) + Dy*(Cy-Ay);
		double Ex = t*Dx+Ax;
		double Ey = t*Dy+Ay;
		double LEC = Math.sqrt((Ex-Cx)*(Ex-Cx) + (Ey-Cy)*(Ey-Cy));
		if(LEC <= r)
			return true;
		
		return false;
	}
	
	/**
	 * Check if the specified GameObject appear between the two points.
	 * @return True if the specified {@code Entity} is intersecting with the given line.
	 */
	public static boolean lineEntityCollide(float x1, float y1, float x2, float y2, Entity entity){		
		if(entity.getHitbox() == Hitbox.CIRCLE)
			return circleVsLine(x1,y1,x2,y2,entity);
		else{
			if(lineIntersect(x1,y1,x2,y2, entity.x(), entity.y(), entity.x() + entity.width(), entity.y())              			||
			   lineIntersect(x1,y1,x2,y2, entity.x(), entity.y(), entity.x(), entity.y() + entity.height())             			||
			   lineIntersect(x1,y1,x2,y2, entity.x() + entity.width(), entity.y(), entity.x() + entity.width(), entity.y() + entity.height()) ||
			   lineIntersect(x1,y1,x2,y2, entity.x(), entity.y() + entity.height(), entity.x() + entity.width(), entity.y() + entity.height()))
				return true;
					
			return false;
		}
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
