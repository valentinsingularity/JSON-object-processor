package valentin.flow;
import com.google.gson.*;

public class TestFlow
{
	public static void main(String[] args){
	Flow testFlow = new Flow("2015-09-22 23:33:42.077","2015-09-22 23:33:42.631",0.554,0,6,"2001:0708:0040",39367,"2001:0638:0709:3000::0019",
			                  443,"S","APF","AS","APF",767467070,783117522,0,0,11,2552,9,2576);
	testFlow.printFlow();
	}
}