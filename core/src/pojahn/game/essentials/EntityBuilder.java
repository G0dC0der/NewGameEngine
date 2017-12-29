package pojahn.game.essentials;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import pojahn.game.core.Entity;
import pojahn.game.events.Event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class EntityBuilder {

    private int zIndex;
    private Animation<Image2D> image;
    private Hitbox hitbox;
    private float x, y, offsetX, offsetY, alpha, rotation;
    private Float width, height;
    private Boolean flipX, flipY;
    private List<Event> events;
    private List<Function<Entity, Event>> eventFunctions = new ArrayList<>();

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

    public EntityBuilder flipX(final boolean flipX) {
        this.flipX = flipX;
        return this;
    }

    public EntityBuilder flipY(final boolean flipY) {
        this.flipY = flipY;
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

    public EntityBuilder event(final Function<Entity, Event> eventFunction) {
        eventFunctions.add(eventFunction);
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
                final Class<?>[] classes = Stream.of(args).map(Object::getClass).toArray(Class[]::new);
                entity = clazz.getDeclaredConstructor(classes).newInstance(args);
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
        dest.offsetX = offsetX;
        dest.offsetY = offsetY;
        dest.tint.a = alpha;
        dest.setRotation(rotation);
        dest.zIndex(zIndex);
        dest.setHitbox(hitbox);
        dest.setIdentifier("Entity Builder" + MathUtils.random());
        if (image != null)
            dest.setImage(image);
        if (width != null)
            dest.bounds.size.width = width;
        if (height != null)
            dest.bounds.size.height = height;
        if (flipX != null)
            dest.flipX = flipX;
        if (flipY != null)
            dest.flipY = flipY;

        events.forEach(dest::addEvent);
        eventFunctions.forEach(eventFunction -> dest.addEvent(eventFunction.apply(dest)));
    }
}