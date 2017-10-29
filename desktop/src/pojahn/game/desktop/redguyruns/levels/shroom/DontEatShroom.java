package pojahn.game.desktop.redguyruns.levels.shroom;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import pojahn.game.core.Entity;
import pojahn.game.core.MobileEntity;
import pojahn.game.core.PlayableEntity;
import pojahn.game.desktop.redguyruns.util.ResourceUtil;
import pojahn.game.entities.TmxEntity;
import pojahn.game.entities.image.BigImage;
import pojahn.game.entities.image.BigImage.RenderStrategy;
import pojahn.game.entities.object.Bouncer;
import pojahn.game.entities.PathDrone;
import pojahn.game.entities.platform.SolidPlatform;
import pojahn.game.entities.main.GravityMan;
import pojahn.game.essentials.Direction;
import pojahn.game.essentials.EntityBuilder;
import pojahn.game.essentials.Factory;
import pojahn.game.essentials.Hitbox;
import pojahn.game.essentials.Image2D;
import pojahn.game.essentials.ResourceManager;
import pojahn.game.essentials.Utils;
import pojahn.game.essentials.shaders.InvertShader;
import pojahn.game.essentials.stages.TileBasedLevel;

import java.io.Serializable;
import java.util.stream.Stream;

public class DontEatShroom extends TileBasedLevel {

    private ResourceManager resources;
    private Music music, noise;
    private PlayableEntity play;
    private InvertShader invertShader;
    private boolean eaten, keyTaken;
    private int vibCounter, vibCounter2;
    private float vibStrength;

    @Override
    public void init(final Serializable meta) throws Exception {
        resources = new ResourceManager();
        resources.loadContentFromDirectory(Gdx.files.internal("res/data"));
        resources.loadContentFromDirectory(Gdx.files.internal("res/general"));
        resources.loadContentFromDirectory(Gdx.files.internal("res/shroom"));

        getEngine().timeFont = resources.getFont("sansserif32.fnt");
        parse(resources.getTiledMap("map.tmx"));

        Stream.of(resources.getAnimation("main")).forEach(Image2D::createPixelData);
        Stream.of(resources.getAnimation("goldmouse")).forEach(Image2D::createPixelData);

        resources.addAsset("pink.png", Utils.toImage(Color.PINK));
        invertShader = new InvertShader();

        music = resources.getMusic("music.ogg");
        noise = resources.getMusic("noise_music.wav");
        Utils.playMusic(music, 9.1022f, .5f);
    }

    @Override
    public void build() {
        eaten = keyTaken = false;
        vibStrength = 0.0f;
        vibCounter = vibCounter2 = 0;
        getEngine().getSpriteBatch().setShader(null);
        invertShader.setInvertValue(0);
        music.setVolume(.5f);
        noise.setVolume(0);
        noise.stop();
        noise.setLooping(true);

        /*
         * Main Character
         */
        play = ResourceUtil.getGravityMan(resources);
        play.move(25 * 24, 66 * 24);
        add(play);
        runOnceWhen(play::lose, ()-> play.y() + play.height() >= getHeight() - 2);

        /*
         * Background and foreground
         */
        add(getWorldImage());
        final Entity bgTiles = new TmxEntity(resources.getTiledMap("bgstuff.tmx"));
        bgTiles.zIndex(-1);
        add(bgTiles);

        final BigImage bg = new BigImage(RenderStrategy.PARALLAX);
        bg.setRatio(.27f);
        bg.setImage(resources.getImage("background0.png"));
        bg.zIndex(-10);
        add(bg);

        /*
         * Mouse
         */
        addMouse(81 * 24, 1505,
                84 * 24, 1505);

        addMouse(2328, 1219,
                2509, 1219);

        addMouse(2880, 1219,
                3020, 1219);

        addMouse(4246, 1171,
                4576, 1171);
        /*
         * Shredder
         */
        add(getShredder(2544, 1176, 120, 72));
        add(getShredder(2688, 1056, 96, 72));
//        add(getShredder(2881, 888, 96, 72));

        /*
         * Moving Platform
         */
        final SolidPlatform solidP = new SolidPlatform(147 * 24, 57 * 24, play);
        solidP.appendPath();
        solidP.appendPath(169 * 24, solidP.y());
        solidP.setImage(resources.getImage("platform.png"));
        solidP.setMoveSpeed(1.5f);
        add(solidP);

        /*
         * Bouncer
         */
        final Bouncer bouncer = new Bouncer(4676, 1195, (GravityMan) play);
        bouncer.setBouncingDirection(Direction.N);
        bouncer.setPower(550);
        bouncer.setImage(resources.getImage("bounce.png"));
        bouncer.setBounceSound(resources.getSound("bounce.wav"));
        add(bouncer);

        /*
         * Gem
         */
        final Entity gem = new EntityBuilder().move(471, 1577).image(resources.getImage("gem.png")).build();
        gem.ifCollides(play).then(() -> {
            play.win();
            resources.getSound("collect1.wav").play();
            discard(gem);
        });
        add(gem);

        /*
         * Door
         */
        final Entity door = new EntityBuilder().move(433, 1545).image(resources.getImage("door.png")).build();
        add(door);
        play.addObstacle(door);

        /*
         * Key
         */
        final Entity key = new Entity();
        key.move(4294, 907);
        key.setImage(resources.getImage("key.png"));
        key.addEvent(() -> {
            if (key.collidesWith(play)) {
                discard(key);
                resources.getSound("collect3.wav").play();
                keyTaken = true;
                runOnceWhen(() -> {
                    door.die();
                    play.removeObstacle(door);
                }, () -> eaten && keyTaken);
            }
        });
        add(key);

        /*
         * Magic Shroom
         */
        final Entity shroom = new Entity();
        shroom.move(4583, 904);
        shroom.setImage(resources.getImage("shroom.png"));
        shroom.addEvent(() -> {
            if (shroom.collidesWith(play)) {
                discard(shroom);
                resources.getSound("eat.wav").play();
                eaten = true;
            }
        });
        add(shroom);

        /*
         * Pink Dots
         */
        final Entity dots = new Entity() {
            int counter = 0;
            final float space = 50f;

            @Override
            public void render(final SpriteBatch batch) {
                getEngine().hudCamera();

                final float val = ++counter % space;

                for (int x = 0; x < 20 * space; x += space) {
                    for (int y = 0; y < 20 * space; y += space) {
                        basicRender(batch, nextImage(), x + val, y);
                    }
                }

                getEngine().gameCamera();
            }
        };
        dots.setImage(resources.getImage("pink.png"));
        dots.zIndex(Integer.MAX_VALUE);
        dots.tint.a = 0;
        dots.addEvent(Factory.fadeIn(dots, .001f));

        /*
         * Vibrator
         */
        final Entity vibrator = new Entity() {
            @Override
            public void logistics() {
                if (++vibCounter % 2 == 0) {
                    vibStrength = Math.min(10, vibStrength + .05f);
                    final int value = vibCounter2++ % 4;
                    float tx = getEngine().tx();
                    float ty = getEngine().ty();

                    switch (value) {
                        case 0:
                            tx += -vibStrength;
                            ty += -vibStrength;
                            break;
                        case 1:
                            tx += vibStrength;
                            ty += -vibStrength;
                            break;
                        case 2:
                            tx += vibStrength;
                            ty += vibStrength;
                            break;
                        case 3:
                            tx -= vibStrength;
                            ty += vibStrength;
                            break;
                    }
                    getEngine().translate(tx, ty);
                }
            }
        };
        vibrator.zIndex(Integer.MAX_VALUE);

        /*
         * Zoomer
         */
        final Entity zoomer = new Entity() {

            float speed, min = .5f, max = 1f;
            float scaleValue;
            boolean increasingScale;

            @Override
            public void init() {
                scaleValue = getEngine().getZoom();
                zIndex(Integer.MAX_VALUE);
            }

            @Override
            public void logistics() {
                speed = Math.min(.012f, speed += .00001f);

                if (increasingScale) {
                    scaleValue += speed;
                    if (scaleValue > max)
                        increasingScale = false;
                } else {
                    scaleValue -= speed;
                    if (scaleValue < min)
                        increasingScale = true;
                }

                getEngine().setZoom(scaleValue);
            }
        };
        zoomer.zIndex(Integer.MAX_VALUE);

        /*
         * Trigger all drug effects
         */
        runOnceWhen(() -> {
            noise.play();
            add(Factory.tuneUp(noise, .001f, .75f));
            add(Factory.tuneDown(music, .001f, .1f));
            ((GravityMan) play).setJumpSound(resources.getSound("jumpdruged.wav"));

            invertShader.use(getEngine().getSpriteBatch());
            add(dots);
            add(vibrator);
            add(zoomer);
            add(() -> {
                invertShader.setInvertValue(Math.min(1f, invertShader.getInvertValue() + .004f));
                invertShader.prepare();
            });
        }, () -> eaten);
    }

    void addMouse(final float... cords) {
        final PathDrone mouse = new PathDrone(cords[0], cords[1]);
        for (int i = 0; i < cords.length; i += 2) {
            mouse.appendPath(cords[i], cords[i + 1]);
        }
        mouse.setMoveSpeed(1);
        mouse.setImage(4, resources.getAnimation("goldmouse"));
        mouse.addEvent(mouse::face);
        mouse.addEvent(Factory.hitMain(mouse, play, -1));
        add(mouse);
    }

    PathDrone getShredder(final float x, final float y, final float width, final float height) {
        final PathDrone shredder = new PathDrone(x, y);
        shredder.setImage(2, resources.getAnimation("shredder"));
        shredder.setHitbox(Hitbox.CIRCLE);
        shredder.setMoveSpeed(2);
        shredder.appendPath();
        shredder.appendPath(x + width - shredder.width(), y);
        shredder.appendPath(x + width - shredder.width(), y + height - shredder.height());
        shredder.appendPath(x, y + height - shredder.height());
        shredder.addEvent(Factory.hitMain(shredder, play, -1));

        return shredder;
    }

    @Override
    public Music getStageMusic() {
        return music;
    }

    @Override
    public String getLevelName() {
        return "Do Not Eat Shroom";
    }

    @Override
    public void dispose() {
        resources.disposeAll();
        invertShader.dispose();
    }
}