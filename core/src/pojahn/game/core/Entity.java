package pojahn.game.core;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import pojahn.game.essentials.Animation;
import pojahn.game.essentials.Hitbox;
import pojahn.game.essentials.Image2D;
import pojahn.game.essentials.SoundEmitter;
import pojahn.game.essentials.geom.Bounds;
import pojahn.game.events.ActionEvent;
import pojahn.game.events.ChainEvent;
import pojahn.game.events.CloneEvent;
import pojahn.game.events.Event;

import java.util.ArrayList;
import java.util.List;

import static pojahn.game.core.BaseLogic.buildMatrix;
import static pojahn.game.core.BaseLogic.circleRectangleCollide;
import static pojahn.game.core.BaseLogic.circleVsCircle;
import static pojahn.game.core.BaseLogic.getBoundingBox;
import static pojahn.game.core.BaseLogic.pixelPerfect;
import static pojahn.game.core.BaseLogic.pixelPerfectRotation;
import static pojahn.game.core.BaseLogic.rectanglesCollide;
import static pojahn.game.core.BaseLogic.rotatedRectanglesCollide;

public class Entity {

    public final Bounds bounds;
    public final SoundEmitter sounds;
    public final Color tint;
    public float offsetX, offsetY, scaleX, scaleY;
    public boolean flipX, flipY;

    protected CloneEvent cloneEvent;

    Level level;
    Engine engine;
    boolean present;

    private String identifier;
    private List<Event> events, deleteEvents;
    private Animation<Image2D> image;
    private Entity originator;
    private Hitbox hitbox;
    private ActionEvent actionEvent;
    private boolean quickCollision, visible, active;
    private int zIndex;

    public Entity() {
        bounds = new Bounds();
        sounds = new SoundEmitter(this);
        tint = Color.valueOf("fffffffe");
        offsetX = offsetY = 1;
        scaleX = scaleY = 1.0f;
        active = true;
        visible = true;
        events = new ArrayList<>();
        hitbox = Hitbox.RECTANGLE;
        deleteEvents = new ArrayList<>();
    }

    public void logistics() {
    }

    public Entity getClone() {
        final Entity clone = new Entity();
        copyData(clone);
        if (cloneEvent != null)
            cloneEvent.handleClonded(clone);

        return clone;
    }

    public void setIdentifier(final String identifier) {
        if (this.identifier != null || present) {
            throw new IllegalArgumentException("Can only set identifier once before it's present.");
        }
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setImage(final Image2D... images) {
        setImage(3, images);
    }

    public void setImage(final int speed, final Image2D... images) {
        setImage(new Animation<>(speed, images));
    }

    public void setImage(final Animation<Image2D> image) {
        this.image = image;

        visible = true;
        bounds.size.width = image.getArray()[0].getWidth();
        bounds.size.height = image.getArray()[0].getHeight();
    }

    public Animation<Image2D> getImage() {
        return image;
    }

    public Entity move(final float x, final float y) {
        bounds.pos.x = x;
        bounds.pos.y = y;
        return this;
    }

    public Entity move(final Vector2 loc) {
        bounds.pos.x = loc.x;
        bounds.pos.y = loc.y;
        return this;
    }

    public Entity center(final Entity target) {
        bounds.pos.x = target.centerX() - (width() / 2);
        bounds.pos.y = target.centerY() - (height() / 2);
        return this;
    }

    public void nudge(final float relX, final float relY) {
        bounds.pos.x += relX;
        bounds.pos.y += relY;
    }

    public void init() {
    }

    public void dispose() {
    }

    public void render(final SpriteBatch batch) {
        basicRender(batch, nextImage());
    }

    public Engine getEngine() {
        return engine;
    }

    public final void zIndex(final int zIndex) {
        this.zIndex = zIndex;
        if (level != null)
            level.sort = true;
    }

    public int getZIndex() {
        return zIndex;
    }

    public Level getLevel() {
        return level;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(final boolean visible) {
        this.visible = visible;
    }

    public boolean isActive() {
        return active;
    }

    public void activate(final boolean activate) {
        this.active = activate;
    }

    public void addEvent(final Event event) {
        events.add(event);
    }

    public void removeEvent(final Event event) {
        deleteEvents.add(event);
    }

    public boolean collidesWith(final Entity entity) {
        final boolean rotated1 = bounds.rotation != 0 && !quickCollision;
        final boolean rotated2 = entity.bounds.rotation != 0 && !entity.quickCollision;

        if (hitbox == Hitbox.NONE || entity.hitbox == Hitbox.NONE) {
            return false;
        } else if (hitbox == Hitbox.RECTANGLE && entity.hitbox == Hitbox.RECTANGLE) {
            return (rotated1 || rotated2) ? rotatedRectanglesCollide(bounds, entity.bounds) : rectanglesCollide(bounds.toRectangle(), entity.bounds.toRectangle());
        } else if ((hitbox == Hitbox.RECTANGLE && entity.hitbox == Hitbox.CIRCLE) || (hitbox == Hitbox.CIRCLE && entity.hitbox == Hitbox.RECTANGLE)) {
            final Entity rectangle = hitbox == Hitbox.RECTANGLE ? this : entity;
            final Entity circle = hitbox == Hitbox.CIRCLE ? this : entity;

            if (rectangle.getRotation() != 0)
                throw new RuntimeException("No collision method for rotated rectangle vs circle.");

            return circleRectangleCollide(circle.bounds.toCircle(), rectangle.bounds.toRectangle());
        } else if (hitbox == Hitbox.CIRCLE && entity.hitbox == Hitbox.CIRCLE) {
            return circleVsCircle(bounds.toCircle(), entity.bounds.toCircle());
        } else if (hitbox == Hitbox.PIXEL || entity.hitbox == Hitbox.PIXEL) {
            if (rotated1 || rotated2)
                return rectanglesCollide(getBoundingBox(bounds), getBoundingBox(entity.bounds)) &&
                        pixelPerfectRotation(buildMatrix(this), getImage().getCurrentObject(), buildMatrix(entity), entity.getImage().getCurrentObject());

            return rectanglesCollide(bounds.toRectangle(), entity.bounds.toRectangle()) &&
                    pixelPerfect(
                            bounds.toRectangle(), getImage().getCurrentObject(), flipX, flipY,
                            entity.bounds.toRectangle(), entity.getImage().getCurrentObject(), entity.flipX, entity.flipY);
        }

        throw new IllegalStateException("No proper collision handling methods found.");
    }

    public Vector2 getCenterCord() {
        return bounds.center();
    }

    public Vector2 getPos() {
        return bounds.pos.cpy();
    }

    public float getRotation() {
        return bounds.rotation;
    }

    public void setRotation(final float rotation) {
        bounds.rotation = rotation;
    }

    public void rotate(final float amount) {
        bounds.rotation += amount;
    }

    public float x() {
        return bounds.pos.x;
    }

    public float y() {
        return bounds.pos.y;
    }

    public float width() {
        return bounds.size.width;
    }

    public float height() {
        return bounds.size.height;
    }

    public float halfWidth() {
        return bounds.size.width / 2;
    }

    public float halfHeight() {
        return bounds.size.height / 2;
    }

    public float centerX() {
        return bounds.pos.x + bounds.size.width / 2;
    }

    public float centerY() {
        return bounds.pos.y + bounds.size.height / 2;
    }

    public Hitbox getHitbox() {
        return hitbox;
    }

    public void setHitbox(final Hitbox hitbox) {
        this.hitbox = hitbox;
    }

    public float dist(final Entity entity) {
        return (float) BaseLogic.distance(x(), y(), entity.x(), entity.y());
    }

    public boolean near(final Entity entity, final float epsilon) {
        return epsilon >= dist(entity);
    }

    public void setQuickCollision(final boolean quickCollision) {
        this.quickCollision = quickCollision;
    }

    public void expand(final float amount) {
        bounds.pos.x -= amount;
        bounds.pos.y -= amount;
        bounds.size.width += amount * 2;
        bounds.size.height += amount * 2;
    }

    public void contract(final float amount) {
        bounds.pos.x += amount;
        bounds.pos.y += amount;
        bounds.size.width -= amount * 2;
        bounds.size.height -= amount * 2;
    }

    public boolean available() {
        return present;
    }

    public Image2D nextImage() {
        return visible && image != null ? image.getObject() : null;
    }

    public boolean isCloneOf(final Entity originator) {
        return this.originator == originator;
    }

    public void setCloneEvent(final CloneEvent cloneEvent) {
        this.cloneEvent = cloneEvent;
    }

    public boolean canSee(final Entity target) {
        return target != null && !BaseLogic.solidSpace(centerX(), centerY(), target.centerX(), target.centerY(), getLevel());
    }

    public Vector2 getFrontPosition() {
        float locX = bounds.pos.x + width() / 2;
        float locY = bounds.pos.y + height() / 2;

        locX += Math.cos(Math.toRadians(getRotation())) * (width() / 2);
        locY += Math.sin(Math.toRadians(getRotation())) * (height() / 2);

        return new Vector2(locX, locY);
    }

    public Vector2 getRarePosition() {
        float locX = bounds.pos.x + width() / 2;
        float locY = bounds.pos.y + height() / 2;

        locX -= Math.cos(Math.toRadians(getRotation())) * (width() / 2);
        locY -= Math.sin(Math.toRadians(getRotation())) * (height() / 2);

        return new Vector2(locX, locY);
    }

    public void setActionEvent(final ActionEvent actionEvent) {
        this.actionEvent = actionEvent;
    }

    public boolean hasActionEvent() {
        return actionEvent != null;
    }

    public void runActionEvent(final Entity caller) {
        actionEvent.eventHandling(caller);
    }

    public ChainEvent ifCollides(final Entity other) {
        final ChainEvent chainEvent = new ChainEvent();
        addEvent(() -> {
            if (collidesWith(other))
                chainEvent.getEvent().eventHandling();
        });
        return chainEvent;
    }

    public void die() {
        getLevel().discard(this);
    }

    protected void copyData(final Entity clone) {
        clone.originator = this;
        clone.bounds.pos.x = bounds.pos.x;
        clone.bounds.pos.y = bounds.pos.y;
        clone.bounds.size.width = bounds.size.width;
        clone.bounds.size.height = bounds.size.height;
        clone.active = active;
        clone.visible = visible;
        clone.tint.set(tint);
        clone.bounds.rotation = bounds.rotation;
        clone.zIndex = zIndex;
        clone.hitbox = hitbox;
        clone.quickCollision = quickCollision;
        clone.offsetX = offsetX;
        clone.offsetY = offsetY;
        clone.flipX = flipX;
        clone.flipY = flipY;
        clone.sounds.maxDistance = sounds.maxDistance;
        clone.sounds.maxVolume = sounds.maxVolume;
        clone.sounds.power = sounds.power;
        clone.sounds.useFalloff = sounds.useFalloff;
        clone.sounds.mute = sounds.mute;
        clone.scaleX = scaleX;
        clone.scaleY = scaleY;
        if (image != null)
            clone.image = image.getClone();
    }

    protected void basicRender(final SpriteBatch batch, final Image2D image2D) {
        basicRender(batch, image2D, x(), y());
    }

    protected void basicRender(final SpriteBatch batch, final Image2D image, final float x, final float y) {
        if (image == null)
            return;

        batch.draw(image,
                x + offsetX,
                y + offsetY,
                (x + bounds.size.width / 2) - (x + offsetX),
                (y + bounds.size.height / 2) - (y + offsetY),
                bounds.size.width,
                bounds.size.height,
                scaleX,
                scaleY,
                bounds.rotation % 360,
                0,
                0,
                (int) bounds.size.width,
                (int) bounds.size.height,
                flipX,
                !flipY);
    }

    void runEvents() {
        events.removeAll(deleteEvents);
        deleteEvents.clear();

        events.forEach(Event::eventHandling);
    }
}
