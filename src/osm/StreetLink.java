/*
 * Link which connects two StreetNodes
 */

package osm;

import gps.GPSNode;

import java.util.NoSuchElementException;
import java.util.Vector;

import algorithm.MatchedRange;

/**
 *
 * @author Daniel Sathees Elmo
 */
public class StreetLink {
	
	private long id;
	
	public final static long NO_ID = -1;

    private StreetNode startNode;
    private StreetNode endNode;
    
    private int selectedCounter;
    
    private boolean artificial;							//is this link artificial (not part of parsed street map)
    
    private Vector<Vector<GPSNode>> matchedGPSNodes;
    private Vector<MatchedRange> matchedRanges;
    
    public static final int NO_CONNECTION = 0;
	public static final int START_NODE = 1;
	public static final int END_NODE = 2;
	public static final int BOTH_NODE = 3;
	
	public StreetLink(StreetNode n1, StreetNode n2) {
    	this(n1, n2, NO_ID, false);
    }
	
	public StreetLink(StreetNode n1, StreetNode n2, long id) {
		this(n1, n2, id, false);
	}
	
	public StreetLink(StreetNode n1, StreetNode n2, boolean artificial) {
		this(n1, n2, NO_ID, artificial);
	}
    
    /**
     * create StreetLink which connects StreetNodes n1 and n2
     * @param n1 first node
     * @param n2 second node
     * @param id identifier
     * @param artificial is this street link virtual
     */
    public StreetLink(StreetNode n1, StreetNode n2, long id, boolean artificial) {
    	// set street nodes
    	startNode = n1;
        endNode = n2;
        // set artificial flag
        this.artificial = artificial;
        // set id
        this.id = id;
        // set selected counter
        selectedCounter = 0;
        // create vector for storing matched gpsNodes
        matchedGPSNodes = new Vector<Vector<GPSNode>>();
        // create vector for storing matching ranges
        matchedRanges = new Vector<MatchedRange>();
    }
    
    /**
     * get first StreetNode
     * @return StreetNode
     */
    public StreetNode getStartNode() {
        return startNode;
    }

    /**
     * get second StreetNode
     * @return StreetNode
     */
    public StreetNode getEndNode() {
        return endNode;
    }

    /**
     * set first StreetNode to node
     * @param node
     */
    public void setStartNode(StreetNode node) {
        startNode = node;
    }

    /**
     * set second StreetNode to node
     * @param node
     */
    public void setEndNode(StreetNode node) {
        endNode = node;
    }

    /**
     * get X-Position of first StreetNode
     * @return (int) X-Pos
     */
    public int getStartX() {
        return startNode.getX();
    }
    /**
     * get Y-Position of first StreetNode
     * @return (int) Y-Pos
     */
    public int getStartY() {
        return startNode.getY();
    }
    /**
     * get X-Position of second StreetNode
     * @return (int) X-Pos
     */
    public int getEndX() {
        return endNode.getX();
    }
    /**
     * get Y-Position of second StreetNode
     * @return (int) Y-Pos
     */
    public int getEndY() {
        return endNode.getY();
    }
    
    public boolean isArtificial() {
    	return artificial;
    }
    
    public long getID() {
    	return id;
    }

    /**
     * compare link to streetLink l
     * @param l
     * @return
     */
    public boolean isLink(StreetLink l) {
        boolean equal=false;
        if ((this.getStartX()==l.getStartX())
                &&(this.getStartY()==l.getStartY())
                 &&(this.getEndX()==l.getEndX())
                  &&(this.getEndY()==l.getEndY())) equal=true;
        return equal;
    }
    
    public double getLength() {
    	// link as vector with x & y components
    	int vecX = getEndX() - getStartX();
    	int vecY = getStartY() - getEndY();
    	
    	// calculate length
    	return Math.sqrt((vecX*vecX) + (vecY*vecY));
    }
    
    /**
     * increase counter how many times this link was selected
     */
    public void increaseSelectCounter() {
    	++selectedCounter;
    }
    
    /**
     * decrease selected counter 
     */
    public void decreaseSelectCounter() {
    	if (selectedCounter > 0) --selectedCounter;
    }
    
    /**
     * returns how many times link was selected
     * @return int
     */
    public int getSelectCounter() {
    	return selectedCounter;
    }
    
    /**
     * resets counter
     */
    public void resetSelectCounter() {
    	selectedCounter=0;
    }
    
    /////////////// Matched Ranges /////////////////////////////////////
    /**
     * add range, which can be matched to this link
     * @param start
     * @param end
     * @param matched
     */
    public void addMatchedRange(int start, int end, boolean matched) {
    	matchedRanges.add(new MatchedRange(start, end, matched));
    }
    
    /**
     * edit last added range
     * @param start
     * @param end
     * @return there was a last range
     */
    public boolean setLastMatchedRange(int start, int end) {
    	if (!matchedRanges.isEmpty()){
    		matchedRanges.lastElement().setRangeStartIndex(start);
    		matchedRanges.lastElement().setRangeEndIndex(end);
    		return true;
    	}
    	
    	return false;
    }
    
    /**
     * edit last added range
     * @param start
     * @param end
     * @param matched
     * @return there was a last range
     */
    public boolean setLastMatchedRange(int start, int end, boolean matched) {
    	if (!matchedRanges.isEmpty()){
    		matchedRanges.lastElement().setRangeStartIndex(start);
    		matchedRanges.lastElement().setRangeEndIndex(end);
    		matchedRanges.lastElement().setMatched(matched);
    		return true;
    	}
    	
    	return false;
    }
    
    /**
     * set start index of last added range
     * @param start
     * @return there was a last range
     */
    public boolean setLastMatchedRangeStart(int start){
    	if (!matchedRanges.isEmpty()){
    		matchedRanges.lastElement().setRangeStartIndex(start);
    		return true;
    	}
    	
    	return false;
    }
    
    /**
     * set end index of last added range
     * @param end
     * @return there was a last range
     */
    public boolean setLastMatchedRangeEnd(int end){
    	if (!matchedRanges.isEmpty()){
    		matchedRanges.lastElement().setRangeEndIndex(end);
    		return true;
    	}
    	
    	return false;
    }
    
    /**
     * set matched state of last added range
     * @param matched
     * @return there was a last range
     */
    public boolean setLastMatched(boolean matched){
    	if (!matchedRanges.isEmpty()){
    		matchedRanges.lastElement().setMatched(matched);
    		return true;
    	}
    	
    	return false;
    }
    
    /**
     * get matched state of last added range
     * @return
     */
    public boolean isLastMatched(){
    	if (!matchedRanges.isEmpty()){
    		return matchedRanges.lastElement().getMatched();
    	}
    	return false;
    }
    
    /**
     * get last added range
     * @return MatchedRange or null if there is no MatchedRange left
     */
    public MatchedRange getLastMatchedRange(){
    	try { return matchedRanges.lastElement(); }
    	catch (NoSuchElementException e) { System.out.println("No last matched Range!"); }
    	
    	return null;
    }
    
    /**
     * return start index of last added range
     * @return
     */
    public int getLastMatchedRangeStart(){
    	if (!matchedRanges.isEmpty())
    		return matchedRanges.lastElement().getRangeStartIndex();
    	else
    		return -1;
    }
    
    /**
     * return end index of last added range
     * @return
     */
    public int getLastMatchedRangeEnd(){
    	if (!matchedRanges.isEmpty())
    		return matchedRanges.lastElement().getRangeEndIndex();
    	else
    		return -1;
    }
    
    /**
     * return size of last added matched range
     * @return
     */
    public int getLastMatchedRangeSize() {
    	if (!matchedRanges.isEmpty())
    		return matchedRanges.lastElement().getRangeSize();
    	else
    		return -1;
    }
    
    /**
     * remove last added range if possible
     * @return there was a range which could be removed
     */
    public boolean removeLastMatchedRange(){
    	if (!matchedRanges.isEmpty()){
    		matchedRanges.remove(matchedRanges.size()-1);
    		return true;
    	}
    	
    	return false;
    }
    
    /**
     * remove all ranges from link
     */
    public void resetMatchedRanges(){
    	matchedRanges.clear();
    }
    
    //matched GPSNodes///////////////////////////////////////
    public void addMatchedGPSNodes(Vector<GPSNode> gpsNodes){
    	matchedGPSNodes.add(gpsNodes);
    }
    
    public boolean removeLastMatchedGPSNodes(){
    	if (!matchedGPSNodes.isEmpty()){
    		matchedGPSNodes.removeElementAt(matchedGPSNodes.size()-1);
    		return true;
    	}
    	//otherwise fail
    	return false;
    }
    
    public Vector<GPSNode> getLastMatchedGPSNodes(){
    	try { return matchedGPSNodes.lastElement(); }
    	catch (NoSuchElementException e) { System.out.println("No last matched GPS Nodes exists"); }
    	//otherwise fail
    	return null;
    }
    
    public void resetMatchedGSPNodes(){
    	matchedGPSNodes.clear();
    }
    
    public int getNrOfLinkMatched(){
    	return matchedGPSNodes.size();
    }
        
    /////////////////////////////////////////////////////////
    
    public int isConnectedTo(StreetLink streetLink){
    	//if link was given
    	if (streetLink != null){
    		//is start node connected to other link
    		boolean startNodeConnection = (startNode == streetLink.getStartNode() ||
        						   		   startNode == streetLink.getEndNode());
    		//is end node connected to other link
    		boolean endNodeConnection = (endNode == streetLink.getStartNode() ||
   				 				 		 endNode == streetLink.getEndNode());
        						   
    		
    		//how are links connected
    		if(startNodeConnection && endNodeConnection){
    			//both nodes are connected
    			return BOTH_NODE;
    		}
    		else if (startNodeConnection){
    			//start node is connecting node
    			return START_NODE;
    		}
    		else if (endNodeConnection){
    				//end node is connecting node
    				return END_NODE;
    		}
    	}
  
    	//no connection between street links
    	return NO_CONNECTION;
    }
    
    public int getStartNode(StreetLink streetLink) {
    	int connectingNode = isConnectedTo(streetLink);
    	
    	switch(connectingNode) {
    		
    		case START_NODE:
    			// end node will be the starting node
    			// when we drive along this street
    			return END_NODE;
    		
    		case END_NODE:
    			// start node will be...
    			return START_NODE;
    		
    		default:
    			return connectingNode;
    	}
    }
}
