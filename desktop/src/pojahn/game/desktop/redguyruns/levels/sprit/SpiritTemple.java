package pojahn.game.desktop.redguyruns.levels.sprit;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import pojahn.game.core.BaseLogic;
import pojahn.game.core.Entity;
import pojahn.game.core.PlayableEntity;
import pojahn.game.desktop.redguyruns.util.GFX;
import pojahn.game.desktop.redguyruns.util.ResourceUtil;
import pojahn.game.entities.image.BigImage;
import pojahn.game.entities.image.BigImage.RenderStrategy;
import pojahn.game.entities.object.Collectable;
import pojahn.game.entities.LineMovement;
import pojahn.game.entities.LineMovement.Movement;
import pojahn.game.entities.enemy.weapon.Missile;
import pojahn.game.entities.particle.Particle;
import pojahn.game.entities.PathDrone;
import pojahn.game.entities.platform.PushableObject;
import pojahn.game.entities.enemy.weapon.Reloadable;
import pojahn.game.entities.platform.SolidPlatform;
import pojahn.game.entities.platform.SolidPlatform.FollowMode;
import pojahn.game.entities.platform.TilePlatform;
import pojahn.game.essentials.Animation;
import pojahn.game.essentials.CameraEffects;
import pojahn.game.essentials.Direction;
import pojahn.game.essentials.EntityBuilder;
import pojahn.game.essentials.Factory;
import pojahn.game.essentials.Hitbox;
import pojahn.game.essentials.Image2D;
import pojahn.game.essentials.ResourceManager;
import pojahn.game.essentials.Utils;
import pojahn.game.essentials.Vibrator;
import pojahn.game.essentials.stages.TileBasedLevel;
import pojahn.game.events.Event;

import java.awt.*;
import java.io.Serializable;
import java.util.stream.Stream;

public class SpiritTemple extends TileBasedLevel {

    private ResourceManager resources;
    private PlayableEntity main;

    @Override
    public void init(final Serializable meta) throws Exception {
        resources = new ResourceManager();
        resources.loadContentFromDirectory(Gdx.files.internal("res/spirit"));
        resources.loadContentFromDirectory(Gdx.files.internal("res/data"));
        resources.loadContentFromDirectory(Gdx.files.internal("res/general"));
        resources.addAsset("push_2", Gdx.audio.newMusic(Gdx.files.internal("res/spirit/push_music.wav")));
        resources.addAsset("push_3", Gdx.audio.newMusic(Gdx.files.internal("res/spirit/push_music.wav")));
        getEngine().timeFont = resources.getFont("sansserif32.fnt");

        parse(resources.getTiledMap("map.tmx"));

        final Pixmap pix = new Pixmap(1, 1, Format.RGBA8888);
        pix.setColor(Color.BLACK);
        pix.fill();
        final Image2D black = new Image2D(pix, false);
        pix.dispose();
        resources.addAsset("black.png", black);

        Stream.of(resources.getAnimation("main")).forEach(Image2D::createPixelData);
        resources.getImage("spike.png").createPixelData();
        resources.getImage("spikes.png").createPixelData();

        Utils.playMusic(resources.getMusic("music.ogg"), 62.09f, .8f);

        resources.getMusic("boss_music.mp3").setOnCompletionListener(music1 -> {
            music1.play();
            music1.setPosition(6.1765f);
        });

        getCheckpointHandler().setReachEvent(() -> GFX.renderCheckpoint(resources, this));
        getCheckpointHandler().appendCheckpoint(5509, 3615, 5319, 3037, 440, 66);
    }

    @Override
    public void build() {
        main = ResourceUtil.getGravityMan(resources);
        main.move(40 * 24, 62 * 24);
        main.touch(1);
        add(main);

        /*
         * Backgrounds and foreground
         */
        add(getWorldImage());
        add(new EntityBuilder().image(resources.getImage("sky.png")).zIndex(-1).build());
        add(new EntityBuilder().image(resources.getImage("background.png")).zIndex(-2).build(BigImage.class, RenderStrategy.PARALLAX_REPEAT));

        final Entity darkShade = new Entity() {
            @Override
            public void render(final SpriteBatch batch) {
                final Dimension size = getEngine().getScreenSize();
                getEngine().hudCamera();
                batch.draw(nextImage(), 0, 0, size.width, size.height);
                getEngine().gameCamera();
            }
        };
        darkShade.tint.a = 0;
        darkShade.zIndex(Integer.MAX_VALUE);
        darkShade.setImage(resources.getImage("black.png"));
        darkShade.addEvent(() -> {
            final float SPEED = .01f;
            if (main.y() > 1800) {
                darkShade.tint.a += SPEED;
                darkShade.tint.a = Math.min(darkShade.tint.a, .4f);
            } else {
                darkShade.tint.a -= SPEED;
                darkShade.tint.a = Math.max(darkShade.tint.a, 0);
            }
        });
        add(darkShade);

        /*
         * Pushable Objects
         */
        final PushableObject yellowBlock = new PushableObject(384, 2088, main);
        yellowBlock.setImage(resources.getImage("block1.png"));
        yellowBlock.pushStrength = 0;
        yellowBlock.setPushingSound(resources.getMusic("push_music.wav"));
        yellowBlock.setSlammingSound(resources.getSound("silverimpact.wav"));
        add(yellowBlock);

        final PushableObject silverBlock = new PushableObject(3144, 505, main);
        silverBlock.setImage(resources.getImage("block2.png"));
        silverBlock.bounds.size.height--;
        silverBlock.bounds.size.width--;
        silverBlock.pushStrength = 0;
        silverBlock.setPushingSound(resources.getMusic("push_2"));
        silverBlock.setSlammingSound(resources.getSound("silverimpact.wav"));
        add(silverBlock);

        final PushableObject megaBlock = new PushableObject(5052, 1011, main);
        megaBlock.setImage(resources.getImage("bigblock.png"));
        megaBlock.setPushingSound(resources.getMusic("push_3"));
        megaBlock.setSlammingSound(resources.getSound("goldimpact.wav"));
        megaBlock.pushStrength = 0;
        megaBlock.setSlamEvent(() -> temp(CameraEffects.vibration(10), 60));
        add(megaBlock);

        /*
         * Spikey
         */
        final LineMovement spike = new LineMovement(Movement.HORIZONTAL);
        spike.setMoveSpeed(5);
        spike.setImage(resources.getImage("spike.png"));
        spike.setHitbox(Hitbox.PIXEL);
        spike.move(2541, 1287);
        spike.setSlamSound(resources.getSound("steelCollide.wav"));
        spike.addEvent(Factory.hitMain(spike, main, -1));
        spike.sounds.useFalloff = true;
        spike.sounds.maxVolume = .7f;
        spike.sounds.maxDistance = 600;
        add(spike);

        /*
         * Gloves
         */
        final Collectable silverGlove = new Collectable(4200, 2894, main);
        silverGlove.setImage(resources.getImage("silverglove.png"));
        silverGlove.setCollectSound(resources.getSound("collect1.wav"));
        silverGlove.setCollectEvent(sub -> {
            yellowBlock.pushStrength = 50;
            silverBlock.pushStrength = 50;
        });
        add(silverGlove);

        final Collectable goldenGlove = new Collectable(1272, 303, main);
        goldenGlove.setImage(resources.getImage("goldenglove.png"));
        goldenGlove.setCollectSound(resources.getSound("collect1.wav"));
        goldenGlove.setCollectEvent(sub -> {
            yellowBlock.pushStrength = 150;
            silverBlock.pushStrength = 150;
            megaBlock.pushStrength = 50;
        });
        add(goldenGlove);

        /*
         * Spikes
         */
        final Entity spikes = new Entity();
        spikes.setImage(resources.getImage("spikes.png"));
        spikes.move(1824, 1272);
        spikes.setHitbox(Hitbox.PIXEL);
        spikes.addEvent(Factory.hitMain(spikes, main, -1));
        add(spikes);

        /*
         * Stone Guys
         */
        final SolidPlatform blocky1 = getBlocky(1951, 1443);
        add(blocky1);

        final SolidPlatform blocky2 = getBlocky(1951, 1443);
        blocky2.skipTo(7);
        add(blocky2);

        final SolidPlatform blocky3 = getBlocky(2213, 1386);
        blocky3.clearData();
        blocky3.appendPath();
        blocky3.appendPath(2228, 1386);
        blocky3.appendPath(2228, 1386);
        blocky3.appendPath(2228, 1370);
        blocky3.appendPath(2298, 1370);
        blocky3.appendPath(2298, 1386);
        blocky3.appendPath(2319, 1386);
        blocky3.appendPath(2319, 1550);
        blocky3.appendPath(2304, 1550);
        blocky3.appendPath(2299, 1573);
        blocky3.appendPath(2234, 1573);
        blocky3.appendPath(2234, 1556);
        blocky3.appendPath(2213, 1556);

        add(blocky3);

        /*
         * Puncher
         */
        final Sound punchSound = resources.getSound("fistslam.wav");
        final Vibrator rv = new Vibrator(this);
        rv.setStrength(150);
        rv.setDuration(20);
        final float speed = 4;

        final TilePlatform puncher1 = new TilePlatform(2593, 2617, main);
        puncher1.setImage(resources.getImage("puncher1.png"));
        puncher1.setMoveSpeed(speed);
        puncher1.sounds.useFalloff = true;
        puncher1.appendPath(2593, 2617, 30, false, null);
        puncher1.appendPath(2770, 2617, 30, false, () -> {
            rv.vibrate(puncher1, Vibrator.VibDirection.CENTER, main);
            puncher1.sounds.play(punchSound);
            puncher1.sounds.play(punchSound);
        });
        add(puncher1);

        final TilePlatform puncher2 = new TilePlatform(2593, 2695, main);
        puncher2.setImage(resources.getImage("puncher1.png"));
        puncher2.setMoveSpeed(speed);
        puncher2.sounds.useFalloff = true;
        puncher2.appendPath(2593, 2695, 30, false, null);
        puncher2.appendPath(2770, 2695, 30, false, () -> {
            rv.vibrate(puncher2, Vibrator.VibDirection.CENTER, main);
            puncher2.sounds.play(punchSound);
        });
        puncher2.skipTo(1);
        add(puncher2);

        final TilePlatform puncher3 = new TilePlatform(2593, 2773, main);
        puncher3.setImage(resources.getImage("puncher1.png"));
        puncher3.setMoveSpeed(speed);
        puncher3.sounds.useFalloff = true;
        puncher3.appendPath(2593, 2773, 30, false, null);
        puncher3.appendPath(2770, 2773, 30, false, () -> {
            rv.vibrate(puncher3, Vibrator.VibDirection.CENTER, main);
            puncher3.sounds.play(punchSound);
        });
        add(puncher3);

        final TilePlatform puncher4 = new TilePlatform(1920, 2946, main);
        puncher4.setImage(resources.getImage("puncher1.png"));
        puncher4.sounds.useFalloff = true;
        puncher4.setMoveSpeed(speed + 2);
        puncher4.appendPath(2266, 2946, 30, false, () -> {
            rv.vibrate(puncher4, Vibrator.VibDirection.CENTER, main);
            puncher4.sounds.play(punchSound);
            puncher4.setMoveSpeed(2);
        });
        puncher4.appendPath(1920, 2946, 30, false, () -> puncher4.setMoveSpeed(speed + 2));
        add(puncher4);

        final TilePlatform puncher5 = new TilePlatform(1680, 2640, main);
        puncher5.setImage(resources.getImage("puncher2.png"));
        puncher5.sounds.useFalloff = true;
        puncher5.setMoveSpeed(speed);
        puncher5.appendPath(1680, 2640, 40, false, null);
        puncher5.appendPath(1680, 2698, 40, false, () -> {
            rv.vibrate(puncher5, Vibrator.VibDirection.CENTER, main);
            puncher5.sounds.play(punchSound);
        });

        add(puncher5);

        /*
         * Reward
         */
        final SolidPlatform rewards = new SolidPlatform(0, 0, main);
        rewards.setImage(resources.getImage("cloud.png"));
        rewards.appendPath(1059, 1443, 80, false, null);
        rewards.appendPath(1059, 335, 80, false, null);
        rewards.setMoveSpeed(1.4f);

        /*
         * Ghost
         */
        final Music ghostMove = resources.getMusic("alienmove_music.wav");

        final Ghost ghost = new Ghost(3401, 2789, main, rewards, resources.getFont("cambria20.fnt"), resources.getSound("ghosttalk.wav"), resources.getSound("success.wav"));
        ghost.setImage(4, resources.getAnimation("ghost"));
        ghost.setMoveSpeed(0);
        ghost.setFacings(2);
        ghost.addEvent(ghost::face);
        ghost.appendPath(3031, 2789);
        ghost.appendPath(3031, 2523);
        ghost.appendPath(2785, 2523);
        ghost.appendPath(2611, 2809);
        ghost.appendPath(2375, 2931);
        ghost.appendPath(1934, 2931);
        ghost.appendPath(1934, 2691);
        ghost.appendPath(1109, 2640);
        ghost.appendPath(768, 2528);
        ghost.appendPath(691, 2133);
        ghost.appendPath(264, 2101);
        ghost.appendPath(283, 1614);
        ghost.appendPath(501, 1410);
        ghost.appendPath(1049, 1410);
        ghost.appendPath(1047, 1410, 0, false, () -> {
            ghost.setMoveSpeed(0);
            ghost.reachedDest = true;
        });
        add(() -> {
            if (ghost.getMoveSpeed() <= 0.0f) {
                ghostMove.stop();
            } else {
                if (!ghostMove.isPlaying()) {
                    ghostMove.setLooping(true);
                    ghostMove.play();
                }
                ghostMove.setVolume(ghost.sounds.calc());
            }
        });
        add(ghost);

        /*
         * Music Changer
         */
        final Music music = resources.getMusic("music.ogg");
        if (!music.isPlaying()) {
            music.setVolume(.8f);
            music.play();
        }
        final Music bossMusic = resources.getMusic("boss_music.mp3");
        bossMusic.setVolume(0);
        if (bossMusic.isPlaying()) {
            bossMusic.pause();
        }

        runOnceWhen(() -> {
            temp(() -> music.setVolume(music.getVolume() - .01f), () -> {
                if (music.getVolume() <= .01) {
                    music.pause();
                    return true;
                }
                return false;
            });
            bossMusic.play();
            temp(() -> bossMusic.setVolume(bossMusic.getVolume() + .01f), () -> bossMusic.getVolume() >= 1);
        }, () -> BaseLogic.rectanglesCollide(5251, 3087, 96, 615, main.x(), main.y(), main.width(), main.height()));

        /*
         * Boss
         */
        final Particle fakeGunfire = Particle.fromSound(resources.getSound("gearfire.wav"));
        fakeGunfire.setVisible(false);

        final Particle gearExp = Particle.imageParticle(4, resources.getAnimation("puff"));
        gearExp.zIndex(Integer.MAX_VALUE);
        gearExp.setIntroSound(resources.getSound("gearboom.wav"));
        gearExp.sounds.useFalloff = true;
        gearExp.sounds.maxDistance = 650;

        final Missile gear = new Missile(0, 0, main, megaBlock);
        gear.setImage(resources.getImage("gear.png"));
        gear.setCloneEvent(clonie -> clonie.addEvent(() -> clonie.rotate(12)));
        gear.setHitbox(Hitbox.CIRCLE);
        gear.setQuickCollision(true);
        gear.setGunfire(fakeGunfire);
        gear.setImpact(gearExp);
        gear.rotate(false);
        gear.follow(true);

        final Particle dieAnimation = Particle.imageParticle(5, resources.getAnimation("bossdeath"));
        dieAnimation.zIndex(Integer.MAX_VALUE);

        final Boss boss = new Boss();
        boss.setFullHealth(new Animation<>(1, resources.getImage("boss1.png")));
        boss.setOnceHit(new Animation<>(1, resources.getImage("boss2.png")));
        boss.setFinalLife(new Animation<>(1, resources.getImage("boss3.png")));
        boss.setSpinnerImage(resources.getImage("spinner.png"));
        boss.setDeathAnim(dieAnimation);
        boss.setHero(main);
        boss.setProj(gear);
        boss.setResource(resources);
        add(boss);

        /*
         * Reloadable
         */
        final Entity item1 = new Entity();
        item1.setImage(4, resources.getAnimation("pickable"));
        item1.tint.a = .7f;
        item1.move(4911, 3592);
        add(item1);

        final Entity item2 = item1.getClone().move(item1.x() + item1.width(), item1.y());
        add(item2);

        final Entity item3 = item2.getClone().move(item2.x() + item2.width(), item1.y());
        add(item3);

        final Entity blazing = new Entity();
        blazing.setImage(6, resources.getAnimation("magic"));
        blazing.move(3139, 3576);

        final Reloadable statue = new Reloadable(3102, 3566);
        statue.setImage(resources.getImage("statue.png"));
        statue.setUsers(main);
        statue.zIndex(-1);
        statue.setItems(item1, item2, item3);
        statue.setGrabEvent(() -> resources.getSound("collect2.wav").play(.7f));
        statue.setLoadedEvent(() -> {
            resources.getSound("collect3.wav").play();
            temp(blazing.getClone(), 400);
            attackBoss(boss);
        });
        add(statue);

        /*
         * Hit Event
         */
        main.setActionEvent(sub -> {
            if (sub.isCloneOf(gear))
                main.touch(-1);
        });
    }

    void attackBoss(final Boss boss) {
        final Entity fadeAway = new Entity();
        fadeAway.setImage(resources.getImage("killerfist.png"));
        fadeAway.addEvent(() -> {
            fadeAway.tint.a -= .02f;
            fadeAway.tint.a = Math.max(fadeAway.tint.a, 0);

            if (0 >= fadeAway.tint.a) {
                discard(fadeAway);
            }
        });

        final Particle smashEffect = Particle.imageParticle(4, resources.getAnimation("bosshit"));
        smashEffect.setIntroSound(resources.getSound("bosshurt.wav"));

        final PathDrone fist = new PathDrone(3799, 3323);
        fist.appendPath(0, fist.y());
        fist.setImage(resources.getImage("killerfist.png"));
        fist.setMoveSpeed(6);
        fist.freeze();
        fist.tint.a = 0;
        fist.addEvent(new Event() {
            @Override
            public void eventHandling() {
                fist.tint.a += .02;
                fist.tint.a = Math.min(fist.tint.a, 1);

                if (fist.tint.a >= 1) {
                    fist.removeEvent(this);
                    fist.unfreeze();
                }
            }
        });
        fist.addEvent(() -> {
            if (fist.collidesWith(boss)) {
                boss.hit();
                discard(fist);
                add(fadeAway.move(fist.x(), fist.y()));

                smashEffect.center(fist);
                smashEffect.bounds.pos.x -= 50;
                add(smashEffect);
                resources.getSound("bosspunched.wav").play();
            }
        });

        add(fist);
    }

    SolidPlatform getBlocky(final float x, final float y) {
        final SolidPlatform blocky = new SolidPlatform(x, y, main);
        blocky.setImage(6, resources.getAnimation("blocky"));
        blocky.setMoveSpeed(1f);
        blocky.addEvent(blocky::face);
        blocky.setFacings(4);
        blocky.zIndex(10);
        blocky.setFollowMode(FollowMode.STRICT);
        blocky.appendPath();
        blocky.appendPath(2080, 1443);
        blocky.appendPath(2083, 1443);
        blocky.appendPath(2083, 1465);
        blocky.appendPath(2104, 1465);
        blocky.appendPath(2104, 1569);
        blocky.appendPath(2088, 1569);
        blocky.appendPath(2088, 1597);
        blocky.appendPath(1922, 1597);
        blocky.appendPath(1922, 1577);
        blocky.appendPath(1900, 1577);
        blocky.appendPath(1900, 1467);
        blocky.appendPath(1914, 1467);
        blocky.appendPath(1914, 1446);

        final Entity dummy = new Entity();
        dummy.zIndex(Integer.MAX_VALUE);
        dummy.addEvent(() -> blocky.flipY = blocky.getFacing() == Direction.W);
        add(dummy);

        return blocky;
    }

    @Override
    public Music getStageMusic() {
        final Music music = resources.getMusic("music.ogg");
        final Music bossMusic = resources.getMusic("boss_music.mp3");
        return music.isPlaying() ? music : bossMusic;
    }

    @Override
    public void dispose() {
        resources.disposeAll();
    }

    @Override
    public String getLevelName() {
        return "Spirit Temple";
    }
}
