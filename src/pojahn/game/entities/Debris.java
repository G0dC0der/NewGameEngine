package pojahn.game.entities;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import pojahn.game.core.Level;

public class Debris extends Particle {

    private Particle impact, trailer;
    private Vector2 vel, tVel;
    private float gravity, mass, damping, delta, vx, toleranceX, vy, toleranceY;
    private int spawns, trailerDelay, counter;
    private boolean first;

    public Debris(float vx, float toleranceX, float vy, float toleranceY) {
        this.vx = vx;
        this.vy = vy;
        this.toleranceX = toleranceX;
        this.toleranceY = toleranceY;
        trailerDelay = 5;

        vel = new Vector2();
        tVel = new Vector2(1000, -800);
        mass = 1.0f;
        gravity = -500;
        delta = 1.0f / 60.0f;
        damping = 0.0001f;

        setVelocity();
    }

    @Override
    public Debris getClone(){
        Debris clone = new Debris(vx, toleranceX, vy, toleranceY);
        copyData(clone);

        if (cloneEvent != null)
            cloneEvent.handleClonded(clone);

        return clone;
    }

    @Override
    public void logistics() {
        Level l = getLevel();

        if(!first){
            first = true;
            if(introSound != null)
                introSound.play(sounds.calc());

            for(int i = 0; i < spawns; i++){
                Debris clone = getClone();
                clone.spawns = 0;
                if(spawns > 3)
                    clone.introSound = null;
                l.add(clone);
            }
        }

        drag();
        applyYForces();
        applyXForces();

        if(trailer != null && ++counter % trailerDelay == 0)
            l.add(trailer.getClone().center(this));

        if(occupiedAt(x(), y())){
            l.discard(this);
            if(impact != null)
                l.add(impact.getClone().center(this));
        }
    }

    public void setTrailer(Particle trailer, int trailerDelay) {
        this.trailer = trailer;
        this.trailerDelay = trailerDelay;
    }

    public void setSpawns(int spawns) {
        this.spawns = spawns;
    }

    public void setImpact(Particle impact){
        this.impact = impact;
    }

    protected void applyXForces(){
        bounds.pos.x -= vel.x * getEngine().delta;
    }

    protected void applyYForces() {
        bounds.pos.y -= vel.y * getEngine().delta;
    }

    protected void drag(){
        float force = mass * gravity;
        vel.y *= 1.0 - (damping * getEngine().delta);

        if(tVel.y < vel.y){
            vel.y += (force / mass) * getEngine().delta;
        }else
            vel.y -= (force / mass) * getEngine().delta;
    }

    protected void copyData(Debris clone){
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
        float halfX = toleranceX / 2;
        float halfY = toleranceY / 2;

        float tolX = MathUtils.random(-halfX, halfX);
        float tolY = MathUtils.random(-halfY, halfY);

        vel.x = vx + tolX;
        vel.y = vy + tolY;

        if(random())
            vel.x = -vel.x;
    }

    private boolean random() {
        return MathUtils.random(0,100) > 50;
    }
}