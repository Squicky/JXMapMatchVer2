package myOSM;

import java.util.LinkedList;
import java.util.List;

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
	
	public void setXY() {
		if (lon != -1 && lat != -1) {
			x = Coordinates.getCartesianX(lon, lat);
			y = Coordinates.getCartesianY(lon, lat);			
		}
	}
}
