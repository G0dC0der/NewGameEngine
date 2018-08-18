package pojahn.game.entities.enemy.weapon;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.ImmutableList;
import pojahn.game.core.BaseLogic;
import pojahn.game.core.Entity;
import pojahn.game.core.MobileEntity;
import pojahn.game.essentials.Animation;
import pojahn.game.essentials.Direction;
import pojahn.game.essentials.Image2D;
import pojahn.game.events.Event;
import pojahn.lang.Obj;

import java.util.List;
import java.util.Optional;

public final class Thwump extends MobileEntity {

    private final float attackSpeed, retreatSpeed;
    private final int attackRecovery, sleepRecovery, scanPadding;
    private final Vector2 initialPosition;
    private final Animation<Image2D> sleepImage, attackImage;
    private final Direction direction;
    private final List<Entity> targets;
    private Event slamEvent, retreatedEvent, detectEvent;
    private boolean attacking, scanning;

    private Thwump(final Builder builder) {
        this.attackSpeed = builder.attackSpeed;
        this.retreatSpeed = builder.retreatSpeed;
        this.attackRecovery = builder.attackRecovery;
        this.sleepRecovery = builder.sleepRecovery;
        this.scanPadding = builder.scanPadding;
        this.initialPosition = builder.initialPosition.cpy();
        this.sleepImage = Obj.nonNull(builder.sleepImage);
        this.attackImage = Obj.nonNull(builder.attackImage);
        this.direction = Obj.nonNull(builder.direction);
        this.targets = ImmutableList.copyOf(builder.targets);

        setImage(sleepImage);
        move(initialPosition);
    }

    @Override
    public void init() {
        scanning = true;
    }

    public void setSlamEvent(final Event slamEvent) {
        this.slamEvent = slamEvent;
    }

    public void setRetreatedEvent(final Event retreatedEvent) {
        this.retreatedEvent = retreatedEvent;
    }

    public void setDetectEvent(final Event detectEvent) {
        this.detectEvent = detectEvent;
    }

    @Override
    public void logistics() {
        if (!isFrozen()) {
            if (scanning) {
                final Rectangle scanningArea = getScanningArea();

                targets.stream()
                    .filter(Entity::isActive)
                    .filter(target -> BaseLogic.rectanglesCollide(target.bounds.toRectangle(), scanningArea))
                    .filter(this::canSee)
                    .findAny()
                    .ifPresent(entity -> {
                        scanning = false;
                        attacking = true;
                        setImage(attackImage);
                        setMoveSpeed(attackSpeed);
                        Optional.ofNullable(detectEvent).ifPresent(Event::eventHandling);
                    });
            } else if (attacking) {
                final Vector2 target = getDirection();
                final Vector2 vector = attemptTowards(target.x, target.y, getMoveSpeed());

                if (occupiedAt(vector.x, vector.y)) {
                    attacking = false;
                    setMoveSpeed(retreatSpeed);
                    freeze();
                    getLevel().runOnceAfter(this::unfreeze, attackRecovery);
                    Optional.ofNullable(slamEvent).ifPresent(Event::eventHandling);
                } else {
                    move(vector);
                }
            } else {
                final Vector2 vector = attemptTowards(initialPosition.x, initialPosition.y, getMoveSpeed());
                if (occupiedAt(vector.x, vector.y)) {
                    scanning = true;
                    setMoveSpeed(attackSpeed);
                    setImage(sleepImage);
                    freeze();
                    getLevel().runOnceAfter(this::unfreeze, sleepRecovery);
                    Optional.ofNullable(retreatedEvent).ifPresent(Event::eventHandling);
                } else {
                    move(vector);
                }
            }
        }
    }

    private Vector2 getDirection() {
        switch (direction) {
            case W:
                return new Vector2(0, y());
            case E:
                return new Vector2(getLevel().getWidth(), y());
            case N:
                return new Vector2(x(), 0);
            case S:
                return new Vector2(x(), getLevel().getHeight());
        }
        throw new RuntimeException("Unhandled direction");
    }

    private Rectangle getScanningArea() {
        final Rectangle rectangle;
        switch (direction) {
            case W:
                rectangle = bounds.toRectangle().setX(0).setWidth(initialPosition.x);
                break;
            case E:
                rectangle = bounds.toRectangle().setWidth(Integer.MAX_VALUE);
                break;
            case N:
                rectangle = bounds.toRectangle().setY(0).setHeight(initialPosition.y);
                break;
            case S:
                rectangle = bounds.toRectangle().setHeight(Integer.MAX_VALUE);
                break;
            default:
                throw new RuntimeException("Unhandled direction");
        }

        return rectangle
            .setX(rectangle.getX() -  scanPadding)
            .setY(rectangle.getY() -  scanPadding)
            .setWidth(rectangle.getWidth() + (scanPadding * 2))
            .setHeight(rectangle.getHeight() + (scanPadding * 2));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private float attackSpeed, retreatSpeed;
        private int attackRecovery, sleepRecovery, scanPadding;
        private Vector2 initialPosition;
        private Animation<Image2D> sleepImage, attackImage;
        private Direction direction;
        private List<Entity> targets;

        private Builder() {
        }

        public Builder attackSpeed(final float attackSpeed) {
            this.attackSpeed = attackSpeed;
            return this;
        }

        public Builder retreatSpeed(final float retreatSpeed) {
            this.retreatSpeed = retreatSpeed;
            return this;
        }

        public Builder attackRecovery(final int attackRecovery) {
            this.attackRecovery = attackRecovery;
            return this;
        }

        public Builder sleepRecovery(final int sleepRecovery) {
            this.sleepRecovery = sleepRecovery;
            return this;
        }

        public Builder scanPadding(final int scanPadding) {
            this.scanPadding = scanPadding;
            return this;
        }

        public Builder initialPosition(final Vector2 initialPosition) {
            this.initialPosition = initialPosition;
            return this;
        }

        public Builder sleepImage(final Animation<Image2D> sleepImage) {
            this.sleepImage = sleepImage;
            return this;
        }

        public Builder attackImage(final Animation<Image2D> attackImage) {
            this.attackImage = attackImage;
            return this;
        }

        public Builder direction(final Direction direction) {
            this.direction = direction;
            return this;
        }

        public Builder targets(final List<Entity> targets) {
            this.targets = targets;
            return this;
        }

        public Thwump build() {
            return new Thwump(this);
        }
    }
}