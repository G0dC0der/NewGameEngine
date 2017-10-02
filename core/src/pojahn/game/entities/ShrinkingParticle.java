package pojahn.game.entities;

public class ShrinkingParticle extends Particle {

    private float shrinkSpeed;

    public ShrinkingParticle() {
        this(.05f);
    }

    public ShrinkingParticle(final float shrinkSpeed) {
        this.shrinkSpeed = shrinkSpeed;
    }

    public void setShrinkSpeed(final float shrinkSpeed) {
        this.shrinkSpeed = shrinkSpeed;
    }

    @Override
    public ShrinkingParticle getClone() {
        final ShrinkingParticle clone = new ShrinkingParticle(shrinkSpeed);
        copyData(clone);

        if (cloneEvent != null) {
            cloneEvent.handleClonded(clone);
        }

        return clone;
    }

    @Override
    public void logistics() {
        scaleY -= shrinkSpeed;
        scaleX -= shrinkSpeed;

        super.logistics();
    }

    @Override
    protected boolean completed() {
        return 0.0f >= scaleX || 0.0f >= scaleY;
    }
}
