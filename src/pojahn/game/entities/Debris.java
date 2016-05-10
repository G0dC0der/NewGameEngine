package pojahn.game.entities;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import pojahn.game.core.Level;

public class Debris extends Particle {

    private Particle impact, trailer;
    private Vector2 vel, tVel;
    private float accX, gravity, mass, damping, delta, vx, toleranceX, vy, toleranceY;
    private int spawns, trailerDelay, counter;
    private boolean first;

    public Debris(float vx, float toleranceX, float vy, float toleranceY) {
        this.vx = vx;
        this.vy = vy;
        this.toleranceY = toleranceY;
        this.toleranceX = toleranceX;
        trailerDelay = 5;

        vel = new Vector2();
        tVel = new Vector2(1000, -800);
        accX = 500;
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

        if(vel.x != 0.0f){
            if(runningLeft()){
                moveRight();
                if(runningRight())
                    vel.x = 0;
            } else if(runningRight()){
                moveLeft();
                if(runningLeft())
                    vel.x = 0;
            }
        }
        drag();
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

    protected void moveLeft(){
        if(vel.x < tVel.x)
            vel.x += accX * getEngine().delta;
    }

    protected void moveRight(){
        if(-vel.x < tVel.x)
            vel.x -= accX * getEngine().delta;
    }

    protected void drag(){
        float force = mass * gravity;
        vel.y *= 1.0 - (damping * getEngine().delta);

        if(tVel.y < vel.y){
            vel.y += (force / mass) * getEngine().delta;
        }else
            vel.y -= (force / mass) * getEngine().delta;
    }

    protected boolean runningLeft(){
        return vel.x > 0;
    }

    protected boolean runningRight(){
        return vel.x < 0;
    }

    protected void copyData(Debris clone){
        super.copyData(clone);
        clone.impact = impact;
        clone.vx = vx;
        clone.vy = vy;
        clone.toleranceX = toleranceX;
        clone.toleranceY = toleranceY;
        clone.spawns = spawns;
        clone.gravity = gravity;
        clone.mass = mass;
        clone.delta = delta;
        clone.damping = damping;
        clone.trailer = trailer;
        clone.trailerDelay = trailerDelay;
    }

    private void setVelocity() {
        boolean flag1 = MathUtils.random(0 ,10) > 5;
        boolean flag2 = MathUtils.random(10,20) > 15;

        float tolX = MathUtils.random(0.0f, toleranceX);
        float tolY = MathUtils.random(0.0f, toleranceY);

        if(flag1) tolX = -tolX;
        if(flag2) tolY = -tolY;

        vel.x = vx + tolX;
        vel.y = vy + tolY;
    }
}