package pojahn.game.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import pojahn.game.core.Collisions;
import pojahn.game.core.Entity;
import pojahn.game.core.MobileEntity;
import pojahn.game.essentials.Image2D;

public class Chain extends MobileEntity {
	
	private Vector2 pt1, pt2;
	private Entity src1, src2;
	private int links;
	private boolean rotate, linkOnEndpoint;

	public Chain(int links) {
		this.links = links;
		linkOnEndpoint = true;
	}

	public void setLinks(int links) {
		this.links = links;
	}

	public void linkOnEndpoint(boolean linkOnEndpoint) {
		this.linkOnEndpoint = linkOnEndpoint;
	}

	public void rotateLinks(boolean rotate) {
		this.rotate = rotate;
	}

	public void endPoint1(Entity src1) {
		this.src1 = src1;
	}

	public void endPoint2(Entity src2) {
		this.src2 = src2;
	}

	public void endPoint1(Vector2 pt1) {
		this.pt1 = pt1;
	}

	public void endPoint2(Vector2 pt2) {
		this.pt2 = pt2;
	}

	@Override
	public final void render(SpriteBatch batch) {
		Vector2 endPoint1 = src1 == null ? pt1 : new Vector2(src1.centerX() - halfWidth(), src1.centerY() - halfHeight());
		Vector2 endPoint2 = src1 == null ? pt2 : new Vector2(src2.centerX() - halfWidth(), src2.centerY() - halfHeight());
		int start = (linkOnEndpoint) ? 0 : 1;
		int end = (linkOnEndpoint) ? links : links + 2;
		int cond = (linkOnEndpoint) ? links : end - 1;
		Image2D img = nextImage();
		
		if (rotate)
			bounds.rotation = (float) Collisions.getAngle(endPoint1.x, endPoint1.y, endPoint2.x, endPoint2.y);

		for (int i = start; i < cond; i++) {
			Vector2 linkPos = new Vector2(endPoint1).lerp(endPoint2, (float) i / (float) (end - 1));
			move(linkPos.x, linkPos.y);
			this.basicRender(batch, img);
		}
	}
}