package dep;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CurrentTimeStamp {

	public static String getFullTimeStamp () {
		return new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
	}
	
	public static String getPartialTimeStamp () {
		return new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
	}
}
