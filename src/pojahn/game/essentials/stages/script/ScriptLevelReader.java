package pojahn.game.essentials.stages.script;

import com.badlogic.gdx.files.FileHandle;
import pojahn.game.core.Level;
import pojahn.lang.IO;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Scanner;

public class ScriptLevelReader {

    private static final String SCRIPT_TEMPLATE = toString(ScriptLevelReader.class.getResourceAsStream("imports.js"));

    public static Level parse(FileHandle scriptFile) throws ParseException {
        String script = null;
        FileInputStream in = null;

        try {
            in = new FileInputStream(scriptFile.file());
            script = SCRIPT_TEMPLATE.replace("_REPLACE_", toString(in));
        } catch (FileNotFoundException e) {
            throw new UncheckedIOException(e);
        } finally {
            IO.close(in);
        }

        try {
            ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
            engine.eval(script);
            Invocable invocable = (Invocable) engine;

            return (Level) invocable.invokeFunction("provide");
        } catch (ScriptException | NoSuchMethodException e) {
            throw new ParseException("Failed to parse map: " + scriptFile.file().getAbsolutePath(), e);
        }
    }

    private static String toString(InputStream in) {
        Scanner scanner = new Scanner(in, "UTF-8");
        scanner.useDelimiter("\\A");
        if(scanner.hasNext()) {
            return scanner.next();
        } else {
            throw new RuntimeException("Trying to convert an empty stream to string.");
        }
    }
}
