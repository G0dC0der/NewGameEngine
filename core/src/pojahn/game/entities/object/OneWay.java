package pojahn.game.entities.object;

import pojahn.game.core.Entity;
import pojahn.game.core.MobileEntity;
import pojahn.game.essentials.Direction;
import pojahn.lang.Obj;

import java.util.List;

public class OneWay extends Entity {

    private final List<MobileEntity> targets;
    private final boolean[] block;
    private final Direction direction;

    public OneWay(final float x, final float y, final Direction direction, final MobileEntity... targets) {
        if (direction.isDiagonal())
            throw new IllegalArgumentException("The direction must be either N, S, W or E.");

        move(x, y);
        this.direction = direction;
        this.targets = Obj.requireNotEmpty(targets);
        block = new boolean[targets.length];
    }

    @Override
    public void logistics() {
        for (int i = 0; i < targets.size(); i++) {
            final MobileEntity mobile = targets.get(0);
            final boolean bool;

            switch (direction) {
                case S:
                    bool = mobile.y() >= y() + height();
                    break;
                case N:
                    bool = mobile.y() + mobile.height() <= y();
                    break;
                case E:
                    bool = x() + width() <= mobile.x();
                    break;
                case W:
                    bool = x() >= mobile.x() + mobile.width();
                    break;
                default:
                    throw new RuntimeException();
            }

            if (bool) {
                if (!block[i]) {
                    block[i] = true;
                    mobile.addObstacle(this);
                }
            } else if (block[i]) {
                block[i] = false;
                mobile.removeObstacle(this);
            }
        }
    }

    public OneWay getClone() {
        final OneWay clone = new OneWay(x(), y(), direction, targets.toArray(new MobileEntity[0]));
        copyData(clone);

        if (cloneEvent != null)
            cloneEvent.handleClonded(clone);

        return clone;
    }
}