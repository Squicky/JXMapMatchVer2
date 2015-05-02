package algorithm;

import gps.GPSNode;

import java.util.NoSuchElementException;
import java.util.Vector;

import osm.StreetLink;

public class SelectableStreetLink {
	
	private long id;
	
	public final static long NO_ID = -1;

	private StreetLink streetLink;
	
	private int selectedCounter;
	    
 	// is this link artificial (not part of parsed street map)
    private boolean artificial;	
    
    private Vector<Vector<GPSNode>> matchedGPSNodes;
    private Vector<MatchedRange> matchedRanges;
    
    public static final int NO_CONNECTION = 0;
   	public static final int START_NODE = 1;
   	public static final int END_NODE = 2;
   	public static final int BOTH_NODE = 3;
	
	/**
	 * initialize class with existing street link
	 * as reference 
	 */
	public SelectableStreetLink(StreetLink streetLink) {
		this.streetLink = streetLink;
	}
	
	public StreetLink getStreetLink() {
		return this.streetLink;
	}
	
	////////////////////////////////////////// Selected Counter /////////////////////////////////
	
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
	/////////////////////////////////////////////////////////////////////////////////////////////
    
    //////////////////////////////////////// Matched Ranges /////////////////////////////////////
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
    /////////////////////////////////////////////////////////////////////////////////////////////
    
    //////////////////////////////////////// Matched GPS Nodes //////////////////////////////////
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
    
    public long getID() {
    	return id;
    }
    
    /////////////////////////////////////////////////////////////////////////////////////////////
}
