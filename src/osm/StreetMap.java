/*
 * Street Map is arrangement of StreetLinks and StreetNodes
 */

package osm;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import cartesian.Coordinates;

/**
 * @author tob
 * @author Daniel Sathees Elmo
 */

//TODO: use more dynamic and modern java structures to store street links/nodes and street links vector

public class StreetMap {
	
	// count/set link for each new street link
	private long linkIDCounter = 0;
    
    //store Street links
    private StreetLink [] streetLinks;
	public Map<Long, Map<Long, Map<Long, StreetLink>>> streetLinksHashMap = new HashMap<Long, Map<Long, Map<Long, StreetLink>>>();
    
    private int NrOfLinks=0;
    private int MaxNrOfLinks=0;
    
    //store Street nodes
    private StreetNode [] streetNodes;
	public Map<Long, StreetNode> streetNodesHashMap = new HashMap<Long, StreetNode>();
	
    private int NrOfNodes=0;
    private int MaxNrOfNodes=0;

    private double minX=0;
    private double minY=0;
    private double maxX=0;
    private double maxY=0;
    
    private File streetMapFile;
    
    public StreetMap(int links, int nodes, String fullFilePath){
    	this(links, nodes, new File(fullFilePath));
    }
    
    public StreetMap(int links, int nodes, File streetMapFile){
    	this(links, nodes);
    	
    	// save reference to street map file, from which we take our data
    	this.streetMapFile = streetMapFile;
    }

    /**
     * StreetMap has pre-defined number of links and nodes
     * @param links
     * @param nodes
     *
     */
    public StreetMap(int links, int nodes){
        MaxNrOfLinks = links;
        MaxNrOfNodes = nodes;
        // create new Links and Nodes
        streetLinks = new StreetLink[MaxNrOfLinks];
        streetNodes = new StreetNode[MaxNrOfNodes];
    }

    public void setColorOfLinks() {
    	
    	for (int i=0; i<NrOfLinks; i++) {
    		streetLinks[i].setDirectionColorOfOneWay();
    	}
    	
    	for (int i=0; i<NrOfLinks; i++) {
    		
    		for (int j=0; j<i; j++) {
    			
    			double a = streetLinks[i].getStartX();
    			double b = streetLinks[i].getStartY();
    			double c = streetLinks[i].getEndX();
    			double d = streetLinks[i].getEndY();
    			
    			
    			if ( a == 8708905 && b == 5605140 && c == 8708900 && d == 5605141) {
    				a = a++;
    			}
    			
    			
    			if (streetLinks[i].getStartX() == streetLinks[j].getEndX() &&
    					streetLinks[i].getStartY() == streetLinks[j].getEndY() &&
    					streetLinks[i].getEndX() == streetLinks[j].getStartX() &&
    					streetLinks[i].getEndY() == streetLinks[j].getStartY() ) {

    				streetLinks[j].DirectionColor = 0;
    				streetLinks[i].DirectionColor = -1;    			

    			}
    				
    		}
    	}
    	
    	
    }
    
    // methods for links
    /**
     * get Nr Of StreetLinks
     * @return (int) NrOfLinks
     */
    public int getNrOfLinks(){
        return NrOfLinks;
    }

    /**
     * add StreetLink l to StreetMap
     * @param l
     */
    public void addLink(StreetLink l){
        addLink(l.getStartNode(),l.getEndNode(), l.myid, l.startNodeId, l.endNodeId);
    }
    
    /**
     * add link (line from [x1,y1] to [x2,y2])to StreetMap
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     */
    public StreetLink addLink(double x1, double y1, double x2, double y2, long parentId, long startNoteId, long endNoteId){
    	
        StreetNode sn1 = addNode(x1,y1, startNoteId);
        StreetNode sn2 = addNode(x2,y2, endNoteId);
        // check if Node adding was successful and add link
        if ( sn1 != null && sn2 != null) {
            // if not add Link to StreetMap
            return addLink(sn1, sn2, parentId, startNoteId, endNoteId);        	
        }
        
        return null;
    }

    /**
     * add link which connects street nodes n1 and n2
     * @param n1
     * @param n2
     */
    public StreetLink addLink(StreetNode n1, StreetNode n2, long parentMyId, long startNoteId, long endNoteId) {
    	
    	StreetLink sn = getLink(n1,n2,parentMyId);
    	
        // check if link already exists, if not create link
        if ( sn == null ){
            if (NrOfLinks < MaxNrOfLinks){
                // create new link, increase id
                streetLinks[NrOfLinks] = new StreetLink(n1,n2, linkIDCounter++, parentMyId, startNoteId, endNoteId);

                n1.addLink(streetLinks[NrOfLinks]);
                n2.addLink(streetLinks[NrOfLinks]);
                                
                Map<Long, Map<Long, StreetLink>> m = streetLinksHashMap.get(n1.myid);
                Map<Long, StreetLink> n = null;
                if (m != null) {
                	
                	n = m.get(n2.myid);
                	
                	if (n != null) {
                		n.put(parentMyId, streetLinks[NrOfLinks]);
                	} else {
                		n = new HashMap<Long, StreetLink>();
                		n.put(parentMyId, streetLinks[NrOfLinks]);
                		m.put(n2.myid, n);
                	}
                } else {
                	m = new HashMap<Long, Map<Long, StreetLink>>();
                	 
                	n = new HashMap<Long, StreetLink>();
             		n.put(parentMyId, streetLinks[NrOfLinks]);
             		m.put(n2.myid, n);
             		
                	streetLinksHashMap.put(n1.myid, m);
                }
               
                // add link to its nodes
                NrOfLinks++;
                
                return streetLinks[NrOfLinks - 1];
            }
        }
        
        return sn;
    }
    
    public StreetLink getStreetLink(long myId1, long myId2, long parentMyId) {
    	
    	/*
    	if (myId1 > myId2) {
    		long l = myId1;
//    		myId1 = myId2;
//    		myId2 = l;    		
    	}
    	*/
    	
    	StreetLink sl = null;
    	
        Map<Long, Map<Long, StreetLink>> m = streetLinksHashMap.get(myId1);
        if (m != null) {
        	
        	Map<Long, StreetLink> n = m.get(myId2);
        	
        	if (n != null) {
            	sl = n.get(parentMyId);        		
        	}
        	
        }
        
        if ( sl == null ) {
        	m = streetLinksHashMap.get(myId2);
            if (m != null) {
            	
            	Map<Long, StreetLink> n = m.get(myId1);
            	
            	if (n != null) {
                	sl = n.get(parentMyId);        		
            	}
            	
            }
        }
        
        return sl;
    }
    
    /**
     * return StreetLink which connects StreetNodes n1 and n2
     * @param n1
     * @param n2
     * @return StreetLink
     */
    public StreetLink getLink(StreetNode n1, StreetNode n2, long parentMyId) {
    	
    	return this.getStreetLink(n1.myid, n2.myid, parentMyId);
    	
    	/*
    	StreetLink sl = null;;
    	Map<Long, StreetLink> m;
    	
//    	if (n1.myid <= n2.myid) {
    		m = this.streetLinksHashMap.get(n1.myid);
    		if (m != null) {
        		sl = m.get(n2.myid);
    		}
    		
    		if (sl == null) {
        		m = this.streetLinksHashMap.get(n2.myid);
        		if (m != null) {
            		sl = m.get(n1.myid);
        		}
    		}
*/
  
/*    		
    	} else {
    		Map<Long, StreetLink> m = this.streetLinksHashMap.get(n2.myid);
    		if (m == null) {
    			return null;
    		}
    		sl = m.get(n1.myid);   		
    	}

    	// check if node already exists
    	if (sl != null) {
        	if ( n1 == sl.getStartNode() && n2 == sl.getEndNode() ) {
        		return sl;
        	}
    	}
*/

    	/*    	
        int i=0;
        while ( i < NrOfLinks ) {

            if ((n1==streetLinks[i].getStartNode())&&(n2==streetLinks[i].getEndNode())) return streetLinks[i];

            //check if node in other direction already exists
            if ((n2==streetLinks[i].getStartNode())&&(n1==streetLinks[i].getEndNode())) return streetLinks[i];
            i++;
        }
        */
        // Link does not exist
//        return sl;
    }

    /**
     * return StreetLink which connects StreetNodes
     * that are located at (x1,y1) and (x2,y2)
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return StreetLink
     */
	// defekt
    /*
     public StreetLink getLink(int x1, int y1, int x2, int y2){
        StreetNode n1 = new StreetNode(x1,y1);
        StreetNode n2 = new StreetNode(x2,y2);
        // return Link(n1,n2) if it exists
        return getLink(n1,n2);
    }
    */
    
    /**
     * returns all street links through an array
     */
    public StreetLink[] getLinks(){
    	return streetLinks;
    }

    /**
     * get first StreetLink
     * returns null if link does not exist
     * @return StreetLink
     */
    public StreetLink getFirstLink(){
        if (NrOfLinks > 0) return streetLinks[0];
        return null;
    }

    /**
     * get last StreetLink
     * returns null if link does not exist
     * @return StreetLink
     */
    public StreetLink getLastLink(){
        if (NrOfLinks > 0)return streetLinks[NrOfLinks-1];
        return null;
    }

    /**
     * get StreetLink i
     * returns null if link does not exist
     * @param i
     * @return StreetLink
     */
    public StreetLink getLink(int i){
        if (i<0) i=0;
        if (i<NrOfLinks) return streetLinks[i];
        return null;
    }
    
    public StreetLink getLink(long i){
    	return getLink((int) i);
    }

    /**
     * get X-Position from Link i's first Node
     * @param i
     * @return int
     */
    public double getStartX(int i){
        if (i<0) i=0;
        if (i<NrOfLinks) return streetLinks[i].getStartX();
        return 0;
    }

    /**
     * get Y-Position from Link i's first Node
     * @param i
     * @return int
     */
    public double getStartY(int i){
        if (i<0) i=0;
        if (i<NrOfLinks) return streetLinks[i].getStartY();
        return 0;
    }
    /**
     * get X-Position from Link i's second Node
     * @param i
     * @return int
     */
    public double getEndX(int i){
        if (i<0) i=0;
        if (i<NrOfLinks) return streetLinks[i].getEndX();
        return 0;
    }
    /**
     * get Y-Position from Link i's second Node
     * @param i
     * @return int
     */
    public double getEndY(int i){
        if (i<0) i=0;
        if (i<NrOfLinks) return streetLinks[i].getEndY();
        return 0;
    }

    // methods for nodes

    /**
     * get Nr Of StreetNodes
     * (StreetNodes connect Links)
     * @return int NrOfNodes
     */
    public int getNrOfNodes(){
        return NrOfNodes;
    }

    /**
     * add StreetNode n to StreetMap
     * @param n
     */
	// defekt
    /*
    public void addNode(StreetNode n){
        addNode(n.getX(),n.getY());
    }
    */
    
    /**
     * add StreetNode located at (x,y) to StreetMap
     * @param x
     * @param y
     */
    public StreetNode addNode(double x, double y, long nodeId){
        //check if node already exists
    	
    	StreetNode n = getNode(x, y, nodeId);
    	
        if (n == null){
            if (NrOfNodes < MaxNrOfNodes){
                // if node does not already exist: create one
                streetNodes[NrOfNodes] = new StreetNode(x, y, nodeId);
                streetNodesHashMap.put(streetNodes[NrOfNodes].myid, streetNodes[NrOfNodes]);
                NrOfNodes++;
                
                return streetNodes[NrOfNodes - 1];
            }
        }
        
        return n;
    }

    /**
     * get StreetNode by Location (x,y)
     * @param x
     * @param y
     * @return Streetnode
     */
    public StreetNode getNode(double x, double y, long myid){

    	StreetNode sn = null;
    	
    	sn = this.streetNodesHashMap.get(myid);
    	
    	if (sn != null) {
    		if ( x==sn.getX() && y==sn.getY() && myid == sn.myid) {
    			return sn;
    		}
    	}
    	
    	/*
    	int i=0;
        while (i<NrOfNodes){
            // check if node exists and has posotion (x,y)
            if ( x==streetNodes[i].getX() && y==streetNodes[i].getY() && myid == streetNodes[i].myid) return streetNodes[i];
            i++;
        }
        */
        // node does not yet exist
        return null; 

    }
    
    public int getCountNodeId(long id) {
    	int z = 0;
    	
    	int i = 0;
    	while (i<NrOfNodes){
            // check if node exists and has posotion (x,y)
            if (streetNodes[i].myid == id) {
            	z++;
            }
            i++;
        }
    	
    	return z;
    }

    
    /**
     * get Node i's X-Position
     * @param i
     * @return int
     */
    public double getNodeX(int i){
        if (i<0) i=0;
        if (i<NrOfNodes) return streetNodes[i].getX();
        return 0;
    }
    /**
     * get Node i's Y-Position
     * @param i
     * @return int
     */
    public double getNodeY(int i){
        if (i<0) i=0;
        if (i<NrOfNodes) return streetNodes[i].getY();
        return 0;
    }

    /**
     * get first StreetNode
     * @return Streetnode
     */
    public StreetNode getFirstNode(){
        if (NrOfNodes > 0) return streetNodes[0];
        return null;
    }
    /**
     * get last Streetnode
     * @return StreetNode
     */
    public StreetNode getLastNode(){
        if (NrOfNodes > 0)return streetNodes[NrOfNodes-1];
        return null;
    }

    /**
     * get StreetNode i
     * @param i
     * @return Streetnode
     */
    public StreetNode getNode(int i){
        if (i<0) i=0;
        if (i<NrOfNodes) return streetNodes[i];
        return null;
    }

    public void setMinX(double m){
        minX = m;
    }

    public void setMinY(double m){
        minY = m;
    }
    public void setMaxX(double m){
        maxX = m;
    }

    public void setMaxY(double m){
        maxY = m;
    }

    public double getMinX(){
        return minX;
    }

    public double getMinY(){
        return minY;
    }
    public double getMaxX(){
        return maxX;
    }

    public double getMaxY(){
        return maxY;
    }
    
    public GeoPosition getMiddlePosition(){
    	return Coordinates.getMiddleGeoPos(minX, minY, maxX, maxY);
    }
    
    public Vector<StreetLink> getStreetLinksVector() {
    	// save street link inside this vector
    	Vector<StreetLink> streetLinksVector = new Vector<StreetLink>();
    	
    	// convert street links array to vector and resize it to real size
    	Collections.addAll(streetLinksVector, this.getLinks());
    	streetLinksVector.setSize(getNrOfLinks());
    	
    	// return converted street links as vector
    	return streetLinksVector;
    }
    
    public File getStreetMapFile(){
    	return streetMapFile;
    }
    
}