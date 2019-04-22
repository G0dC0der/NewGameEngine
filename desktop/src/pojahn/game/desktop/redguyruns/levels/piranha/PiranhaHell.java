package pojahn.game.desktop.redguyruns.levels.piranha;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import pojahn.game.core.Entity;
import pojahn.game.desktop.redguyruns.util.ResourceUtil;
import pojahn.game.entities.enemy.weapon.Bullet;
import pojahn.game.entities.enemy.weapon.Projectile;
import pojahn.game.entities.enemy.weapon.Weapon;
import pojahn.game.entities.main.GravityMan;
import pojahn.game.entities.movement.PathDrone;
import pojahn.game.entities.particle.Particle;
import pojahn.game.essentials.EntityBuilder;
import pojahn.game.essentials.Factory;
import pojahn.game.essentials.Hitbox;
import pojahn.game.essentials.Image2D;
import pojahn.game.essentials.ResourceManager;
import pojahn.game.essentials.stages.PixelBasedLevel;

import java.io.Serializable;
import java.util.stream.Stream;

public class PiranhaHell extends PixelBasedLevel {

    private Music music;
    private ResourceManager res;
    private GravityMan play;

    @Override
    public void init(final Serializable meta) throws Exception {
        res = new ResourceManager();
        res.loadContentFromDirectory(Gdx.files.internal("res/data"));
        res.loadContentFromDirectory(Gdx.files.internal("res/general"));
        res.loadContentFromDirectory(Gdx.files.internal("res/pihell"));
        res.loadAnimation(Gdx.files.internal("res/mtrace/flag"));
        getEngine().timeFont = res.getFont("sansserif32.fnt");

        createMap(res.getPixmap("pixmap.png"));
        Stream.of(res.getAnimation("main")).forEach(Image2D::createPixelData);
        Stream.of(res.getAnimation("dullplant")).forEach(Image2D::createPixelData);

        music = res.getMusic("music.ogg");
        music.setOnCompletionListener(music -> {
            music.play();
            music.setPosition(4.18f);
        });
        music.play();
    }

    @Override
    public void build() {
        //Main Character
        play = ResourceUtil.getGravityMan(res);
        play.move(145, 843);
        play.touch(2);
        add(play);

        //Background & Foreground
        add(new EntityBuilder().image(res.getImage("foreground.png")).zIndex(100).build());
        add(new EntityBuilder().image(res.getImage("background.png")).zIndex(-100).build());

        //Goal
        final Entity theFlag = new Entity();
        theFlag.move(1612, 785);
        theFlag.setImage(4, res.getAnimation("flag"));
        add(theFlag);

        //Beginning, bottom
        addReddie(1, 30);
        addReddie(2, 30);
        addReddie(3, 30);
        addReddie(6, 30);
        addReddie(7, 30);
        addReddie(8, 30);
        addReddie(9, 30);
        addReddie(10, 30);
        addReddie(12, 30);
        addReddie(13, 30);
        addReddie(14, 30);
        addReddie(16, 30);
        addReddie(17, 30);
        addReddie(18, 30);
        addReddie(19, 30);
        addReddie(21, 30);
        addReddie(22, 30);
        addReddie(23, 30);
        addReddie(24, 30);
        addReddie(25, 30);
        addReddie(27, 30);
        addReddie(28, 30);
        addReddie(29, 30);
        addReddie(30, 30);
        addReddie(31, 30);
        addReddie(32, 30);

        addReddie(33, 27);
        addReddie(34, 27);
        addReddie(30, 23);

        //Stairs
        addReddie(24, 11);
        addReddie(26, 13);
        addReddie(28, 15);
        addReddie(30, 17);

        //Hindrance room
        addWhitey(21, 6, false);
        addWhitey(17, 6, true);
        addReddie(15, 10);
        addReddie(14, 10);
        addReddie(9, 10);

        //Going Down
        addReddie(41, 6);
        addReddie(42, 6);
        addReddie(43, 6);
        addReddie(44, 6);

        addReddie(45, 10);
        addReddie(46, 10);
        addReddie(47, 10);

        addReddie(48, 13);
        addReddie(49, 13);
        addReddie(50, 13);
        addReddie(51, 13);

        addReddie(52, 16);
        addReddie(53, 16);
        addReddie(54, 16);
        addReddie(55, 16);
        addReddie(56, 16);
        addReddie(57, 16);
        addReddie(58, 16);
        addReddie(59, 16);
        addReddie(63, 16);
        addReddie(64, 16);
        addReddie(67, 16);
        addReddie(68, 16);
        addReddie(69, 16);
        addReddie(72, 16);
        addReddie(73, 16);
        addReddie(74, 16);

        //Whitey pipes
        addWhitey(77, 8, false);
        addWhitey(78, 8, false);
        addWhitey(79, 7, true);
        addWhitey(80, 7, true);
        addWhitey(81, 8, false);
        addWhitey(82, 8, false);
        addWhitey(83, 7, true);
        addWhitey(84, 7, true);
        addWhitey(85, 8, false);
        addWhitey(86, 8, false);
        addWhitey(87, 7, true);
        addWhitey(88, 7, true);
        addWhitey(89, 8, false);
        addWhitey(90, 8, false);

        //Later platforms
        addReddie(92, 20);
        addReddie(94, 23);
        addReddie(91, 26);

        //Bottom end
        for (int x = 52; x <= 99; x++) {
            if (x != 88)
                addReddie(x, 30);
        }

        //End, whites
        addWhitey(88, 28, false);
        addWhitey(83, 26, true);
        addWhitey(77, 24, false);
        addWhitey(72, 27, true);
        addWhitey(67, 26, false);

        addWhitey(62, 23, true);
        addWhitey(61, 23, true);

        addWhitey(55, 25, false);

        //Bigger plants(dull)
        final PathDrone dull = new PathDrone(2096, 384);
        dull.setImage(8, res.getAnimation("dullplant"));
        dull.setMoveSpeed(2);
        dull.appendPath(dull.x(), dull.y(), 100, false, null);
        dull.appendPath(dull.x(), dull.y() + dull.height() + 10, 100, false, null);
        dull.addEvent(Factory.hitMain(dull, play, -1));
        dull.setHitbox(Hitbox.PIXEL);
        add(dull);

        //Firing plant
        final PathDrone plantBody = new PathDrone(1840, 384);
        plantBody.setImage(res.getImage("body.png"));
        plantBody.setMoveSpeed(1.5f);
        plantBody.appendPath(1840, 384, 100, false, null);
        plantBody.appendPath(1840, 384 - plantBody.height() - 40, 100, false, null);
        add(plantBody);

        final Projectile fireBall = new Bullet(0, 0, play);
        fireBall.setImage(res.getImage("fireball.png"));
        fireBall.setQuickCollision(true);
        fireBall.addEvent(() -> fireBall.rotate(5));
        fireBall.setMoveSpeed(3);
        fireBall.setGunfire(Particle.fromSound(res.getSound("fire.wav")));

        final Weapon plantHead = new Weapon(1840, 384, 1, 1, 90, play);
        plantHead.setProjectile(fireBall);
        plantHead.setRotateWhileRecover(true);
        plantHead.setRotationSpeed(.1f);
        plantHead.addEvent(Factory.follow(plantBody, plantHead, 0, plantBody.height()));
        plantHead.setImage(res.getImage("head.png"));
        plantHead.setFrontFire(true);
        plantHead.zIndex(99);
        add(plantHead);

        play.setActionEvent(caller -> {
            if (caller.isCloneOf(fireBall))
                play.touch(-1);
        });
    }

    private void addReddie(final int x, final int y) {
        final Entity redPlant = new Entity();
        redPlant.move(x * 32 + 9, y * 32 + 16);
        redPlant.setImage(6, res.getAnimation("pih1"));
        redPlant.addEvent(Factory.hitMain(redPlant, play, -1));

        add(redPlant);
    }

    private void addWhitey(final int x, final int y, final boolean startHidden) {
        final PathDrone whitePlant = new PathDrone(0, 0);
        whitePlant.setImage(7, res.getAnimation("pih2"));
        whitePlant.addEvent(Factory.hitMain(whitePlant, play, -1));
        whitePlant.setMoveSpeed(.7f);

        if (!startHidden) {
            final int myX = x * 32 + 9;
            final int myY = y * 32 + 16;

            whitePlant.move(myX, myY);
            whitePlant.appendPath(myX, myY, 70, false, null);
            whitePlant.appendPath(myX, myY + 20, 70, false, null);
        } else {
            final int myX = x * 32 + 9;
            final int myY = y * 32 + 16 + 20;

            whitePlant.move(myX, myY);
            whitePlant.appendPath(myX, myY, 70, false, null);
            whitePlant.appendPath(myX, myY - 20, 70, false, null);
        }

        add(whitePlant);
    }

    @Override
    public void dispose() {
        res.disposeAll();
    }

    @Override
    public String getLevelName() {
        return "Piranha Hell";
    }

    @Override
    public Music getStageMusic() {
        return music;
    }
}
