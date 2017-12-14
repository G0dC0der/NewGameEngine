package pojahn.game.desktop.redguyruns.levels.battery;

import com.google.common.collect.ImmutableList;
import pojahn.game.core.BaseLogic;
import pojahn.game.core.Entity;
import pojahn.game.core.MobileEntity;

import java.util.List;
import java.util.Objects;

public class GroupedPlatform extends Entity {

    private MobileEntity target;
    private List<MobileEntity> platforms;

    public GroupedPlatform(final MobileEntity target, final List<? extends MobileEntity> platforms) {
        this.platforms = ImmutableList.copyOf(platforms);
        this.target = Objects.requireNonNull(target);
    }

    @Override
    public void init() {
        super.init();
        platforms.forEach(target::addObstacle);
    }

    @Override
    public void dispose() {
        super.dispose();
        platforms.forEach(target::removeObstacle);
    }

    @Override
    public void logistics() {
        if (!target.isActive())
            return;

        final int scanSize = 1;

        for (final MobileEntity platform : platforms) {
            final float x = platform.x() - scanSize;
            final float y = platform.y() - scanSize;
            final float w = platform.width() + scanSize * 2;
            final float h = platform.height() + scanSize * 2;

            if (BaseLogic.rectanglesCollide(target.bounds.toRectangle(), x, y, w, h)) {
                final float nextX = target.x() + (platform.x() - platform.prevX());
                final float nextY = target.y() + (platform.y() - platform.prevY());

                if (!target.occupiedAt(nextX, nextY))
                    target.move(nextX, nextY);

                if (BaseLogic.rectanglesCollide(platform.bounds.toRectangle(), target.bounds.toRectangle()))
                    platform.collisionResponse(target);

                break;
            }
        }
    }
}
