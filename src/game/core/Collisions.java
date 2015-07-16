package game.core;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;

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
	
	public static boolean rotatedRectanglesCollide(Entity rec1, Entity rec2)
	{
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
		
		if (t < 0)
		{
			t = A.x; A.x = B.x; B.x = t;
			t = A.y; A.y = B.y; B.y = t;
		}
		if (sina < 0)
		{
			B.x = -B.x;
			B.y = -B.y;
		}
		if (B.x > TR.x || B.x > -BL.x) 
			return false;
		
		if (t == 0)
		{
			ext1 = A.y;
			ext2 = -ext1;
		}
		else
		{
			x = BL.x-A.x;
			a = TR.x-A.x;
			ext1 = A.y;
			  
			if (a*x > 0)
			{
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
		
			if (a*x > 0)
			{
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
	
	public static boolean circleRectangleCollide(Entity circle, Entity rect)
	{
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
	
	public static boolean circleVsCircle(Entity c1, Entity c2)
	{
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
	
	public static boolean polygonsCollide(Entity entity1, Entity entity2)
	{
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

	public static boolean pixelPerfect(Entity entity1, Entity entity2)
	{	
		Image2D image1 = entity1.nextImage();
		Image2D image2 = entity2.nextImage();
				
		float width1  = image1.getWidth();
		float width2  = image2.getWidth();
		float height1 = image1.getHeight();
		float height2 = image2.getHeight();
		int top    = (int) Math.max(entity1.bounds.y, entity2.bounds.y);
		int bottom = (int) Math.min(entity1.bounds.y + height1, entity2.bounds.y + height2);
		int left   = (int) Math.max(entity1.bounds.x, entity2.bounds.x);
		int right  = (int) Math.min(entity1.bounds.x + width1, entity2.bounds.x + width2);
		
		for (int y = top; y < bottom; y++)
		{
			for (int x = left; x < right; x++)
			{
				int x1 = (int) ((entity1.flipX) ? width1  - (x - entity1.bounds.x) - 1 : x - entity1.bounds.x);
				int y1 = (int) ((entity1.flipY) ? height1 - (y - entity1.bounds.y) - 1 : y - entity1.bounds.y);
				int x2 = (int) ((entity2.flipX) ? width2  - (x - entity2.bounds.x) - 1 : x - entity2.bounds.x);
				int y2 = (int) ((entity2.flipY) ? height2 - (y - entity2.bounds.y) - 1 : y - entity2.bounds.y);
				
				if (image1.getPixel(x1, y1) != 0 && image2.getPixel(x2, y2) != 0)
					return true;
			}
		}
		return false;
	}
	
	public static double getAngle(float x1, float y1, float x2, float y2)
	{
		float deltaX = x2 - x1;
		float deltaY = y2 - y1;
		
		return Math.toDegrees(Math.atan2(deltaY, deltaX));
	}
	
	private static void addVectors2D(Vector2 v1, Vector2 v2)
	{
		v1.x += v2.x;
		v1.y += v2.y;
	}

	private static void subVectors2D(Vector2 v1, Vector2 v2)
	{
		v1.x -= v2.x;
		v1.y -= v2.y;
	}

	private static void rotateVector2DClockwise(Vector2 v, float ang)
	{
		float cosa = (float) Math.cos(ang),
			  sina = (float) Math.sin(ang),
			  t = v.x;
		 
		v.x =  t * cosa + v.y * sina;
		v.y = -t * sina + v.y * cosa;
	}
	
	private static class RotRect
	{
		Vector2 C, S;
		float ang;
	}
}
