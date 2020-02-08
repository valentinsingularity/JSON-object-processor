package valentin.flow;
import java.util.ArrayList;
import java.util.List;

public class TimedFlowCounter extends App
{
	public int binDuration;
	public List<Flow> filteredFlows;
	public List<FlowBin> binList;
	public Sting description;
	public FlowFilter filter;
	public FlowGrouper grouper;
	
	TimedFlowCounter(int binDuration,FlowFilter filter,FlowGrouper grouper, String description)
	{
		this.binDuration = binDuration;
		filteredFlows = new ArrayList<Flow>();
		binList = new ArrayList<FlowBin>();
		this.description = description;
		this.filter = filter;
		this.grouper = grouper;
	}
	
	
}