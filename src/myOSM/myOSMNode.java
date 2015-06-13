package myOSM;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import cartesian.Coordinates;

public class myOSMNode {

	public long id = -1;
		
	public double lon = -1;
	public double lat = -1;

	public int x = -1;
	
	public int y = -1;
	
	public int countIsStartOfWay = 0;
	
	public int countIsEndOfWay = 0;
	
	public int countIsInnerNoteofWay = 0;
	
	private List<myOSMWayPart> WayPartsOutgoing = new LinkedList<myOSMWayPart>();

	private List<myOSMWayPart> WayPartsIncoming = new LinkedList<myOSMWayPart>();
	
	public myOSMNode() {}
	
    public myOSMNode(int x, int y, long id) {
    	this.x = x;
    	this.y = y;
        
        this.id = id;
    }
	
	public void setXY() {
		if (lon != -1 && lat != -1) {
			x = Coordinates.getCartesianX(lon, lat);
			y = Coordinates.getCartesianY(lon, lat);			
		}
	}
	
	public void WayPartsOutgoing_add(myOSMWayPart wp) {
		WayPartsOutgoing.add(wp);
		
		wp.endNode.WayPartsIncoming.add(wp);
	}

	public int WayPartsOutgoing_size() {
		 return WayPartsOutgoing.size();
	}

	public myOSMWayPart WayPartsOutgoing_get(int index) {
		 return WayPartsOutgoing.get(index);
	}
	
	public boolean WayPartsOutgoing_contains(myOSMWayPart wp) {
		 return WayPartsOutgoing.contains(wp);
	}

	public boolean WayPartsIncoming_contains(myOSMWayPart wp) {
		 return this.WayPartsIncoming.contains(wp);
	}

    public Vector<myOSMWayPart> getOutgoingWayPartExceptNotTo(myOSMNode excludedNode) {
    	// container for street links
    	Vector<myOSMWayPart> linkContainer = new Vector<>();
    	
    	// add all outgoing links except given one
    	for (myOSMWayPart link : this.WayPartsOutgoing) {
    		if (link.endNode != excludedNode) {
    			linkContainer.add(link);
    		}
    	}
    	
    	// return extracted outgoing links
    	return linkContainer;
    }

    public Vector<myOSMWayPart> getIncomingWayPartExceptNotFrom(myOSMNode excludedNode) {
    	// container for street links
    	Vector<myOSMWayPart> linkContainer = new Vector<>();
    	
    	// add all outgoing links except given one
    	for (myOSMWayPart link : this.WayPartsIncoming) {
    		if (link.startNode != excludedNode) {
    			linkContainer.add(link);
    		}
    	}
    	
    	// return extracted outgoing links
    	return linkContainer;
    }

    public Vector<myOSMWayPart> getIncomingWayPartExceptNotTo(myOSMNode excludedNode) {
    	// container for street links
    	Vector<myOSMWayPart> linkContainer = new Vector<>();
    	
    	// add all outgoing links except given one
    	for (myOSMWayPart link : this.WayPartsIncoming) {
    		if (link.endNode != excludedNode) {
    			linkContainer.add(link);
    		}
    	}
    	
    	// return extracted outgoing links
    	return linkContainer;
    }
	
    public Vector<myOSMWayPart> getOutgoingWayPartExcept(myOSMWayPart excludedLink) {
    	// container for street links
    	Vector<myOSMWayPart> linkContainer = new Vector<>();
    	
    	// add all outgoing links except given one
    	for (myOSMWayPart link : this.WayPartsOutgoing) {
    		if (link != excludedLink) {
    			linkContainer.add(link);
    		}
    	}
    	
    	// return extracted outgoing links
    	return linkContainer;
    }
    
    public List<myOSMWayPart> getLinks(){
    	//return all links belongs to this node
    	return this.WayPartsOutgoing;
    }
    
    public void addLink(myOSMWayPart link){
    	
    	this.WayPartsOutgoing.add(link);
    	
    	System.out.println("Error: myOSMNode: addLink(myOSMWayPart link)");
    	
    }
    
    public void removeLink(myOSMWayPart streetLink) {
    	
    	WayPartsOutgoing.remove(streetLink);
    	
    	System.out.println("Error: myOSMNode: removeLink(myOSMWayPart link)");
    }
}
