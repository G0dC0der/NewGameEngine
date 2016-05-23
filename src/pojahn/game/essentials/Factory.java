package pojahn.game.essentials;

import com.badlogic.gdx.audio.Music;
import pojahn.game.core.Collisions;
import pojahn.game.core.Entity;
import pojahn.game.core.Level.Tile;
import pojahn.game.core.Level.TileLayer;
import pojahn.game.core.MobileEntity;
import pojahn.game.core.PlayableEntity;
import pojahn.game.events.Event;
import pojahn.game.events.TileEvent;
import pojahn.lang.Int32;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class Factory {

    public static Event spazz(Entity entity, float tolerance, int freq) {
        Int32 counter = new Int32();
        return () -> {
            if (++counter.value % freq == 0) {
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

    public static Event solidify(Entity entity) {
        Image2D img = entity.getImage().getArray()[0];
        TileLayer tileLayer = Utils.from(img);
        boolean[] once = new boolean[1];
        return () -> {
            if(!once[0]) {
                once[0] = true;
                entity.getLevel().addTileLayer(tileLayer);
            }
            tileLayer.setPosition((int)entity.x(), (int)entity.y());
        };
    }

    /**
     * Rotate the given unit towards its given direction.
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

    public static Event walkingSounds(MobileEntity target, Music start, Music end, Music... loop) {
        return null; //TODO: Implement
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
