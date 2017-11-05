package pojahn.lang;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class Obj {

    public static <T> T nonNull(final T defaultValue, final Supplier<T> valueSupplier) {
        return defaultValue != null ? defaultValue : valueSupplier.get();
    }

    public static <T> T nonNull(final T defaultValue, final T backupValue) {
        return defaultValue != null ? defaultValue : backupValue;
    }

    public static <T> T nonNull(final T value) {
        return Optional.ofNullable(value)
            .orElseThrow(()-> new IllegalArgumentException("Value can not be null!"));
    }

    public static <T> List<T> requireNotEmpty(final T[] arr) {
        if (arr == null || arr.length == 0) {
            throw new IllegalArgumentException("List was expected not to be null.");
        }

        return ImmutableList.copyOf(arr);
    }
}
