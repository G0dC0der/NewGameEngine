package pojahn.game.desktop.redguyruns.logic;

import pojahn.game.core.Level;

public class Worlds {

    private static final Class[][] WORLDS = {
            new Class[]{
                    //TrainingStage1.class
                    //TrainingStage2.class
            },
            new Class[]{
                    //TrainingStage2.class
                    //TrainingStage3.class
            }
    };

    private int world, level;

    public Worlds() {
    }

    public Worlds(int level, int world) {
        this.level = level;
        this.world = world;
    }

    public Level getCurrent() {
        try {
            return (Level) WORLDS[world][level].newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    public Level getNext() {
        if (++level > WORLDS[world].length) {
            world++;
            level = 0;
        }
        return getCurrent();
    }

    public int getWorldLevel() {
        return world;
    }

    public int getLevel() {
        return level;
    }
}
