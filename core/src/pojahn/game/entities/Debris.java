package pojahn.game.entities;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class Debris extends Particle {

    private final Vector2 vel, tVel;
    private final float vx, vy, toleranceX, toleranceY;
    private float gravity, mass, damping, delta;
    private Particle impact, trailer;
    private int spawns, trailerDelay, counter;

    public Debris(final float vx, final float toleranceX, final float vy, final float toleranceY) {
        this.vx = vx;
        this.vy = vy;
        this.toleranceX = toleranceX;
        this.toleranceY = toleranceY;
        this.trailerDelay = 5;

        this.vel = new Vector2();
        this.tVel = new Vector2(1000, -800);
        this.mass = 1.0f;
        this.gravity = -500;
        this.delta = 1.0f / 60.0f;
        this.damping = 0.0001f;

        setVelocity();
    }

    @Override
    public Debris getClone() {
        final Debris clone = new Debris(vx, toleranceX, vy, toleranceY);
        copyData(clone);

        if (cloneEvent != null)
            cloneEvent.handleClonded(clone);

        return clone;
    }

    @Override
    protected boolean completed() {
        return occupiedAt(x(), y());
    }

    @Override
    protected void outro() {
        if (impact != null)
            getLevel().add(impact.getClone().center(this));
    }

    @Override
    protected void erupt() {
        for (int i = 0; i < spawns; i++) {
            final Debris clone = getClone();
            clone.spawns = 0;
            if (spawns > 3)
                clone.setIntroSound(null);

            getLevel().add(clone);
        }
    }

    @Override
    protected void frameStep() {
        drag();
        applyYForces();
        applyXForces();

        if (trailer != null && ++counter % trailerDelay == 0)
            getLevel().add(trailer.getClone().center(this));
    }

    public void setTrailer(final Particle trailer, final int trailerDelay) {
        this.trailer = trailer;
        this.trailerDelay = trailerDelay;
    }

    public void setSpawns(final int spawns) {
        this.spawns = spawns;
    }

    public void setImpact(final Particle impact) {
        this.impact = impact;
    }

    private void applyXForces() {
        bounds.pos.x -= vel.x * getEngine().delta;
    }

    private void applyYForces() {
        bounds.pos.y -= vel.y * getEngine().delta;
    }

    private void drag() {
        final float force = mass * gravity;
        vel.y *= 1.0 - (damping * getEngine().delta);

        if (tVel.y < vel.y) {
            vel.y += (force / mass) * getEngine().delta;
        } else
            vel.y -= (force / mass) * getEngine().delta;
    }

    protected void copyData(final Debris clone) {
        super.copyData(clone);
        clone.impact = impact;
        clone.spawns = spawns;
        clone.gravity = gravity;
        clone.mass = mass;
        clone.delta = delta;
        clone.damping = damping;
        clone.trailer = trailer;
        clone.trailerDelay = trailerDelay;
    }

    private void setVelocity() {
        final float halfX = toleranceX / 2;
        final float halfY = toleranceY / 2;

        final float tolX = MathUtils.random(-halfX, halfX);
        final float tolY = MathUtils.random(-halfY, halfY);

        vel.x = vx + tolX;
        vel.y = vy + tolY;

        if (random())
            vel.x = -vel.x;
    }

    private boolean random() {
        return MathUtils.random(0, 100) > 50;
    }
}