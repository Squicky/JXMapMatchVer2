/*
 * this class represents a Node which is connected to several StreetLinks
 * StreetNodes store information about connected StreetLinks
 */

package osm;

import java.util.Vector;

/**
 * @author Daniel Sathees Elmo
 */

public class StreetNode {
	
	// x, y position of node
    private int posX=0;
    private int posY=0;
    
    // store Street links connected to his node
    private Vector<StreetLink> streetLinks = new Vector<>();
    private int NrOfLinks=0;

    /**
     * create StreetNode at Position (x,y)
     * @param x
     * @param y
     */
    public StreetNode(int x, int y) {
        posX = x;
        posY = y;
    }
    
    // methods for links
    /**
     * get Nr of StreetLinks connected to this StreetNode
     * @return (int) Nr of Links
     */
    public int getNrOfLinks(){
        return NrOfLinks;
    }

    /**
     * add StreetLink link to StreetNode
     * @param link
     */
    public void addLink(StreetLink link){
        streetLinks.add(link);
        NrOfLinks=streetLinks.size();
    }

    /**
     * delete StreetLink at position i
     * @param i
     */
    public void deleteLink(int i){
        if (i<0) i=0;
        if (i<NrOfLinks){
            streetLinks.removeElementAt(i);
            NrOfLinks=streetLinks.size();
        }
    }
    
    public void removeLink(StreetLink streetLink) {
    	streetLinks.remove(streetLink);
    	NrOfLinks=streetLinks.size();
    }

    /**
     * get StreetLink at position i
     * @param i
     * @return StreetLink
     */
    public StreetLink getLink(int i){
        if (i<0) i=0;
        if (i<NrOfLinks) return (StreetLink)streetLinks.elementAt(i);
        return null;
    }
    
    /**
     * get all StreetLinks belongs to this node
     * @return vector of street links
     */
    public Vector<StreetLink> getLinks(){
    	//return all links belongs to this node
    	return streetLinks;
    }

    //methods for node

    public int getX(){
        return posX;
    }

    public int getY(){
        return posY;
    }

    public void setX(int x){
        posX = x;
    }

    public void setY(int y){
        posY = y;
    }
    
    /**
     * get outgoing links excluding given one
     * @param excludedLink
     * @return
     */
    public Vector<StreetLink> getLinksExcept(StreetLink excludedLink) {
    	// container for street links
    	Vector<StreetLink> linkContainer = new Vector<>();
    	
    	// add all outgoing links except given one
    	for (StreetLink link : this.streetLinks) {
    		if (link != excludedLink) {
    			linkContainer.add(link);
    		}
    	}
    	
    	// return extracted outgoing links
    	return linkContainer;
    }


   /*
    *     use lon and lat
    private double latitude=0;
    private double longitude=0;

    public StreetNode(double lon, double lat){
        longitude = lon;
        latitude = lat;
    }

    public double getLongitude(){
        return longitude;
    }

    public double getLatitude(){
        return latitude;
    }

    public void setLongitude(double lon){
        longitude = lon;
    }

    public void setLatitude(double lat){
        latitude = lat;
    }
    */

}
