package pojahn.game.essentials;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import pojahn.game.core.Entity;
import pojahn.game.events.Event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EntityBuilder {

    private int zIndex;
    private Animation<Image2D> image;
    private Hitbox hitbox;
    private float x, y, width, height, offsetX, offsetY, alpha, rotation;
    private List<Event> events;

    public static EntityBuilder fromVector(final Vector2 vector2) {
        return new EntityBuilder().move(vector2);
    }

    public EntityBuilder() {
        events = new ArrayList<>();
        alpha = 1;
        hitbox = Hitbox.RECTANGLE;
    }

    public EntityBuilder zIndex(final int zIndex) {
        this.zIndex = zIndex;
        return this;
    }

    public EntityBuilder image(final Image2D... image) {
        image(3, image);
        return this;
    }

    public EntityBuilder image(final int speed, final Image2D... image) {
        image(new Animation<>(speed, image));
        return this;
    }

    public EntityBuilder image(final Animation<Image2D> image) {
        this.image = image;
        return this;
    }

    public EntityBuilder x(final float x) {
        this.x = x;
        return this;
    }

    public EntityBuilder y(final float y) {
        this.y = y;
        return this;
    }

    public EntityBuilder move(final float x, final float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public EntityBuilder move(final Vector2 pos) {
        this.x = pos.x;
        this.y = pos.y;
        return this;
    }

    public EntityBuilder width(final float width) {
        this.width = width;
        return this;
    }

    public EntityBuilder height(final float heigh) {
        this.height = heigh;
        return this;
    }

    public EntityBuilder offsetX(final float offsetX) {
        this.offsetX = offsetX;
        return this;
    }

    public EntityBuilder offsetY(final float offsetY) {
        this.offsetY = offsetY;
        return this;
    }

    public EntityBuilder alpha(final float alpha) {
        this.alpha = alpha;
        return this;
    }

    public EntityBuilder rotation(final float rotation) {
        this.rotation = rotation;
        return this;
    }

    public EntityBuilder events(final Event... events) {
        this.events.addAll(Arrays.asList(events));
        return this;
    }

    public EntityBuilder hitbox(final Hitbox hitbox) {
        this.hitbox = hitbox;
        return this;
    }

    public Entity build() {
        final Entity entity = new Entity();
        pasteData(entity);
        return entity;
    }

    public <T extends Entity> T build(final Class<T> clazz, final Object... args) {
        final T entity;
        try {
            if (args.length == 0)
                entity = clazz.newInstance();
            else {
                final List<Class> clazzez = Stream.of(args).map(Object::getClass).collect(Collectors.toList());
                entity = clazz.getDeclaredConstructor(clazzez.toArray(new Class[clazzez.size()])).newInstance(args);
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        pasteData(entity);
        return entity;
    }

    private void pasteData(final Entity dest) {
        dest.bounds.pos.x = x;
        dest.bounds.pos.y = y;
        dest.bounds.size.width = width;
        dest.bounds.size.height = height;
        dest.offsetX = offsetX;
        dest.offsetY = offsetY;
        dest.tint.a = alpha;
        dest.setRotation(rotation);
        dest.zIndex(zIndex);
        dest.setHitbox(hitbox);
        dest.setIdentifier("Entity Builder" + MathUtils.random());
        if (image != null)
            dest.setImage(image);

        events.forEach(dest::addEvent);
    }
}