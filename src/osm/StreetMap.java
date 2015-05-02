/*
 * Street Map is arrangement of StreetLinks and StreetNodes
 */

package osm;

import java.io.File;
import java.util.Collections;
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
    private int NrOfLinks=0;
    private int MaxNrOfLinks=0;
    
    //store Street nodes
    private StreetNode [] streetNodes;
    private int NrOfNodes=0;
    private int MaxNrOfNodes=0;

    private int minX=0;
    private int minY=0;
    private int maxX=0;
    private int maxY=0;
    
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
        addLink(l.getStartNode(),l.getEndNode());
    }
    
    /**
     * add link (line from [x1,y1] to [x2,y2])to StreetMap
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     */
    public void addLink(int x1, int y1, int x2, int y2){
        addNode(x1,y1);
        addNode(x2,y2);
        // check if Node adding was successful and add link
        if ((getNode(x1,y1)!=null)&&(getNode(x2,y2)!=null))
            // if not add Link to StreetMap
            addLink(getNode(x1, y1),getNode(x2,y2));
    }

    /**
     * add link which connects street nodes n1 and n2
     * @param n1
     * @param n2
     */
    public void addLink(StreetNode n1, StreetNode n2) {
        // check if link already exists, if not create link
        if (getLink(n1,n2)==null){
            if (NrOfLinks < MaxNrOfLinks){
                // create new link, increase id
                streetLinks[NrOfLinks]=new StreetLink(n1,n2, linkIDCounter++);
                // add link to its nodes
                n1.addLink(streetLinks[NrOfLinks]);
                n2.addLink(streetLinks[NrOfLinks]);
                NrOfLinks++;
            }
        }
    }

    /**
     * return StreetLink which connects StreetNodes n1 and n2
     * @param n1
     * @param n2
     * @return StreetLink
     */
    public StreetLink getLink(StreetNode n1, StreetNode n2) {
        int i=0;
        while (i<NrOfLinks){
            // check if node already exists
            if ((n1==streetLinks[i].getStartNode())&&(n2==streetLinks[i].getEndNode())) return streetLinks[i];
            //check if node in other direction already exists
            if ((n2==streetLinks[i].getStartNode())&&(n1==streetLinks[i].getEndNode())) return streetLinks[i];
            i++;
        }
        // Link does not exist
        return null;
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
    public StreetLink getLink(int x1, int y1, int x2, int y2){
        StreetNode n1 = new StreetNode(x1,y1);
        StreetNode n2 = new StreetNode(x2,y2);
        // return Link(n1,n2) if it exists
        return getLink(n1,n2);
    }
    
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
    public int getStartX(int i){
        if (i<0) i=0;
        if (i<NrOfLinks) return streetLinks[i].getStartX();
        return 0;
    }

    /**
     * get Y-Position from Link i's first Node
     * @param i
     * @return int
     */
    public int getStartY(int i){
        if (i<0) i=0;
        if (i<NrOfLinks) return streetLinks[i].getStartY();
        return 0;
    }
    /**
     * get X-Position from Link i's second Node
     * @param i
     * @return int
     */
    public int getEndX(int i){
        if (i<0) i=0;
        if (i<NrOfLinks) return streetLinks[i].getEndX();
        return 0;
    }
    /**
     * get Y-Position from Link i's second Node
     * @param i
     * @return int
     */
    public int getEndY(int i){
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
    public void addNode(StreetNode n){
        addNode(n.getX(),n.getY());
    }
    /**
     * add StreetNode located at (x,y) to StreetMap
     * @param x
     * @param y
     */
    public void addNode(int x, int y){
        //check if node already exists
        if (getNode(x, y)==null){
            if (NrOfNodes < MaxNrOfNodes){
                // if node does not already exist: create one
                streetNodes[NrOfNodes]=new StreetNode(x,y);
                //System.out.println("add Node "+(NrOfNodes+1)+"/"+MaxNrOfNodes+" Lon: "+x+" Lat: "+y);
                NrOfNodes++;
            }
        }
    }

    /**
     * get StreetNode by Location (x,y)
     * @param x
     * @param y
     * @return Streetnode
     */
    public StreetNode getNode(int x, int y){
        int i=0;
        while (i<NrOfNodes){
            // check if node exists and has posotion (x,y)
            if ((x==streetNodes[i].getX())&&(y==streetNodes[i].getY())) return streetNodes[i];
            i++;
        }
        // node does not yet exist
        return null; 
    }

    
    /**
     * get Node i's X-Position
     * @param i
     * @return int
     */
    public int getNodeX(int i){
        if (i<0) i=0;
        if (i<NrOfNodes) return streetNodes[i].getX();
        return 0;
    }
    /**
     * get Node i's Y-Position
     * @param i
     * @return int
     */
    public int getNodeY(int i){
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

    public void setMinX(int m){
        minX = m;
    }

    public void setMinY(int m){
        minY = m;
    }
    public void setMaxX(int m){
        maxX = m;
    }

    public void setMaxY(int m){
        maxY = m;
    }

    public int getMinX(){
        return minX;
    }

    public int getMinY(){
        return minY;
    }
    public int getMaxX(){
        return maxX;
    }

    public int getMaxY(){
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