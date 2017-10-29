package pojahn.game.desktop.redguyruns.levels.forbiddencastle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import pojahn.game.core.Entity;
import pojahn.game.core.PlayableEntity;
import pojahn.game.desktop.redguyruns.util.ResourceUtil;
import pojahn.game.entities.image.BigImage;
import pojahn.game.entities.platform.DestroyablePlatform;
import pojahn.game.essentials.Animation;
import pojahn.game.essentials.EntityBuilder;
import pojahn.game.essentials.Image2D;
import pojahn.game.essentials.ResourceManager;
import pojahn.game.essentials.Utils;
import pojahn.game.essentials.stages.PixelBasedLevel;

import java.io.Serializable;
import java.util.stream.Stream;

public class ForbiddenCastle extends PixelBasedLevel {

    private ResourceManager res;
    private Music music;
    private PlayableEntity play;

    @Override
    public void init(final Serializable meta) throws Exception {
        res = new ResourceManager();
        res.loadContentFromDirectory(Gdx.files.internal("res/data"));
        res.loadContentFromDirectory(Gdx.files.internal("res/general"));
        res.loadContentFromDirectory(Gdx.files.internal("res/forbiddencastle"));

        getEngine().timeFont = res.getFont("sansserif32.fnt");
        Stream.of(res.getAnimation("main")).forEach(Image2D::createPixelData);
        createMap(res.getPixmap("pixmap.png"));

        music = res.getMusic("music.ogg");
        Utils.playMusic(music, 0.60f, .8f);
    }

    @Override
    public void build() {
        /*
         * Main Character
         */
        play = ResourceUtil.getGravityMan(res);
        play.move(93, 790 - 1);
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
        add(new EntityBuilder().image(res.getImage("background.png")).zIndex(-5).build(BigImage.class, BigImage.RenderStrategy.PARALLAX));

        /*
         * Weak Platforms
         */
        addWeak(18, 28);

        for (int i = 24; i <= 32; i++)
            addWeak(i, 26);

        for (int i = 24; i <= 31; i++)
            addWeak(i, 23);

        for (int i = 26; i <= 32; i++)
            addWeak(i, 20);

        addWeak(37, 21);
        addWeak(42, 21);
        addWeak(44, 18);
        addWeak(44, 17);
        addWeak(41, 14);
        addWeak(41, 13);
        addWeak(41, 12);
        addWeak(46, 13);
        addWeak(46, 12);
        addWeak(46, 11);
        addWeak(53, 14);
        addWeak(53, 13);
        addWeak(53, 12);
        addWeak(53, 11);
        addWeak(53, 10);
        addWeak(53, 9);
        addWeak(53, 8);
        addWeak(49, 8);
        addWeak(49, 7);
        addWeak(49, 6);
        addWeak(49, 5);
        addWeak(49, 4);
        addWeak(53, 4);
        addWeak(54, 4);
        addWeak(55, 4);
        addWeak(56, 4);
        addWeak(57, 4);

        for (int i = 75; i <= 83; i++)
            addWeak(i, 17);

        for (int i = 76; i <= 82; i++)
            addWeak(i, 14);

        for (int i = 77; i <= 81; i++)
            addWeak(i, 11);

        for (int i = 78; i <= 80; i++)
            addWeak(i, 8);

        addWeak(79, 5);
        addWeak(44, 16);

		/*
         * Goal
		 */
        final Entity goal = new Entity();
        goal.setImage(5, res.getAnimation("coin"));
        goal.move(79 * 30, 3 * 30);
        goal.bounds.pos.x += 10;
        goal.bounds.pos.y += 15;
        goal.addEvent(() -> {
            if (goal.collidesWith(play)) {
                discard(goal);
                play.win();
                res.getSound("collect2.wav").play();
            }
        });
        add(goal);
    }

    void addWeak(final int x, final int y) {
        final Animation<Image2D> destroyImg = new Animation<>(6, res.getAnimation("weak"));
        destroyImg.setLoop(false);

        final DestroyablePlatform dp = new DestroyablePlatform(x * 30, y * 30, play);
        dp.setImage(res.getAnimation("weak")[0]);
        dp.setDestroyFrames(60);
        dp.setDestroyImage(destroyImg);
        dp.setDestroySound(res.getSound("weakDieSound.wav"));

        add(dp);
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
        return "Forbidden Castle";
    }
}
