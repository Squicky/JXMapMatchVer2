/*
 * This class is used to draw Routes, GPS Data, .... into JXMapKit
 */

package graphic;

import myOSM.myOSMMap;
import myOSM.myOSMNode;
import myOSM.myOSMWay;
import myOSM.myOSMWayPart;

import org.jdesktop.swingx.*;
import algorithm.MatchedGPSNode;
import algorithm.MatchedLink;
import algorithm.MatchedNLink;
import algorithm.ReorderedMatchedGPSNode;
import osm.StreetMap;
import route.*;
import gps.GPSTrace;
import java.awt.*;
import java.util.Random;
import java.util.Vector;

/**
 * @author Tobias
 * @author Daniel Sathees Elmo
 */

public class JXMapPainter {

    /**
     * draw n Route route on Graphics g (Color: color)
     * use JXMapView map to get zoom
     * @param g
     * @param map
     * @param route
     * @param color
     */
	/*
    public void drawRoute(Graphics2D g,JXMapViewer map, Route route, Color color){
        // create graphics
        g = (Graphics2D) g.create();
        
        //convert from viewport to world bitmap
        Rectangle rect = map.getViewportBounds();
        g.translate(-rect.x, -rect.y);

        //do the drawing
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // set brush
        g.setStroke(new BasicStroke(1));
        // set color
        g.setColor(color);

        for(int i=0; i<route.getNrOfSelectedLinks();i++){
            // draw line for every link in route
            // devide x,y coordinates by 2^(zoom-1) to fit to current zoom
            g.drawLine((int)(route.getLink(i).getStartX()/(Math.pow(2, map.getZoom()-1))),
                    (int)(route.getLink(i).getStartY()/(Math.pow(2, map.getZoom()-1))),
                    (int)(route.getLink(i).getEndX()/(Math.pow(2, map.getZoom()-1))),
                    (int)(route.getLink(i).getEndY()/(Math.pow(2, map.getZoom()-1))));
        }
        g.dispose();
    }


    /**
     * draw Route route on Graphics g (Color: color)
     * use JXMapView map to get zoom
     * @param g
     * @param map
     * @param route
     * @param color
     */
     /*
    public void drawRouteNodes(Graphics2D g,JXMapViewer map, Route route, Color color){
        // create graphics
        g = (Graphics2D) g.create();
        //convert from viewport to world bitmap
        Rectangle rect = map.getViewportBounds();
        g.translate(-rect.x, -rect.y);

        //do the drawing
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // set brush
        g.setStroke(new BasicStroke(1));
        // set color
        g.setColor(color);

        for(int i=0; i<route.getNrOfNodes();i++){
            // draw line for every link in route
            // devide x,y coordinates by 2^(zoom-1) to fit to current zoom
            //g.drawRect((int)(route.getNode(i).getX()/(Math.pow(2, map.getZoom()-1))),
            //           (int)(route.getNode(i).getY()/(Math.pow(2, map.getZoom()-1))), 3, 3);
            g.fillRect((int)(route.getNode(i).getX()/(Math.pow(2, map.getZoom()-1))),
                       (int)(route.getNode(i).getY()/(Math.pow(2, map.getZoom()-1))), 3, 3);
        }
        g.dispose();
    }
    */

    /**
     * draw GPSPath path on Graphics g (Color: color)
     * GPS Point n is highlighted
     * use JXMapView map to get zoom
     * @param g
     * @param map
     * @param gpsNodesToMatch
     * @param gpsColor
     * @param n
     */
    public void drawGPSPath(Graphics2D g,JXMapViewer map, Vector<MatchedGPSNode> gpsNodesToMatch, MatchedGPSNode gpsNextNodeToMatch, Color gpsColor, Color gpsNextToMatchColor,  double zoomFactor){
        // create graphics
        g = (Graphics2D) g.create();
        //convert from viewport to world bitmap
        Rectangle rect = map.getViewportBounds();
        //System.out.println(rect.getX() + ", " + rect.getY() + "  " + rect.getWidth() + ", " + rect.getHeight());
        g.translate(-rect.x, -rect.y);

        //do the drawing
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // set brush
        g.setStroke(new BasicStroke(1));
        
        //draw every GPS node of trace
        for(MatchedGPSNode matchedGPSNode : gpsNodesToMatch){
        	//set color
        	g.setColor(matchedGPSNode.getColor());
            // draw rect for every GPS Point
            // devide x,y coordinates by 2^(zoom-1) to fit to current zoom
            g.drawRect((int)(matchedGPSNode.getDrawX()/zoomFactor),
                       (int)(matchedGPSNode.getDrawY()/zoomFactor), 3, 3);
        }
        
        if (gpsNextNodeToMatch != null) {
        	// set color for next GPS node to match
        	g.setColor(gpsNextToMatchColor);
        	// next GPS node to match
        	g.drawRect((int) (gpsNextNodeToMatch.getDrawX()/zoomFactor),
        		   (int) (gpsNextNodeToMatch.getDrawY()/zoomFactor), 3, 3);
        }
        
        // release graphics
        g.dispose();
    }
    
    public void drawGPSPath(Graphics2D g,JXMapViewer map, GPSTrace gpsTrace, Color gpsColor, double zoomFactor){
        // create graphics
        g = (Graphics2D) g.create();
        //convert from viewport to world bitmap
        Rectangle rect = map.getViewportBounds();
        //System.out.println(rect.getX() + ", " + rect.getY() + "  " + rect.getWidth() + ", " + rect.getHeight());
        g.translate(-rect.x, -rect.y);

        //do the drawing
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // set brush
        g.setStroke(new BasicStroke(1));
        
        //draw every GPS node of trace
        for(int i=gpsTrace.getNrOfNodes()-1; i>=0; i--){
        	//set color
        	g.setColor(gpsColor);
        	
        	if (gpsTrace.getNodeStatus(i) == 1) {
        		g.setColor(Color.RED);
        	}
        	
            // draw rect for every GPS Point
            // devide x,y coordinates by 2^(zoom-1) to fit to current zoom
            g.drawRect((int)(gpsTrace.getNodeX(i)/zoomFactor),
                       (int)(gpsTrace.getNodeY(i)/zoomFactor), 3, 3);
        }
      
        // release graphics
        g.dispose();
    }
    
    static int cc = 0;
    
    public void drawNRoute(Graphics2D g, JXMapViewer map, Vector<NRoute> nRoutes, Color nRouteColor, double zoomFactor) {
    	// create graphics
        g = (Graphics2D) g.create();
        //convert from view port to world bitmap
        Rectangle rect = map.getViewportBounds();
        g.translate(-rect.x, -rect.y);
        
        // do the drawing
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // set color
        g.setColor(nRouteColor);
        
        // set brush
        g.setStroke(new BasicStroke(3));
        
        g.setColor(Color.black);
        
		// draw route for each route
        for (int i=0; i < nRoutes.size(); i++ ){
        	
        	if (i == cc) 
        	{

        		NRoute nRoute = nRoutes.get(i);
        		
        		System.out.println("printing nRoute.IdOfThisNRoute: " + nRoute.IdOfThisNRoute + " | s: " + nRoute.getScore());

    			for (MatchedLink nRouteLink : nRoute.getNRouteLinks()) {
    				
    				System.out.print(nRouteLink.getStreetLink().startNode.id + " - " + nRouteLink.getStreetLink().endNode.id + " | ");
    				
    				// draw line for every link
    				// devide x,y coordinates by 2^(zoom-1) to fit to current zoom
    				g.drawLine((int) (nRouteLink.getStreetLink().startNode.x / zoomFactor),
    						(int) (nRouteLink.getStreetLink().startNode.y / zoomFactor),
    						(int) (nRouteLink.getStreetLink().endNode.x / zoomFactor),
    						(int) (nRouteLink.getStreetLink().endNode.y / zoomFactor));
    			}
    			
    			System.out.print("\n");
        		
        	}
	
        }
		
        if (nRoutes.size() > 0) {
    		cc++;
    		cc = cc % nRoutes.size();        	
        }

    }
    
    public void drawSelectedNRoute(Graphics2D g, JXMapViewer map, SelectedNRoute selectedNRoute, Color nRouteColor, Color selectableColor, Color deletableColor, double zoomFactor) {
     	// create graphics
        g = (Graphics2D) g.create();
        // convert from view port to world bitmap
        Rectangle rect = map.getViewportBounds();
        g.translate(-rect.x, -rect.y);

        // do the drawing
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // set brush
        g.setStroke(new BasicStroke(4));
        
        // set color for normal n route links
        g.setColor(nRouteColor);
        
        //draw selected N route (Start)
		for (myOSMWayPart nRouteLink : selectedNRoute.getNRouteLinksStart()) {
			// draw line for every link
			// devide x,y coordinates by 2^(zoom-1) to fit to current zoom

			g.drawLine((int) (nRouteLink.getStartX() / zoomFactor),
					   (int) (nRouteLink.getStartY() / zoomFactor),
					   (int) (nRouteLink.getEndX() / zoomFactor),
					   (int) (nRouteLink.getEndY() / zoomFactor));
		}
		
		//draw selected N route (End)
		for (myOSMWayPart nRouteLink : selectedNRoute.getNRouteLinksEnd()) {
			// draw line for every link
			// devide x,y coordinates by 2^(zoom-1) to fit to current zoom
			g.drawLine((int) (nRouteLink.getStartX() / zoomFactor),
					   (int) (nRouteLink.getStartY() / zoomFactor),
					   (int) (nRouteLink.getEndX() / zoomFactor),
					   (int) (nRouteLink.getEndY() / zoomFactor));
		}
		
		// draw selectable street link 
		g.setColor(selectableColor);
		
		myOSMWayPart selectableLink = selectedNRoute.getSelectableLink();
		
		if (selectableLink != null) {
			g.drawLine((int) (selectableLink.getStartX() / zoomFactor),
					   (int) (selectableLink.getStartY() / zoomFactor),
					   (int) (selectableLink.getEndX() / zoomFactor),
					   (int) (selectableLink.getEndY() / zoomFactor));
		}
		
		// draw selectable street link 
		g.setColor(Color.RED);
				
		myOSMWayPart deletableStreetLink = selectedNRoute.getDeletableLink();
				
		if (deletableStreetLink != null) {
			g.drawLine((int) (deletableStreetLink.getStartX() / zoomFactor),
					   (int) (deletableStreetLink.getStartY() / zoomFactor),
					   (int) (deletableStreetLink.getEndX() / zoomFactor),
					   (int) (deletableStreetLink.getEndY() / zoomFactor));
		}
				
				

    }
    
    public void drawSelectedRoute(Graphics2D g, JXMapViewer map, SelectedRoute selectedRoute, Color selectableColor, Color multiSelectableColor, Color selectedColor, Color nonMatchedColor, double zoomFactor){
    	// create graphics
        g = (Graphics2D) g.create();
        // convert from view port to world bitmap
        Rectangle rect = map.getViewportBounds();
        g.translate(-rect.x, -rect.y);

        // do the drawing
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // set brush
        g.setStroke(new BasicStroke(4));

        //draw selected route
        if (!selectedRoute.isEmpty()){
        	//get selected street links, draw them
        	for(myOSMWayPart selectedStreetLink : selectedRoute.getSelectedLinks()){
                // set color
                g.setColor((selectedStreetLink.isLastMatched() ? selectedColor : nonMatchedColor));                
        		// draw line for every link
        		// devide x,y coordinates by 2^(zoom-1) to fit to current zoom
        		g.drawLine( (int)(selectedStreetLink.getStartX()/zoomFactor),
        				    (int)(selectedStreetLink.getStartY()/zoomFactor),
        					(int)(selectedStreetLink.getEndX()/zoomFactor),
        					(int)(selectedStreetLink.getEndY()/zoomFactor));
        	}
        }

        //draw selectable link
        if (selectedRoute.selectableStreetLink()){
        	//System.out.println(selectedRoute.getSelectableStreetLink().getSelectCounter());
        	//set color
        	g.setColor( (selectedRoute.getSelectableLink().getSelectCounter() < 1) ? selectableColor : multiSelectableColor); 
        	g.drawLine( (int) (selectedRoute.getSelectableLink().getStartX()/zoomFactor),
    				    (int) (selectedRoute.getSelectableLink().getStartY()/zoomFactor),
    				    (int) (selectedRoute.getSelectableLink().getEndX()/zoomFactor),
    				    (int) (selectedRoute.getSelectableLink().getEndY()/zoomFactor));
        }
        
        //release graphics
        g.dispose();
    }
    
    static int c = 0;
    
    /**
     * draw StreetLinks of StreetMap street on Graphics g (Color: color)
     * use JXMapView map to get zoom
     * @param g
     * @param map
     * @param streetMap
     * @param color
     */
    public void drawStreetMap(Graphics2D g, JXMapViewer map, StreetMap streetMap, Color color, double zoomFactor, myOSMMap myMap){
    	
        // create graphics
        g = (Graphics2D) g.create();
        //convert from viewport to world bitmap
        Rectangle rect = map.getViewportBounds();
        g.translate(-rect.x, -rect.y);

        //do the drawing
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    	
        // set brush
        g.setStroke(new BasicStroke(3));
        // set color
        g.setColor(color);

    	Random random = new Random();
    	
    	if (myMap != null ) {
        	
    		myOSMNode n1;
    		myOSMNode n2;
    		myOSMWayPart wp;
    		for (int i=0; i < myMap.ways.size(); i++) {
//      		for (int i=19; i < 20 ; i++) {

				myOSMWay w = myMap.ways.get(i);
				
    			color = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
        		g.setColor(color);
        		
    			for (int j=0; j < w.WayParts.length; j++) {

    				wp = w.WayParts[j];
    				n1 = wp.startNode;
    				n2 = wp.endNode;

    				g.setColor(new Color(150,150,255));
    				
    				/*
					if (w.onyWay == false) {
        				g.setColor(Color.BLUE);
        			} 
            		else {
            			if (n1.x == n2.x) {
            				if (n1.y <= n2.y) {
            					g.setColor(Color.ORANGE);
            				}
            				else {
            					g.setColor(Color.YELLOW);
            				}
            			} else {
            				if (n1.x < n2.x) {
            					g.setColor(Color.YELLOW);
            				} else {
            					g.setColor(Color.ORANGE);
            				}
            			}
            		}
            		*/

        			g.drawLine((int)(n1.x / zoomFactor), (int)(n1.y / zoomFactor), (int)(n2.x / zoomFactor), (int)(n2.y / zoomFactor));
                	
    			}
    			
    		}

    		
    		g.dispose();
        }
    }
    	
    /**
     * draw StreetNodes of StreetMap street on Graphics g (Color: color)
     * use JXMapView map to get zoom
     * @param g
     * @param map
     * @param street
     * @param color
     */
    public void drawStreetNodes(Graphics2D g,JXMapViewer map, StreetMap street, Color color, double zoomFactor){
        // create graphics
        g = (Graphics2D) g.create();
        //convert from viewport to world bitmap
        Rectangle rect = map.getViewportBounds();
        
        g.translate(-rect.x, -rect.y);

        //do the drawing
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // set brush
        g.setStroke(new BasicStroke(1));
        // set color
        g.setColor(color);

        for(int i=0; i<street.getNrOfNodes();i++){
            // draw point for every node
            // devide x,y coordinates by 2^(zoom-1) to fit to current zoom
            g.drawRect((int)(street.getNodeX(i)/zoomFactor),
                    (int)(street.getNodeY(i)/zoomFactor),2,2);
        }
        g.dispose();
    }

	public void drawMatchedGPStoNRoute(Graphics2D g, JXMapViewer map,
			Vector<MatchedNLink> matchedNLinks,
			Vector<ReorderedMatchedGPSNode> matchedGPSNodes, double zoomFactor) {
		
		
		// create graphics
        g = (Graphics2D) g.create();
        // convert from view port to world bitmap
        Rectangle rect = map.getViewportBounds();
        g.translate(-rect.x, -rect.y);

        // do the drawing
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // set brush
        g.setStroke(new BasicStroke(4));
        
        //draw selected N route (Start)
		for (MatchedNLink matchedNLink : matchedNLinks) {
			
			// set Color
			g.setColor(matchedNLink.getColor());
			
			// draw line for every link
			// devide x,y coordinates by 2^(zoom-1) to fit to current zoom
			g.drawLine((int) (matchedNLink.getStreetLink().getStartX() / zoomFactor),
					   (int) (matchedNLink.getStreetLink().getStartY() / zoomFactor),
					   (int) (matchedNLink.getStreetLink().getEndX() / zoomFactor),
					   (int) (matchedNLink.getStreetLink().getEndY() / zoomFactor));
		}
		
		
		
		 //draw every GPS node of trace
        for(ReorderedMatchedGPSNode matchedGPSNode : matchedGPSNodes){
        	
        	boolean hasIndexChanged = matchedGPSNode.hasIndexChanged();
        
        	 // set brush
            g.setStroke(new BasicStroke(hasIndexChanged ? 2 : 1));
        	
        	//set color
        	g.setColor(hasIndexChanged ? Color.MAGENTA : matchedGPSNode.getColor());
        	
            // draw rect for every GPS Point
            // devide x,y coordinates by 2^(zoom-1) to fit to current zoom
            g.drawRect((int)(matchedGPSNode.getDrawX()/zoomFactor),
                       (int)(matchedGPSNode.getDrawY()/zoomFactor), 3, 3);
            
        }
        
        g.dispose();
	}
}
