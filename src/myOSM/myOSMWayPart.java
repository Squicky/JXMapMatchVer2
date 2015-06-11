package myOSM;

import osm.StreetLink;

public class myOSMWayPart {

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
		
		startNode.WayPartsToConnectedNotes.add(this);
	}
	
}
