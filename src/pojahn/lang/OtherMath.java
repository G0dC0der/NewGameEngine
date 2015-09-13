package pojahn.lang;

public class OtherMath {	
	
	public static boolean between(int value, int min, int max) {
		return (value >= min) && (value <= max);
	}
	
	/**
	 * Reduces the amount of decimals for the given values-
	 * @param value The value to round-
	 * @param precision The amount of decimals to keep.
	 * @return The rounded values.
	 */
	public static double round (double value, int precision){
		double prec = Math.pow(10, precision);
		return Math.round(value * prec) / prec;
	}
	
	private static final double B_MIN  = 0x00;
	private static final double B_MAX  = 0x3FF;
	private static final double KB_MIN = 0x400;
	private static final double KB_MAX = 0xFFFFF;
	private static final double MB_MIN = 0x100000;
	private static final double MB_MAX = 0x3FFFFFFF;
	private static final double GB_MIN = 0x40000000;
	private static final double GB_MAX = 0xFFFFFFFFFFL;
	private static final double TB_MIN = 0x10000000000L;
	private static final double TB_MAX = 0x4000000000000L;
	
	public static String getSizeInfo (long size){ //Arg in bytes
		String info = null;
		double temp;
		
		if (size >= B_MIN && size <= B_MAX)
			info = size + " b";
		else if (size >= KB_MIN && size <= KB_MAX){
			temp = (double) (size / KB_MIN);
			info = OtherMath.round (temp, 1)  + " kb";
		} else if (size >= MB_MIN  && size <= MB_MAX){
			temp = (double) (size / MB_MIN);
			info = OtherMath.round (temp, 2) + " mb";
		} else if (size >= GB_MIN && size <= GB_MAX){
			temp = (double) (size / GB_MIN);
			info = OtherMath.round (temp, 3) + " gb";
		} else if (size >= TB_MIN && size <= TB_MAX){
			temp = (double) (size / TB_MIN);
			info = OtherMath.round (temp, 4) + " tb";
		} else 
			throw new IllegalArgumentException ("Illegal size: " + Long.toString (size));
			
		return info;
	}
}