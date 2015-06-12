package myOSM;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import osm.StreetLink;
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
	
	public List<myOSMWayPart> WayPartsToConnectedNotes = new LinkedList<myOSMWayPart>();
	
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
	
    public Vector<myOSMWayPart> getLinksExcept(myOSMWayPart excludedLink) {
    	// container for street links
    	Vector<myOSMWayPart> linkContainer = new Vector<>();
    	
    	// add all outgoing links except given one
    	for (myOSMWayPart link : this.WayPartsToConnectedNotes) {
    		if (link != excludedLink) {
    			linkContainer.add(link);
    		}
    	}
    	
    	// return extracted outgoing links
    	return linkContainer;
    }
    
    public List<myOSMWayPart> getLinks(){
    	//return all links belongs to this node
    	return this.WayPartsToConnectedNotes;
    }
    
    public void addLink(myOSMWayPart link){
    	
    	this.WayPartsToConnectedNotes.add(link);
    	
    	System.out.println("Error: myOSMNode: addLink(myOSMWayPart link)");
    	
    }
    
    public void removeLink(myOSMWayPart streetLink) {
    	
    	WayPartsToConnectedNotes.remove(streetLink);
    	
    	System.out.println("Error: myOSMNode: removeLink(myOSMWayPart link)");
    }
}
