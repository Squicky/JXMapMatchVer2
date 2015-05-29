/*
 * This class is used to draw Routes, GPS Data, .... into JXMapKit
 */

package graphic;

import org.jdesktop.swingx.*;

import algorithm.MatchedGPSNode;
import algorithm.MatchedLink;
import algorithm.MatchedNLink;
import algorithm.ReorderedMatchedGPSNode;
import osm.StreetLink;
import osm.StreetMap;
import route.*;
import gps.GPSNode;
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
        for(int i=0; i<gpsTrace.getNrOfNodes(); i++){
        	//set color
        	g.setColor(gpsColor);
            // draw rect for every GPS Point
            // devide x,y coordinates by 2^(zoom-1) to fit to current zoom
            g.drawRect((int)(gpsTrace.getNodeX(i)/zoomFactor),
                       (int)(gpsTrace.getNodeY(i)/zoomFactor), 3, 3);
        }
        
        // release graphics
        g.dispose();
    }
    
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
        
		// draw route for each route
		for (NRoute nRoute : nRoutes) {
			
			for (MatchedLink nRouteLink : nRoute.getNRouteLinks()) {
				// draw line for every link
				// devide x,y coordinates by 2^(zoom-1) to fit to current zoom
				g.drawLine((int) (nRouteLink.getStreetLink().getStartX() / zoomFactor),
						(int) (nRouteLink.getStreetLink().getStartY() / zoomFactor),
						(int) (nRouteLink.getStreetLink().getEndX() / zoomFactor),
						(int) (nRouteLink.getStreetLink().getEndY() / zoomFactor));
			}
	
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
		for (StreetLink nRouteLink : selectedNRoute.getNRouteLinksStart()) {
			// draw line for every link
			// devide x,y coordinates by 2^(zoom-1) to fit to current zoom
			g.drawLine((int) (nRouteLink.getStartX() / zoomFactor),
					   (int) (nRouteLink.getStartY() / zoomFactor),
					   (int) (nRouteLink.getEndX() / zoomFactor),
					   (int) (nRouteLink.getEndY() / zoomFactor));
		}
		
		//draw selected N route (End)
		for (StreetLink nRouteLink : selectedNRoute.getNRouteLinksEnd()) {
			// draw line for every link
			// devide x,y coordinates by 2^(zoom-1) to fit to current zoom
			g.drawLine((int) (nRouteLink.getStartX() / zoomFactor),
					   (int) (nRouteLink.getStartY() / zoomFactor),
					   (int) (nRouteLink.getEndX() / zoomFactor),
					   (int) (nRouteLink.getEndY() / zoomFactor));
		}
		
		// draw selectable street link 
		g.setColor(selectableColor);
		
		StreetLink selectableLink = selectedNRoute.getSelectableLink();
		
		if (selectableLink != null) {
			g.drawLine((int) (selectableLink.getStartX() / zoomFactor),
					   (int) (selectableLink.getStartY() / zoomFactor),
					   (int) (selectableLink.getEndX() / zoomFactor),
					   (int) (selectableLink.getEndY() / zoomFactor));
		}
		
		// draw selectable street link 
		g.setColor(deletableColor);
		
		StreetLink deletableLink = selectedNRoute.getDeletableLink();
		
		if (deletableLink != null) {
			g.drawLine((int) (deletableLink.getStartX() / zoomFactor),
					   (int) (deletableLink.getStartY() / zoomFactor),
					   (int) (deletableLink.getEndX() / zoomFactor),
					   (int) (deletableLink.getEndY() / zoomFactor));
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
        	for(StreetLink selectedStreetLink : selectedRoute.getSelectedLinks()){
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

    /**
     * draw StreetLinks of StreetMap street on Graphics g (Color: color)
     * use JXMapView map to get zoom
     * @param g
     * @param map
     * @param street
     * @param color
     */
    public void drawStreetMap(Graphics2D g,JXMapViewer map, StreetMap street, Color color, double zoomFactor){
    	
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

        for(int i=0; i<street.getNrOfLinks();i++){
        	
        	int DirectionColor = street.getLink(i).DirectionColor;
        	
        	if (DirectionColor == 0) {
        		color = Color.red;
        	}
        	else if (DirectionColor == 1) {
        		color = Color.orange;
        	}  
        	else if (DirectionColor == 2) {
        		color = Color.yellow;
        	}
        	else {
            	color = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
        	}

    		color = Color.red;
        	color = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
        	
        	if (DirectionColor != -1 || true) {
                g.setColor(color);
            	
                // draw line for every link
                // devide x,y coordinates by 2^(zoom-1) to fit to current zoom
                g.drawLine((int)(street.getStartX(i)/zoomFactor),
                        (int)(street.getStartY(i)/zoomFactor),
                        (int)(street.getEndX(i)/zoomFactor),
                        (int)(street.getEndY(i)/zoomFactor));        		
        	}

        }
        g.dispose();
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
