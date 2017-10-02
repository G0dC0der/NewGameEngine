package pojahn.game.desktop.redguyruns.levels.hill;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import pojahn.game.core.Entity;
import pojahn.game.core.PlayableEntity;
import pojahn.game.desktop.redguyruns.util.ResourceUtil;
import pojahn.game.entities.Particle;
import pojahn.game.entities.PathDrone;
import pojahn.game.entities.SolidPlatform;
import pojahn.game.entities.Trailer;
import pojahn.game.essentials.EntityBuilder;
import pojahn.game.essentials.Hitbox;
import pojahn.game.essentials.Image2D;
import pojahn.game.essentials.ResourceManager;
import pojahn.game.essentials.Utils;
import pojahn.game.essentials.stages.PixelBasedLevel;

import java.io.Serializable;
import java.util.stream.Stream;

public class GreenHill extends PixelBasedLevel {

    private ResourceManager res;
    private Music music;
    private PlayableEntity play;
    private int ringCounter, rings;

    @Override
    public void init(final Serializable meta) throws Exception {
        res = new ResourceManager();
        res.loadContentFromDirectory(Gdx.files.internal("res/data"));
        res.loadContentFromDirectory(Gdx.files.internal("res/general"));
        res.loadContentFromDirectory(Gdx.files.internal("res/hill"));

        getEngine().timeFont = res.getFont("sansserif32.fnt");
        Stream.of(res.getAnimation("main")).forEach(Image2D::createPixelData);
        Stream.of(res.getAnimation("enemy")).forEach(Image2D::createPixelData);
        createMap(res.getPixmap("pixmap.png"));

        music = res.getMusic("music.ogg");
        Utils.playMusic(music, 4.05f, .8f);
    }

    @Override
    public void build() {
        ringCounter = rings = 0;

        /*
         * Main Character
         */
        play = ResourceUtil.getGravityMan(res);
        play.move(23, 525);
        play.zIndex(50);
        play.addTileEvent((tileType) -> {
            if (tileType == Tile.LETHAL)
                play.lose();
        });
        add(play);

        /*
         * Background and foreground
         */
        add(new EntityBuilder().image(res.getImage("foreground.png")).zIndex(Integer.MAX_VALUE).build());
        add(new EntityBuilder().image(res.getImage("background.png")).zIndex(-5).build());

        /*
         * Platforms
		 */

        final SolidPlatform sp = new SolidPlatform(451, 740, play);
        sp.setImage(res.getImage("platform.png"));
        sp.setMoveSpeed(2);
        sp.setFollowMode(SolidPlatform.FollowMode.STRICT);
        sp.appendPath(451, 740, 20, false, null);
        sp.appendPath(752, 740, 20, false, null);

        final SolidPlatform sp2 = sp.getClone();
        sp2.move(1940, 413);
        sp2.setMoveSpeed(1);
        sp2.clearData();
        sp2.appendPath(1940, 413, 30, false, null);
        sp2.appendPath(2100, 413, 30, false, null);

        final SolidPlatform sp3 = sp2.getClone();
        sp3.move(2100, 300);
        sp3.clearData();
        sp3.appendPath(2100, 300, 30, false, null);
        sp3.appendPath(1940, 300, 30, false, null);

        final SolidPlatform sp4 = sp2.getClone();
        sp4.move(1940, 187);
        sp4.clearData();
        sp4.appendPath(1940, 187, 30, false, null);
        sp4.appendPath(2100, 187, 30, false, null);

        add(sp);
        add(sp2);
        add(sp3);
        add(sp4);

        /*
         * Kirby Enemies
         */

        final PathDrone enemy1 = new PathDrone(1367, 755);
        enemy1.setImage(7, res.getAnimation("enemy"));
        enemy1.setMoveSpeed(0.8f);
        enemy1.setFacings(2);
        enemy1.addEvent(enemy1::face);
        enemy1.setHitbox(Hitbox.PIXEL);
        enemy1.ifCollides(play).thenRunOnce(play::lose);
        enemy1.appendPath(1367, 755, 0, false, null);
        enemy1.appendPath(1014, 755, 0, false, null);

        final PathDrone enemy2 = enemy1.getClone();
        enemy2.move(1525, 723);
        enemy2.clearData();
        enemy2.addEvent(enemy2::face);
        enemy2.ifCollides(play).thenRunOnce(play::lose);
        enemy2.appendPath(1525, 723, 0, false, null);
        enemy2.appendPath(1623, 723, 0, false, null);

        add(enemy1);
        add(enemy2);

        /*
         * Bird
         */

        final Trailer bird = new Trailer(910, 10);
        bird.setImage(5, res.getAnimation("enemy2"));
        bird.setFacings(2);
        bird.addEvent(bird::face);
        bird.setMoveSpeed(2);
        bird.setFrequency(60);
        bird.appendPath(910, 100, 2, false, null);
        bird.appendPath(1375, 100, 2, false, null);

        final Particle pooImpact = new Particle();
        pooImpact.setImage(res.getAnimation("poosplash"));
        pooImpact.setIntroSound(res.getSound("splash.wav"));
        pooImpact.sounds.useFalloff = true;

        final FallingProjectile sproj = new FallingProjectile(0, 0, bird, this, play);
        sproj.setImage(res.getImage("poo.png"));
        sproj.setImpact(pooImpact);
        sproj.rotate(false);
        sproj.setCloneEvent(clonie -> clonie.addEvent(() -> clonie.bounds.rotation += 15));

        bird.setSpawners(sproj);

        add(bird);
        play.setActionEvent(hitter -> {
            if (hitter.isCloneOf(sproj))
                play.touch(-1);
        });

        /*
         * Rings
         */
        addRing(63, 482);
        addRing(83, 482);
        addRing(103, 482);
        addRing(353, 452);
        addRing(373, 452);
        addRing(462, 722);
        addRing(484, 722);
        addRing(509, 722);
        addRing(531, 722);
        addRing(556, 722);
        addRing(578, 722);
        addRing(603, 722);
        addRing(625, 722);
        addRing(650, 722);
        addRing(672, 722);
        addRing(697, 722);
        addRing(719, 722);
        addRing(745, 722);
        addRing(767, 722);
        addRing(790, 722);
        addRing(1167, 424);
        addRing(1186, 424);
        addRing(393, 452);
        addRing(1110, 642);
        addRing(1133, 642);
        addRing(1157, 642);
        addRing(1248, 675);
        addRing(1018, 675);
        addRing(1399, 703);
        addRing(1419, 703);
        addRing(1439, 703);
        addRing(1529, 672);
        addRing(1549, 672);
        addRing(1569, 672);
        addRing(1632, 688);
        addRing(2915, 362);
        addRing(2935, 362);
        addRing(2956, 362);
        addRing(1839, 170);
        addRing(1839, 200);
        addRing(1839, 230);
        addRing(1839, 260);
        addRing(1839, 290);
        addRing(1839, 320);
        addRing(1839, 350);
        addRing(1839, 380);
    }

    void addRing(final float x, final float y) {
        rings++;

        final Entity ring = new Entity();
        ring.move(x, y);
        ring.setImage(5, res.getAnimation("ring"));
        ring.ifCollides(play).then(() -> {
            ring.die();
            res.getSound("collectring.wav").play();
            if (++ringCounter == rings)
                play.win();
        });
        add(ring);
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
        return "Green Hill";
    }
}
