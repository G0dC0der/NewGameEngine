package pojahn.lang;

public class Obj {

    public static <T> T nonNull(final T defaultValue, final T backupValue) {
        return defaultValue != null ? defaultValue : backupValue;
    }
}
