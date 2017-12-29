package pojahn.game.essentials;

import com.google.common.collect.Lists;
import pojahn.game.entities.object.Collectable;

import java.util.ArrayList;
import java.util.List;

public class ItemCollection {

    private List<Collectable> collectables;

    public ItemCollection() {
        collectables = new ArrayList<>();
    }

    public ItemCollection(final Collectable... collectables) {
        this.collectables = Lists.newArrayList(collectables);
    }

    public void add(final Collectable collectable) {
        collectables.add(collectable);
    }

    public boolean allCollected() {
        return collectables.isEmpty() || collectables.stream().allMatch(Collectable::isCollected);
    }
}
