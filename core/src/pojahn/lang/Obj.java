package pojahn.lang;

public class Obj {

    public static <T> T nonNull(T defaultValue, T backupValue) {
        return  defaultValue != null ? defaultValue : backupValue;
    }
}
