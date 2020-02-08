package valentin.flow;
import com.google.gson.*;
import com.google.gson.reflect.*;
import java.util.*;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Type; 
import java.time.Instant;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.text.DateFormat;
import java.lang.Long;
import java.lang.annotation.*;
import java.sql.*;


public class App
{	
	
	public static long StringToMSeconds(String s)
    {
		try{ 
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH);
	    java.util.Date date =  df.parse(s);  
	    Instant instant = Instant.ofEpochSecond(date.getTime());
	    return instant.getEpochSecond();
		} 
     	catch(ParseException e) { return 0;}
	  }
	
	
	public static void main(String[] args) throws Exception{
 
		
		   /*  
	    Comparator<Flow> byStart = new Comparator<Flow>(){
	    	public int compare(Flow f1,Flow f2)
	    	{
	    		if(StringToMSeconds(f1.start) < StringToMSeconds(f2.start)) return -1;
	    		else if(StringToMSeconds(f1.start) == StringToMSeconds(f2.start)) return 0;
	    		else return 1;
	    	}

	    Collections.sort(flowlist,byStart);
	 
	    long binDuration=100; //seconds
	    long earliestStart = StringToMSeconds(flowlist.get(0).start);
	    
	    long flowStart,flowEnd;
	    int i;
	    List<FlowBin> tcpBinList = new ArrayList<FlowBin>();
	    for(Flow x : flowlist)
	    {
	      if(filterTCP.test(x))
	      {
	    	  
	    	flowStart=StringToMSeconds(x.start);
	    	flowEnd=StringToMSeconds(x.end);
	    	
	    	int binNumberStart= (int) ((flowStart - earliestStart)/(1000*binDuration));
	    	int binNumberEnd= (int) ((flowEnd - earliestStart)/(1000*binDuration));
	    	double ratio;
	    	
	        for(i=binNumberStart;i<=binNumberEnd;i++) 
	        {
	        	if(i==binNumberStart) ratio= i+1- (flowStart - earliestStart)/(1000*binDuration);
	        	else if(i==binNumberEnd) ratio= (flowEnd - earliestStart)/(1000*binDuration)-i;
	            else ratio=1; 
	        	if(tcpBinList.size()==0 || ( i*binDuration > tcpBinList.get(tcpBinList.size()-1).start ) )
	        	{
	        	   tcpBinList.add(new FlowBin("TCP flows",binDuration,i*binDuration,(i+1)*binDuration));
	        	   tcpBinList.get(tcpBinList.size()-1).addFlow(x,ratio);
	        	}
	        	else 
	        		for(FlowBin bin : tcpBinList) 
	        			if(i*binDuration==bin.start)
	        				{
	        				bin.addFlow(x,ratio);
	        				break;
	        				}
	        }	        
	      }   
	    }
	    */
	    
	    List<Flow> flowlist = new ArrayList<Flow>();
		Type collectionType = new TypeToken<ArrayList<Flow>>(){}.getType();
		Gson gson = new Gson();
		for(int i=0; i < args.length; i++)
		{
			try{
				BufferedReader reader = new BufferedReader(new FileReader(args[i]));
				flowlist = gson.fromJson(reader,collectionType);
			}
			finally{
			}
		}
	    
	    AS srcAS=null;
		AS destAS=null;
		Map<String,AS> asList = new HashMap<String,AS>(); 
		
		Class.forName("org.sqlite.JDBC");
        Connection conn = DriverManager.getConnection("jdbc:sqlite:ripe.db");
        Statement stat = conn.createStatement();
        stat.executeUpdate("CREATE TABLE IF NOT EXISTS src_iptable (ip VARCHAR(42), prefix VARCHAR(46) not NULL, asn INTEGER not NULL, holder VARCHAR(255));");
        stat.executeUpdate("CREATE TABLE IF NOT EXISTS dest_iptable (ip VARCHAR(42), prefix VARCHAR(46) not NULL, asn INTEGER not NULL, holder VARCHAR(255));");
        int ok;
        ResultSet rs;
        PreparedStatement src_prep = conn.prepareStatement("INSERT INTO src_iptable VALUES (?, ?, ?, ?);");
        PreparedStatement dest_prep = conn.prepareStatement("INSERT INTO dest_iptable VALUES (?, ?, ?, ?);");
        PreparedStatement src_prep2= conn.prepareStatement("SELECT * FROM src_iptable WHERE ip = ?");
        PreparedStatement dest_prep2= conn.prepareStatement("SELECT * FROM dest_iptable WHERE ip = ?");
		for(Flow aFlow : flowlist)
	    {
			src_prep2.setString(1, aFlow.sip);
			rs = src_prep2.executeQuery();
			if(rs.getRow()!=0) srcAS = asList.get(aFlow.sip);
			else
			{
				srcAS = new AS(aFlow.sip);
				asList.put(aFlow.sip,srcAS);
				src_prep.setString(1,aFlow.sip);
				src_prep.setString(2,srcAS.prefix);
				src_prep.setInt(3,srcAS.ASN);
				src_prep.setString(4,srcAS.holder);
				src_prep.addBatch();
				src_prep.executeBatch();
			}
			
			
			dest_prep2.setString(1, aFlow.sip);
			rs = dest_prep2.executeQuery();
			if(rs.getRow()!=0) destAS = asList.get(aFlow.dip);
			else
			{
				destAS = new AS(aFlow.dip);
				asList.put(aFlow.dip,destAS);
				dest_prep.setString(1,aFlow.dip);
				dest_prep.setString(2,destAS.prefix);
				dest_prep.setInt(3,destAS.ASN);
				dest_prep.setString(4,destAS.holder);
				dest_prep.addBatch();
				dest_prep.executeBatch();
			}		
			

	        aFlow.setAnnotation("src-as",srcAS);
	        aFlow.setAnnotation("dest-as",destAS);
	    }
		
		

		
        FlowFilter filterUDP = new FlowFilter(){
	        public boolean test(Flow aFlow)
	    	{
	    		return (aFlow.proto == 17);
	    	}
	    };
	    FlowFilter filterICMPv6 = new FlowFilter(){
	        public boolean test(Flow aFlow)
	    	{
	    		return (aFlow.proto == 58);
	    	}
	    };
	    FlowFilter filterTCP = new FlowFilter(){
	        public boolean test(Flow aFlow)
	    	{
	    		return (aFlow.proto == 6);
	    	}
	    };
	    FlowFilter filterHTTP = new FlowFilter(){
	        public boolean test(Flow aFlow)
	    	{
	    		return (aFlow.proto == 6 && (aFlow.dp == 80 || aFlow.dp == 443 ));
	    	}
	    };
	    FlowFilter filterQUIC = new FlowFilter(){
	        public boolean test(Flow aFlow)
	    	{
	    		return (aFlow.proto == 17 && (aFlow.dp == 80 || aFlow.dp == 443 ));
	    	}
	    };
	    FlowFilter filterOthers = new FlowFilter(){
	        public boolean test(Flow aFlow)
	    	{
	    		return (aFlow.proto != 17 && aFlow.proto != 58  && aFlow.proto != 6  );
	    	}
	    };
        
	    
		FlowGrouper groupBySourceAS = new FlowGrouper(){
	        public String group(Flow aFlow)
	    	{
	    		return aFlow.annotations.get("src-as").prefix;
	    	}
	    };
	    
	    FlowGrouper groupByDestinationAS = new FlowGrouper(){
	        public String group(Flow aFlow)
	    	{
	    		return aFlow.annotations.get("dest-as").prefix;
	    	}
	    };
	    
	    List<TimedFlowCounter> pipelines = new ArrayList<TimedFlowCounter>();
	    piplines.add(new TimedFlowCounter(100, filterUDP, groupBySourceAS, "udp-flows-by-src-as"));
	    piplines.add(new TimedFlowCounter(100, filterICMPv6, groupBySourceAS, "icmpv6-flows-by-src-as"));
	    piplines.add(new TimedFlowCounter(100, filterTCP, groupBySourceAS, "tcp-flows-by-src-as"));
	    piplines.add(new TimedFlowCounter(100, filterHTTP, groupBySourceAS, "http-flows-by-src-as"));
	    piplines.add(new TimedFlowCounter(100, filterQUIC, groupBySourceAS, "quick-flows-by-src-as"));
	    piplines.add(new TimedFlowCounter(100, filterOthers, groupBySourceAS, "other-flows-by-src-as"));
	    piplines.add(new TimedFlowCounter(100, filterUDP, groupByDestinationAS, "udp-flows-by-dest-as"));
	    piplines.add(new TimedFlowCounter(100, filterICMPv6, groupByDestinationAS, "icmpv6-flows-by-dest-as"));
	    piplines.add(new TimedFlowCounter(100, filterTCP, groupByDestinationAS, "tcp-flows-by-dest-as"));
	    piplines.add(new TimedFlowCounter(100, filterHTTP, groupByDestinationAS, "http-flows-by-dest-as"));
	    piplines.add(new TimedFlowCounter(100, filterQUIC, groupByDestinationAS, "quick-flows-by-dest-as"));
	    piplines.add(new TimedFlowCounter(100, filterOthers, groupByDestinationAS, "other-flows-by-dest-as"));
	    
	    for(Flow aFlow : flowlist)
	    	for(TimedFlowCounter aPipeline : pipelines)
	    		if(aPipeline.getFilter().test(aFlow)) TimedFlowCounter.filteredFlows.add(aFlow);  
	    
	    Comparator<Flow> byStart = new Comparator<Flow>(){
	    	public int compare(Flow f1,Flow f2)
	    	{
	    		if(StringToMSeconds(f1.start) < StringToMSeconds(f2.start)) return -1;
	    		else if(StringToMSeconds(f1.start) == StringToMSeconds(f2.start)) return 0;
	    		else return 1;
	    		
	    for(TimedFlowCounter aPipeline : pipelines)
	    {
		    Collections.sort(aPipeline.filteredFlows,byStart)
		    long earliestStart = StringToMSeconds(aPipeline.filteredFlows.get(0).start);
		    for(Flow aFLow : aPipeline.filteredFlows)
		    {
		       long flowStart=StringToMSeconds(aFlow.start);
	       	   long flowEnd=StringToMSeconds(aFlow.end);
	       	   int binNumberStart= (int) ((flowStart - earliestStart)/(1000*aPipline.binDuration));
	     	   int binNumberEnd= (int) ((flowEnd - earliestStart)/(1000*aPipeline.binDuration));
	     	   if(aPipeline.binList.size()==0 || ( i*aPipeline.binDuration > binList.get(aPipeline.binList.size()-1).start ) )
	        	{
	     		  aPipeline.binList.add(new FlowBin(aPipeline.binDuration,i*aPipeline.binDuration);
	     		  aPipeline.binList.get(tcpBinList.size()-1).addFlow(aFlow);
	        	}
	        	else 
	        		for(FlowBin bin : aPipeline.binList) 
	        			if(bin.start < flowStart && bin.end > flowEnd)
	        				{
	        				bin.addFlow(aFlow);
	        				break;
	        				}
		    }
	    }
	   
	    
	    Connection conn2 = DriverManager.getConnection("jdbc:sqlite:flows.db");
        Statement stmt = conn2.createStatement();
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS udp_flows_by_src_as(time INTEGER not NULL, group VARCHAR(255), type VARCHAR(4) not NULL, min INTEGER not NULL, q1 INTEGER not NULL,median INTEGER not NULL,q3 INTEGER not NULL,max INTEGER not NULL, size INTEGER not NULL,sum INTEGER not NULL);");
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS icmpv6_flows_by_src_as(time INTEGER not NULL, group VARCHAR(255), type VARCHAR(4) not NULL, min INTEGER not NULL, q1 INTEGER not NULL,median INTEGER not NULL,q3 INTEGER not NULL,max INTEGER not NULL, size INTEGER not NULL,sum INTEGER not NULL);");
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS tcp_flows_by_src_as(time INTEGER not NULL, group VARCHAR(255), type VARCHAR(4) not NULL, min INTEGER not NULL, q1 INTEGER not NULL,median INTEGER not NULL,q3 INTEGER not NULL,max INTEGER not NULL, size INTEGER not NULL,sum INTEGER not NULL);");
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS http_flows_by_src_as(time INTEGER not NULL, group VARCHAR(255), type VARCHAR(4) not NULL, min INTEGER not NULL, q1 INTEGER not NULL,median INTEGER not NULL,q3 INTEGER not NULL,max INTEGER not NULL, size INTEGER not NULL,sum INTEGER not NULL);");
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS quic_flows_by_src_as(time INTEGER not NULL, group VARCHAR(255), type VARCHAR(4) not NULL, min INTEGER not NULL, q1 INTEGER not NULL,median INTEGER not NULL,q3 INTEGER not NULL,max INTEGER not NULL, size INTEGER not NULL,sum INTEGER not NULL);");
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS other_flows_by_src_as(time INTEGER not NULL, group VARCHAR(255), type VARCHAR(4) not NULL, min INTEGER not NULL, q1 INTEGER not NULL,median INTEGER not NULL,q3 INTEGER not NULL,max INTEGER not NULL, size INTEGER not NULL,sum INTEGER not NULL);");
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS udp_flows_by_dest_as(time INTEGER not NULL, group VARCHAR(255), type VARCHAR(4) not NULL, min INTEGER not NULL, q1 INTEGER not NULL,median INTEGER not NULL,q3 INTEGER not NULL,max INTEGER not NULL, size INTEGER not NULL,sum INTEGER not NULL);");
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS icmpv6_flows_by_dest_as(time INTEGER not NULL, group VARCHAR(255), type VARCHAR(4) not NULL, min INTEGER not NULL, q1 INTEGER not NULL,median INTEGER not NULL,q3 INTEGER not NULL,max INTEGER not NULL, size INTEGER not NULL,sum INTEGER not NULL);");
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS tcp_flows_by_dest_as(time INTEGER not NULL, group VARCHAR(255), type VARCHAR(4) not NULL, min INTEGER not NULL, q1 INTEGER not NULL,median INTEGER not NULL,q3 INTEGER not NULL,max INTEGER not NULL, size INTEGER not NULL,sum INTEGER not NULL);");
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS http_flows_by_dest_as(time INTEGER not NULL, group VARCHAR(255), type VARCHAR(4) not NULL, min INTEGER not NULL, q1 INTEGER not NULL,median INTEGER not NULL,q3 INTEGER not NULL,max INTEGER not NULL, size INTEGER not NULL,sum INTEGER not NULL);");
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS quic_flows_by_dest_as(time INTEGER not NULL, group VARCHAR(255), type VARCHAR(4) not NULL, min INTEGER not NULL, q1 INTEGER not NULL,median INTEGER not NULL,q3 INTEGER not NULL,max INTEGER not NULL, size INTEGER not NULL,sum INTEGER not NULL);");
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS other_flows_by_dest_as(time INTEGER not NULL, group VARCHAR(255), type VARCHAR(4) not NULL, min INTEGER not NULL, q1 INTEGER not NULL,median INTEGER not NULL,q3 INTEGER not NULL,max INTEGER not NULL, size INTEGER not NULL,sum INTEGER not NULL);");
        
        PreparedStatement prepStatement;
        
        for(TimedFlowCounter aPipeline : pipelines)
        {
        prepStatement = conn2.prepareStatement("INSERT INTO ? VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
        prepStatement.setString(1,aPipeline.getDescription());
        prepStatement.setInteger(2,aPipeline.);
        prepStatement.setString(3,);
        prepStatement.setString(4,);
        prepStatement.setInteger(5,);
        prepStatement.setInteger(6,);
        prepStatement.setInteger(7,);
        prepStatement.setInteger(8,);
        prepStatement.setInteger(9,);
        prepStatement.setInteger(10,);
        prepStatement.setInteger(11,);
        prepStatementp.addBatch();
        prepStatement.executeBatch();
        }
        
        
	    /*
	    
	    Map<String,List<Flow>> srcFlowGroups = new HashMap<String,List<Flow>>();
	    Map<String,List<Flow>> destFlowGroups = new HashMap<String,List<Flow>>();
	    for(Flow aFlow : flowlist)
	    {
	       String key=groupBySourceAS.group(aFlow);
	       if(srcFlowGroups.get(key)==null)
	       {
	    	   List<Flow> list = new ArrayList<Flow>();
	    	   list.add(aFlow);
	    	   srcFlowGroups.put(key,list);
	       }
	       else srcFlowGroups.get(key).add(aFlow);
	       
	       key=groupByDestinationAS.group(aFlow);
	       if(destFlowGroups.get(key)==null)
	       {
	    	   List<Flow> list = new ArrayList<Flow>();
	    	   list.add(aFlow);
	    	   destFlowGroups.put(key,list);
	       }
	       else destFlowGroups.get(key).add(aFlow);
	       
	       
	    }
	    
/*	    Quantile pkts, octs, rpkts, rocts;
	    int i=0;
	    for(Map.Entry<String, List<Flow>> entry : srcFlowGroups.entrySet())
	    {
	    	i++;
	        String key = entry.getKey();
	        List<Flow> list = entry.getValue();
	        String holder = list.get(0).annotations.get("dest-as").holder;
	        int ASN = list.get(0).annotations.get("dest-as").ASN;
	        pkts = new Quantile();
	        octs = new Quantile();
	        rpkts = new Quantile();
	        rocts = new Quantile();
	        for(Flow x : list)
			{
	            pkts.addNumber(x.getPkt());
	            octs.addNumber(x.getOct());
	            rpkts.addNumber(x.getRpkt());
	            rocts.addNumber(x.getRoct());
			}
	        
	        System.out.format("Source Group: " + i + "\nPrefix: %s\nASN: %d\nHolder: %s\n",key,ASN,holder);
	        System.out.format("Pkts:   min:%d f_q:%d  med:%d  t_q:%d  max:%d sum:%d\n",pkts.getMin(),pkts.getFirstQuantile(),pkts.getMedian(),pkts.getThirdQuantile(),pkts.getMax(),pkts.getSum());
	        System.out.format("Octs:   min:%d f_q:%d  med:%d  t_q:%d  max:%d sum:%d\n",octs.getMin(),octs.getFirstQuantile(),octs.getMedian(),octs.getThirdQuantile(),octs.getMax(),octs.getSum());
	        System.out.format("Rpkts:   min:%d f_q:%d  med:%d  t_q:%d  max:%d sum:%d\n",rpkts.getMin(),rpkts.getFirstQuantile(),rpkts.getMedian(),rpkts.getThirdQuantile(),rpkts.getMax(),rpkts.getSum());
	        System.out.format("Rocts:   min:%d f_q:%d  med:%d  t_q:%d  max:%d sum:%d\n\n",rocts.getMin(),rocts.getFirstQuantile(),rocts.getMedian(),rocts.getThirdQuantile(),rocts.getMax(),rocts.getSum());
	    }
	    
	    i=0;
	    for(Map.Entry<String, List<Flow>> entry : destFlowGroups.entrySet())
	    {
	    	i++;
	        String key = entry.getKey();
	        List<Flow> list = entry.getValue();
	        String holder = list.get(0).annotations.get("dest-as").holder;
	        int ASN = list.get(0).annotations.get("dest-as").ASN;
	        pkts = new Quantile();
	        octs = new Quantile();
	        rpkts = new Quantile();
	        rocts = new Quantile();
	        for(Flow x : list)
			{
	            pkts.addNumber(x.getPkt());
	            octs.addNumber(x.getOct());
	            rpkts.addNumber(x.getRpkt());
	            rocts.addNumber(x.getRoct());
			}
	        
	        System.out.format("Destination Group: " + i + "\nPrefix: "+key+"\nASN: "+ASN+"\nHolder: "+holder+"\n");
	        System.out.format("Pkts:   min:%d f_q:%d  med:%d  t_q:%d  max:%d sum:%d\n",pkts.getMin(),pkts.getFirstQuantile(),pkts.getMedian(),pkts.getThirdQuantile(),pkts.getMax(),pkts.getSum());
	        System.out.format("Octs:   min:%d f_q:%d  med:%d  t_q:%d  max:%d sum:%d\n",octs.getMin(),octs.getFirstQuantile(),octs.getMedian(),octs.getThirdQuantile(),octs.getMax(),octs.getSum());
	        System.out.format("Rpkts:   min:%d f_q:%d  med:%d  t_q:%d  max:%d sum:%d\n",rpkts.getMin(),rpkts.getFirstQuantile(),rpkts.getMedian(),rpkts.getThirdQuantile(),rpkts.getMax(),rpkts.getSum());
	        System.out.format("Rocts:   min:%d f_q:%d  med:%d  t_q:%d  max:%d sum:%d\n\n",rocts.getMin(),rocts.getFirstQuantile(),rocts.getMedian(),rocts.getThirdQuantile(),rocts.getMax(),rocts.getSum());
	    }
	
	}
	*/
}