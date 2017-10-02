package pojahn.game.entities;

import com.badlogic.gdx.audio.Sound;
import pojahn.game.core.Entity;
import pojahn.game.core.MobileEntity;
import pojahn.game.essentials.Animation;
import pojahn.game.essentials.Image2D;

public class PlatformSpawner extends Entity {

    private boolean solid, permanent, resetBlockImage, trigger;
    private int spawnDelay, removeDelay;
    private MobileEntity[] users;
    private Entity[] blocks;
    private Animation<Image2D> actionImage, orgImage;
    private Particle removePart;
    private Sound spawnSound, removeSound;
    private int spawnCounter, removeCounter, index;
    private boolean cleared;

    public PlatformSpawner(final float x, final float y, final MobileEntity... users) {
        move(x, y);
        this.users = users;
        cleared = true;
        spawnDelay = removeDelay = 10;
    }

    public void setBlocks(final Entity... blocks) {
        final Entity[] bls = new Entity[blocks.length + 1];
        bls[0] = null;
        for (int i = 1; i < bls.length; i++)
            bls[i] = blocks[i - 1];

        this.blocks = bls;
    }

    public void blocksSolid(final boolean solid) {
        this.solid = solid;
    }

    public void setPermanent(final boolean permanent) {
        this.permanent = permanent;
    }

    public void setSpawnDelay(final int spawnDelay) {
        this.spawnDelay = spawnDelay;
        spawnCounter = spawnDelay - 1;
    }

    public void setRemoveDelay(final int removeDelay) {
        this.removeDelay = removeDelay;
        removeCounter = removeDelay - 1;
    }

    public void setActionImage(final Animation<Image2D> image) {
        actionImage = image;
        orgImage = getImage();
    }

    public void setRemoveParticle(final Particle removePart) {
        this.removePart = removePart;
    }

    public void resetBlockImage(final boolean resetBlockImage) {
        this.resetBlockImage = resetBlockImage;
    }

    public void setSpawnSound(final Sound sound) {
        spawnSound = sound;
    }

    public void setRemoveSound(final Sound sound) {
        removeSound = sound;
    }

    public void triggerBlocks(final boolean trigger) {
        this.trigger = trigger;
    }

    @Override
    public void logistics() {
        if (0 > index)
            index = 0;

        if (trigger || buttonDown()) {
            if (actionImage != null)
                setImage(actionImage);

            if (index + 1 <= blocks.length - 1 && ++spawnCounter % spawnDelay == 0) {
                index++;

                if (index == 0)
                    return;

                getLevel().add(blocks[index]);
                if (resetBlockImage)
                    blocks[index].getImage().reset();
                if (spawnSound != null)
                    spawnSound.play(sounds.calc());
                if (solid)
                    for (final MobileEntity mobile : users)
                        mobile.addObstacle(blocks[index]);

                cleared = false;
            }
        } else {
            if (actionImage != null)
                setImage(orgImage);

            if (!cleared && !permanent && ++removeCounter % removeDelay == 0) {
                if (index > blocks.length - 1)
                    index--;

                if (index == 0)
                    return;

                if (removeSound != null)
                    removeSound.play(sounds.calc());
                if (solid)
                    for (final MobileEntity mobile : users)
                        mobile.removeObstacle(blocks[index]);

                if (index <= 0)
                    cleared = true;

                if (removePart != null)
                    getLevel().add(removePart.getClone().center(blocks[index]));

                getLevel().discard(blocks[index]);
                index--;
            }
        }
    }

    private boolean buttonDown() {
        for (final Entity user : users)
            if (collidesWith(user))
                return true;
        return false;
    }
}