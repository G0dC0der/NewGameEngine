package pojahn.lang;

import java.lang.reflect.Array;

public class ArrayUtils {

	@SuppressWarnings("unchecked")
	public static <T> T[] refCopy(T[] src, Class<T> clazz){
		T[] copy = (T[]) Array.newInstance(clazz, src.length);
		for(int i = 0; i < copy.length; i++)
			copy[i] = src[i];
		
		return copy;
	}
}
