package route;

import java.awt.Component;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Vector;

import logging.Logger;

import org.jdesktop.swingx.mapviewer.GeoPosition;

import algorithm.MatchedLink;
import algorithm.NRouteAlgorithm;
import cartesian.Coordinates;
import osm.StreetLink;
import osm.StreetMap;
import osm.StreetNode;

public class SelectedNRoute {
	
	private StreetMap streetMap;
	
	private Component drawComponent;
	
	private int lastKnownPosX;						// saves last known x-position
	private int lastKnownPosY;						// saves last known y-position
	
	private ArrayList<StreetLink> streetLinksStart;
	private ArrayList<StreetLink> streetLinksEnd;
	
	private StreetLink selectableLink;
	
	private StreetLink deletableLink;
	
	private boolean isNRouteSplitted = false;
	
	public SelectedNRoute(StreetMap streetMap, NRouteAlgorithm nRouteAlgorithm, Component drawComponent) {
		// call other constructor
		this(streetMap, drawComponent);
		
		// initialize start array list, convert best route to array list
		streetLinksStart = convertNRouteToArrayList(nRouteAlgorithm.getNRoute(0));
	}
	
	public SelectedNRoute(StreetMap streetMap, Component drawComponent) {
		// save references
		this.streetMap = streetMap;
		this.drawComponent = drawComponent;
		
		// initialize array lists 
		streetLinksStart = new ArrayList<>();
		streetLinksEnd = new ArrayList<>();
		
		// initialize mouse position
		saveLastKnownPosition(0, 0);
	}
	
	/**
	 * adds given link to start link set
	 * note: should only used by algorithm not by user
	 * 
	 * @param link
	 * @return
	 */
	public boolean addStartLink(StreetLink link) {
		
		// add link to start links array list
		streetLinksStart.add(link);
		
		return true;
	}
	
	/**
 	 * adds given link to end link set
	 * note: should only used by algorithm not by user
	 * 
	 * @param link
	 * @return
	 */
	public boolean addEndLink(StreetLink link) {
		
		// add link to end links array list
		streetLinksEnd.add(link);
		
		// update state
		isNRouteSplitted = true;
		
		return true;
	}
	
	private ArrayList<StreetLink> convertNRouteToArrayList(Vector<NRoute> nRouteVec) {
		ArrayList<StreetLink> streetLinksArrayList = new ArrayList<>();
		
		for (NRoute nRoute : nRouteVec) {
			// get n route matched links
			Vector<MatchedLink> matchedLinksVector = nRoute.getNRouteLinks();
			
			for (MatchedLink matchedLink : matchedLinksVector) {
				// add to array list
				streetLinksArrayList.add(matchedLink.getStreetLink());
			}
		}
		
		return streetLinksArrayList;
	}
	
	public void setEditableLinks(int x, int y) {
		saveLastKnownPosition(x, y);
		seekSelectableLink();
		seekDeletableLink();
	}
	
	public void addLink(int x, int y) {
		saveLastKnownPosition(x, y);
		
		// add to first part?
		if (!streetLinksStart.isEmpty()) {
			// get first link
			StreetLink streetLink = streetLinksStart.get(0);
			
			if (streetLink.getStartNode().getLinks().contains(selectableLink) ||
				streetLink.getEndNode().getLinks().contains(selectableLink)) {
				streetLinksStart.add(0, selectableLink);
				if (canNRoutesBeMerged()) mergeNRoutes();
				setEditableLinks(lastKnownPosX, lastKnownPosY);
				return;
			}
			
			int streetLinkStartSize = streetLinksStart.size();
			
			if (streetLinkStartSize > 1) {
				
				streetLink = streetLinksStart.get(streetLinkStartSize - 1);
				
				if (streetLink.getStartNode().getLinks().contains(selectableLink) ||
						streetLink.getEndNode().getLinks().contains(selectableLink)) {
						streetLinksStart.add(selectableLink);
						if (canNRoutesBeMerged()) mergeNRoutes();
						setEditableLinks(lastKnownPosX, lastKnownPosY);
						return;
				}
			}
		}
		
		// add to second part?
		if (!streetLinksEnd.isEmpty()) {
			// get first link
			StreetLink streetLink = streetLinksEnd.get(0);
			
			if (streetLink.getStartNode().getLinks().contains(selectableLink) ||
				streetLink.getEndNode().getLinks().contains(selectableLink)) {
				streetLinksEnd.add(0, selectableLink);
				if (canNRoutesBeMerged()) mergeNRoutes();
				setEditableLinks(lastKnownPosX, lastKnownPosY);
				return;
			}
			
			int streetLinkStartSize = streetLinksEnd.size();
			
			if (streetLinkStartSize > 1) {
				
				streetLink = streetLinksEnd.get(streetLinkStartSize - 1);
				
				if (streetLink.getStartNode().getLinks().contains(selectableLink) ||
						streetLink.getEndNode().getLinks().contains(selectableLink)) {
						streetLinksEnd.add(selectableLink);
						if (canNRoutesBeMerged()) mergeNRoutes();
						setEditableLinks(lastKnownPosX, lastKnownPosY);
						return;
				}
			}
		}	
	}
	
	public void deleteLink(int x, int y) {
		saveLastKnownPosition(x, y);
		
		if (isNRouteSplitted) {
			// start part
			if (streetLinksStart.contains(deletableLink)) streetLinksStart.remove(deletableLink);
			
			// end part
			if (streetLinksEnd.contains(deletableLink)) streetLinksEnd.remove(deletableLink);
			
		} else {
			
			// start or end?
			if (!streetLinksStart.isEmpty()) {
				StreetLink streetLinkStart = streetLinksStart.get(0);
				StreetLink streetLinkEnd = streetLinksStart.get(streetLinksStart.size()-1);
				
				if (deletableLink == streetLinkStart || deletableLink == streetLinkEnd) {
					streetLinksStart.remove(deletableLink);
					setEditableLinks(lastKnownPosX, lastKnownPosY);
					return;
				}
			}
			
			// split
			ArrayList<StreetLink> tmpStreetLinksStart = new ArrayList<>();
			ArrayList<StreetLink> tmpStreetLinksEnd = new ArrayList<>();
			
			boolean linkToDeleteFound = false;
			
			// split n route
			for (StreetLink streetLink : streetLinksStart) {
				
				if (streetLink == deletableLink) {
					linkToDeleteFound = true;
					continue;
				}
				
				if (linkToDeleteFound) {
					tmpStreetLinksEnd.add(streetLink);
				} else {
					tmpStreetLinksStart.add(streetLink);
				}
			}
			
			isNRouteSplitted = true;
			streetLinksStart = tmpStreetLinksStart;
			streetLinksEnd = tmpStreetLinksEnd;			
		}
		
		setEditableLinks(x, y);
	}
	
	private void seekSelectableLink() {
		//save nearest point coordinates on street link
		double nearestX;
		double nearestY;
		
        //start and end position of streetLink
        int ax,ay,bx,by;
		
		// save distance/current minimal distance to a street link
		double distance = Double.MAX_VALUE;
        double minDistance = Double.MAX_VALUE;
        
        // store nearest street link here
		StreetLink nearestStreetLink = null;
		
		// get street links which could be expanded
		ArrayList<StreetLink> streetLinksToExpandPool = new ArrayList<>();
		
		if (!streetLinksStart.isEmpty()) {
			streetLinksToExpandPool.add(streetLinksStart.get(0));
			if (streetLinksStart.size() > 1) {
				streetLinksToExpandPool.add(streetLinksStart.get(streetLinksStart.size()-1));
			}
		}
		
		if (!streetLinksEnd.isEmpty()) {
			streetLinksToExpandPool.add(streetLinksEnd.get(0));
			if (streetLinksEnd.size() > 1) {
				streetLinksToExpandPool.add(streetLinksEnd.get(streetLinksEnd.size()-1));
			}
		}
		
		for (StreetLink streetLink : streetLinksToExpandPool) {
			//get StartNode and EndNode of Link i
    		ax = streetLink.getStartX();
    		ay = streetLink.getStartY();
    		bx = streetLink.getEndX();
    		by = streetLink.getEndY();
    		
    		//get distance
    		nearestX=Coordinates.getNearestPointX(lastKnownPosX,lastKnownPosY,ax,ay,bx,by);
    		nearestY=Coordinates.getNearestPointY(lastKnownPosX,lastKnownPosY,ax,ay,bx,by);
    		distance=Coordinates.getDistanceSquared(lastKnownPosX,lastKnownPosY,nearestX,nearestY);
    		
    		//check if distance is below current minimal distance
    		if (distance < minDistance){
    			minDistance = distance;
    			nearestStreetLink=streetLink;
    		}
		}
		
		// get right node
		Vector<StreetLink> adjustableLinks = getOutgoingStreetNode(nearestStreetLink).getLinksExcept(nearestStreetLink);
		
		// reset values
		distance = Double.MAX_VALUE;
        minDistance = Double.MAX_VALUE;
		
		for(StreetLink streetLink : adjustableLinks) {
			
			//get StartNode and EndNode of Link i
    		ax = streetLink.getStartX();
    		ay = streetLink.getStartY();
    		bx = streetLink.getEndX();
    		by = streetLink.getEndY();
    		
    		//get distance
    		nearestX=Coordinates.getNearestPointX(lastKnownPosX,lastKnownPosY,ax,ay,bx,by);
    		nearestY=Coordinates.getNearestPointY(lastKnownPosX,lastKnownPosY,ax,ay,bx,by);
    		distance=Coordinates.getDistanceSquared(lastKnownPosX,lastKnownPosY,nearestX,nearestY);
    		
    		//check if distance is below current minimal distance
    		if (distance < minDistance){
    			minDistance = distance;
    			nearestStreetLink=streetLink;
    		}
			
		}
		
		// get outgoing link for nearest expandable street link
		selectableLink = nearestStreetLink;
	}
	
	private StreetNode getOutgoingStreetNode(StreetLink streetLink) {
		StreetNode startNode = streetLink.getStartNode();
		StreetNode endNode = streetLink.getEndNode();
		
		Vector<StreetLink> startLinks =  startNode.getLinksExcept(streetLink);
		Vector<StreetLink> endLinks = endNode.getLinksExcept(streetLink);
		
		for (StreetLink link : startLinks) {
			if (streetLinksStart.contains(link) || streetLinksEnd.contains(link)) {
				return endNode;
			}
		}
		
		for (StreetLink link : endLinks) {
			if (streetLinksStart.contains(link) || streetLinksEnd.contains(link)) {
				return startNode;
			}
		}
		
		return null;
	}
	
	private boolean canNRoutesBeMerged() {
		if (isNRouteSplitted && !streetLinksStart.isEmpty() && !streetLinksEnd.isEmpty()) {
			StreetLink streetLinkStartLast = streetLinksStart.get(streetLinksStart.size()-1);
			
			Vector<StreetLink> startLinks = streetLinkStartLast.getStartNode().getLinksExcept(streetLinkStartLast);
			Vector<StreetLink> endLinks = streetLinkStartLast.getEndNode().getLinksExcept(streetLinkStartLast);
			
			StreetLink streetLinkEndFirst = streetLinksEnd.get(0);
			
			if (startLinks.contains(streetLinkEndFirst) || endLinks.contains(streetLinkEndFirst)) {
				Logger.println("N Routes merged");
				return true;
			} 
		}
		
		return false;
	}
	
	private void mergeNRoutes() {
		ArrayList<StreetLink> tmpStreetLinksStart = new ArrayList<>();
		
		tmpStreetLinksStart.addAll(streetLinksStart);
		tmpStreetLinksStart.addAll(streetLinksEnd);
		
		streetLinksStart = tmpStreetLinksStart;
		streetLinksEnd.clear();
		
		isNRouteSplitted = false;
	}
	
	private void seekDeletableLink() {
		//save nearest point coordinates on street link
		double nearestX;
		double nearestY;
		
        //start and end position of streetLink
        int ax,ay,bx,by;
		
		// save distance/current minimal distance to a street link
		double distance = Double.MAX_VALUE;
        double minDistance = Double.MAX_VALUE;
        
        // store nearest street link here
		StreetLink nearestStreetLink = null;
		
		// get street links which could be expanded
		ArrayList<StreetLink> streetLinksToExpandPool = new ArrayList<>();
		
		if (isNRouteSplitted) {
			if (!streetLinksStart.isEmpty()) {
				streetLinksToExpandPool.add(streetLinksStart.get(0));
				if (streetLinksStart.size() > 1) {
					streetLinksToExpandPool.add(streetLinksStart.get(streetLinksStart.size()-1));
				}
			}
			
			if (!streetLinksEnd.isEmpty()) {
				streetLinksToExpandPool.add(streetLinksEnd.get(0));
				if (streetLinksEnd.size() > 1) {
					streetLinksToExpandPool.add(streetLinksEnd.get(streetLinksEnd.size()-1));
				}
			}
		} else {
			streetLinksToExpandPool = streetLinksStart;
		}

		for (StreetLink streetLink : streetLinksToExpandPool) {
			//get StartNode and EndNode of Link i
    		ax = streetLink.getStartX();
    		ay = streetLink.getStartY();
    		bx = streetLink.getEndX();
    		by = streetLink.getEndY();
    		
    		//get distance
    		nearestX=Coordinates.getNearestPointX(lastKnownPosX,lastKnownPosY,ax,ay,bx,by);
    		nearestY=Coordinates.getNearestPointY(lastKnownPosX,lastKnownPosY,ax,ay,bx,by);
    		distance=Coordinates.getDistanceSquared(lastKnownPosX,lastKnownPosY,nearestX,nearestY);
    		
    		//check if distance is below current minimal distance
    		if (distance < minDistance){
    			minDistance = distance;
    			nearestStreetLink=streetLink;
    		}
		}
		
		// get outgoing link for nearest expandable street link
		deletableLink = nearestStreetLink;
	}
	
	public ArrayList<StreetLink> getNRouteLinksStart() {
		return streetLinksStart;
	}
	
	public ArrayList<StreetLink> getNRouteLinksEnd() {
		return streetLinksEnd;
	}
	
	public StreetLink getSelectableLink() {
		return selectableLink;
	}
	
	public StreetLink getDeletableLink() {
		return deletableLink;
	}
	
	/**
	 * saves last known mouse
	 * @param x
	 * @param y
	 */
	private void saveLastKnownPosition(int x, int y) {
		lastKnownPosX = x;
		lastKnownPosY = y;
	}
	
	public boolean isNRouteSplit() {
		return isNRouteSplitted;
	}
	
	public void printStartLinks() {
		System.out.print("\nStreetStartLinks: ");
		for (StreetLink link : streetLinksStart) {
			System.out.print(link.getID() + ",");
		}
	}
	
	public void printEndLinks() {
		System.out.print("\nStreetEndLinks: ");
		for (StreetLink link : streetLinksEnd) {
			System.out.print(link.getID() + ",");
		}
	}
	
	public GeoPosition getStartGeoPos() {
		// get first link of start link
		StreetLink startLink = streetLinksStart.get(0);
		
    	return Coordinates.getGeoPos(startLink.getStartX(), startLink.getStartY());
    }
}
