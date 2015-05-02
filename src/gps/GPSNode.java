package gps;

import java.awt.Color;

/**
 * @author Daniel Sathees Elmo
 * 
 * class represents an GPS Point, saves also info about its matched position and color for
 * drawing 
 */

//TODO: don't save matched/draw values here, instead create separate class with reference to this node 

public class GPSNode {
	
	// time stamp
    private long timestamp = 0;
    private int x = 0;
    private int y = 0;
    
    /**
     * create new GPSNode at position (x,y) with timestamp t
     * @param x
     * @param y
     * @param timestamp
     */
    public GPSNode(int x , int y, long timestamp){
    	// save position and timestamp
        this.x = x;
        this.y = y;
        this.timestamp = timestamp;	
    }
    
    /**
     * set GPSNode's X-Position
     * @param x
     */
    public void setX(int x){
        this.x = x;
    }

    /**
     * set GPSNode's Y-Position
     * @param y
     */
    public void setY(int y){
        this.y = y;
    }

    /**
     * get GPSNode's X-Position
     * @return (int) X-Position
     */
    public int getX(){
        return x;
    }
    
    /**
     * get GPSNode's X-Position
     * @return (int) Y-Position
     */
    public int getY(){
        return y;
    }
    
    /**
     * set GPSNode's timestamp
     * @param timestamp
     */
    public void setTimestamp(long timestamp){
        this.timestamp = timestamp;
    }

    /**
     * get GPSNode's timestamp
     * @return (long) timestamp
     */
    public long getTimestamp(){
        return timestamp;
    }
}
