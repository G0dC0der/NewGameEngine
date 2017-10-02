package pojahn.game.entities;

import pojahn.game.core.Collisions;
import pojahn.game.core.MobileEntity;
import pojahn.game.essentials.Hitbox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class SolidPlatform extends PathDrone {

    public enum FollowMode {
        NONE,
        NORMAL,
        STRICT
    }

    private MobileEntity[] subjects;
    private List<MobileEntity> intersectors;
    private FollowMode followMode;
    private float scanSize;
    private boolean ignoreInactive;

    public SolidPlatform(final float x, final float y, final MobileEntity... subjects) {
        super(x, y);
        intersectors = new ArrayList<>(subjects.length);
        this.subjects = subjects;
        setFollowMode(FollowMode.NORMAL);
        ignoreInactive = true;
    }

    @Override
    public SolidPlatform getClone() {
        final SolidPlatform clone = new SolidPlatform(x(), y(), subjects);
        copyData(clone);
        if (cloneEvent != null)
            cloneEvent.handleClonded(clone);

        return clone;
    }

    @Override
    public void init() {
        super.init();
        Stream.of(subjects).forEach(mobileEntity -> mobileEntity.addObstacle(this));
    }

    @Override
    public void logistics() {
        super.logistics();
        intersectors.clear();

        final float x = x() - scanSize;
        final float y = y() - scanSize;
        final float w = width() + scanSize * 2;
        final float h = height() + scanSize * 2;

        Arrays.stream(subjects).filter(sub -> !ignoreInactive || sub.isActive()).forEach(sub -> {
            if (Collisions.rectanglesCollide(x, y, w, h, sub.x(), sub.y(), sub.width(), sub.height())) {
                intersectors.add(sub);

                final float nextX = sub.x() + (x() - prevX());
                final float nextY = sub.y() + (y() - prevY());

                if (!sub.occupiedAt(nextX, nextY))
                    sub.move(nextX, nextY);

                if (Collisions.rectanglesCollide(bounds.toRectangle(), sub.bounds.toRectangle()))
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

    public List<MobileEntity> getActiveSubjects() {
        return new ArrayList<>(intersectors);
    }

    public void setIgnoreInactive(final boolean ignoreInactive) {
        this.ignoreInactive = ignoreInactive;
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
        if (subjects != null) {
            Stream.of(subjects).forEach(sub -> sub.removeObstacle(this));
        }
    }

    protected void copyData(final SolidPlatform clone) {
        super.copyData(clone);
        clone.followMode = followMode;
        clone.scanSize = scanSize;
        clone.ignoreInactive = ignoreInactive;
    }
}