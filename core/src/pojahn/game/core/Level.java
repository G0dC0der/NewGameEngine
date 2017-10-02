package pojahn.game.core;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.Vector2;
import pojahn.game.essentials.CheckPointHandler;
import pojahn.game.essentials.Keystrokes;
import pojahn.game.essentials.Utils;
import pojahn.game.essentials.Vitality;
import pojahn.game.events.Event;
import pojahn.game.events.TaskEvent;
import pojahn.lang.Entry;
import pojahn.lang.Int32;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class Level {

    public enum Tile {
        SOLID,
        HOLLOW,
        GOAL,
        LETHAL,
        CUSTOM_1,
        CUSTOM_2,
        CUSTOM_3,
        CUSTOM_4,
        CUSTOM_5,
        CUSTOM_6,
        CUSTOM_7,
        CUSTOM_8,
        CUSTOM_9,
        CUSTOM_10
    }

    public static class TileLayer {

        private int x, y;
        private Tile[][] layer;

        public TileLayer(int width, int height) {
            setSize(width, height);
        }

        public int width() {
            return layer.length;
        }

        public int height() {
            return layer[0].length;
        }

        public void setPosition(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public void setSize(int width, int height) {
            layer = new Tile[width][height];
        }

        public void setTile(int x, int y, Tile tile) {
            layer[x][y] = tile;
        }

        public void fill(Tile tile) {
            for (Tile[] layerRow : layer) {
                Arrays.fill(layerRow, tile);
            }
        }

        public TileLayer copy() {
            TileLayer tileLayer = new TileLayer(0, 0);
            tileLayer.x = x;
            tileLayer.y = y;
            tileLayer.layer = layer;

            return tileLayer;
        }
    }

    private List<Entry<Integer, Entity>> awaitingObjects, deleteObjects;
    private List<PlayableEntity> mainCharacters;
    private List<Entity> focusObjects;
    private List<TileLayer> tileLayers;
    private CheckPointHandler cph;
    private boolean focusOnPassive;

    List<Entity> gameObjects, soundListeners;
    Engine engine;
    boolean sort;

    protected Level() {
        awaitingObjects = new LinkedList<>();
        deleteObjects = new LinkedList<>();
        soundListeners = new ArrayList<>();
        gameObjects = new LinkedList<>();
        tileLayers = new ArrayList<>();
        mainCharacters = new ArrayList<>();
        focusObjects = new ArrayList<>();
        cph = new CheckPointHandler();
        focusOnPassive = true;
    }

    public abstract int getWidth();

    public abstract int getHeight();

    public abstract void init(Serializable meta) throws Exception;

    public abstract void build();

    public abstract void dispose();

    protected abstract Tile tileAtInternal(int x, int y);

    public boolean isSolid(int x, int y) {
        return tileAt(x, y) == Tile.SOLID;
    }

    public boolean isHollow(int x, int y) {
        return tileAt(x, y) == Tile.HOLLOW;
    }

    public final Tile tileAt(int x, int y) {
        if (outOfBounds(x, y))
            return Tile.HOLLOW;

        Tile tile = onLayer(x, y);
        if(tile != null)
            return tile;

        return tileAtInternal(x, y);
    }

    public Tile tileAt(Vector2 cord) {
        return tileAt((int) cord.x, (int) cord.y);
    }

    public Serializable getMeta() {
        return null;
    }

    public boolean cpPresent() {
        return getCheckpointHandler().getLatestCheckpoint() != null;
    }

    public Music getStageMusic() {
        return null;
    }

    public String getLevelName() {
        return "Level " + toString();
    }

    public String getDescription() {
        return null;
    }

    public Engine getEngine() {
        return engine;
    }

    public CheckPointHandler getCheckpointHandler() {
        return cph;
    }

    public void addTileLayer(TileLayer tileLayer) {
        tileLayers.add(tileLayer);
    }

    public void removeTileLayer(TileLayer tileLayer) {
        for(int i = 0; i < tileLayers.size(); i++) {
            if(tileLayers.get(i) == tileLayer) {
                tileLayers.remove(i);
                return;
            }
        }
    }

    public boolean outOfBounds(float targetX, float targetY) {
        return  targetX >= getWidth() ||
                targetY >= getHeight() ||
                targetX < 0 ||
                targetY < 0;
    }

    public void add(Entity entity) {
        awaitingObjects.add(new Entry<>(0, entity));
    }

    public void addAfter(Entity entity, int framesDelay) {
        awaitingObjects.add(new Entry<>(framesDelay, entity));
    }

    public void addWhen(Entity entity, TaskEvent addEvent) {
        Entity wrapper = new Entity();
        wrapper.addEvent(() -> {
            if (addEvent.eventHandling()) {
                wrapper.getLevel().add(entity);
                wrapper.getLevel().discard(wrapper);
            }
        });

        add(wrapper);
    }

    public void temp(Entity entity, int lifeFrames) {
        add(entity);
        discardAfter(entity, lifeFrames);
    }

    public void temp(Entity entity, TaskEvent discardCondition) {
        add(entity);
        discardWhen(entity, discardCondition);
    }

    public Entity add(Event event) {
        Entity wrapper = Utils.wrap(event);
        addAfter(wrapper, 0);

        return wrapper;
    }

    public Entity addAfter(Event event, int framesDelay) {
        Entity wrapper = Utils.wrap(event);
        addAfter(wrapper, framesDelay);

        return wrapper;
    }

    public Entity addWhen(Event event, TaskEvent addEvent) {
        Entity wrapper = Utils.wrap(event);
        addWhen(wrapper, addEvent);

        return wrapper;
    }

    public Entity temp(Event event, int lifeFrames) {
        Entity wrapper = Utils.wrap(event);
        add(wrapper);
        discardAfter(wrapper, lifeFrames);

        return wrapper;
    }

    public Entity temp(Event event, TaskEvent discardCondition) {
        Entity wrapper = Utils.wrap(event);
        add(wrapper);
        discardWhen(wrapper, discardCondition);

        return wrapper;
    }

    public Entity temp(Event event, int lifeFrames, Event endEvent) {
        Entity wrapper = Utils.wrap(event);
        add(wrapper);
        discardAfter(wrapper, lifeFrames);
        runOnceAfter(endEvent, lifeFrames);

        return wrapper;
    }

    public Entity temp(Event event, TaskEvent discardCondition, Event endEvent) {
        Entity wrapper = Utils.wrap(event);
        add(wrapper);
        discardWhen(wrapper, discardCondition);
        runOnceWhen(endEvent, discardCondition);

        return wrapper;
    }

    public Entity runOnceWhen(Event event, TaskEvent whenToRun) {
        Entity wrapper = new Entity();
        wrapper.addEvent(() -> {
            if (whenToRun.eventHandling()) {
                event.eventHandling();
                wrapper.getLevel().discard(wrapper);
            }
        });
        add(wrapper);

        return wrapper;
    }

    public Entity runOnceAfter(Event event, int framesDelay) {
        int[] counter = {0};

        Entity wrapper = new Entity();
        wrapper.addEvent(() -> {
            if (counter[0]++ == framesDelay) {
                event.eventHandling();
                discard(wrapper);
            }
        });
        add(wrapper);

        return wrapper;
    }

    public Entity runWhile(Event event, TaskEvent condition) {
        Entity wrapper = new Entity();
        wrapper.addEvent(()->{
            if (condition.eventHandling())
                event.eventHandling();
        });

        add(wrapper);
        return wrapper;
    }

    public Entity interval(Event event, int freq) {
        Entity wrapped = new Entity();
        Int32 counter = new Int32();
        wrapped.addEvent(() -> {
            if (++counter.value % freq == 0) {
                event.eventHandling();
            }
        });

        add(wrapped);
        return wrapped;
    }

    public Entity interval(Event event, int freq, TaskEvent discardCondition) {
        Entity wrapped = new Entity();
        Int32 counter = new Int32();
        wrapped.addEvent(() -> {
            if(discardCondition.eventHandling()) {
                discard(wrapped);
            } else {
                if (++counter.value % freq == 0) {
                    event.eventHandling();
                }
            }
        });

        add(wrapped);
        return wrapped;
    }

    public void discard(Entity entity) {
        discardAfter(entity, 0);
    }

    public void discardAfter(Entity entity, int framesDelay) {
        deleteObjects.add(new Entry<>(framesDelay, entity));
    }

    public void discardWhen(Entity entity, TaskEvent discardEvent) {
        Entity wrapper = new Entity();
        wrapper.addEvent(() -> {
            if (discardEvent.eventHandling()) {
                discard(entity);
                discard(wrapper);
            }
        });

        add(wrapper);
    }

    public List<PlayableEntity> getMainCharacters() {
        return mainCharacters;
    }

    public List<PlayableEntity> getNonDeadMainCharacters() {
        return mainCharacters.stream()
                .filter(el -> el.isAlive() || el.isDone())
                .collect(Collectors.toList());
    }

    public List<PlayableEntity> getAliveMainCharacters() {
        return mainCharacters.stream()
                .filter(PlayableEntity::isAlive)
                .collect(Collectors.toList());
    }

    public List<? extends Entity> getSoundListeners() {
        return soundListeners.isEmpty() ? getNonDeadMainCharacters() : soundListeners;
    }

    public void addSoundListener(Entity listener) {
        soundListeners.add(listener);
    }

    public void removeSoundListener(Entity listener) {
        soundListeners.remove(listener);
    }

    public void addFocusObject(Entity entity) {
        focusObjects.add(entity);
    }

    public void removeFocusObject(Entity entity) {
        focusObjects.remove(entity);
    }

    public void focusOnPassive(boolean focusOnPassive) {
        this.focusOnPassive = focusOnPassive;
    }

    protected Tile onLayer(int x, int y) {
        for(TileLayer tileLayer : tileLayers) {
            if(Collisions.pointRectangleOverlap(tileLayer.x, tileLayer.y, tileLayer.layer.length - 1, tileLayer.layer[0].length - 1, x, y)) {
                int relX = x - tileLayer.x;
                int relY = y - tileLayer.y;

                if(tileLayer.layer[relX][relY] != null)
                    return tileLayer.layer[relX][relY];
            }
        }
        return null;
    }

    protected void clean() {
        getCheckpointHandler().clearUsers();
        awaitingObjects.clear();
        deleteObjects.clear();
        gameObjects.forEach(Entity::dispose);
        gameObjects.clear();
        mainCharacters.clear();
        tileLayers.clear();
        focusObjects.clear();
    }

    void gameLoop() {
        place();

        if (sort) {
            gameObjects.sort(Comparator.comparingInt(Entity::getZIndex));
            sort = false;
        }

        focusCamera();
        updateEntities();
        getCheckpointHandler().update();
    }

    private void updateEntities() {
        for (Entity entity : gameObjects) {
            if (entity.isActive()) {
                if (entity instanceof PlayableEntity) {
                    PlayableEntity play = (PlayableEntity) entity;
                    Keystrokes buttonsDown;

                    if (play.isGhost())
                        buttonsDown = play.nextInput();
                    else if (engine.active() && play.isAlive())
                        buttonsDown = engine.isReplaying() ? engine.getDevice().nextInput(play.getBadge()) : Keystrokes.from(play.getController());
                    else
                        buttonsDown = Keystrokes.AFK;

                    if (play.isAlive() && !play.isGhost() && engine.active() && !engine.isReplaying())
                        engine.getDevice().addFrame(play.getBadge(), buttonsDown);

                    if (buttonsDown.suicide) {
                        play.setState(Vitality.DEAD);
                    } else {
                        play.setKeysDown(buttonsDown);
                        play.logistics();
                        play.runEvents();

                        if (play.tileEvents.size() > 0)
                            tileIntersection(play, play.getOccupyingCells());

                        play.updateFacing();
                        play.setPrevs();
                    }
                } else if (entity instanceof MobileEntity) {
                    MobileEntity mobile = (MobileEntity) entity;

                    mobile.logistics();
                    mobile.runEvents();

                    if (mobile.tileEvents.size() > 0)
                        tileIntersection(mobile, mobile.getOccupyingCells());

                    mobile.updateFacing();
                    mobile.setPrevs();
                } else {
                    entity.logistics();
                    entity.runEvents();
                }
            }
        }
    }

    private void tileIntersection(MobileEntity mobile, Set<Tile> tiles) {
        for (Tile tile : tiles) {
            switch (tile) {
                case HOLLOW:
                    /*/ Do nothing /*/
                    break;
                default:
                    mobile.runTileEvents(tile);
                    break;
            }
        }
    }

    void place() {
        final Iterator<Entry<Integer, Entity>> iterator = awaitingObjects.iterator();
        while (iterator.hasNext()) {
            final Entry<Integer, Entity> entry = iterator.next();

            if (entry.key-- <= 0) {
                iterator.remove();
                gameObjects.add(entry.value);
                sort = true;

                entry.value.level = this;
                entry.value.engine = engine;
                entry.value.present = true;
                entry.value.badge = engine.provideBadge();
                entry.value.init();

                if (entry.value instanceof PlayableEntity) {
                    PlayableEntity play = (PlayableEntity) entry.value;
                    if (!play.isGhost()) {
                        mainCharacters.add(play);
                        cph.addUser(play);
                        engine.getDevice().addEntry(play.getBadge());
                    }
                }
            }
        }

        for (int i = 0; i < deleteObjects.size(); i++) {
            Entry<Integer, Entity> entry = deleteObjects.get(i);
            if (entry.key-- <= 0) {
                deleteObjects.remove(i);
                i--;
                gameObjects.remove(entry.value);
                entry.value.present = false;
                entry.value.dispose();

                if (entry.value instanceof PlayableEntity) {
                    PlayableEntity play = (PlayableEntity) entry.value;
                    if (!play.isGhost()) {
                        mainCharacters.remove(play);
                    }
                }
            }
        }
    }

    private void focusCamera() {
        List<? extends Entity> list = focusObjects.isEmpty() ? getNonDeadMainCharacters() : focusObjects;
        list = list.isEmpty() ? getMainCharacters() : list;
        list = focusOnPassive ? list : list.stream().filter(Entity::isActive).collect(Collectors.toList());

        final Dimension size = getEngine().getScreenSize();
        final float stageWidth = getWidth();
        final float stageHeight = getHeight();
        final float windowWidth = size.width;
        final float windowHeight = size.height;
        float tx = 0;
        float ty = 0;
        float zoom = getEngine().getZoom();

        if (list.size() == 1 ) {
            Entity entity = list.get(0);
            if (getEngine().getZoom() == 1.0f) {
                float marginX = windowWidth  / 2;
                float marginY = windowHeight  / 2;

                tx = Math.min(stageWidth  - windowWidth,   Math.max(0, entity.centerX() - marginX)) + marginX;
                ty = Math.min(stageHeight - windowHeight,  Math.max(0, entity.centerY() - marginY)) + marginY;
            } else {
                tx = entity.centerX();
                ty = entity.centerY();
            }
        } else if (list.size() > 1) {
            Entity first = list.get(0);

            final float marginX = windowWidth / 2;
            final float marginY = windowHeight / 2;
            final float padding = 20;

            float boxX = first.x();
            float boxY = first.y();
            float boxWidth = boxX + first.width();
            float boxHeight = boxY + first.height();

            for (int i = 1; i < list.size(); i++) {
                Entity focus = list.get(i);

                boxX = Math.min(boxX, focus.x());
                boxY = Math.min(boxY, focus.y());

                boxWidth = Math.max(boxWidth, focus.x() + focus.width());
                boxHeight = Math.max(boxHeight, focus.y() + focus.height());
            }
            boxWidth = boxWidth - boxX;
            boxHeight = boxHeight - boxY;

            boxX -= padding;
            boxY -= padding;
            boxWidth += padding * 2.0f;
            boxHeight += padding * 2.0f;

            boxX = Math.max(boxX, 0);
            boxX = Math.min(boxX, stageWidth - boxWidth);

            boxY = Math.max(boxY, 0);
            boxY = Math.min(boxY, stageHeight - boxHeight);

            if ((float) boxWidth / (float) boxHeight > (float) windowWidth / (float) windowHeight)
                zoom = boxWidth / windowWidth;
            else
                zoom = boxHeight / windowHeight;

            zoom = Math.max( zoom, 1.0f );

            tx = boxX + (boxWidth / 2.0f);
            ty = boxY + (boxHeight / 2.0f);

            if (marginX > tx)
                tx = marginX;
            else if (tx > stageWidth - marginX)
                tx = stageWidth - marginX;

            if (marginY > ty)
                ty = marginY;
            else if (ty > stageHeight - marginY)
                ty = stageHeight - marginY;

        }
        getEngine().setZoom(zoom);
        getEngine().translate(tx, ty);
    }
}
