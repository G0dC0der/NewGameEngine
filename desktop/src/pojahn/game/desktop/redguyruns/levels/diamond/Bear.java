package pojahn.game.desktop.redguyruns.levels.diamond;

import com.badlogic.gdx.audio.Sound;
import pojahn.game.core.Entity;
import pojahn.game.essentials.Animation;
import pojahn.game.essentials.Image2D;

class Bear extends Entity {

    private Animation<Image2D> attackImage, orgImage;
    private Entity target, scanBox;
    private Sound attack;
    private int recovery;
    private boolean attacking;

    Bear(final float x, final float y, final Entity target) {
        move(x, y);
        this.target = target;

        scanBox = new Entity();
        scanBox.move(x - 100, y + 50);
        scanBox.bounds.size.width = 100;
        scanBox.bounds.size.height = 20;
    }

    void setAttackSound(final Sound attack) {
        this.attack = attack;
    }

    void setAttackImage(final Animation<Image2D> attackImage) {
        this.attackImage = attackImage;
        attackImage.setLoop(false);
    }

    @Override
    public void setImage(final Animation<Image2D> obj) {
        orgImage = obj;
        super.setImage(obj);
    }

    @Override
    public void logistics() {
        if (recovery-- < 0) {
            if (!attacking && scanBox.collidesWith(target)) {
                attacking = true;
                setImage(attackImage);
//                bounds.pos.x -= 25;
                attack.play();

                target.runActionEvent(this);
            }

            if (attacking && attackImage.hasEnded()) {
                attackImage.reset();
                setImage(orgImage);
                attacking = false;
                recovery = 120;
//                bounds.pos.x += 25;
            }
        }
    }

}
