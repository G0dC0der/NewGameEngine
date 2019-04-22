package pojahn.game.desktop.redguyruns.logic;

import pojahn.game.core.Level;
import pojahn.game.desktop.redguyruns.levels.atari.AtariStyle;
import pojahn.game.desktop.redguyruns.levels.battery.FlyingBattery;
import pojahn.game.desktop.redguyruns.levels.blocks.SquareTown;
import pojahn.game.desktop.redguyruns.levels.cave.CollapsingCave;
import pojahn.game.desktop.redguyruns.levels.climb.Climb;
import pojahn.game.desktop.redguyruns.levels.dark.LightsOut;
import pojahn.game.desktop.redguyruns.levels.deathegg.DeathEgg;
import pojahn.game.desktop.redguyruns.levels.diamond.DiamondCave;
import pojahn.game.desktop.redguyruns.levels.forbiddencastle.ForbiddenCastle;
import pojahn.game.desktop.redguyruns.levels.ghostbridge.GhostBridge;
import pojahn.game.desktop.redguyruns.levels.hill.GreenHill;
import pojahn.game.desktop.redguyruns.levels.hurry.InAHurry;
import pojahn.game.desktop.redguyruns.levels.lasers.LaserMadness;
import pojahn.game.desktop.redguyruns.levels.mtrace.MountainRace;
import pojahn.game.desktop.redguyruns.levels.mutant.MutantLab;
import pojahn.game.desktop.redguyruns.levels.orbit.OrbitalStation;
import pojahn.game.desktop.redguyruns.levels.phanto.GuardedKey;
import pojahn.game.desktop.redguyruns.levels.race.Race;
import pojahn.game.desktop.redguyruns.levels.sand.Sandopolis;
import pojahn.game.desktop.redguyruns.levels.shadow.ShadowSection;
import pojahn.game.desktop.redguyruns.levels.shroom.DontEatShroom;
import pojahn.game.desktop.redguyruns.levels.sprit.SpiritTemple;
import pojahn.game.desktop.redguyruns.levels.steel.SteelFactory;
import pojahn.game.desktop.redguyruns.levels.stress.StressLevel;
import pojahn.game.desktop.redguyruns.levels.training1.TrainingStage1;
import pojahn.game.desktop.redguyruns.levels.training2.TrainingStage2;
import pojahn.game.desktop.redguyruns.levels.training3.TrainingStage3;

import java.util.ArrayList;
import java.util.List;

public class World {

    public static final List<Class<? extends Level>> LEVELS = new ArrayList<>() {{
        add(TrainingStage1.class);
        add(TrainingStage2.class);
        add(TrainingStage3.class);
        add(Race.class); //TODO: FIX AI
        add(GreenHill.class);
        add(InAHurry.class);
        add(MountainRace.class);
        add(Climb.class);
        add(DontEatShroom.class);
        add(LightsOut.class);
        add(StressLevel.class);
        add(GhostBridge.class);
        add(LaserMadness.class);
        add(SquareTown.class);
        add(AtariStyle.class);
        add(SpiritTemple.class);
        add(Sandopolis.class);
        add(DiamondCave.class);
        add(GuardedKey.class);
        add(CollapsingCave.class);
        add(OrbitalStation.class);
        add(FlyingBattery.class);
        add(MutantLab.class);
        add(ForbiddenCastle.class);
        add(ShadowSection.class);
        add(DeathEgg.class);
        add(SteelFactory.class);
    }};
}
