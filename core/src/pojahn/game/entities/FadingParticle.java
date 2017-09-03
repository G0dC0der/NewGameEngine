package pojahn.game.entities;

public class FadingParticle extends Particle{

    private float fadingSpeed;

    public FadingParticle() {
        this(.05f);
    }

    public FadingParticle(float fadingSpeed) {
        this.fadingSpeed = fadingSpeed;
    }

    public void setFadingSpeed(float fadingSpeed) {
        this.fadingSpeed = fadingSpeed;
    }

    @Override
    public FadingParticle getClone() {
        FadingParticle clone = new FadingParticle(fadingSpeed);
        copyData(clone);

        if (cloneEvent != null) {
            cloneEvent.handleClonded(clone);
        }

        return clone;
    }

    @Override
    public void logistics() {
        tint.a -= fadingSpeed;

        super.logistics();
    }

    @Override
    protected boolean completed() {
        return 0.0f > tint.a;
    }

    protected void copyData(FadingParticle clone) {
        super.copyData(clone);
        clone.fadingSpeed = fadingSpeed;
    }
}
