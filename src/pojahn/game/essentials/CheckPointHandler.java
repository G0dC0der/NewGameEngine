package pojahn.game.essentials;

import java.util.ArrayList;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import pojahn.game.core.Collisions;
import pojahn.game.core.Entity;
import pojahn.game.events.Event;

public class CheckPointHandler {
	private static class Checkpoint {
		float startX, startY, x, y, width, height;
		boolean taken;

		Checkpoint(float startX, float startY, float x, float y, float width, float height) {
			super();
			this.startX = startX;
			this.startY = startY;
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}
	}

	private ArrayList<Checkpoint> checkpoints;
	private Entity users[];
	private Event reachEvent;

	public CheckPointHandler() {
		checkpoints = new ArrayList<>();
	}

	public void setUsers(Entity... users) {
		this.users = users;
	}

	public void appendCheckpoint(Vector2 startPos, Rectangle area) {
		appendCheckpoint(startPos.x, startPos.y, area.x, area.y, area.width, area.height);
	}

	public void appendCheckpoint(float startX, float startY, Rectangle area) {
		appendCheckpoint(startX, startY, area.x, area.y, area.width, area.height);
	}

	public void appendCheckpoint(Vector2 startPos, float x, float y, float width, float height) {
		appendCheckpoint(startPos.x, startPos.y, x, y, width, height);
	}

	public void appendCheckpoint(float startX, float startY, float x, float y, float width, float height) {
		checkpoints.add(new Checkpoint(startX, startY, x, y, width, height));
	}

	public void reset() {
		for (Checkpoint cp : checkpoints)
			cp.taken = false;
	}

	public Vector2 getLastestCheckpoint() {
		for (int i = checkpoints.size() - 1; i >= 0; i--) {
			Checkpoint cp = checkpoints.get(i);

			if (cp.taken)
				return new Vector2(cp.startX, cp.startY);
		}

		return null;
	}

	public boolean reached(int cpIndex) {
		return checkpoints.get(cpIndex).taken;
	}

	public void setReachEvent(Event reachEvent) {
		this.reachEvent = reachEvent;
	}

	public void update() {
		Outer: for (Checkpoint cp : checkpoints) {
			if (!cp.taken) {
				for (Entity user : users) {
					if (Collisions.rectanglesCollide(user.x(), user.y(), user.width(), user.height(), cp.x, cp.y, cp.width, cp.height)) {
						cp.taken = true;
						if (reachEvent != null)
							reachEvent.eventHandling();

						continue Outer;
					}
				}
			}
		}
	}
}