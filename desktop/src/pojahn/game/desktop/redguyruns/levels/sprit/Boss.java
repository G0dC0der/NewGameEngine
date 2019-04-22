package pojahn.game.desktop.redguyruns.levels.sprit;

import com.badlogic.gdx.audio.Music;
import pojahn.game.core.BaseLogic;
import pojahn.game.core.Entity;
import pojahn.game.core.MobileEntity;
import pojahn.game.core.PlayableEntity;
import pojahn.game.entities.enemy.weapon.Projectile;
import pojahn.game.entities.particle.Particle;
import pojahn.game.essentials.Animation;
import pojahn.game.essentials.Factory;
import pojahn.game.essentials.GameState;
import pojahn.game.essentials.Image2D;
import pojahn.game.essentials.ResourceManager;
import pojahn.game.essentials.Vitality;

public class Boss extends MobileEntity {

    private Animation<Image2D> fullHealth, onceHit, finalLife;
    private int relX, relY, hp, projFireCounter, reload;
    private Projectile proj;
    private Entity spinner;
    private PlayableEntity hero;
    private Particle deathAnim;
    private ResourceManager resource;

    public Boss() {
        spinner = new Entity();
        zIndex(10);
        relX = -9;
        relY = -12;
        hp = 3;
        reload = 90;
        setMoveSpeed(0);
    }

    public void setFullHealth(final Animation<Image2D> fullHealth) {
        this.fullHealth = fullHealth;
    }

    public void setOnceHit(final Animation<Image2D> onceHit) {
        this.onceHit = onceHit;
    }

    public void setFinalLife(final Animation<Image2D> finalLife) {
        this.finalLife = finalLife;
    }

    public void setProj(final Projectile proj) {
        this.proj = proj;
    }

    public void setSpinnerImage(final Image2D spinnerImage) {
        spinner.setImage(spinnerImage);
    }

    public void setResource(final ResourceManager resource) {
        this.resource = resource;
    }

    public void setDeathAnim(final Particle deathAnim) {
        this.deathAnim = deathAnim;
    }

    public void setHero(final PlayableEntity hero) {
        this.hero = hero;
    }

    public void hit() {
        hp--;

        if (hp == 2) {
            setImage(onceHit);
            reload = 70;
            setMoveSpeed(4f);
        } else if (hp == 1) {
            setImage(finalLife);
            reload = 50;
            setMoveSpeed(4.5f);
        } else if (hp == 0) {
            setVisible(false);
            getLevel().add(deathAnim.getClone().center(this));
            getLevel().discard(spinner);
            getLevel().discard(this);

            if (getLevel().getEngine().getGameState() == GameState.ACTIVE) {
                getLevel().addAfter(() -> {
                    hero.setState(Vitality.COMPLETED);
                    final Music music = resource.getMusic("music.ogg");
                    final Music bossMusic = resource.getMusic("boss_music.mp3");
                    bossMusic.pause();
                    music.setVolume(1);
                    music.play();
                }, 60);
            }
        }

        if (hp > 0) {
            getLevel().temp(Factory.spazz(this, 5, 1), hp == 1 ? Integer.MAX_VALUE : 70);
            nudge(-50, 0);
        }
    }

    @Override
    public void init() {
        super.init();
        getLevel().add(spinner);
        spinner.addEvent(() -> spinner.rotate(2.5f));
        setImage(fullHealth);
        move(4519, 3295);

        getLevel().runOnceWhen(() -> {
            setMoveSpeed(3.5f);
        }, () -> BaseLogic.rectanglesCollide(x(), y(), 500, 1000, hero.x(), hero.y(), hero.width(), hero.height()));
    }

    @Override
    public void logistics() {
        if (getMoveSpeed() > 0 && hero.isAlive()) {

            if (getMoveSpeed() < Math.abs(hero.x() - x()))
                moveTowards(Math.min(hero.x(), 5544), y());

            if (++projFireCounter % reload == 0) {
                final Projectile proj = this.proj.getClone();
                proj.move(centerX(), centerY());
                proj.setTarget(hero);
                getLevel().add(proj);
            }
        }

        spinner.move(x() + relX, y() + relY);
    }
}
