package pojahn.game.entities;

import com.badlogic.gdx.math.Vector2;

import pojahn.game.core.Collisions;
import pojahn.game.core.Level;

public class Shrapnel extends Particle {
	private Projectile split;
	private boolean once;

	public Shrapnel(float x, float y, Projectile shrapnel) {
		this.split = shrapnel;
	}
	
	public Shrapnel getClone(float x, float y) {
		Shrapnel s = new Shrapnel(x, y, split);
		copyData(s);
		if (cloneEvent != null)
			cloneEvent.handleClonded(s);

		return s;
	}

	@Override
	public void logistics() {
		if (!once) {
			once = true;

			Vector2[] edgePoints = getEightDirection();
			Level l = getLevel();

			for (Vector2 edgePoint : edgePoints) {
				Projectile proj = split.getClone();
				proj.center(this);
				proj.setTarget(edgePoint);
				l.add(proj);
			}

		}

		super.logistics();
	}

	Vector2[] getEightDirection(){
		float middleX = centerX(),
			  middleY = centerY(),
			  x,y;
		
		//NW Point
		x = middleX - 1;
		y = middleY - 1;
		Vector2 p1 = Collisions.findEdgePoint(middleX, middleY, x, y, getLevel());
		
		//N Point
		x = middleX;
		y = middleY - 1;
		Vector2 p2 = Collisions.findEdgePoint(middleX, middleY, x, y, getLevel());
		
		//NE Point
		x = middleX + 1;
		y = middleY - 1;
		Vector2 p3 = Collisions.findEdgePoint(middleX, middleY, x, y, getLevel());
		
		//E Point
		x = middleX + 1;
		y = middleY;
		Vector2 p4 = Collisions.findEdgePoint(middleX, middleY, x, y, getLevel());
		
		//SE Point
		x = middleX + 1;
		y = middleY + 1;
		Vector2 p5 = Collisions.findEdgePoint(middleX, middleY, x, y, getLevel());
		
		//S Point
		x = middleX;
		y = middleY + 1;
		Vector2 p6 = Collisions.findEdgePoint(middleX, middleY, x, y, getLevel());
		
		//SW Point
		x = middleX - 1;
		y = middleY + 1;
		Vector2 p7 = Collisions.findEdgePoint(middleX, middleY, x, y, getLevel());
		
		//W Point
		x = middleX - 1;
		y = middleY;
		Vector2 p8 = Collisions.findEdgePoint(middleX, middleY, x, y, getLevel());
		
		return new Vector2[]{p1,p2,p3,p4,p5,p6,p7,p8};
	}
}