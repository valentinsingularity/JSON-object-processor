package valentin.flow;
import com.google.gson.*;
import com.google.gson.annotations.*;
import java.util.ArrayList;
import java.util.List;



public class FlowBin extends App
{
	@SerializedName("descr")
	String descr;
	@SerializedName("pkts")
	long pkts;
	@SerializedName("octs")
	Long octs;
	@SerializedName("rpkts")
	Long rpkts;
	@SerializedName("rocts")
	Long rocts;
	@SerializedName("flows")
	long flows;
	@SerializedName("start")
	long start;
	@SerializedName("end")
	long end;
	Quantile pktsStats, octsStats, rpktsStats, roctsStats;
	
	
	public FlowBin(long start, long end) {
        pkts = octs = rpkts = rocts = flows = 0;
        this.descr = descr;
        this.start = start;
        this.end = end;
        pktsStats = new Quantile();
        octsStats = new Quantile();
        rpktsStats = new Quantile();
        roctsStats = new Quantile();
    }	

    public void addFlow(Flow f) {
        pkts += f.getPkt();
        pktsStats.addNumber(f.getPkt());
        octs += f.getOct();
        octsStats.addNumber(f.getOct());
        rpkts += f.getRpkt();
        rpktsStats.addNumber(f.getRpkt());
        rocts += f.getRoct();
        roctsStats.addNumber(f.getRoct());
        flows++;
    }
    
    public void addFlowPortion(Flow f, double portion) {
        long newPkts = (long)(f.getPkt() * portion);
        pkts += newPkts;
        pktsStats.addNumber(newPkts);
        
        long newOcts = (long)(f.getOct() * portion);
        octs += newOcts;
        octsStats.addNumber(newOcts);
        
        long newRpkts = (long)(f.getRpkt() * portion);
        rpkts += newRpkts;
        rpktsStats.addNumber(newRpkts);
        
        long newRocts = (long)(f.getRoct() * portion);
        rocts += newRocts;
        roctsStats.addNumber(newRocts);
        
        flows++;
    }

    public long getPkts() {
        return pkts;
    }

    public long getOcts() {
        return octs;
    }

    public long getRpkts() {
        return rpkts;
    }

    public long getRocts() {
        return rocts;
    }

    public long getFlows() {
        return flows;
    }
    
    public long getStart() {
        return start;
    }
    
    public long getEnd() {
        return end;
    }
    
    public Quantile getPktsStats() {
        return pktsStats;
    }
    
    public Quantile getOctsStats() {
        return octsStats;
    }
    
    public Quantile getRoctsStats() {
        return roctsStats;
    }
    
    public Quantile getRpktsStats() {
        return rpktsStats;
    }
    
	String BinToJson()
    {
    	Gson gson = new Gson();
    	String json = gson.toJson(this);
    	return json;
    }
    void printBin()
    {
    	System.out.println(this.BinToJson());
    }
	
	
}