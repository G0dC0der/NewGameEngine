package pojahn.game.entities.platform;

import com.google.common.collect.ImmutableList;
import pojahn.game.core.BaseLogic;
import pojahn.game.core.Entity;
import pojahn.game.core.MobileEntity;
import pojahn.game.entities.movement.EarthDrone;

import java.util.List;

public class EarthSolidPlatform extends EarthDrone {

    private final List<MobileEntity> subjects;

    public EarthSolidPlatform(final float x, final float y, final MobileEntity... subjects) {
        super(x, y);
        this.subjects = ImmutableList.copyOf(subjects);
        this.subjects.forEach(subject -> subject.addObstacle(this));
    }

    @Override
    public void dispose() {
        super.dispose();
        this.subjects.forEach(subject -> subject.removeObstacle(this));
    }

    @Override
    public void logistics() {
        super.logistics();

        final float scanSize = 2f;
        final float x = x() - scanSize;
        final float y = y() - scanSize;
        final float w = width() + scanSize * 2;
        final float h = height() + scanSize * 2;

        subjects.stream()
            .filter(Entity::isActive)
            .filter(subject -> BaseLogic.rectanglesCollide(x, y, w, h, subject.x(), subject.y(), subject.width(), subject.height()))
            .forEach(sub -> {
                final float nextX = sub.x() + (x() - prevX());
                final float nextY = sub.y() + (y() - prevY());

                if (!sub.occupiedAt(nextX, nextY))
                    sub.move(nextX, nextY);

                if (BaseLogic.rectanglesCollide(bounds.toRectangle(), sub.bounds.toRectangle()))
                    collisionResponse(sub);
            });
    }
}
