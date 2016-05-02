package pojahn.game.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import pojahn.game.core.Collisions;
import pojahn.game.core.MobileEntity;
import pojahn.game.events.Event;

import com.badlogic.gdx.math.Vector2;

public class PathDrone extends MobileEntity {

	public static class Waypoint {

		public float targetX, targetY;
		public int frames;
		public boolean jump;
		public Event event;

		public Waypoint(float targetX, float targetY, int frames, boolean jump, Event event) {
			this.targetX = targetX;
			this.targetY = targetY;
			this.frames = frames;
			this.jump = jump;
			this.event = event;
		}

		public Waypoint(float targetX, float targetY) {
			this(targetX, targetY, 0, false, null);
		}

		@Override
		public String toString() {
			return new StringBuilder(15).append(targetX).append(" ").append(targetY).append(" ").append(frames).append(" ").append(jump).toString();
		}
	}

	private List<Waypoint> waypoints;
	private boolean rock, skip;
	private int dataCounter, stillCounter;
	private boolean playEvent;

	public PathDrone(float x, float y) {
		move(x, y);
		waypoints = new ArrayList<>();
		dataCounter = stillCounter = 0;
	}

	public PathDrone getClone() {
		PathDrone clone = new PathDrone(x(), y());
		copyData(clone);
		if(cloneEvent != null)
			cloneEvent.handleClonded(clone);
		
		return clone;
	}

	protected void copyData(PathDrone clone) {
		super.copyData(clone);
		clone.waypoints.addAll(waypoints);
		clone.skip = skip;
		clone.rock = rock;
	}

	public void appendPath(float x, float y, int frames, boolean jump, Event event) {
		waypoints.add(new Waypoint(x, y, frames, jump, event));
	}

	public void appendPath(Vector2 loc, int frames, boolean jump, Event event) {
		waypoints.add(new Waypoint(loc.x, loc.y, frames, jump, event));
	}

	public void appendPath(Waypoint pd) {
		waypoints.add(pd);
	}

	public void appendPath(Waypoint[] list) {
		waypoints.addAll(Arrays.asList(list));
	}

	public void appendPath(float x, float y) {
		appendPath(x, y, 0, false, null);
	}

	public void appendPath() {
		waypoints.add(new Waypoint(x(), y(), 0, false, null));
	}

	public Vector2 getCurrentTarget() {
		Waypoint wp = waypoints.get(dataCounter >= waypoints.size() ? 0 : dataCounter);
		return new Vector2(wp.targetX, wp.targetY);
	}

	public void appendReversed() {
		List<Waypoint> reversed = new ArrayList<>(waypoints);
		reversed.remove(reversed.size() - 1);
		Collections.reverse(reversed);

		waypoints.addAll(reversed);
	}

	public void clearData() {
		waypoints.clear();
		rollback();
	}

	public void rollback() {
		dataCounter = stillCounter = 0;
	}

	@Override
	public void logistics() {
		if (!waypoints.isEmpty() && getMoveSpeed() > 0 && !isFrozen()) {
			if (dataCounter >= waypoints.size())
				dataCounter = 0;

			Waypoint wp = waypoints.get(dataCounter);

			if (reached(wp)) {
				if (++stillCounter > wp.frames)
					dataCounter++;

				forgetPast();

				bounds.pos.x = wp.targetX;
				bounds.pos.y = wp.targetY;

				if (playEvent && wp.event != null) {
					wp.event.eventHandling();
					playEvent = false;
				}
			} else {
				playEvent = true;
				stillCounter = 0;

				if (wp.jump)
					move(wp.targetX, wp.targetY);
				else
					moveTowards(wp.targetX, wp.targetY);
			}
		}
	}

	@Override
	public void dumbMoveTowards(float targetX, float targetY, float steps) {
		if (isFrozen())
			return;

		Vector2 next = attemptTowards(targetX, targetY, steps);
		
		if (rock) {
			boolean canNext = !occupiedAt(next.x, next.y);
			if (canNext)
				move(next.x, next.y);
			else if (!canNext && skip)
				dataCounter++;
			else if (!canNext && !skip)
				forgetPast();
		} else
			move(next.x, next.y);
	}
	
	@Override
	protected void smartMoveTowards(float targetX, float targetY, float steps) {
		if (isFrozen())
			return;

		Vector2 next = attemptTowards(targetX, targetY, steps);
		
		if (rock) {
			if(smartMove(next.x, next.y)){}
			else if (skip)
				dataCounter++;
			else
				forgetPast();
		} else
			smartMove(next.x, next.y);
	}

	/**
	 * A PathDrone that is rocky will respect walls and solid objects.
	 * @param rock True if this PathDrone respect walls.
	 * @param skip Whether or not to skip the current waypoint and go for the next one when blocked.
	 */
	public void setRock(boolean rock, boolean skip) {
		this.rock = rock;
		this.skip = skip;
	}

	protected boolean reached(Waypoint pd) {
		return getMoveSpeed() > Collisions.distance(pd.targetX, pd.targetY, x(), y());
	}
}