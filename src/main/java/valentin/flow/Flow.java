package valentin.flow;
import com.google.gson.*;
import com.google.gson.annotations.*;
import java.util.*;


public class Flow
{ 
	@SerializedName("start")
	String start;
	@SerializedName("end")
	String end;
	@SerializedName("duration")
	double duration;
	@SerializedName("rtt")
	double rtt;
	@SerializedName("proto")
	int proto;
	@SerializedName("sip")
	String sip;
	@SerializedName("sp")
	int sp;
	@SerializedName("dip")
	String dip;
	@SerializedName("dp")
	int dp;
	@SerializedName("iflags")
	String iflags;
	@SerializedName("uflags")
	String uflags;
	@SerializedName("riflags")
	String riflags;
	@SerializedName("ruflags")
	String ruflags;
	@SerializedName("isn")
	long isn;
	@SerializedName("risn")
	long risn;
	@SerializedName("tag")
	long tag;
	@SerializedName("rtag")
	long rtag;
	@SerializedName("pkt")
	long pkt;
	@SerializedName("oct")
	long oct;
	@SerializedName("rpkt")
	long rpkt;
	@SerializedName("roct")
	long roct;
	Map<String,AS> annotations = new HashMap<String,AS>();
	Flow() {}
	Flow(String start, String end, double duration, double rtt, int proto,String sip, int sp, String dip, int dp, String iflags,String uflags, String riflags, 
			String ruflags, long isn, long risn, long tag, long rtag, long pkt, long oct, long rpkt, long roct)
	{
		this.start=start;
		this.end=end;
		this.duration=duration;
		this.rtt=rtt;
		this.proto=proto;
		this.sip=sip;
		this.sp=sp;
	    this.dip=dip;
		this.dp=dp;
		this.iflags=iflags;
		this.uflags=uflags;
		this.riflags=riflags;
	    this.ruflags=ruflags;
		this.isn=isn;
	    this.risn=risn;
	    this.tag=tag;
		this.rtag=rtag;
		this.pkt=pkt;
		this.oct=oct;																					
		this.rpkt=rpkt;
	    this.roct=roct;
	}
	
	Flow JsonToFlow(String Data)
	{
		Flow myFlow = new Gson().fromJson(Data,Flow.class);
		return myFlow;
	}
    String FlowToJson()
    {
    	Gson gson = new Gson();
    	String json = gson.toJson(this);
    	return json;
    }
    void printFlow()
    {
    	System.out.println(this.FlowToJson());
    }
    void setAnnotation(String key,AS anAS)
    {
    	annotations.put(key,anAS);
    }
    
    public long getOct() {
        return oct;
    }

    public long getPkt() {
        return pkt;
    }

    public long getRoct() {
        return roct;
    }

    public long getRpkt() {
        return rpkt;
    }

    public int getProto() {
        return proto;
    }

    public int getDp() {
        return dp;
    }

    public int getSp() {
        return sp;
    }
    
    public String getStart() {
        return start;
    }
    
    public double getDuration() {
        return duration;
    }
    
    public String getEnd() {
        return end;
    }
}