package pojahn.game.desktop.redguyruns.levels.hurry;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import pojahn.game.core.Entity;
import pojahn.game.desktop.redguyruns.util.ResourceUtil;
import pojahn.game.entities.image.RepeatingParallaxImage;
import pojahn.game.entities.particle.Particle;
import pojahn.game.entities.main.GravityMan;
import pojahn.game.essentials.EntityBuilder;
import pojahn.game.essentials.Image2D;
import pojahn.game.essentials.ResourceManager;
import pojahn.game.essentials.Utils;
import pojahn.game.essentials.Vitality;
import pojahn.game.essentials.geom.Dimension;
import pojahn.game.essentials.stages.TileBasedLevel;
import pojahn.lang.Int32;

import java.io.Serializable;
import java.util.stream.Stream;

public class InAHurry extends TileBasedLevel {

    private ResourceManager resources;
    private GravityMan main;
    private int timeLeft;

    @Override
    public void init(final Serializable meta) throws Exception {
        resources = new ResourceManager();
        resources.loadContentFromDirectory(Gdx.files.internal("res/hurry"));
        resources.loadContentFromDirectory(Gdx.files.internal("res/data"));
        resources.loadContentFromDirectory(Gdx.files.internal("res/general"));
        getEngine().timeFont = resources.getFont("sansserif32.fnt");

        parse(resources.getTiledMap("map.tmx"));
        Stream.of(resources.getAnimation("main")).forEach(Image2D::createPixelData);

        Utils.playMusic(resources.getMusic("music.mp3"), 2.2400f, .7f);
    }

    @Override
    public void build() {
        main = ResourceUtil.getGravityMan(resources);
        main.move(4 * 28, 40 * 28);
        add(main);

        /*
         * Backgrounds and foreground
         */
        add(getWorldImage());
        add(new EntityBuilder().image(resources.getImage("background.png")).zIndex(-1).build(RepeatingParallaxImage.class));

        /*
         * Coin
         */
        final Particle coinTake = Particle.imageParticle(4, resources.getAnimation("cointake"));

        final Entity coin = new Entity();
        coin.setImage(5, resources.getAnimation("coin"));
        coin.move(3192, 472);
        coin.zIndex(Integer.MAX_VALUE);
        coin.addEvent(() -> {
            if (coin.collidesWith(main)) {
                main.setState(Vitality.COMPLETED);
                discard(coin);
                add(coinTake.getClone().center(coin));
                resources.getSound("win.ogg").play();
            }
        });
        add(coin);

        /*
         * Time
         */
        final BitmapFont font = resources.getFont("sansserif32.fnt");
        final Dimension screen = getEngine().getScreenSize();
        final Sound drain = resources.getSound("drain.ogg");
        final Int32 counter = new Int32();
        timeLeft = -1400;
        add(Utils.wrap(batch -> {
            if (timeLeft == 0) {
                if (main.getState() == Vitality.ALIVE)
                    main.setState(Vitality.DEAD);
            } else if (main.getState() == Vitality.ALIVE) {
                timeLeft += 1;
                if (counter.value++ % 6 == 0)
                    drain.play(.2f);
            }

            getEngine().hudCamera();
            font.setColor(Color.YELLOW);
            font.draw(batch, Integer.toString(timeLeft), screen.width - 100, 20);
            getEngine().gameCamera();

        }, Integer.MAX_VALUE));
        final Int32 counter2 = new Int32();
        add(() -> {
            if (main.isAlive() && ++counter2.value % 7 == 0) {
                add(coinTake.getClone().center(main));
            }
        });
    }

    @Override
    public void dispose() {
        resources.disposeAll();
    }

    @Override
    public String getLevelName() {
        return "In A Hurry";
    }
}
