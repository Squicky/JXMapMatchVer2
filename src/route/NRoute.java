package route;

import java.util.Vector;

import algorithm.MatchedLink;
import logging.Logger;
import cartesian.Coordinates;
import gps.GPSNode;
import gps.GPSTrace;
import osm.StreetLink;
import osm.StreetNode;
import static osm.StreetLink.*;

/**
 * @author Daniel Sathees Elmo
 * 
 * this class represents an route which will be build by the N Route algorithm
 * it also implements the comparable interface so it can be automatically added
 * into an sorted set (uses compareTo method to sort)
 */

public class NRoute implements Comparable<NRoute>, Cloneable {
	// constants for compareTo method
	public static final int EQUAL_SCORE = 0;
	public static final int BETTER_SCORE = -1;
	public static final int WORSE_SCORE = 1;
	
	// save reference to GPS trace
	private GPSTrace gpsTrace; 
	
	// GPS node index offset for looking points in future
	private int gpsNodeIndexOffset;
	
	// values for offset (in order to determine outgoing node of a link
	// we look a GPS node in future and measure shortest distance to a link)
	public static final int DEFAULT_GPS_NODE_INDEX_OFFSET = 5;
	public static final int MAX_GPS_NODE_INDEX_OFFSET = 10;
	
	// save matched street links added to route as matched link
	private Vector<MatchedLink> nRouteLinks;
	
	// save score of this route in respect of GPS trace
	private double score;
	
	// save (optional) reference to previous n route
	private NRoute previousNRoute;
	
	// should current GPS node be matched to previous link?
	private boolean matchToPreviousLink;
	
	// reference to previous matched link
	private MatchedLink previousMatchedLink;
	
	/**
	 * initialize with GPS trace
	 * @param gpsTrace
	 */
	public NRoute(GPSTrace gpsTrace){
		// save reference
		this.gpsTrace = gpsTrace;
		// initialize vector
		this.nRouteLinks = new Vector<MatchedLink>();
		// set score zero at beginning
		score = 0;
		// set previous route null for now
		this.previousNRoute = null;
		// set default GPS node index offset
		this.gpsNodeIndexOffset = DEFAULT_GPS_NODE_INDEX_OFFSET;
	}
	
	/**
	 * add link to n route container, set matched range, and set as matched
	 * @param streetLink
	 * @param minGPSNodeIndex
	 * @param maxGPSNodeIndex
	 */
	public void addLink(StreetLink streetLink, int minGPSNodeIndex, int maxGPSNodeIndex) {	
		// create new matched link
		MatchedLink matchedLink = new MatchedLink(streetLink, minGPSNodeIndex, maxGPSNodeIndex);
		// add link and range
		nRouteLinks.add(matchedLink);
		// update score
		updateScore( getScoreForLink(matchedLink) );
	}
	
	
	
	/**
	 * add link to n route container, set matched range, and set as matched
	 * @param streetLink
	 * @param minGPSNodeIndex
	 * @param maxGPSNodeIndex
	 */
	/*
	public void addLink(StreetLink streetLink, int minGPSNodeIndex, int maxGPSNodeIndex) {
		
		
		
		// create new matched link
		MatchedLink matchedLink = new MatchedLink(streetLink, minGPSNodeIndex, maxGPSNodeIndex);
		// add link and range
		nRouteLinks.add(matchedLink);
		// update score
		updateScore( getScoreForLink(matchedLink) );
	}
	*/
	
	
	/**
	 * add link to n route container, set matched range, and set as matched
	 * @param streetLink
	 * @param gpsNodeIndex
	 */
	public void addLink(StreetLink streetLink, int gpsNodeIndex) {
		// we assume that given GPS node should be matched to previous link due to better score
		matchToPreviousLink = true;
		
		// check if n route link vector isn't empty
		if (!nRouteLinks.isEmpty()) {
			
			// get previous matched link and new GPS node
			previousMatchedLink = nRouteLinks.lastElement();
			StreetLink previousStreetLink = previousMatchedLink.getStreetLink();
			GPSNode gpsNode = gpsTrace.getNode(gpsNodeIndex);
			
			// calculate distance to previous and new link
			double distanceToLastMatchedLink = Coordinates.getDistance(gpsNode, previousStreetLink);
			double distanceToNewStreetLink = Coordinates.getDistance(gpsNode, streetLink);
			
			// if distance to previous link less...
			if (distanceToLastMatchedLink < distanceToNewStreetLink) {
				// ...match new GPS node to previous link
				addGPSNodeToLastLink(gpsNodeIndex);
				
				// create "unmatched" link for new link and add it to n route link vector
				MatchedLink matchedLink = new MatchedLink(streetLink, -1, -1);
				nRouteLinks.add(matchedLink);
				
				// done
				return;
			}
			
		}
		
		// if distance to new link is shorter or n route link vector is empty, create/match to new link
		addLink(streetLink, gpsNodeIndex, gpsNodeIndex);
		matchToPreviousLink = false;
	}
	
	/**
	 * adds an GPS node to last added link
	 * @param GPSNodeIndex
	 * @return boolean
	 */
	public boolean addGPSNodeToLastLink(int GPSNodeIndex) {
		// add gpsNode to range of last added link, if container is not empty
		if (!nRouteLinks.isEmpty()) {
			
			// get last added link
			MatchedLink lastAddedMatchedLink = nRouteLinks.lastElement();
			
			// if last GPS node was matched to previous link, check if we can match the new GPS node
			// to new link
			if (matchToPreviousLink) {
				
				// get last link, previous matched link and new GPS node
				StreetLink lastStreetLink = lastAddedMatchedLink.getStreetLink();
				StreetLink previousStreetLink = previousMatchedLink.getStreetLink();
				GPSNode gpsNode = gpsTrace.getNode(GPSNodeIndex);
				
				// calculate distance to previous matched and last added link
				double distanceToPreviousMatchedLink = Coordinates.getDistance(gpsNode, previousStreetLink);
				double distanceToLastAddedLink = Coordinates.getDistance(gpsNode, lastStreetLink);
				
				// if distance to previous link less...
				if (distanceToLastAddedLink < distanceToPreviousMatchedLink) {
					// ... match to new link
					lastAddedMatchedLink.setRangeStartIndex(GPSNodeIndex);
					matchToPreviousLink = false;
				} 
			}
			
			// update range
			lastAddedMatchedLink.setRangeEndIndex(GPSNodeIndex);
			// update score for just last added GPS node index
			updateScore( getScoreForLinkAndRange(lastAddedMatchedLink, 
												 lastAddedMatchedLink.getRangeEndIndex(),
												 lastAddedMatchedLink.getRangeEndIndex()) );
			
			// adding successful
			return true;
		}
		
		// no link there, return false
		return false;
	}
	
	/**
	 * adds an GPS node to last added link
	 * @param GPSNodeIndex
	 * @return boolean
	 */
	/*
	public boolean addGPSNodeToLastLink(int GPSNodeIndex) {
		// add gpsNode to range of last added link, if container is not empty
		if (!nRouteLinks.isEmpty()) {
			// get last added link
			MatchedLink lastAddedMatchedLink = nRouteLinks.lastElement();
			// update range
			lastAddedMatchedLink.setRangeEndIndex(GPSNodeIndex);
			// update score for just last added GPS node index
			updateScore( getScoreForLinkAndRange(lastAddedMatchedLink, 
												 lastAddedMatchedLink.getRangeEndIndex(),
												 lastAddedMatchedLink.getRangeEndIndex()) );
			
			// adding successful
			return true;
		}
		
		// otherwise return false
		return false;
	}
	*/
	
	/**
	 * removes last matched GPS if possible
	 * @return boolean
	 */
	public boolean removeLastGPSNodeFromLastLink() {
		// remove last matched GPS node from last link
		// if route container isn't empty and we got at least one matched GPS node
		if (!nRouteLinks.isEmpty() && (nRouteLinks.lastElement().getRangeSize() > 0)) {
			// get last added matched link
			MatchedLink lastAddedMatchedLink = nRouteLinks.lastElement();
			// update score by subtracting score
			updateScore( -getScoreForLinkAndRange(lastAddedMatchedLink,
												  lastAddedMatchedLink.getRangeEndIndex(),
												  lastAddedMatchedLink.getRangeEndIndex()) );
			
			
			// NOW remove last matched GPS node after updating score
			lastAddedMatchedLink.setRangeEndIndex( lastAddedMatchedLink.getRangeEndIndex()-1 );
			
			// removing successful
			return true;
		}
		
		// couldn't remove last GPS node
		return false;
	}
	
	/**
	 * updates score of this path by add/sub difference
	 * @param difference
	 */
	private void updateScore(double difference) {
		// update score
		score += difference;
		
		// String prefix = (difference < 0) ? "-" : "+";
		// Logger.println("Score(" + prefix + "): " + score);
	}
	
	/**
	 * get score of this path
	 * @return
	 */
	public double getScore() {
		return score;
	}
	
	/**
	 * set GPS node index offset we look in order
	 * to determine outgoing link of street link
	 * @param offset
	 */
	public void setGPSNodeIndexOffset(int offset) {
		// clear and save value
		offset = Math.abs(offset);
		this.gpsNodeIndexOffset = (offset <= MAX_GPS_NODE_INDEX_OFFSET) ? offset : MAX_GPS_NODE_INDEX_OFFSET;
	}
	
	/**
	 * get current set GPS node index offset (to determine outgoing node)
	 * @param offset
	 * @return int
	 */
	public int getGPSNodeIndexOffset (int offset) {
		return this.gpsNodeIndexOffset;
	}
	
	/**
	 * compares to another n route, returns zero if both route have same score
	 * if current route's score is smaller return -1, otherwise return 1
	 */
	@Override
	public int compareTo(NRoute nRoute) {
		// compare n routes
		if (score == nRoute.getScore()) return EQUAL_SCORE;
		if (score < nRoute.getScore()) 	return BETTER_SCORE;	
		
		// otherwise the comparative n route has a better score 
		return WORSE_SCORE;
	}
	
	@Override
	public NRoute clone() {
		// create new instance
		NRoute nRouteClone = new NRoute(this.gpsTrace);
		
		// copy properties
		nRouteClone.score = this.score;
		nRouteClone.gpsNodeIndexOffset = this.gpsNodeIndexOffset;
		nRouteClone.previousNRoute = this.previousNRoute;
		
		/* 
		 * copy vector, here be careful! references to street link can be adopted,
		 * but copy (create new) matched ranges! 
		 */
		 
		// create new vector for copied matched link
		Vector<MatchedLink> nRouteLinksClone = new Vector<MatchedLink>();
		
		for (MatchedLink matchedLink : nRouteLinks) {
			// copy matched link
			MatchedLink matchedLinkClone = new MatchedLink(matchedLink.getStreetLink(),
														   matchedLink.getRangeStartIndex(),
														   matchedLink.getRangeEndIndex());
			// add to vector
			nRouteLinksClone.add(matchedLinkClone);
		}
		
		// assign copied vector to cloned object
		nRouteClone.nRouteLinks = nRouteLinksClone; 
		
		// return cloned NRoute
		return nRouteClone;
	}
	
	/**
	 * calculates score of a link
	 * @param matchedLink
	 * @return double
	 */
	private double getScoreForLink(MatchedLink matchedLink) {
		return getScoreForLinkAndRange(matchedLink, matchedLink.getRangeStartIndex(), matchedLink.getRangeEndIndex());
	}
	
	/**
	 * calculates score of a link to a certain range 
	 * @param streetLink
	 * @param minGPSNodeIndex
	 * @param maxGPSNodeIndex
	 * @return double
	 */
	private double getScoreForLinkAndRange(MatchedLink matchedLink, int minGPSNodeIndex, int maxGPSNodeIndex) {
		// store score for link here
		double linkScore = 0;	
		
		// calculate score for link to its GPS nodes
		for (int i = minGPSNodeIndex; i <= maxGPSNodeIndex; i++) {
			linkScore += Coordinates.getDistance(gpsTrace.getNode(i), matchedLink.getStreetLink());
		}		
		
		// return score
		return linkScore;
	}
	
	/**
	 * get street links in this route
	 * @return
	 */
	public Vector<MatchedLink> getNRouteLinks() {
		return nRouteLinks;
	}
	
	/**
	 * get last added matched link
	 * @return MatchedLink
	 */
	public MatchedLink getLastMatchedLink() {
		// check if vector is empty
		if (!nRouteLinks.isEmpty()) {
			return nRouteLinks.lastElement();
		}
		
		// return null if there is no last added link
		return null;
	}
	
	public Vector<StreetLink> getOutgoingLinksForLastLink() {
		
		// check if vector is not empty
		if (!nRouteLinks.isEmpty()) {
			
			// get last added link
			MatchedLink lastMatchedLink = nRouteLinks.lastElement();
			StreetLink lastAddedLink = lastMatchedLink.getStreetLink();
			
			// check if we got at least two links in our vector, find outgoing node/links
			// through connection between last two links
			if (nRouteLinks.size() > 1) {
				
				// get last but one added street links
				StreetLink lastButOneAddedLink = nRouteLinks.get(nRouteLinks.size()-2).getStreetLink();
				
				// check where they are connected
				int connectionType = lastAddedLink.isConnectedTo(lastButOneAddedLink);
				
				// get street links connected to outgoing node
				if (connectionType == START_NODE) return lastAddedLink.getEndNode().getLinksExcept(lastAddedLink);
				if (connectionType == END_NODE) return lastAddedLink.getStartNode().getLinksExcept(lastAddedLink);
				
				// Logger.errln("No connection between last two links!");
				
			// otherwise find out outgoing link via shortest distance, if we got some more
			// GPS points in forward
			} else if (lastMatchedLink.getRangeEndIndex() < (gpsTrace.getNrOfNodes()-1)) {
				// Logger.println("Find outgoing node via distance!");
				
				// get distance to last GPS node
				int offsetToLastGPSNode = ((gpsTrace.getNrOfNodes()-1) - lastMatchedLink.getRangeEndIndex());
				
				// if distance is greater then current GPS node index, take current set GPS node index offset,
				// otherwise take calculated offset
				int offsetToNextGPSNode = (offsetToLastGPSNode > gpsNodeIndexOffset) ? gpsNodeIndexOffset : offsetToLastGPSNode;
								  
				// get "next" GPS node, we'll use this to determine which street node is the outgoing one
				GPSNode nextGPSNode = gpsTrace.getNode(lastMatchedLink.getRangeEndIndex() + offsetToNextGPSNode);
				
				// get start-/end- node of street link
				StreetNode startNode = lastAddedLink.getStartNode();
				StreetNode endNode = lastAddedLink.getEndNode();
				
				// calculate distance between start-/end node and "next" GPS node
				double distanceToStartNode = Coordinates.getDistance(nextGPSNode, startNode);
				double distanceToEndNode = Coordinates.getDistance(nextGPSNode, endNode);
				
				// return outgoing links of street node with the shortest distance to last GPS node
				return (distanceToStartNode > distanceToEndNode) ?
						endNode.getLinksExcept(lastAddedLink) : startNode.getLinksExcept(lastAddedLink);
			}
		}
		
		// otherwise no links there
		return null;
	}
	
	/**
	 * set/notice previous n route
	 * @param nRoute
	 */
	public void setPreviousNRoute(NRoute nRoute) {
		previousNRoute = nRoute;
	}
	
	/**
	 * get previous n route
	 * @return
	 */
	public NRoute getPreviousNRoute() {
		return previousNRoute;
	}
	
	public int getNRouteLenght() {
		// get size of n route link vector
		int nRouteLenght = nRouteLinks.size();
		
		// if previous n route existing, add whose size by recursion 
		if (previousNRoute != null)
			nRouteLenght += previousNRoute.getNRouteLenght();
		
		// return length
		return nRouteLenght;
	}

}
