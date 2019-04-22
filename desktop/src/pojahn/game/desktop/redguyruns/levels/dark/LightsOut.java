package pojahn.game.desktop.redguyruns.levels.dark;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import pojahn.game.core.BaseLogic;
import pojahn.game.core.Entity;
import pojahn.game.core.PlayableEntity;
import pojahn.game.desktop.redguyruns.util.GFX;
import pojahn.game.desktop.redguyruns.util.ResourceUtil;
import pojahn.game.entities.Thor;
import pojahn.game.entities.enemy.weapon.Missile;
import pojahn.game.entities.enemy.weapon.SimpleWeapon;
import pojahn.game.entities.movement.PathDrone;
import pojahn.game.entities.particle.Particle;
import pojahn.game.entities.platform.SolidPlatform;
import pojahn.game.essentials.Direction;
import pojahn.game.essentials.EntityBuilder;
import pojahn.game.essentials.Factory;
import pojahn.game.essentials.Hitbox;
import pojahn.game.essentials.ResourceManager;
import pojahn.game.essentials.Utils;
import pojahn.game.essentials.stages.PixelBasedLevel;

import java.io.Serializable;

public class LightsOut extends PixelBasedLevel {

    private ResourceManager res;
    private Music music;
    private PlayableEntity play;

    @Override
    public void init(final Serializable meta) throws Exception {
        res = new ResourceManager();
        res.loadContentFromDirectory(Gdx.files.internal("res/data"));
        res.loadContentFromDirectory(Gdx.files.internal("res/general"));
        res.loadContentFromDirectory(Gdx.files.internal("res/lightsout"));

        getEngine().timeFont = res.getFont("sansserif32.fnt");
        createMap(res.getPixmap("pixmap.png"));

        music = res.getMusic("music.ogg");
        Utils.playMusic(music, 0, .8f);

        getCheckpointHandler().setReachEvent(() -> GFX.renderCheckpoint(res, this));
        getCheckpointHandler().appendCheckpoint(new Vector2(1520, 339), new Rectangle(1549, 323, 217, 114));
    }

    @Override
    public void build() {
        /*
         * Main Character
         */
        play = ResourceUtil.getGravityMan(res);
        play.move(1960, 1299);
        play.deathImage.tint.set(Color.BLACK);
        play.tint.set(Color.BLACK);
        play.zIndex(2);
        add(play);

        /*
         * Background and foreground
         */
        add(new EntityBuilder().image(res.getImage("foreground.png")).zIndex(100).build());
        add(new EntityBuilder().image(res.getImage("background.png")).zIndex(-5).build());

        /*
         * Top Saws, left to right.
		 */
        addSaw(0, 13);

        final PathDrone saw2 = addSaw(4, 7);
        saw2.appendPath();
        saw2.appendPath(4 * 30, 5 * 30);

        final PathDrone saw3 = addSaw(7, 5);
        saw3.appendPath();
        saw3.appendPath(7 * 30, 7 * 30);

        final PathDrone saw4 = addSaw(10, 7);
        saw4.appendPath();
        saw4.appendPath(10 * 30, 5 * 30);

        final PathDrone saw5 = addSaw(13, 5);
        saw5.appendPath();
        saw5.appendPath(13 * 30, 7 * 30);

        final PathDrone saw6 = addSaw(17, 13);
        saw6.bounds.pos.x -= 18;

        final PathDrone saw7 = addSaw(23, 13);
        saw7.bounds.pos.x -= 10;

        addSaw(29, 12);

        final PathDrone saw9 = addSaw(0, 0);
        saw9.move(1005, 295 + 25);

        addSaw(38, 12);
        addSaw(44, 11);
        addSaw(81, 6);
        addSaw(81, 11);
        addSaw(93, 12);

        /*
         * Lightning
         */
        final Rectangle rec = new Rectangle(1865 - 20, 0, 5, 432);

        final Thor thor = new Thor(1865 - 20, 0, new Entity().move(1865, 432));
        thor.setImage(res.getImage("blob.png"));
        thor.thunderSound(res.getMusic("electric_music.wav"));
        thor.setBoltColors(Color.BLACK);
        thor.setBlend(false);
        thor.setPrecisionLevel(10);
        thor.sounds.useFalloff = true;
        thor.sounds.maxDistance = 800;
        thor.addEvent(() -> {
            if (BaseLogic.rectanglesCollide(rec, play.bounds.toRectangle())) {
                play.lose();
            }
        });
        add(thor);

        /*
		 * Item
		 */
        final Entity i = new Entity();
        i.move(88, 301);
        i.setImage(res.getImage("item.png"));
        i.ifCollides(play).then(() -> {
            discard(i);
            discard(thor);
            res.getSound("collect1.wav").play();
        });
        add(i);

        /*
		 * Goal
		 */
        final Entity goal = new Entity();
        goal.move(2954, 244);
        goal.setImage(7, res.getAnimation("flag"));
        goal.ifCollides(play).then(play::win);
        add(goal);

        /*
		 * Cannons
		 */
        final SolidPlatform dummy1 = new SolidPlatform(690, 1033, play);
        dummy1.setImage(res.getImage("dummy.png"));
        dummy1.setMoveSpeed(1.5f);
        dummy1.appendPath();
        dummy1.appendPath(dummy1.x(), 884);

        final SolidPlatform dummy2 = new SolidPlatform(690, 719, play);
        dummy2.setImage(res.getImage("dummy.png"));
        dummy2.setMoveSpeed(1.5f);
        dummy2.appendPath();
        dummy2.appendPath(dummy2.x(), 868);

        final Particle trailer = Particle.imageParticle(1, res.getAnimation("explosion"));

        final Particle fireExp = Particle.imageParticle(3, res.getAnimation("explosion"));
        fireExp.setIntroSound(res.getSound("missileexp.wav"));
        fireExp.sounds.useFalloff = true;
        fireExp.sounds.power = 15;

        final Particle firingAnim = Particle.imageParticle(2, res.getAnimation("explosion"));
        firingAnim.setIntroSound(res.getSound("missilefire.wav"));
        firingAnim.sounds.useFalloff = true;
        firingAnim.sounds.power = 15;

        final Missile proj = new Missile(0, 0, play);
        proj.setImage(res.getImage("missile.png"));
        proj.setMoveSpeed(4);
        proj.setTrailer(trailer);
        proj.setTrailerDelay(8);
        proj.setImpact(fireExp);

        final SimpleWeapon weap1 = new SimpleWeapon(0, 0, proj, Direction.E, 80);
        weap1.setImage(res.getImage("cannon.png"));
        weap1.addEvent(Factory.follow(dummy1, weap1, 0, 0));
        weap1.spawnOffset(weap1.width() - 10, 0);
        weap1.setFiringAnimation(firingAnim);

        final SimpleWeapon weap2 = new SimpleWeapon(0, 0, proj, Direction.E, 80);
        weap2.setImage(res.getImage("cannon.png"));
        weap2.addEvent(Factory.follow(dummy2, weap2, 0, 0));
        weap2.spawnOffset(weap1.width() - 10, 0);
        weap2.setFiringAnimation(firingAnim);

        add(dummy1);
        add(dummy2);
        add(weap1);
        add(weap2);

        play.setActionEvent(hitter -> {
            if (hitter.isCloneOf(proj))
                play.touch(-1);
        });

        /*
         * Blocking Door & The Key
         */
        final SolidPlatform blocker = new SolidPlatform(1620, 671 - 5, play);
        blocker.setImage(res.getImage("door.png"));
        add(blocker);

        final Entity k = new Entity();
        k.move(695, 930);
        k.setImage(res.getImage("key.png"));
        k.addEvent(() -> {
            if (k.collidesWith(play)) {
                discard(blocker);
                discard(k);
                res.getSound("collect1.wav").play();
            }
        });
        add(k);

        /*
		 * Start Spikes
		 */
        final float xStart = 2731;
        final float xEnd = 1880;
        final Sound spikesspawn = res.getSound("spikesspawn.wav");
        final Sound spikesend = res.getSound("spikesend.wav");

        for (int value = 0; value < 3; value++) {
            float y = 1230 + 8;
            if (value == 1)
                y = 1050 + 8;
            else if (value == 2)
                y = 870 + 8;

            final PathDrone spik = new PathDrone(xStart, y);
            spik.sounds.useFalloff = true;
            spik.appendPath(spik.x(), spik.y(), 0, true, () -> spik.sounds.play(spikesspawn));
            spik.appendPath(xEnd, y, 0, false, () -> spik.sounds.play(spikesend));
            spik.setImage(res.getImage("spikes.png"));
            spik.setMoveSpeed(4);
            spik.addEvent(Factory.hitMain(spik, play, -1));

            final SolidPlatform solp = new SolidPlatform(xStart + spik.width(), y, play);
            solp.appendPath(solp.x(), solp.y(), 0, true, null);
            solp.appendPath(xEnd + spik.width(), y);
            solp.setMoveSpeed(4);
            solp.setImage(res.getImage("spikesblock.png"));

            add(spik);
            add(solp);
        }
    }

    private PathDrone addSaw(final int x, final int y) {
        final PathDrone saw = new PathDrone(x * 30, y * 30);
        saw.setImage(res.getImage("saw90.png"));
        saw.addEvent(() -> saw.bounds.rotation += 7);
        saw.addEvent(Factory.hitMain(saw, play, -1));
        saw.setHitbox(Hitbox.CIRCLE);
        saw.setMoveSpeed(.7f);

        add(saw);
        return saw;
    }

    @Override
    public void dispose() {
        res.disposeAll();
    }

    @Override
    public Music getStageMusic() {
        return music;
    }

    @Override
    public String getLevelName() {
        return "Lights Out";
    }
}
