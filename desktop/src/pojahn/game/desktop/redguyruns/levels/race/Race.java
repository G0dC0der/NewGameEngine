package pojahn.game.desktop.redguyruns.levels.race;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import pojahn.game.core.Entity;
import pojahn.game.desktop.redguyruns.util.ResourceUtil;
import pojahn.game.entities.image.RepeatingParallaxImage;
import pojahn.game.entities.platform.SolidPlatform;
import pojahn.game.entities.main.GravityMan;
import pojahn.game.essentials.Animation;
import pojahn.game.essentials.Controller;
import pojahn.game.essentials.Direction;
import pojahn.game.essentials.EntityBuilder;
import pojahn.game.essentials.Factory;
import pojahn.game.essentials.HUDMessage;
import pojahn.game.essentials.Hitbox;
import pojahn.game.essentials.Image2D;
import pojahn.game.essentials.ResourceManager;
import pojahn.game.essentials.recording.KeySession;
import pojahn.game.essentials.stages.PixelBasedLevel;
import pojahn.lang.Bool;

import java.io.Serializable;
import java.util.stream.Stream;

public class Race extends PixelBasedLevel {

    private ResourceManager res;

    @Override
    public void init(final Serializable meta) throws Exception {
        res = new ResourceManager();
        res.loadContentFromDirectory(Gdx.files.internal("res/data"));
        res.loadContentFromDirectory(Gdx.files.internal("res/general"));
        res.loadContentFromDirectory(Gdx.files.internal("res/race"));
        res.loadAnimation(Gdx.files.internal("res/mtrace/cont1"));
        res.loadAnimation(Gdx.files.internal("res/mtrace/cont2"));
        res.loadAnimation(Gdx.files.internal("res/mtrace/cont3"));
        getEngine().timeFont = res.getFont("sansserif32.fnt");

        createMap(res.getPixmap("pixmap.png"));
        Stream.of(res.getAnimation("main")).forEach(Image2D::createPixelData);
        Stream.of(res.getAnimation("bounce2")).forEach(Image2D::createPixelData);

        final Music music = res.getMusic("music.ogg");
        music.setOnCompletionListener(m -> {
            music.play();
            music.setPosition(3.325f);
        });
        music.play();
    }

    @Override
    public void build() {
        /*
         * Main Character
         */
        final GravityMan play = ResourceUtil.getGravityMan(res);
        play.move(40, 538);
        add(play);

        /*
         * Contestants
         */
        final GravityMan cont1 = getContestant((KeySession) res.getAsset("cont1.rlp"), res.getAnimation("cont1"), "Weedy");
        final GravityMan cont2 = getContestant((KeySession) res.getAsset("cont2.rlp"), res.getAnimation("cont2"), "White Boy");
        final GravityMan cont3 = getContestant((KeySession) res.getAsset("cont3.rlp"), res.getAnimation("cont3"), "Black Guy");
        add(cont1);
        add(cont2);
        add(cont3);

        /*
         * Background & Foreground
         */
        add(new EntityBuilder().image(res.getImage("foreground.png")).zIndex(100).build());
        add(new EntityBuilder().image(res.getImage("background.png")).zIndex(-100).build(RepeatingParallaxImage.class));

        /*
         * Hindrances
		 */
        final SolidPlatform blocker1 = new SolidPlatform(1104, 511, play, cont1, cont2, cont3);
        blocker1.setImage(new Animation<>(1, res.getImage("blocker.png")));
        blocker1.setMoveSpeed(1);
        blocker1.setFollowMode(SolidPlatform.FollowMode.NORMAL);
        blocker1.appendPath(1104, 511, 0, false, null);
        blocker1.appendPath(1176, 511, 0, false, null);

        final SolidPlatform blocker2 = blocker1.getClone();
        blocker2.move(1176, 310);
        blocker2.clearData();
        blocker2.appendPath(1176, 310, 0, false, null);
        blocker2.appendPath(1104, 310, 0, false, null);

        final SolidPlatform blocker3 = blocker1.getClone();
        blocker3.move(1104, 119);
        blocker3.clearData();
        blocker3.appendPath(1104, 119, 0, false, null);
        blocker3.appendPath(1176, 119, 0, false, null);

        final SolidPlatform blocker4 = blocker1.getClone();
        blocker4.move(1272, 511);
        blocker4.clearData();
        blocker4.appendPath(1272, 511, 0, false, null);
        blocker4.appendPath(1363, 511, 0, false, null);

        final SolidPlatform blocker5 = blocker1.getClone();
        blocker5.move(1363, 310);
        blocker5.clearData();
        blocker5.appendPath(1363, 310, 0, false, null);
        blocker5.appendPath(1272, 310, 0, false, null);

        final SolidPlatform blocker6 = blocker1.getClone();
        blocker6.move(1272, 119);
        blocker6.clearData();
        blocker6.appendPath(1272, 119, 0, false, null);
        blocker6.appendPath(1363, 119, 0, false, null);

        add(blocker1);
        add(blocker2);
        add(blocker3);
        add(blocker4);
        add(blocker5);
        add(blocker6);

        final OldBouncer b = new OldBouncer(1670, 491, 360, 1, null, play, cont1, cont2, cont3);
        b.setImage(new Animation<>(1, res.getImage("bounce.png")));
        b.setHitbox(Hitbox.CIRCLE);
        b.setMoveSpeed(2);
        b.setShake(true, 30, 1, 1);
        b.appendPath(1670, 491, 0, false, null);
        b.appendPath(1670, 350, 0, false, null);
        b.setShakeSound(res.getSound("bounceball.wav"), 5);
        b.sounds.useFalloff = true;

        final OldBouncer b2 = new OldBouncer(1800, 532, 400, 3, Direction.W, play, cont1, cont2, cont3);
        b2.setImage(new Animation<>(5, res.getAnimation("bounce2")));
        b2.setHitbox(Hitbox.PIXEL);
        b2.setMoveSpeed(2);
        b2.appendPath(1800, 532, 0, false, null);
        b2.appendPath(2110, 532, 0, false, null);
        b2.setShakeSound(res.getSound("bounceblock.wav"), 5);

        final OldBouncer b3 = new OldBouncer(2110, 500, 400, 3, Direction.W, play, cont1, cont2, cont3);
        b3.setImage(new Animation<>(5, res.getAnimation("bounce2")));
        b3.setHitbox(Hitbox.PIXEL);
        b3.setMoveSpeed(2);
        b3.appendPath(2110, 500, 0, false, null);
        b3.appendPath(1800, 500, 0, false, null);
        b3.setShakeSound(res.getSound("bounceblock.wav"), 5);

        add(b);
        add(b2);
        add(b3);

		/*
		 * Other
		 */
        final Entity flag = new Entity();
        flag.setImage(new Animation<>(6, res.getAnimation("flag")));
        flag.move(3912, 514);
        add(flag);
    }

    private GravityMan getContestant(final KeySession data, final Image2D[] img, final String name) {
        final GravityMan cont = new GravityMan();
        cont.setImage(4, img);
        cont.setController(Controller.DEFAULT_CONTROLLER);
        cont.setJumpSound(res.getSound("jump.wav"));
        cont.addEvent(cont::face);
        cont.setFacings(2);
        cont.move(40, 538);
        cont.sounds.useFalloff = true;
        cont.setGhostData(data.keystrokes);
        final Bool bool = new Bool();
        cont.addTileEvent(tile -> {
            if (!bool.value && tile == Tile.GOAL) {
                bool.value = true;
                final HUDMessage winText = HUDMessage.centeredMessage(name + " completed!", getEngine().getScreenSize(), Color.BLACK);
                temp(Factory.drawCenteredText(winText, getEngine().timeFont), 100);
            }
        });
        return cont;
    }

    @Override
    public void dispose() {
        res.disposeAll();
    }

    @Override
    public String getLevelName() {
        return "Race";
    }

    @Override
    public Music getStageMusic() {
        return res.getMusic("music.ogg");
    }
}
