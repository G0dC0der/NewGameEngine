package pojahn.game.entities.platform;

import com.google.common.collect.ImmutableList;
import pojahn.game.core.BaseLogic;
import pojahn.game.core.Entity;
import pojahn.game.core.MobileEntity;
import pojahn.game.entities.movement.PathDrone;
import pojahn.game.essentials.Hitbox;
import pojahn.lang.Obj;

import java.util.ArrayList;
import java.util.List;

public class SolidPlatform extends PathDrone {

    public enum FollowMode {
        NONE,
        NORMAL,
        STRICT
    }

    private final List<MobileEntity> intersectors, subjects;
    private FollowMode followMode;
    private float scanSize;

    public SolidPlatform(final float x, final float y, final MobileEntity... subjects) {
        super(x, y);
        this.subjects = Obj.requireNotEmpty(subjects);
        intersectors = new ArrayList<>(subjects.length);
        setFollowMode(FollowMode.NORMAL);
    }

    @Override
    public SolidPlatform getClone() {
        final SolidPlatform clone = new SolidPlatform(x(), y(), subjects.toArray(new MobileEntity[0]));
        copyData(clone);
        if (cloneEvent != null)
            cloneEvent.handleClonded(clone);

        return clone;
    }

    @Override
    public void init() {
        super.init();
        subjects.forEach(mobileEntity -> mobileEntity.addObstacle(this));
    }

    @Override
    public void logistics() {
        super.logistics();
        intersectors.clear();

        final float x = x() - scanSize;
        final float y = y() - scanSize;
        final float w = width() + scanSize * 2;
        final float h = height() + scanSize * 2;

        subjects.stream()
            .filter(Entity::isActive)
            .forEach(sub -> {
                if (BaseLogic.rectanglesCollide(x, y, w, h, sub.x(), sub.y(), sub.width(), sub.height())) {
                    intersectors.add(sub);

                    final float nextX = sub.x() + (x() - prevX());
                    final float nextY = sub.y() + (y() - prevY());

                    if (!sub.occupiedAt(nextX, nextY))
                        sub.move(nextX, nextY);

                    if (BaseLogic.rectanglesCollide(bounds.toRectangle(), sub.bounds.toRectangle()))
                        collisionResponse(sub);
                }
            });
    }

    public void setFollowMode(final FollowMode followMode) {
        this.followMode = followMode;
        switch (followMode) {
            case NONE:
                scanSize = 0;
                break;
            case NORMAL:
                scanSize = 1;
                break;
            case STRICT:
                scanSize = getMoveSpeed() + 1;
                break;
        }
    }

    List<MobileEntity> getActiveSubjects() {
        return ImmutableList.copyOf(intersectors);
    }

    @Override
    public void setMoveSpeed(final float moveSpeed) {
        super.setMoveSpeed(moveSpeed);
        setFollowMode(this.followMode);
    }

    @Override
    @Deprecated
    public void setHitbox(final Hitbox hitbox) {
        throw new UnsupportedOperationException("SolidPlatforms are restricted to rectangular hitbox.");
    }

    @Override
    public void dispose() {
        subjects.forEach(sub -> sub.removeObstacle(this));
    }

    protected void copyData(final SolidPlatform clone) {
        super.copyData(clone);
        clone.followMode = followMode;
        clone.scanSize = scanSize;
    }
}