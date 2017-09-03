package pojahn.game.essentials;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import pojahn.game.core.Collisions;
import pojahn.game.core.Entity;
import pojahn.game.core.Level;
import pojahn.game.core.Level.Tile;
import pojahn.game.core.Level.TileLayer;
import pojahn.game.core.MobileEntity;
import pojahn.game.core.PlayableEntity;
import pojahn.game.entities.mains.GravityMan;
import pojahn.game.events.Event;
import pojahn.game.events.TileEvent;
import pojahn.lang.Bool;
import pojahn.lang.Int32;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Factory {

    public static Event preventHorizontalOverlap(GravityMan man, Level level) {
        return () -> {
            int x = (int) man.x();
            int right = (int) (x + man.width());

            if (x <= 0 && man.getKeysDown().left)
                man.vel.x = 0;
            else if (right >= level.getWidth() && man.getKeysDown().right) {
                man.vel.x = 0;
                man.bounds.pos.x = level.getWidth() - man.width();
            }
        };
    }

    public static Event fadeIn(Entity target, float speed) {
        Bool bool = new Bool();
        return () -> {
            if (!bool.value && target.tint.a < 1.0f) {
                target.tint.a = Math.min(1, target.tint.a += speed);
            } else {
                bool.value = true;
            }
        };
    }

    public static Event repeatSound(Entity emitter, Sound sound, int delay) {
        Int32 c = new Int32();
        return () -> {
            if (++c.value % delay == 0) {
                sound.play(emitter.sounds.calc());
            }
        };
    }

    public static Entity construct(TiledMap tiledMap) {
        return new Entity() {
            TiledMapRenderer tiledMapRenderer;
            List<TiledMapTileLayer> layers = new ArrayList<>();

            {
                tiledMap.getLayers().forEach(mapLayer -> layers.add((TiledMapTileLayer) mapLayer));
                Collections.reverse(layers);
            }

            @Override
            public void render(SpriteBatch batch) {
                if (tiledMapRenderer == null)
                    tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, batch);

                if (getRotation() != 0 || flipX || flipY)
                    throw new RuntimeException("Rotation and flip are not supported for tile based image.");

                Color color = batch.getColor();
                batch.setColor(tint);

                OrthographicCamera cam = getEngine().getGameCamera();
                cam.update();
                tiledMapRenderer.setView(cam);
                layers.forEach(tiledMapRenderer::renderTileLayer);

                batch.setColor(color);
            }
        };
    }

    public static Entity tuneUp(Music music, float power) {
        return tuneUp(music, power, 1.0f);
    }

    public static Entity tuneUp(Music music, float power, float targetVolume) {
        return new Entity() {
            @Override
            public void logistics() {
                music.setVolume(Math.min(targetVolume, music.getVolume() + power));
                if (music.getVolume() >= targetVolume)
                    die();
            }
        };
    }

    public static Entity tuneDown(Music music, float power) {
        return tuneDown(music, power, 0);
    }

    public static Entity tuneDown(Music music, float power, float targetVolume) {
        return new Entity() {
            @Override
            public void logistics() {
                music.setVolume(Math.max(targetVolume, music.getVolume() - power));
                if (music.getVolume() <= targetVolume)
                    die();
            }
        };
    }

    public static Event spazz(Entity entity, float tolerance, int freq) {
        Int32 counter = new Int32();
        return () -> {
            if (freq <= 0 || ++counter.value % freq == 0) {
                entity.offsetX = MathUtils.random(-tolerance, tolerance);
                entity.offsetY = MathUtils.random(-tolerance, tolerance);
            }
        };
    }

    public static Entity drawText(HUDMessage message, BitmapFont font) {
        return new Entity() {
            {
                zIndex(Integer.MAX_VALUE);
            }

            @Override
            public void render(SpriteBatch batch) {
                message.draw(batch, font);
            }
        };
    }

    public static Entity drawCenteredText(HUDMessage message, BitmapFont font) {
        return new Entity() {
            {
                zIndex(Integer.MAX_VALUE);
            }

            @Override
            public void render(SpriteBatch batch) {
                getEngine().hudCamera();
                message.draw(batch, font);
                getEngine().gameCamera();
            }
        };
    }

    public static Event solidify(Entity entity) {
        Image2D img = entity.getImage().getArray()[0];
        TileLayer tileLayer = Utils.from(img);
        boolean[] once = new boolean[1];
        return () -> {
            if (!once[0]) {
                once[0] = true;
                entity.getLevel().addTileLayer(tileLayer);
            }
            tileLayer.setPosition((int) entity.x(), (int) entity.y());
        };
    }

    /**
     * Rotate the given unit towards its current direction.
     */
    public static Event stareAt(MobileEntity walker) {
        return () -> {
            Vector2 center = walker.getCenterCord();
            walker.setRotation((float) Collisions.getAngle(walker.prevX(), walker.prevY(), center.x, center.y));
        };
    }

    public static Event follow(Entity src, Entity tail, float offsetX, float offsetY) {
        return () -> tail.move(src.bounds.pos.x + offsetX, src.bounds.pos.y + offsetY);
    }

    public static Event follow(Entity src, Entity tail) {
        return follow(src, tail, 0, 0);
    }

    public static TileEvent crushable(PlayableEntity entity) {
        return (tile) -> {
            if (tile == Tile.SOLID)
                entity.setState(Vitality.DEAD);
        };
    }

    public static TileEvent completable(PlayableEntity entity) {
        return (tile) -> {
            if (tile == Tile.GOAL)
                entity.setState(Vitality.COMPLETED);
        };
    }

    public static TileEvent hurtable(PlayableEntity entity, int damage) {
        return (tile) -> {
            if (tile == Tile.LETHAL)
                entity.touch(damage);
        };
    }

    public static Event hitMain(Entity enemy, PlayableEntity play, int hp) {
        return () -> {
            if (enemy.collidesWith(play))
                play.touch(hp);
        };
    }

    public static Event keepInBounds(Entity entity) {
        return () -> {
            entity.bounds.pos.x = Math.max(0, Math.min(entity.x(), entity.getLevel().getWidth()));
            entity.bounds.pos.y = Math.max(0, Math.min(entity.y(), entity.getLevel().getHeight()));
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
    public static Event roomMusic(Rectangle room, Music roomMusic, Music outsideMusic, double fadeSpeed, double maxVolume, Entity... listeners) {
        return () -> {
            boolean oneColliding = false;

            for (Entity listener : listeners) {
                if (Collisions.rectanglesCollide(listener.x(), listener.y(), listener.width(), listener.height(), room.x, room.y, room.width, room.height)) {
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

    public static LaserBeam dottedLaser(Animation<Image2D> dotImage, float size) {
        return new LaserBeam() {

            List<Task> tasks = new ArrayList<>();

            @Override
            public void fireAt(float srcX, float srcY, float destX, float destY, int active) {
                tasks.add(new Task(srcX, srcY, destX, destY, active));
            }

            @Override
            public void drawLasers(SpriteBatch batch) {
                if (dotImage.hasEnded() && !dotImage.isLooping())
                    dotImage.reset();

                int size = tasks.size();
                for (int i = 0; i < size; i++) {
                    final Task t = tasks.get(i);

                    if (0 >= t.active--) {
                        tasks.remove(t);
                        size--;
                    }

                    Image2D img = dotImage.getObject();
                    Vector2 start = new Vector2(t.srcX, t.srcY);
                    Vector2 end = new Vector2(t.destX, t.destY);
                    float dist = (float) Collisions.distance(t.srcX, t.srcY, t.destX, t.destY);
                    float links = dist / size;

                    for (int j = 0; j < links; j++) {
                        Vector2 pos = start.cpy().lerp(end, i / links);
                        batch.draw(img, pos.x, pos.y);
                    }
                }
            }
        };
    }

    public static LaserBeam threeStageLaser(Animation<Image2D> laserBegin, Animation<Image2D> laserBeam, Animation<Image2D> laserImpact) {
        return new LaserBeam() {
            List<Task> tasks = new ArrayList<>();

            @Override
            public void fireAt(float srcX, float srcY, float destX, float destY, int active) {
                tasks.add(new Task(srcX, srcY, destX, destY, active));
            }

            @Override
            public void drawLasers(SpriteBatch b) {
                int size = tasks.size();
                for (int i = 0; i < size; i++) {
                    final Task t = tasks.get(i);

                    if (0 >= t.active--) {
                        tasks.remove(t);
                        size--;
                        continue;
                    }

                    final float angle = (float) Collisions.getAngle(t.srcX, t.srcY, t.destX, t.destY);

                    if (laserBeam != null) {
                        Image2D beam = laserBeam.getObject();
                        float dx = (float) (beam.getHeight() / 2 * Math.cos(Math.toRadians(angle - 90)));
                        float dy = (float) (beam.getHeight() / 2 * Math.sin(Math.toRadians(angle - 90)));
                        float width = (float) Collisions.distance(t.srcX + dx, t.srcY + dy, t.destX, t.destY);

                        b.draw(beam, t.srcX + dx, t.srcY + dy, 0, 0, width, beam.getHeight(), 1, 1, angle, 0, 0,
                                (int) width, (int) beam.getHeight(), false, true);
                    }

                    if (laserImpact != null) {
                        Image2D exp = laserImpact.getObject();
                        float halfWidth = exp.getWidth() / 2;
                        float halfHeight = exp.getHeight() / 2;
                        b.draw(exp, t.destX - halfWidth, t.destY - halfHeight, halfWidth, halfHeight, exp.getWidth(),
                                exp.getHeight(), 1, 1, angle, 0, 0, (int) exp.getWidth(), (int) exp.getHeight(), false, true);
                    }

                    if (laserBegin != null) {
                        Image2D begin = laserBegin.getObject();
                        float halfWidth = begin.getWidth() / 2;
                        float halfHeight = begin.getHeight() / 2;
                        b.draw(begin, t.srcX - halfWidth, t.srcY - halfHeight, halfHeight, halfHeight, begin.getWidth(),
                                begin.getHeight(), 1, 1, angle, 0, 0, (int) begin.getWidth(), (int) begin.getHeight(), false, true);
                    }
                }
            }
        };
    }
}
