package algorithm;

import osm.StreetLink;

/**
 * @author Daniel Sathees Elmo
 *
 * this class represents an matched link. it assign a range of matched GPS node index
 */

public class MatchedLink {
	
	private StreetLink matchedStreetLink;	// reference to street link
	private MatchedRange matchedRange;		// reference to matched range
	
	/**
	 * creates a matched link with matched range 
	 * @param streetLink
	 * @param rangeStartIndex of range
	 * @param rangeEndIndex of range
	 */
	public MatchedLink(StreetLink matchedStreetLink, int rangeStartIndex, int rangeEndIndex) {
		// street link is matched by default, that's why it's called matched link :)
		this(matchedStreetLink, new MatchedRange(rangeStartIndex, rangeEndIndex, true));
	}
	
	/**
	 * creates a matched link with matched range 
	 * @param streetLink
	 * @param matchedRange
	 */
	public MatchedLink(StreetLink matchedStreetLink, MatchedRange matchedRange) {
		this.matchedStreetLink = matchedStreetLink;
		this.matchedRange = matchedRange;
		this.matchedRange.setMatched(true); // set matched true!
	}
	
	/**
	 * set matched street link
	 * @param matchedStreetLink
	 */
	public void setStreetLink(StreetLink matchedStreetLink) {
		this.matchedStreetLink = matchedStreetLink;
	}
	
	/**
	 * get matched street link
	 * @return StreetLink
	 */
	public StreetLink getStreetLink() {
		return matchedStreetLink;
	}
	
	/**
	 * set start index of matched range
	 * @param rangeStartIndex
	 */
	public void setRangeStartIndex(int rangeStartIndex) {
		matchedRange.setRangeStartIndex(rangeStartIndex);
	}
	
	/**
	 * get start index of matched range
	 * @return int
	 */
	public int getRangeStartIndex() {
		return matchedRange.getRangeStartIndex();
	}
	
	/**
	 * set end index of matched range 
	 * @param rangeEndIndex
	 */
	public void setRangeEndIndex(int rangeEndIndex) {
		matchedRange.setRangeEndIndex(rangeEndIndex);
	}
	
	/**
	 * get end index of matched range
	 * @param int
	 */
	public int getRangeEndIndex() {
		return matchedRange.getRangeEndIndex();
	}
	
	/**
	 * get range size
	 * @return int
	 */
	public int getRangeSize() {
		return matchedRange.getRangeSize();
	}
}
