package myOSM;

import osm.StreetLink;

public class myOSMWayPart {

	private static int runID = 0;
	
	public int ObjID = -1;
	
	public myOSMNode startNode;
	public myOSMNode endNode;
	public myOSMWay parentWay;
	public int parentWayStepNr = -1;
	public boolean isBackDirection = false;
	public StreetLink streetLink = null;

	
	
	public myOSMWayPart (myOSMNode n1, myOSMNode n2, myOSMWay way, int StepNr, boolean BackDirection) {
		
		startNode = n1;
		endNode = n2;
		parentWay = way;
		parentWayStepNr = StepNr;
		isBackDirection = BackDirection;
		
		ObjID = runID;
		runID++;
		
		startNode.WayPartsToConnectedNotes.add(this);
	}
	
}
