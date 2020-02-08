package valentin.flow;

import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.*;
import java.io.Serializable.*;
import org.json.*;

public class AS
{
	public int ASN;
	public String holder;
	public String prefix;
	AS(String IP) throws MalformedURLException,IOException
	{
		 URL url = new URL("https://stat.ripe.net/data/prefix-overview/data.json?resource="+IP);
		 InputStream ins = url.openStream();
	     BufferedReader in = new BufferedReader(new InputStreamReader(ins));
	     StringBuilder b = new StringBuilder();
         String line;
		 while((line = in.readLine()) != null) b.append(line);
		 String s = b.toString();
		 JSONObject obj = new JSONObject(s);
		 prefix = obj.getJSONObject("data").getString("resource");
		 try{
		 JSONArray asns = obj.getJSONObject("data").getJSONArray("asns");
	     holder = asns.getJSONObject(0).getString("holder");
	     ASN = asns.getJSONObject(0).getInt("asn");
		 } catch(Exception e) {holder=null; ASN=0;}
    }
	
    int getASN() { return ASN; }
    String getHolder() { return holder; }
    String getPrefix() { return prefix; }
    
}