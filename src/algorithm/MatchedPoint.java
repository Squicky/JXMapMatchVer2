package algorithm;

/**
 * 
 * this class represents a matched Point and stores info
 * about distance and if its an orthographic felled point 
 * 
 * @author Daniel Sathees Elmo
 */

public class MatchedPoint {
	
	private final int matchedX;			// matched x-coordinate of GPS point
	private final int matchedY;			// matched x-coordinate of GPS point
	private final double distance;		// distance to
	private final boolean isEuclidian;	// is matched point vertical to unmatched position
	
	/**
	 * constructor saves given values
	 * @param matchedX
	 * @param matchedY
	 * @param distance
	 * @param isEuclidian
	 */
	public MatchedPoint(int matchedX, int matchedY, double distance, boolean isEuclidian){
		this.matchedX = matchedX;
		this.matchedY = matchedY;
		this.distance = distance;
		this.isEuclidian = isEuclidian;
	}
	
	/**
	 * @return matched x-coordinate
	 */
	public int getX(){
		return matchedX;
	}
	
	/**
	 * @return matched y-coordinate
	 */
	public int getY(){
		return matchedY;
	}
	
	/**
	 * @return distance from matched- to unmatched position
	 */
	public double getDistance(){
		return distance;
	}
	
	/**
	 * @return is matched position vertical to unmatched point?
	 */
	public boolean isEuclidian(){
		return isEuclidian;
	}
}
