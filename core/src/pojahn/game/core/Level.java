package pojahn.game.core;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import pojahn.game.essentials.AwaitingObject;
import pojahn.game.essentials.CheckPointHandler;
import pojahn.game.essentials.Keystrokes;
import pojahn.game.essentials.Utils;
import pojahn.game.essentials.Vitality;
import pojahn.game.essentials.geom.Dimension;
import pojahn.game.events.Event;
import pojahn.game.events.TaskEvent;
import pojahn.lang.Int32;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.partitioningBy;

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

    public static final class TileLayer {

        private int x, y;
        private final Tile[][] layer;

        public TileLayer(final Tile[][] layer) {
            this(layer, true);
        }

        private TileLayer(final Tile[][] layer, final boolean copy) {
            if (copy) {
                this.layer = new Tile[layer.length][layer[0].length];

                for (int i = 0; i < layer.length; i++) {
                    System.arraycopy(layer[i], 0, this.layer[i], 0, layer[0].length);
                }
            } else {
                this.layer = layer;
            }
        }

        public void setPosition(final int x, final int y) {
            this.x = x;
            this.y = y;
        }

        public TileLayer copy() {
            final TileLayer tileLayer = new TileLayer(layer, false);
            tileLayer.x = x;
            tileLayer.y = y;

            return tileLayer;
        }
    }

    private List<AwaitingObject<Entity>> awaitingObjects, deleteObjects;
    private List<PlayableEntity> mainCharacters;
    private List<Entity> focusObjects;
    private List<TileLayer> tileLayers;
    private Set<String> identifiers;
    private CheckPointHandler cph;
    private List<Entity> soundListeners;

    List<Entity> gameObjects;
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
        identifiers = new HashSet<>();
        cph = new CheckPointHandler();
    }

    public abstract int getWidth();

    public abstract int getHeight();

    public abstract void init(Serializable meta) throws Exception;

    public abstract void build();

    public abstract void dispose();

    protected abstract Tile tileAtInternal(int x, int y);

    public boolean isSolid(final int x, final int y) {
        return tileAt(x, y) == Tile.SOLID;
    }

    public boolean isHollow(final int x, final int y) {
        return tileAt(x, y) == Tile.HOLLOW;
    }

    public final Tile tileAt(final int x, final int y) {
        if (outOfBounds(x, y))
            return Tile.HOLLOW;

        final Tile tile = onLayer(x, y);
        if (tile != null)
            return tile;

        return tileAtInternal(x, y);
    }

    public Tile tileAt(final Vector2 cord) {
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

    public Engine getEngine() {
        return engine;
    }

    public CheckPointHandler getCheckpointHandler() {
        return cph;
    }

    public void addTileLayer(final TileLayer tileLayer) {
        tileLayers.add(tileLayer);
    }

    public void removeTileLayer(final TileLayer tileLayer) {
        for (int i = 0; i < tileLayers.size(); i++) {
            if (tileLayers.get(i) == tileLayer) {
                tileLayers.remove(i);
                return;
            }
        }
    }

    public boolean outOfBounds(final float targetX, final float targetY) {
        return targetX >= getWidth() ||
                targetY >= getHeight() ||
                targetX < 0 ||
                targetY < 0;
    }

    public void add(final Entity entity) {
        awaitingObjects.add(new AwaitingObject<>(0, entity));
    }

    public void addAfter(final Entity entity, final int framesDelay) {
        awaitingObjects.add(new AwaitingObject<>(framesDelay, entity));
    }

    public void addWhen(final Entity entity, final TaskEvent addEvent) {
        final Entity wrapper = new Entity();
        wrapper.addEvent(() -> {
            if (addEvent.eventHandling()) {
                wrapper.getLevel().add(entity);
                wrapper.getLevel().discard(wrapper);
            }
        });

        add(wrapper);
    }

    public void temp(final Entity entity, final int lifeFrames) {
        add(entity);
        discardAfter(entity, lifeFrames);
    }

    public void temp(final Entity entity, final TaskEvent discardCondition) {
        add(entity);
        discardWhen(entity, discardCondition);
    }

    public Entity add(final Event event) {
        final Entity wrapper = Utils.wrap(event);
        addAfter(wrapper, 0);

        return wrapper;
    }

    public Entity addAfter(final Event event, final int framesDelay) {
        final Entity wrapper = Utils.wrap(event);
        addAfter(wrapper, framesDelay);

        return wrapper;
    }

    public Entity addWhen(final Event event, final TaskEvent addEvent) {
        final Entity wrapper = Utils.wrap(event);
        addWhen(wrapper, addEvent);

        return wrapper;
    }

    public Entity temp(final Event event, final int lifeFrames) {
        final Entity wrapper = Utils.wrap(event);
        add(wrapper);
        discardAfter(wrapper, lifeFrames);

        return wrapper;
    }

    public Entity temp(final Event event, final TaskEvent discardCondition) {
        final Entity wrapper = Utils.wrap(event);
        add(wrapper);
        discardWhen(wrapper, discardCondition);

        return wrapper;
    }

    public Entity temp(final Event event, final int lifeFrames, final Event endEvent) {
        final Entity wrapper = Utils.wrap(event);
        add(wrapper);
        discardAfter(wrapper, lifeFrames);
        runOnceAfter(endEvent, lifeFrames);

        return wrapper;
    }

    public Entity temp(final Event event, final TaskEvent discardCondition, final Event endEvent) {
        final Entity wrapper = Utils.wrap(event);
        add(wrapper);
        discardWhen(wrapper, discardCondition);
        runOnceWhen(endEvent, discardCondition);

        return wrapper;
    }

    public Entity runOnceWhen(final Event event, final TaskEvent whenToRun) {
        final Entity wrapper = new Entity();
        wrapper.addEvent(() -> {
            if (whenToRun.eventHandling()) {
                event.eventHandling();
                wrapper.getLevel().discard(wrapper);
            }
        });
        add(wrapper);

        return wrapper;
    }

    public Entity runOnceAfter(final Event event, final int framesDelay) {
        final int[] counter = {0};

        final Entity wrapper = new Entity();
        wrapper.addEvent(() -> {
            if (counter[0]++ == framesDelay) {
                event.eventHandling();
                discard(wrapper);
            }
        });
        add(wrapper);

        return wrapper;
    }

    public Entity runWhile(final Event event, final TaskEvent condition) {
        final Entity wrapper = new Entity();
        wrapper.addEvent(() -> {
            if (condition.eventHandling())
                event.eventHandling();
        });

        add(wrapper);
        return wrapper;
    }

    public Entity interval(final Event event, final int freq) {
        final Entity wrapped = new Entity();
        final Int32 counter = new Int32(-1);
        wrapped.addEvent(() -> {
            if (++counter.value % freq == 0) {
                event.eventHandling();
            }
        });

        add(wrapped);
        return wrapped;
    }

    public Entity interval(final Event event, final int freq, final TaskEvent discardCondition) {
        final Entity wrapped = new Entity();
        final Int32 counter = new Int32();
        wrapped.addEvent(() -> {
            if (discardCondition.eventHandling()) {
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

    public Entity runOnceDelayed(final Event event, final int delay, final TaskEvent whenToRun) {
        final Entity wrapper  = new Entity();
        wrapper.addEvent(()-> {
            if (whenToRun.eventHandling()) {
                wrapper.die();
                wrapper.getLevel().runOnceAfter(event, delay);
            }
        });

        return wrapper;
    }

    public void discard(final Entity entity) {
        discardAfter(entity, 0);
    }

    public void discardAfter(final Entity entity, final int framesDelay) {
        deleteObjects.add(new AwaitingObject<>(framesDelay, entity));
    }

    public void discardWhen(final Entity entity, final TaskEvent discardEvent) {
        final Entity wrapper = new Entity();
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

    public void addSoundListener(final Entity listener) {
        soundListeners.add(listener);
    }

    public void removeSoundListener(final Entity listener) {
        soundListeners.remove(listener);
    }

    public void addFocusObject(final Entity entity) {
        focusObjects.add(entity);
    }

    public void removeFocusObject(final Entity entity) {
        focusObjects.remove(entity);
    }

    private Tile onLayer(final int x, final int y) {
        for (final TileLayer tileLayer : tileLayers) {
            if (BaseLogic.pointRectangleOverlap(tileLayer.x, tileLayer.y, tileLayer.layer.length - 1, tileLayer.layer[0].length - 1, x, y)) {
                final int relX = x - tileLayer.x;
                final int relY = y - tileLayer.y;

                if (tileLayer.layer[relX][relY] != null)
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
        identifiers.clear();
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
        for (final Entity entity : gameObjects) {
            if (entity.isActive()) {
                if (entity instanceof PlayableEntity) {
                    final PlayableEntity play = (PlayableEntity) entity;
                    final Keystrokes buttonsDown;

                    if (play.isGhost())
                        buttonsDown = play.nextInput();
                    else if (engine.active() && play.isAlive())
                        buttonsDown = engine.isReplaying() ? engine.getDevice().nextInput(play.getIdentifier()) : Keystrokes.from(play.getController());
                    else
                        buttonsDown = Keystrokes.AFK;

                    if (play.isAlive() && !play.isGhost() && engine.active() && !engine.isReplaying())
                        engine.getDevice().addFrame(play.getIdentifier(), buttonsDown);

                    if (buttonsDown.suicide) {
                        play.setState(Vitality.DEAD);
                    } else {
                        play.setKeysDown(buttonsDown);
                        play.logistics();
                        play.runEvents();

                        if (play.tileEvents.size() > 0)
                            tileIntersection(play, play.getOccupyingCells());

                        play.updateFacing();
                    }
                } else if (entity instanceof MobileEntity) {
                    final MobileEntity mobile = (MobileEntity) entity;

                    mobile.logistics();
                    mobile.runEvents();

                    if (mobile.tileEvents.size() > 0)
                        tileIntersection(mobile, mobile.getOccupyingCells());

                    mobile.updateFacing();
                } else {
                    entity.logistics();
                    entity.runEvents();
                }
            }
        }

        gameObjects.stream()
            .filter(MobileEntity.class::isInstance)
            .map(MobileEntity.class::cast)
            .forEach(MobileEntity::setPrevs);
    }

    private void tileIntersection(final MobileEntity mobile, final Set<Tile> tiles) {
        for (final Tile tile : tiles) {
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
        final Map<Boolean, List<AwaitingObject<Entity>>> awaitingMap = awaitingObjects.stream().collect(partitioningBy(AwaitingObject::tick));

        awaitingObjects = awaitingMap.get(Boolean.FALSE);
        awaitingMap.get(Boolean.TRUE)
            .stream()
            .map(AwaitingObject::unwrap)
            .forEach(this::addEntity);

        final Map<Boolean, List<AwaitingObject<Entity>>> deleteMap = deleteObjects.stream().collect(partitioningBy(AwaitingObject::tick));

        deleteObjects = deleteMap.get(Boolean.FALSE);
        deleteMap.get(Boolean.TRUE)
            .stream()
            .map(AwaitingObject::unwrap)
            .forEach(this::removeEntity);
    }

    private void removeEntity(final Entity entity) {
        gameObjects.remove(entity);
        entity.present = false;
        entity.dispose();
        if (entity.getIdentifier() != null) {
            identifiers.remove(entity.getIdentifier());
        }

        if (entity instanceof PlayableEntity) {
            final PlayableEntity play = (PlayableEntity) entity;
            if (!play.isGhost()) {
                mainCharacters.remove(play);
            }
        }
    }

    private void addEntity(final Entity entity) {
        gameObjects.add(entity);
        sort = true;

        entity.level = this;
        entity.engine = engine;
        entity.present = true;
        entity.init();

        if (entity.getIdentifier() != null) {
            if (identifiers.contains(entity.getIdentifier())) {
                throw new RuntimeException("Identifier is already used: " + entity.getIdentifier());
            } else {
                identifiers.add(entity.getIdentifier());
            }
        }

        if (entity instanceof PlayableEntity) {
            final PlayableEntity play = (PlayableEntity) entity;
            if (!play.isGhost()) {
                if (play.getIdentifier() == null) {
                    throw new RuntimeException("A non-ghost main character must have an identifier set!");
                }

                mainCharacters.add(play);
                cph.addUser(play);
                engine.getDevice().addEntry(play.getIdentifier());
            }
        }
    }

    private void focusCamera() {
        List<? extends Entity> list = focusObjects.isEmpty() ? getNonDeadMainCharacters() : focusObjects;
        list = list.isEmpty() ? getMainCharacters() : list;

        if (list.isEmpty())
            return;

        final Dimension size = getEngine().getScreenSize();
        final float stageWidth = getWidth();
        final float stageHeight = getHeight();
        final float windowWidth = size.width;
        final float windowHeight = size.height;
        float zoom = getEngine().getZoom();
        float tx;
        float ty;

        if (list.size() == 1) {
            final Entity entity = list.get(0);

            if (getEngine().getZoom() == 1.0f) {
                final float marginX = windowWidth / 2;
                final float marginY = windowHeight / 2;

                tx = MathUtils.clamp(entity.centerX(), marginX, stageWidth - marginX);
                ty = MathUtils.clamp(entity.centerY(), marginY, stageHeight - marginY);
            } else {
                tx = entity.centerX();
                ty = entity.centerY();
            }
        } else if (list.size() > 1) {
            final Entity first = list.get(0);

            final float marginX = windowWidth / 2;
            final float marginY = windowHeight / 2;
            final float padding = 20;

            float boxX = first.x();
            float boxY = first.y();
            float boxWidth = boxX + first.width();
            float boxHeight = boxY + first.height();

            for (int i = 1; i < list.size(); i++) {
                final Entity focus = list.get(i);

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

            zoom = Math.max(zoom, 1.0f);

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

        } else {
            return;
        }
        getEngine().setZoom(zoom);
        getEngine().translate(tx, ty);
    }
}
