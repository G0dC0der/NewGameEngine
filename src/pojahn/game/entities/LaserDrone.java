package pojahn.game.entities;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import pojahn.game.core.Collisions;
import pojahn.game.core.Entity;
import pojahn.game.core.Level.Tile;
import pojahn.game.essentials.LaserBeam;

public class LaserDrone extends PathDrone {

	private float targetX, targetY;
	private int laserStartup, laserDuration, reload, sucounter, ducounter, reloadCounter;
	private Tile stopTile;
	private boolean fireAtVisible, firing, allowFiringSound;
	private Particle exp;
	private Color laserTint;
	private final Entity[] targets;
	private LaserBeam firingBeam, chargeBeam;
	private Sound startupSound, firingSound;

	public LaserDrone(float x, float y, int laserStartup, int laserDuration, int reload, Entity... targets) {
		super(x, y);
		this.laserStartup = laserStartup;
		this.laserDuration = laserDuration;
		this.targets = targets;
		this.reload = reload;
		this.fireAtVisible = true;
		targetX = targetY = -1;
		sucounter = ducounter = reloadCounter = 0;
		stopTile = Tile.SOLID;
		laserTint = Color.valueOf("CC0000FF");
	}

	public void setStartupSound(Sound startupSound) {
		this.startupSound = startupSound;
	}

	public void setFiringSound(Sound firingSound) {
		this.firingSound = firingSound;
	}

	public void setExplosion(Particle exp) {
		this.exp = exp;
	}

	public boolean haveTarget() {
		return targetX != -1;
	}

	public void setStopTile(Tile stopTile) {
		this.stopTile = stopTile;
	}

	public void fireAtVisible(boolean fireAtVisible) {
		this.fireAtVisible = fireAtVisible;
	}

	public void setLaserTint(Color tint) {
		this.laserTint = tint;
	}

	public LaserBeam getFiringBeam() {
		return firingBeam;
	}

	public void setFiringBeam(LaserBeam firingBeam) {
		this.firingBeam = firingBeam;
	}

	public LaserBeam getChargeBeam() {
		return chargeBeam;
	}

	public void setChargeBeam(LaserBeam chargeBeam) {
		this.chargeBeam = chargeBeam;
	}

	@Override
	public void logistics() {
		if (--reloadCounter > 0) {
			if (!fireAtVisible)
				super.logistics();
			return;
		}

		if (!haveTarget()) {
			allowFiringSound = true;
			Entity target = null;
			if (fireAtVisible)
				target = Collisions.findClosestSeeable(this, targets);
			else
				target = Collisions.findClosest(this, targets);

			if (target != null) {
				int x1 = (int) (x() + width() / 2), y1 = (int) (y() + height() / 2);
				int x2 = (int) (target.x() + target.width() / 2), y2 = (int) (target.y() + target.height() / 2);

				Vector2 wallPoint = Collisions.searchTile(x1, y1, x2, y2, stopTile, getLevel());
				if (wallPoint == null)
					wallPoint = Collisions.findEdgePoint(x1, y1, x2, y2, getLevel());

				targetX = wallPoint.x;
				targetY = wallPoint.y;

				if (startupSound != null)
					startupSound.play(sounds.calc());
			} else
				super.logistics();
		}
		if (haveTarget()) {
			if (!fireAtVisible)
				super.logistics();

			if (!firing && ++sucounter % laserStartup == 0) {
				firing = true;

				if (exp != null)
					getLevel().add(exp.getClone().move(targetX - exp.width() / 2, targetY - exp.height() / 2));
			}
			if (firing) {
				firingBeam.fireAt(x() + width() / 2, y() + height() / 2, targetX, targetY, 1);

				if (startupSound != null)
					startupSound.stop();

				if (firingSound != null && allowFiringSound) {
					firingSound.play(sounds.calc());
					allowFiringSound = false;
				}

				for (Entity entity : targets)
					if (Collisions.lineRectangle((int) x(), (int) y(), (int) targetX, (int) targetY,
							entity.bounds.toRectangle()))
						entity.runActionEvent(this);

				if (++ducounter % laserDuration == 0) {
					targetX = targetY = -1;
					reloadCounter = reload;
					firing = false;
				}
			} else
				chargeBeam.fireAt(x() + width() / 2, y() + height() / 2, targetX, targetY, 1);
		}
	}

	@Override
	public void render(SpriteBatch b) {
		super.render(b);

		Color defaultColor = b.getColor();

		if (laserTint != null)
			b.setColor(laserTint);

		chargeBeam.drawLasers(b);
		firingBeam.drawLasers(b);

		b.setColor(defaultColor);
	}
}