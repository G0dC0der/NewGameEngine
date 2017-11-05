package pojahn.game.essentials;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import pojahn.game.core.BaseLogic;
import pojahn.game.core.Entity;
import pojahn.game.core.Level.Tile;
import pojahn.game.core.Level.TileLayer;
import pojahn.game.core.PlayableEntity;
import pojahn.game.entities.main.GravityMan;
import pojahn.game.events.Event;
import pojahn.game.events.TileEvent;
import pojahn.lang.Bool;
import pojahn.lang.Int32;

import java.util.ArrayList;
import java.util.List;

public class Factory {

    public static Event fadeIn(final Entity target, final float speed) {
        final Bool bool = new Bool();
        return () -> {
            if (!bool.value && target.tint.a < 1.0f) {
                target.tint.a = Math.min(1, target.tint.a += speed);
            } else {
                bool.value = true;
            }
        };
    }

    public static Event repeatSound(final Entity emitter, final Sound sound, final int delay) {
        final Int32 c = new Int32();
        return () -> {
            if (++c.value % delay == 0) {
                emitter.sounds.play(sound);
            }
        };
    }

    public static Entity tuneUp(final Music music, final float power) {
        return tuneUp(music, power, 1.0f);
    }

    public static Entity tuneUp(final Music music, final float power, final float targetVolume) {
        return new Entity() {
            @Override
            public void logistics() {
                music.setVolume(Math.min(targetVolume, music.getVolume() + power));
                if (music.getVolume() >= targetVolume)
                    die();
            }
        };
    }

    public static Entity tuneDown(final Music music, final float power) {
        return tuneDown(music, power, 0);
    }

    public static Entity tuneDown(final Music music, final float power, final float targetVolume) {
        return new Entity() {
            @Override
            public void logistics() {
                music.setVolume(Math.max(targetVolume, music.getVolume() - power));
                if (music.getVolume() <= targetVolume)
                    die();
            }
        };
    }

    public static Event spazz(final Entity entity, final float tolerance, final int freq) {
        final Int32 counter = new Int32();
        return () -> {
            if (freq <= 0 || ++counter.value % freq == 0) {
                entity.offsetX = MathUtils.random(-tolerance, tolerance);
                entity.offsetY = MathUtils.random(-tolerance, tolerance);
            }
        };
    }

    public static Entity drawText(final HUDMessage message, final BitmapFont font) {
        return new Entity() {
            {
                zIndex(Integer.MAX_VALUE);
            }

            @Override
            public void render(final SpriteBatch batch) {
                message.draw(batch, font);
            }
        };
    }

    public static Entity drawCenteredText(final HUDMessage message, final BitmapFont font) {
        return new Entity() {
            {
                zIndex(Integer.MAX_VALUE);
            }

            @Override
            public void render(final SpriteBatch batch) {
                getEngine().hudCamera();
                message.draw(batch, font);
                getEngine().gameCamera();
            }
        };
    }

    public static Event solidify(final Entity entity) {
        final Image2D img = entity.getImage().getArray()[0];
        final TileLayer tileLayer = Utils.fromImage(img);
        final boolean[] once = new boolean[1];
        return () -> {
            if (!once[0]) {
                once[0] = true;
                entity.getLevel().addTileLayer(tileLayer);
            }
            tileLayer.setPosition((int) entity.x(), (int) entity.y());
        };
    }

    public static Event follow(final Entity src, final Entity tail, final float offsetX, final float offsetY) {
        return () -> tail.move(src.bounds.pos.x + offsetX, src.bounds.pos.y + offsetY);
    }

    public static Event follow(final Entity src, final Entity tail) {
        return follow(src, tail, 0, 0);
    }

    public static TileEvent crushable(final PlayableEntity entity) {
        return (tile) -> {
            if (tile == Tile.SOLID)
                entity.setState(Vitality.DEAD);
        };
    }

    public static TileEvent completable(final PlayableEntity entity) {
        return (tile) -> {
            if (tile == Tile.GOAL)
                entity.setState(Vitality.COMPLETED);
        };
    }

    public static TileEvent hurtable(final PlayableEntity entity, final int damage) {
        return (tile) -> {
            if (tile == Tile.LETHAL)
                entity.touch(damage);
        };
    }

    public static Event hitMain(final Entity enemy, final PlayableEntity play, final int hp) {
        return () -> {
            if (enemy.collidesWith(play))
                play.touch(hp);
        };
    }

    public static Event keepGravityManInBounds(final GravityMan gravityMan) {
        return () -> {
            if (gravityMan.x() < 0) {
                gravityMan.bounds.pos.x = 0;
                gravityMan.vel.x = 0;
            } else if (gravityMan.x() + gravityMan.width() > gravityMan.getLevel().getWidth()) {
                gravityMan.bounds.pos.x = gravityMan.getLevel().getWidth() - gravityMan.width();
                gravityMan.vel.x = 0;
            }

            if (gravityMan.y() < 0) {
                gravityMan.bounds.pos.y = 0;
                gravityMan.vel.y = 0;
            } else if (gravityMan.y() + gravityMan.height() > gravityMan.getLevel().getHeight()) {
                gravityMan.bounds.pos.y = gravityMan.getLevel().getHeight() - gravityMan.height();
                gravityMan.vel.y = 0;
            }
        };
    }

    /**
     * Fade to the room music if inside the given rectangle. When a {@code listener} is colliding with the given rectangle, {@code roomMusic} will start fade in and {@code outsideMusic} out.
     *
     * @param room         The room where {@code roomMusic} is played.
     * @param roomMusic    The music to play in the room.
     * @param outsideMusic The music thats played outside the room. Usually the stage music. Null is accepted.
     * @param fadeSpeed    The speed to fade in/out.
     * @param maxVolume    The max volume of the songs when fading.
     * @param listeners    The entities interacting with this event.
     * @return The event.
     */
    public static Event roomMusic(final Rectangle room, final Music roomMusic, final Music outsideMusic, final double fadeSpeed, final double maxVolume, final Entity... listeners) {
        return () -> {
            boolean oneColliding = false;

            for (final Entity listener : listeners) {
                if (BaseLogic.rectanglesCollide(listener.x(), listener.y(), listener.width(), listener.height(), room.x, room.y, room.width, room.height)) {
                    roomMusic.setVolume((float) Math.min(roomMusic.getVolume() + fadeSpeed, maxVolume));

                    if (outsideMusic != null) {
                        outsideMusic.setVolume((float) Math.max(outsideMusic.getVolume() - fadeSpeed, 0));
                    }

                    oneColliding = true;
                    break;
                }
            }

            if (!oneColliding) {
                roomMusic.setVolume((float) Math.max(roomMusic.getVolume() - fadeSpeed, 0));

                if (outsideMusic != null) {
                    outsideMusic.setVolume((float) Math.min(outsideMusic.getVolume() + fadeSpeed, maxVolume));
                }
            }
        };
    }

    public static LaserBeam dottedLaser(final Animation<Image2D> dotImage, final float size) {
        return new LaserBeam() {

            List<Task> tasks = new ArrayList<>();

            @Override
            public void fireAt(final float srcX, final float srcY, final float destX, final float destY, final int active) {
                tasks.add(new Task(srcX, srcY, destX, destY, active));
            }

            @Override
            public void drawLasers(final SpriteBatch batch) {
                if (dotImage.hasEnded() && !dotImage.isLooping())
                    dotImage.reset();

                int size = tasks.size();
                for (int i = 0; i < size; i++) {
                    final Task t = tasks.get(i);

                    if (0 >= t.active--) {
                        tasks.remove(t);
                        size--;
                    }

                    final Image2D img = dotImage.getObject();
                    final Vector2 start = new Vector2(t.srcX, t.srcY);
                    final Vector2 end = new Vector2(t.destX, t.destY);
                    final float dist = (float) BaseLogic.distance(t.srcX, t.srcY, t.destX, t.destY);
                    final float links = dist / size;

                    for (int j = 0; j < links; j++) {
                        final Vector2 pos = start.cpy().lerp(end, i / links);
                        batch.draw(img, pos.x, pos.y);
                    }
                }
            }
        };
    }

    public static LaserBeam threeStageLaser(final Animation<Image2D> laserBegin, final Animation<Image2D> laserBeam, final Animation<Image2D> laserImpact) {
        return new LaserBeam() {
            List<Task> tasks = new ArrayList<>();

            @Override
            public void fireAt(final float srcX, final float srcY, final float destX, final float destY, final int active) {
                tasks.add(new Task(srcX, srcY, destX, destY, active));
            }

            @Override
            public void drawLasers(final SpriteBatch b) {
                int size = tasks.size();
                for (int i = 0; i < size; i++) {
                    final Task t = tasks.get(i);

                    if (0 >= t.active--) {
                        tasks.remove(t);
                        size--;
                        continue;
                    }

                    final float angle = (float) BaseLogic.getAngle(t.srcX, t.srcY, t.destX, t.destY);

                    if (laserBeam != null) {
                        final Image2D beam = laserBeam.getObject();
                        final float dx = (float) (beam.getHeight() / 2 * Math.cos(Math.toRadians(angle - 90)));
                        final float dy = (float) (beam.getHeight() / 2 * Math.sin(Math.toRadians(angle - 90)));
                        final float width = (float) BaseLogic.distance(t.srcX + dx, t.srcY + dy, t.destX, t.destY);

                        b.draw(beam, t.srcX + dx, t.srcY + dy, 0, 0, width, beam.getHeight(), 1, 1, angle, 0, 0,
                                (int) width, (int) beam.getHeight(), false, true);
                    }

                    if (laserImpact != null) {
                        final Image2D exp = laserImpact.getObject();
                        final float halfWidth = exp.getWidth() / 2;
                        final float halfHeight = exp.getHeight() / 2;
                        b.draw(exp, t.destX - halfWidth, t.destY - halfHeight, halfWidth, halfHeight, exp.getWidth(),
                                exp.getHeight(), 1, 1, angle, 0, 0, (int) exp.getWidth(), (int) exp.getHeight(), false, true);
                    }

                    if (laserBegin != null) {
                        final Image2D begin = laserBegin.getObject();
                        final float halfWidth = begin.getWidth() / 2;
                        final float halfHeight = begin.getHeight() / 2;
                        b.draw(begin, t.srcX - halfWidth, t.srcY - halfHeight, halfHeight, halfHeight, begin.getWidth(),
                                begin.getHeight(), 1, 1, angle, 0, 0, (int) begin.getWidth(), (int) begin.getHeight(), false, true);
                    }
                }
            }
        };
    }
}
