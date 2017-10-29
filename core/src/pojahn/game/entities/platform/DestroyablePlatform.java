package pojahn.game.entities.platform;

import com.badlogic.gdx.audio.Sound;
import pojahn.game.core.MobileEntity;
import pojahn.game.essentials.Animation;
import pojahn.game.essentials.Image2D;
import pojahn.lang.Obj;

import java.util.List;

public class DestroyablePlatform extends SolidPlatform {

    private List<MobileEntity> subjects;
    private Animation<Image2D> destroyImage;
    private int destroyFrames, aliveCounter;
    private Sound breakSound, destroySound;

    public DestroyablePlatform(final float x, final float y, final MobileEntity... subjects) {
        super(x, y, subjects);
        destroyFrames = 100;
        aliveCounter = -1;
        this.subjects = Obj.requireNotEmpty(subjects);
    }

    public DestroyablePlatform getClone() {
        final DestroyablePlatform clone = new DestroyablePlatform(x(), y(), subjects.toArray(new MobileEntity[subjects.size()]));
        copyData(clone);
        if (cloneEvent != null)
            cloneEvent.handleClonded(clone);

        return clone;
    }

    @Override
    public void logistics() {
        super.logistics();

        if (aliveCounter-- == 0) {
            if (destroySound != null)
                sounds.play(destroySound);
            getLevel().discard(this);
        } else if (aliveCounter < 0) {
            if (!getActiveSubjects().isEmpty())
                collapse();
        }
    }

    public void collapse() {
        aliveCounter = destroyFrames;
        sounds.play(breakSound);
        if (destroyImage != null)
            setImage(destroyImage);
    }

    public void setBreakSound(final Sound sound) {
        this.breakSound = sound;
    }

    public void setDestroySound(final Sound sound) {
        this.destroySound = sound;
    }

    public void setDestroyImage(final Animation<Image2D> destroyImage) {
        this.destroyImage = destroyImage;
    }

    public void setDestroyFrames(final int destroyFrames) {
        this.destroyFrames = destroyFrames;
    }

    protected void copyData(final DestroyablePlatform clone) {
        super.copyData(clone);
        if (destroyImage != null)
            clone.destroyImage = destroyImage.getClone();
        clone.destroyFrames = destroyFrames;
        clone.breakSound = breakSound;
        clone.destroySound = destroySound;
    }
}
