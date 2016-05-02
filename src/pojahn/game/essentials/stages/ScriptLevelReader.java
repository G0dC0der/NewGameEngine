package pojahn.game.essentials.stages;

import pojahn.game.core.Level;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class ScriptLevelReader {

    public static Level parse(FileReader src) throws ScriptException, NoSuchMethodException {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        engine.eval(src);
        Invocable invocable = (Invocable) engine;

        return (Level) invocable.invokeFunction("provide");
    }

    public static void main(String... args) throws Exception {
        TileBasedLevel level = (TileBasedLevel) parse(new FileReader(new File("C:\\Projects\\NewGameEngine\\src\\pojahn\\game\\essentials\\stages\\imports.js")));
        level.init();
    }
}
