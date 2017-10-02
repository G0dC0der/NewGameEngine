package pojahn.game.entities;

public class FadingParticle extends Particle {

    private float fadingSpeed;

    public FadingParticle() {
        this(.05f);
    }

    public FadingParticle(final float fadingSpeed) {
        this.fadingSpeed = fadingSpeed;
    }

    public void setFadingSpeed(final float fadingSpeed) {
        this.fadingSpeed = fadingSpeed;
    }

    @Override
    public FadingParticle getClone() {
        final FadingParticle clone = new FadingParticle(fadingSpeed);
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

    protected void copyData(final FadingParticle clone) {
        super.copyData(clone);
        clone.fadingSpeed = fadingSpeed;
    }
}
