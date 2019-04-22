package pojahn.game.desktop.redguyruns.levels.lasers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import pojahn.game.core.Entity;
import pojahn.game.core.PlayableEntity;
import pojahn.game.desktop.redguyruns.util.ResourceUtil;
import pojahn.game.entities.enemy.TargetLaser;
import pojahn.game.entities.movement.Circle;
import pojahn.game.entities.particle.Particle;
import pojahn.game.essentials.EntityBuilder;
import pojahn.game.essentials.ResourceManager;
import pojahn.game.essentials.Utils;
import pojahn.game.essentials.stages.PixelBasedLevel;

import java.io.Serializable;

public class LaserMadness extends PixelBasedLevel {

    private ResourceManager res;
    private Music music;
    private Sound laserSound;
    private PlayableEntity play;
    private boolean taken;

    @Override
    public void init(final Serializable meta) throws Exception {
        res = new ResourceManager();
        res.loadContentFromDirectory(Gdx.files.internal("res/data"));
        res.loadContentFromDirectory(Gdx.files.internal("res/general"));
        res.loadContentFromDirectory(Gdx.files.internal("res/lasereverywhere"));
        res.loadAnimation(Gdx.files.internal("res/clubber/trailer"));

        getEngine().timeFont = res.getFont("sansserif32.fnt");
        createMap(res.getPixmap("pixmap.png"));

        laserSound = res.getSound("laser.wav");
        music = res.getMusic("music.ogg");
        Utils.playMusic(music, 15.93f, .8f);
    }

    @Override
    public void build() {
        laserSound.stop();
        taken = false;

        /*
         * Main Character
         */
        play = ResourceUtil.getGravityMan(res);
        play.move(123, 1281);
        play.zIndex(2);
        add(play);

        /*
         * Background and foreground
         */
        add(new EntityBuilder().image(res.getImage("foreground.png")).build());
        add(new EntityBuilder().image(res.getImage("background.png")).zIndex(-5).build());

        /*
         * Circle pathing
		 */
        final float mx = getWidth() / 2;
        final float my = getHeight() / 2;
        final float moveSpeed = 0.0035f;

        final Circle c1 = new Circle(mx, my, 100, 0);
        c1.setMoveSpeed(moveSpeed);

        final Circle c2 = new Circle(mx, my, 100, (float) Math.toRadians(45));
        c2.setMoveSpeed(moveSpeed);

        final Circle c3 = new Circle(mx, my, 100, (float) Math.toRadians(90));
        c3.setMoveSpeed(moveSpeed);

        final Circle c4 = new Circle(mx, my, 100, (float) Math.toRadians(135));
        c4.setMoveSpeed(moveSpeed);

        final Circle c5 = new Circle(mx, my, 100, (float) Math.toRadians(180));
        c5.setMoveSpeed(moveSpeed);

        final Circle c6 = new Circle(mx, my, 100, (float) Math.toRadians(225));
        c6.setMoveSpeed(moveSpeed);

        final Circle c7 = new Circle(mx, my, 100, (float) Math.toRadians(270));
        c7.setMoveSpeed(moveSpeed);

        final Circle c8 = new Circle(mx, my, 100, (float) Math.toRadians(315));
        c8.setMoveSpeed(moveSpeed);

        add(c1);
        add(c2);
        add(c3);
        add(c4);
        add(c5);
        add(c6);
        add(c7);
        add(c8);

        /*
		 * Lasers
		 */
        final Particle impact = Particle.imageParticle(3, res.getAnimation("trailer"));
        impact.setIntroSound(res.getSound("boom.wav"));
        impact.sounds.useFalloff = true;
        impact.sounds.maxDistance = 550;
        impact.sounds.power = 30;

        final int expDelay = 8;

        final TargetLaser tl1 = new TargetLaser(mx, my, c1, play);
        tl1.setLaserBeam(ResourceUtil.getFiringLaser(res));
        tl1.setExplosion(impact);
        tl1.setExplosionDelay(expDelay);
        tl1.setStopTile(Tile.CUSTOM_1);

        final TargetLaser tl2 = new TargetLaser(mx, my, c2, play);
        tl2.setLaserBeam(ResourceUtil.getFiringLaser(res));
        tl2.setExplosion(impact);
        tl2.setExplosionDelay(expDelay);
        tl2.setStopTile(Tile.CUSTOM_1);

        final TargetLaser tl3 = new TargetLaser(mx, my, c3, play);
        tl3.setLaserBeam(ResourceUtil.getFiringLaser(res));
        tl3.setExplosion(impact);
        tl3.setExplosionDelay(expDelay);
        tl3.setStopTile(Tile.CUSTOM_1);

        final TargetLaser tl4 = new TargetLaser(mx, my, c4, play);
        tl4.setLaserBeam(ResourceUtil.getFiringLaser(res));
        tl4.setExplosion(impact);
        tl4.setExplosionDelay(expDelay);
        tl4.setStopTile(Tile.CUSTOM_1);

        final TargetLaser tl5 = new TargetLaser(mx, my, c5, play);
        tl5.setLaserBeam(ResourceUtil.getFiringLaser(res));
        tl5.setExplosion(impact);
        tl5.setExplosionDelay(expDelay);
        tl5.setStopTile(Tile.CUSTOM_1);

        final TargetLaser tl6 = new TargetLaser(mx, my, c6, play);
        tl6.setLaserBeam(ResourceUtil.getFiringLaser(res));
        tl6.setExplosion(impact);
        tl6.setExplosionDelay(expDelay);
        tl6.setStopTile(Tile.CUSTOM_1);

        final TargetLaser tl7 = new TargetLaser(mx, my, c7, play);
        tl7.setLaserBeam(ResourceUtil.getFiringLaser(res));
        tl7.setExplosion(impact);
        tl7.setExplosionDelay(expDelay);
        tl7.setStopTile(Tile.CUSTOM_1);

        final TargetLaser tl8 = new TargetLaser(mx, my, c8, play);
        tl8.setLaserBeam(ResourceUtil.getFiringLaser(res));
        tl8.setExplosion(impact);
        tl8.setExplosionDelay(expDelay);
        tl8.setStopTile(Tile.CUSTOM_1);

        play.setActionEvent(hitter -> {
            play.lose();
        });

        /*
		 * Diamond
		 */
        final Entity diamond = new Entity();
        diamond.setImage(6, res.getAnimation("diamond"));
        diamond.move(64, 106);
        diamond.ifCollides(play).then(() -> {
            discard(diamond);
            laserSound.loop();
            taken = true;
            res.getSound("collect3.wav").play();

            add(tl1);
            add(tl2);
            add(tl3);
            add(tl4);
            add(tl5);
            add(tl6);
            add(tl7);
            add(tl8);
        });
        add(diamond);

                /*
		 * Flag
		 */
        final Entity f = new Entity();
        f.setImage(4, res.getAnimation("flag"));
        f.move(125, 1261);
        f.addEvent(() -> {
            if (taken && f.collidesWith(play)) {
                play.win();
                laserSound.stop();
                discard(tl1);
                discard(tl2);
                discard(tl3);
                discard(tl4);
                discard(tl5);
                discard(tl6);
                discard(tl7);
                discard(tl8);
            }
        });
        add(f);
    }

    @Override
    public void dispose() {
        res.disposeAll();
    }

    @Override
    public String getLevelName() {
        return "Laser Madness";
    }

    @Override
    public Music getStageMusic() {
        return music;
    }
}
