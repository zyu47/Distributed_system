package dep;

public class CompareIDrange {
	public static boolean inrange(String testID, String smallID, String largeID){
		int test = Integer.parseInt(testID, 16);
		int small = Integer.parseInt(smallID, 16);
		int large = Integer.parseInt(largeID, 16);
		
		if (small == large) {
			if (test != small) {
				return true;
			} else {
				return false;
			}
		} else if (small < large) {
			return (test > small) && (large > test);
		} else {
			large += (int)Math.pow(2, 16);
			if (test < small) {
				test += (int)Math.pow(2, 16);
			}
			return (test > small) && (large > test);
		}
	}
}
