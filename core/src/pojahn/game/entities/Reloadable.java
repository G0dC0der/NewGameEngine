package pojahn.game.entities;

import pojahn.game.core.Entity;
import pojahn.game.events.Event;

import java.util.stream.Stream;

public class Reloadable extends Entity {

    private Item[] items;
    private User[] users;
    private Event loadedEvent, grabEvent;

    public Reloadable(float x, float y){
        move(x, y);
    }

    public void setUsers(Entity... users) {
        this.users = Stream.of(users).map(User::new).toArray(User[]::new);
    }

    public void setItems(Entity... items) {
        this.items = Stream.of(items).map(Item::new).toArray(Item[]::new);
    }

    public void setLoadedEvent(Event loadedEvent) {
        this.loadedEvent = loadedEvent;
    }

    public void setGrabEvent(Event grabEvent) {
        this.grabEvent = grabEvent;
    }

    @Override
    public void logistics() {
        for (User user : users) {
            if (!user.haveItem()) {
                for (int i = 0; i < items.length; i++) {
                    if (items[i] != null && user.entity.collidesWith(items[i].entity)) {
                        user.item = items[i];
                        items[i] = null;
                        if(grabEvent != null)
                           grabEvent.eventHandling();
                        break;
                    }
                }
            } else {
                user.item.entity.center(user.entity).bounds.pos.y -=  user.item.entity.halfHeight() - 8;

                if (collidesWith(user.entity)) {
                    getLevel().discard(user.item.entity);
                    user.item = null;
                    if(loadedEvent != null)
                        loadedEvent.eventHandling();
                }
            }
        }
    }

    private static class Item {
        private Entity entity;

        Item(Entity entity) {
            this.entity = entity;
        }
    }

    private static class User {
        Entity entity;
        Item item;

        User(Entity entity) {
            this.entity = entity;
        }

        boolean haveItem() {
            return item != null;
        }
    }
}
