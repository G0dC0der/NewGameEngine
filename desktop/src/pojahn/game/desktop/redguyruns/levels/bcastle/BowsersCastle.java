package pojahn.game.desktop.redguyruns.levels.bcastle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.ImmutableList;
import pojahn.game.core.BaseLogic;
import pojahn.game.core.Entity;
import pojahn.game.desktop.redguyruns.levels.orbit.Fireball;
import pojahn.game.desktop.redguyruns.util.ResourceUtil;
import pojahn.game.entities.enemy.weapon.Bullet;
import pojahn.game.entities.enemy.weapon.Thwump;
import pojahn.game.entities.enemy.weapon.Weapon;
import pojahn.game.entities.image.RepeatingParallaxImage;
import pojahn.game.entities.main.GravityMan;
import pojahn.game.entities.movement.PathDrone;
import pojahn.game.entities.particle.EntityExplosion;
import pojahn.game.entities.particle.Particle;
import pojahn.game.entities.platform.SolidPlatform;
import pojahn.game.essentials.*;
import pojahn.game.essentials.stages.TileBasedLevel;
import pojahn.lang.PingPongFloat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BowsersCastle extends TileBasedLevel {

    private ResourceManager res;
    private GravityMan man;

    @Override
    public void load(final Serializable meta) throws Exception {
        res = new ResourceManager();
        res.loadContentFromDirectory(Gdx.files.internal("res/data"));
        res.loadContentFromDirectory(Gdx.files.internal("res/general"));
        res.loadContentFromDirectory(Gdx.files.internal("res/bcastle"));
        res.loadAnimation(Gdx.files.internal("res/orbit/lava"));
        res.loadImage(Gdx.files.internal("res/orbit/otherlava.png"));

        getEngine().timeFont = res.getFont("sansserif32.fnt");

        Stream.of(res.getAnimation("main")).forEach(Image2D::createPixelData);
        Stream.of(res.getAnimation("teleporting-fireball")).forEach(Image2D::createPixelData);
        res.getImage("thwump-awake.png").createPixelData();
        res.getImage("thwump-sleep.png").createPixelData();
        res.getImage("roller.png").createPixelData();
        res.getImage("spikes.png").createPixelData();
    }

    @Override
    public TiledMap getTileMap() {
        return res.getTiledMap("map.tmx");
    }

    @Override
    public void build() {
        /*
         * Main Character
         */
        man = ResourceUtil.getGravityMan(res);
        man.zIndex(1);
        man.move(14 * getTileHeight(), 24 * getTileHeight() + (getTileHeight()- man.height() - 1));
        add(man);

        /*
         * Backgrounds & Foreground
         */
        final Entity foreground = getWorldImage();
        foreground.zIndex(Integer.MAX_VALUE);
        add(foreground);
        add(new EntityBuilder().image(res.getImage("background.png")).zIndex(-100).build(RepeatingParallaxImage.class));

        /*
         * Spikes
         */
        final Entity layingSpikes = new EntityBuilder().move(1720 - 10, 1362).image(res.getImage("laying-spikes.png")).build();
        layingSpikes.addEvent(Factory.hitMain(layingSpikes, man, -1));
        add(layingSpikes);

        addSmallSpikes(64, 51);
        addSmallSpikes(92, 55);
        addSmallSpikes(153, 37);
        addSmallSpikes(155, 37);

        addSpikes(119, 41, false);
        addSpikes(124, 41, false);
        addSpikes(120, 48, true);
        addSpikes(120, 50, false);
        addSpikes(123, 48, true);
        addSpikes(123, 50, false);
        addSpikes(120, 46, false);
        addSpikes(123, 46, false);
        addSpikes(116, 48, false);
        addSpikes(117, 48, false);
        addSpikes(126, 48, false);
        addSpikes(127, 48, false);

        addSpikes(118, 52, true);
        addSpikes(119, 52, true);
        addSpikes(120, 52, true);
        addSpikes(123, 52, true);
        addSpikes(124, 52, true);
        addSpikes(125, 52, true);
        addSpikes(116, 46, true);
        addSpikes(117, 46, true);
        addSpikes(118, 46, true);
        addSpikes(125, 46, true);
        addSpikes(126, 46, true);
        addSpikes(127, 46, true);

        addSpikes(119, 43, true);
        addSpikes(120, 43, true);
        addSpikes(123, 43, true);
        addSpikes(124, 43, true);

        addSpikes(116, 38, false);
        addSpikes(117, 38, false);
        addSpikes(118, 38, false);
        addSpikes(119, 38, false);
        addSpikes(124, 38, false);
        addSpikes(125, 38, false);
        addSpikes(126, 38, false);
        addSpikes(127, 38, false);

        /*
         * Statue & Weapon
         */
        final Particle burn = Particle.imageParticle(5, res.getAnimation("burn"));

        final Bullet fireball = new Bullet(man);
        fireball.setImage(4, res.getAnimation("fireball"));
        fireball.setMoveSpeed(2.5f);
        fireball.setImpact(burn);

        final Weapon weapon = new Weapon(1738, 1224, 1, 0, 80, man);
        weapon.setImage(res.getImage("statue.png"));
        weapon.setProjectile(fireball);
        weapon.setFiringOffsets(40, 25);

        add(weapon);

        /*
         * Thwomp
         */
        final List<Entity> thwomps = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            final Thwump thwump = Thwump.builder()
                .initialPosition(new Vector2(1887 + (220 * i), 1062))
                .direction(Direction.S)
                .attackSpeed(6)
                .retreatSpeed(1.5f)
                .scanPadding(5)
                .attackRecovery(40)
                .sleepRecovery(15)
                .targets(ImmutableList.of(man))
                .attackImage(new Animation<>(res.getImage("thwump-awake.png")))
                .sleepImage(new Animation<>(res.getImage("thwump-sleep.png")))
                .build();
            thwump.addEvent(Factory.hitMain(thwump, man, -1));
            thwump.setHitbox(Hitbox.PIXEL);

            final Vibrator slamVibrator = new Vibrator(this, thwump, Vibrator.VibDirection.CENTER, man);
            slamVibrator.setDuration(40);
            slamVibrator.setStrength(85);

            thwump.setSlamEvent(()-> {
                slamVibrator.vibrate();
                //TODO: Slamsound
            });
            add(thwump);
            thwomps.add(thwump);
        }

        /*
         * Pipe & Rolling Thwump
         */
        final Entity pipe = new EntityBuilder().image(res.getImage("pipe.png")).move(2632, 1061).zIndex(10000).build();
        man.addObstacle(pipe);
        add(pipe);

        interval(()-> {
            final RollingThwump rollingThwump = new RollingThwump();
            rollingThwump.setImage(res.getImage("roller.png"));
            rollingThwump.move(2643, 1065);
            rollingThwump.setFallingLine(1136);
            rollingThwump.setMoveSpeed(1);
            rollingThwump.setHitbox(Hitbox.CIRCLE);
            rollingThwump.addEvent(Factory.hitMain(rollingThwump, man, -1));
            rollingThwump.setDieEntities(ImmutableList.<Entity>builder().addAll(thwomps).add(layingSpikes).build());

            final Vibrator landingVib = new Vibrator(this, rollingThwump, Vibrator.VibDirection.CENTER, man);
            landingVib.setStrength(40);
            landingVib.setDuration(30);
            landingVib.setRadius(800);

            final Vibrator dieVib = new Vibrator(this, rollingThwump, Vibrator.VibDirection.CENTER, man);
            dieVib.setStrength(20);
            dieVib.setDuration(20);
            dieVib.setRadius(400);

            final EntityExplosion rollerDie = new EntityExplosion(rollingThwump, 8, 8, Arrays.stream(res.getAnimation("roller-die")).map(image2D -> new Animation<>(image2D)).collect(Collectors.toList()));
            rollerDie.setVx(150);
            rollerDie.setVy(200);
            rollerDie.setToleranceX(200);
            rollerDie.setToleranceY(100);

            rollingThwump.setDieParticle(rollerDie);
            rollingThwump.setLandingVib(landingVib);
            rollingThwump.setDieVib(dieVib);

            add(rollingThwump);
        }, 240);

        /*
         * Lava
         */
        for (int i = 0; i < 190; i++) {
            final Entity lava = new Entity();
            lava.setImage(6, res.getAnimation("lava"));
            lava.move(1120 + (i * lava.width()),2367);
            lava.zIndex(10000);
            add(lava);
        }

        final Image2D lava = res.getAnimation("lava")[0];
        final Entity otherLava = new EntityBuilder().image(res.getImage("otherlava.png")).move(1120, 2367 + lava.getHeight()).build();
        otherLava.bounds.size.set(lava.getWidth() * 190, 1868);
        otherLava.zIndex(1000);
        add(otherLava);

        final Rectangle lavaHitbox = new Rectangle(1120, 2367, 3191, 1868);
        add(()-> {
            if (BaseLogic.rectanglesCollide(man.bounds.toRectangle(), lavaHitbox)) {
                man.touch(-10);
            }
        });

        /*
         * Fireballs
         */
        addFireBall(2528, 2457 + 500);
        addTeleportingFireball(3000, 2412, 600, .05f, 60);
        addTeleportingFireball(3248, 2431, 700, .1f, 400);
        addTeleportingFireball(3523, 2431, 550, .1f, -100);

        /*
         * Bar & Creeper
         */
        final SolidPlatform bar = new SolidPlatform(121 * getTileWidth(),  58 * getTileHeight(), man);
        bar.setImage(res.getImage("bar.png"));
        bar.zIndex(100);


        add(bar);

        final PathDrone creeper = new PathDrone(121 * getTileWidth() + 3,  58 * getTileHeight() + 5);
        creeper.setImage(res.getImage("creeper.png"));
        creeper.appendPath(creeper.x(), 37 * getTileHeight());
        creeper.addEvent(Factory.hitMain(creeper, man, -1));
        creeper.freeze();
        creeper.setMoveSpeed(1.2f);
        add(creeper);
        runOnceWhenMainCollides(()-> {
            creeper.unfreeze();
            discard(bar);
        }, 121, 55, 2, 1);

        final PingPongFloat pingPongFloat = new PingPongFloat(.91f, 1.0f, .01f);
        creeper.addEvent(() -> creeper.scaleX = creeper.scaleY = pingPongFloat.get());

        /*
         * Finalize
         */
        man.setActionEvent(caller -> {
            if (caller.isCloneOf(fireball)) {
                man.touch(-1);
            }
        });
    }

    @Override
    public void dispose() {
        res.disposeAll();
    }

    @Override
    public String getLevelName() {
        return "Bowsers Castle";
    }

    private void addSmallSpikes(final int tileX, final int tileY) {
        final Entity smallSpikes = new Entity();
        smallSpikes.move(tileX * getTileWidth(), tileY * getTileHeight());
        smallSpikes.setImage(res.getImage("spikes.png"));
        smallSpikes.addEvent(Factory.hitMain(smallSpikes, man, -1));
        smallSpikes.setHitbox(Hitbox.PIXEL);
        add(smallSpikes);
    }

    private void addSpikes(final int tileX, final int tileY, final boolean flipY) {
        final Entity spikes = new Entity();
        spikes.move(tileX * getTileWidth(), tileY * getTileHeight());
        spikes.setImage(res.getImage("spikes.png"));
        spikes.setHitbox(Hitbox.PIXEL);
        spikes.addEvent(Factory.hitMain(spikes, man, -1));
        spikes.flipY = flipY;
        if (flipY) {
            spikes.bounds.pos.y -= 2;
        }
        add(spikes);
    }

    private void addFireBall(final float x, final float y) {
        final Animation<Image2D> fireballImage = new Animation<>(3, res.getAnimation("fireball-enemy"));
        fireballImage.pingPong(true);

        final Fireball fireball = new Fireball(x, y, 900);
        fireball.setImage(fireballImage);
        fireball.addEvent(Factory.hitMain(fireball, man, -1));
        add(fireball);
    }

    private void addTeleportingFireball(final float x, final float y, final float flyPower, final float fadeSpeed, final float threshold) {
        final TeleportingFireball teleportingFireball = new TeleportingFireball(x, y, flyPower);
        teleportingFireball.setFadeSpeed(fadeSpeed);
        teleportingFireball.setThreshold(threshold);
        teleportingFireball.setImage(res.getAnimation("teleporting-fireball"));
        teleportingFireball.addEvent(Factory.hitMain(teleportingFireball,  man, -1));
        teleportingFireball.setHitbox(Hitbox.PIXEL);
        add(teleportingFireball);
    }
}
