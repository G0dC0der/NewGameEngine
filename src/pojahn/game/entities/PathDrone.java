package pojahn.game.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import pojahn.game.core.Collisions;
import pojahn.game.core.MobileEntity;
import pojahn.game.events.Event;

import com.badlogic.gdx.math.Vector2;

/**
 * Most enemies inherit this class rather than {@code Enemy} as it offer a common used functionality: waypoint pathing.<br>
 * The {@code PathDrone} moves to the given waypoints and return to the first one when the final waypoint is reached.  Thus looping.<br>
 * Events and other functionality can be added to the waypoints to customize the behavior of the drone.
 * 
 * @author Pojahn Moradi
 */
public class PathDrone extends MobileEntity {

	/**
	 * Instances of this class are passed to a {@code PathDrone} object which it will use to navigate.
	 * @author Pojahn Moradi
	 */
	public static class Waypoint {

		/**
		 * The coordinate to move to.
		 */
		public float targetX, targetY;
		/**
		 * The amount of frames to stay at the target when reached.
		 */
		public int frames;
		/**
		 * Whether or not to jump at the target(i e moving there instantly).
		 */
		public boolean jump;
		/**
		 * The event to fire when the target has been reached.
		 */
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

	/**
	 * Constructs a {@code PathDrone} with no waypoints.
	 * 
	 * @param x The starting X position.
	 * @param y The starting Y position.
	 */
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

	/**
	 * Appends a path to the waypoint list.
	 * 
	 * @param x The target X coordinate.
	 * @param y The target Y coordinate.
	 * @param frames The amount of frames to stay at the target.
	 * @param jump Whether or not to jump to the target.
	 * @param event The event to execute when the target has been reached.
	 */
	public void appendPath(float x, float y, int frames, boolean jump, Event event) {
		waypoints.add(new Waypoint(x, y, frames, jump, event));
	}

	public void appendPath(Vector2 loc, int frames, boolean jump, Event event) {
		waypoints.add(new Waypoint(loc.x, loc.y, frames, jump, event));
	}

	/**
	 * Appends a path to the waypoint list.
	 * @param pd The waypoint.
	 */
	public void appendPath(Waypoint pd) {
		waypoints.add(pd);
	}

	/**
	 * Appends an array of paths to the waypoint list.
	 * @param list A list of waypoints.
	 */
	public void appendPath(Waypoint[] list) {
		waypoints.addAll(Arrays.asList(list));
	}

	/**
	 * Appends a path to the waypoint list.
	 * @param x The target X coordinate.
	 * @param y The target Y coordinate.
	 */
	public void appendPath(float x, float y) {
		appendPath(x, y, 0, false, null);
	}

	/**
	 * Appends the current position to the waypoint list.
	 */
	public void appendPath() {
		waypoints.add(new Waypoint(x(), y(), 0, false, null));
	}

	/**
	 * Returns the current target.
	 * @return The coordinate.
	 */
	public Vector2 getCurrentTarget() {
		Waypoint wp = waypoints.get(dataCounter >= waypoints.size() ? 0 : dataCounter);
		return new Vector2(wp.targetX, wp.targetY);
	}

	/**
	 * Creates a clone of the current waypoint list, reverses the order and appends it to the list.
	 */
	public void appendReversed() {
		List<Waypoint> reversed = new ArrayList<>(waypoints);
		reversed.remove(reversed.size() - 1);
		Collections.reverse(reversed);

		waypoints.addAll(reversed);
	}

	/**
	 * Clears all the waypoints from this unit.
	 */
	public void clearData() {
		waypoints.clear();
		rollback();
	}

	/**
	 * Sets the current waypoint to the first one(reseting).
	 */
	public void rollback() {
		dataCounter = stillCounter = 0;
	}

	@Override
	public void logics() {
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
					moveToward(wp.targetX, wp.targetY);
			}
		}
	}

	@Override
	public void moveToward(float targetX, float targetY, float steps) {
		if (isFrozen())
			return;

		float fX = targetX - bounds.pos.x;
		float fY = targetY - bounds.pos.y;
		double dist = Math.sqrt(fX * fX + fY * fY);
		double step = steps / dist;

		float tx = (float) (bounds.pos.x + fX * step);
		float ty = (float) (bounds.pos.y + fY * step);

		if (rock) {
			boolean canNext = !occupiedAt(tx, ty);
			if (canNext)
				move(tx, ty);
			else if (!canNext && skip)
				dataCounter++;
			else if (!canNext && !skip)
				forgetPast();
		} else
			move(tx, ty);
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