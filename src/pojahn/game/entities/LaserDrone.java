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
	protected float targetX, targetY;
	private int laserStartup, laserDuration, reload, sucounter, ducounter, reloadCounter;
	private Tile stopTile;
	private boolean scan, firing, allowFiringSound;
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
		this.scan = true;
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
		scan = fireAtVisible;
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
	public void logics() {
		if (--reloadCounter > 0) {
			if (!scan)
				super.logics();
			return;
		}

		if (!haveTarget()) {
			allowFiringSound = true;
			Entity target = null;
			if (scan)
				target = Collisions.findClosestSeeable(this, targets);
			else
				target = Collisions.findClosest(this, targets);

			if (target != null) {
				int x1 = (int) (x() + width() / 2), y1 = (int) (y() + height() / 2),
						x2 = (int) (target.x() + target.width() / 2), y2 = (int) (target.y() + target.height() / 2);

				Vector2 wallp = Collisions.searchTile(x1, y1, x2, y2, stopTile, getLevel());
				if (wallp == null)
					wallp = Collisions.findEdgePoint(x1, y1, x2, y2, getLevel());

				targetX = wallp.x;
				targetY = wallp.y;

				if (startupSound != null)
					startupSound.play(sounds.calc());
			} else
				super.logics();
		}
		if (haveTarget()) {
			if (!scan)
				super.logics();

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

				for (Entity go : targets)
					if (Collisions.lineRectangle((int) x(), (int) y(), (int) targetX, (int) targetY,
							go.bounds.toRectangle()))
						go.runActionEvent(this);

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