package myOSM;

import java.io.File;
import java.io.FileInputStream;
import java.lang.instrument.Instrumentation;
import java.sql.Time;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import cartesian.Coordinates;
import osm.StreetLink;
import osm.StreetMap;

public class myOSMMap {

	public int test = 1;
	
	//public List<myOSMNode> notes = new LinkedList<myOSMNode>();
	public Map<Long, myOSMNode> nodes = new HashMap<Long, myOSMNode>();
	public long count_nodes = 0;
	
	
	//public List<myOSMWay> ways = new LinkedList<myOSMWay>();
	public Map<Long, myOSMWay> ways = new HashMap<Long, myOSMWay>();
	public long count_ways = 0;
	
	public double osmMinLat;
	public double osmMaxLat;
	public double osmMinLon;
	public double osmMaxLon;
    
    public String osmVersion = "";
    public String osmGenerator = "";
	
	//XML classes
	private XMLInputFactory factory = XMLInputFactory.newInstance(); 
	private XMLStreamReader parser;
	
	//file class to access XML file
	private File xmlFile;
	
	//spacer to check XML formation
	private StringBuilder spacer = new StringBuilder();

	private myOSMWay tempWay = new myOSMWay();

	private String lastkey = ""; 
	
	public myOSMMap(File file) {
		
		xmlFile = file;
		
		myOSMNode[] myNodes = null;

		int x = 5000000;
		myNodes = new myOSMNode[x];
		for(int i = 0; i < x; i++) {
			myNodes[i] = new myOSMNode();
		}
		
		x = 10000;
		myNodes = new myOSMNode[x];
		for(int i = 0; i < x; i++) {
			myNodes[i] = new myOSMNode();
		}
		
		x = 100000;
		myNodes = new myOSMNode[x];
		for(int i = 0; i < x; i++) {
			myNodes[i] = new myOSMNode();
		}

		x = 12121731;
		myNodes = new myOSMNode[x];
		for(int i = 0; i < x; i++) {
			myNodes[i] = new myOSMNode();
		}
		
		try {
			parser = factory.createXMLStreamReader( new FileInputStream( xmlFile));
		} catch (Exception e) {
			System.err.println("Error: " + e.toString());
		}
		
		parseXML();
	}
	
	public StreetMap getSteetMap() {
		
		StreetMap streetMap = new StreetMap(getNrOfAllWayParts(), nodes.size(), "");
        
        //set boundary
        streetMap.setMinX(Coordinates.getCartesianX(osmMinLon, osmMinLat));
        streetMap.setMinY(Coordinates.getCartesianY(osmMinLon, osmMinLat));
        streetMap.setMaxX(Coordinates.getCartesianX(osmMaxLon, osmMaxLat));
        streetMap.setMaxY(Coordinates.getCartesianY(osmMaxLon, osmMaxLat));
		
		for (int i = 0; i < ways.size(); i++) {
		
			for (int j = 0; j < ways.get(i).WayParts.size(); j++) {
				
				myOSMWayPart wp = ways.get(i).WayParts.get(j);
				
				streetMap.addLink(wp.startNode.x, wp.startNode.y, wp.endNode.x, wp.endNode.y, wp.parentWay.id, wp.startNode.id, wp.endNode.id);
				
			}
			
		}
        
        return streetMap;
	}
	
	public int getNrOfAllWayParts() {
		
		int z = 0;
		
		for(int i = 0; i < ways.size(); i++) {
				z = z + ways.get(i).WayParts.size();							
		}
		
		return z;
	}
	
	public void linkToStreetMap(StreetMap sm) {

		for (int i=0; i < ways.size(); i++) {
			
			myOSMWay w = ways.get(i);
    		
			for (int j=0; j < w.WayParts.size(); j++) {
				
				myOSMWayPart wp = w.WayParts.get(j);
				
				for (int k=0; k < sm.getNrOfLinks(); k++) {
					
					StreetLink sl = sm.getLinks()[k];
					
					if (sl == null || wp == null) {
						System.out.println("Error: linkToStreetMap: (sl == null || wp == null)");
						System.exit(-1);
					}
					
					if (sl.myid == wp.parentWay.id) {
						if (
								sl.getStartX() == wp.startNode.x && 
								sl.getStartY() == wp.startNode.y && 
								sl.getEndX() == wp.endNode.x && 
								sl.getEndY() == wp.endNode.y && 
								sl.startNodeId == wp.startNode.id && 
								sl.endNodeId == wp.endNode.id && 
								wp.isBackDirection == false
							)
						{
							if (wp.streetLink == null && sl.myWayPart == null)
							{
								sl.myWayPart = wp;
								wp.streetLink = sl;
							}
							else {
								System.out.println("Warrning: linkToStreetMap");
							}
						}
						
						if (
								sl.getStartX() == wp.endNode.x && 
								sl.getStartY() == wp.endNode.y && 
								sl.getEndX() == wp.startNode.x && 
								sl.getEndY() == wp.startNode.y && 
								sl.startNodeId == wp.endNode.id && 
								sl.endNodeId == wp.startNode.id && 
								wp.isBackDirection == true
							)
						{
							if (wp.streetLink == null && sl.myWayPartBackDirection == null)
							{
								sl.myWayPartBackDirection = wp;
								wp.streetLink = sl;
							}
							else {
								System.out.println("Warrning: linkToStreetMap");
							}
						}
						

					}
					
				}
				
			}
		
		}
		
		myOSMWayPart wp = null;
		myOSMWay w = null;
		
		for (int i=0; i < ways.size(); i++) {
			
			w = ways.get(i);
    		
			for (int j=0; j < w.WayParts.size(); j++) {
				
				wp = w.WayParts.get(j);

				if (wp.streetLink == null) {
					System.out.println("Error: linkToStreetMap");
					System.exit(-1);
				} else {
					
					if (wp.isBackDirection) {
						if (wp.streetLink.myWayPartBackDirection != wp) {
							System.out.println("Error: linkToStreetMap");
							System.exit(-1);							
						}
						
						if (wp.startNode.id != wp.streetLink.endNodeId || wp.endNode.id != wp.streetLink.startNodeId) {
							System.out.println("Error: linkToStreetMap");
							System.exit(-1);
						}
					} else {
						
						if (wp.streetLink.myWayPart != wp) {
							System.out.println("Error: linkToStreetMap");
							System.exit(-1);														
						}
						
						if (wp.startNode.id != wp.streetLink.startNodeId || wp.endNode.id != wp.streetLink.endNodeId) {
							System.out.println("Error: linkToStreetMap");
							System.exit(-1);
						}
					}
					
				}
			}
		}
		
		StreetLink sl = null;
		
		for (int k=0; k < sm.getNrOfLinks(); k++) {
			sl = sm.getLinks()[k];
			
			if (sl.myWayPart == null) {
				System.out.println("Error: linkToStreetMap");
				System.exit(-1);
			} else {
				
				if (sl.myWayPart.streetLink != sl) {
					System.out.println("Error: linkToStreetMap");
					System.exit(-1);					
				}
				
				if (sl.myWayPart.parentWay.onyWay == false) {
					if (sl.myWayPartBackDirection == null ) {
						System.out.println("Error: linkToStreetMap");
						System.exit(-1);
					}
				} else {
					if (sl.myWayPartBackDirection != null ) {
						System.out.println("Error: linkToStreetMap");
						System.exit(-1);
					}					
				}
				
				if (sl.myWayPartBackDirection != null ) {				
					if (sl.myWayPartBackDirection.streetLink != sl) {
						System.out.println("Error: linkToStreetMap");
						System.exit(-1);
					}
				}
				
				if (sl.startNodeId != sl.myWayPart.startNode.id || sl.endNodeId != sl.myWayPart.endNode.id) {
					System.out.println("Error: linkToStreetMap");
					System.exit(-1);
				}

				if (sl.myWayPartBackDirection != null ) {	
					if (sl.startNodeId != sl.myWayPartBackDirection.endNode.id || sl.endNodeId != sl.myWayPartBackDirection.startNode.id) {
						System.out.println("Error: linkToStreetMap");
						System.exit(-1);
					}
				}

			}
		}
	}
	
	
	public void removeUnusedNotesAndWaysAndSetWayParts() {
		
		for(int i = 0; i < ways.size(); i++) {
			ways.get(i).set_meansOfTransport();
		}
		
		for(int i = (ways.size() - 1); i >= 0 ; i--) {
			
			if (ways.get(i).getMeansOfTransportPermission(myOSMWay.CAR)) {
				ways.get(i).setCountAndXYOfNotes();
				ways.get(i).setWayParts();
			}
			else {
				ways.remove(i);
			}			
		}

		/*
		for (int i = nodes.size() -1; i >= 0; i--) {
			if (nodes.get(i).countIsEndOfWay == 0 && nodes.get(i).countIsInnerNoteofWay == 0 && nodes.get(i).countIsStartOfWay == 0) {
				nodes.remove(i);
			}
		}
		*/
		
		nodes.clear();
		
		for (int i = ways.size() - 1; i >= 0; i--) {
//			System.out.println(ways.get(i).id + " : " + ways.get(i).meansOfTransport);
		}
	}
	
	public myOSMNode getNode(long nodeID) {
		
		if (nodes.containsValue(nodeID)) {
			return nodes.get(nodeID);
		}
		
		/*
		for (myOSMNode note : notes) {
			if (note.id == nodeID) {
				return note;
			}
		}
		*/
		
		return null;
	}
	
	/**
	 * Parses the XML file to a dynamic osmData Datastructure
	 * @return
	 */
	private boolean parseXML(){		
		//start reading xml data via "stream"
		try {

			long abc = 0;
			  
			parser_loop:

			while ( parser.hasNext() ) 
			{ 
				abc++;
				
//				abc = ;
				if ((abc % 1000000) == 0) {
					System.out.println((new GregorianCalendar()).getTime().toString() + " | " + abc + " |n: " + count_nodes + " | w: " + count_ways);
				}


				/*
				if (abc > 45002536) {
					if ((abc % 1) == 0) {
						System.out.print((new GregorianCalendar()).getTime().toString() + " | " + abc );
						System.out.println(" |n: " + count_nodes + " | w: " + count_ways);
					}
				}
				*/

				/*
				if (abc > 25570000) {
					if ((abc % 100) == 0) {
						System.out.println((new GregorianCalendar()).getTime().toString() + " | " + abc);
					}
				}
				*/
				
				boolean Systemoutprint = false;		
				  				
				if (Systemoutprint) System.out.println( "Event: " + parser.getEventType() );
				switch (parser.getEventType()) 
				{ 
				case XMLStreamConstants.START_DOCUMENT: 
					if (Systemoutprint) System.out.println( "START_DOCUMENT: " + parser.getVersion() ); 
					break; 

				case XMLStreamConstants.END_DOCUMENT: 
					if (Systemoutprint) System.out.println( "END_DOCUMENT: " ); 
					parser.close(); 
					break; 

				case XMLStreamConstants.NAMESPACE: 
					if (Systemoutprint) System.out.println( "NAMESPACE: " + parser.getNamespaceURI() ); 
					break; 

				case XMLStreamConstants.START_ELEMENT: 
					spacer.append( "  " ); 
					if (Systemoutprint) System.out.println( /*spacer + */ "START_ELEMENT: " + parser.getLocalName() + "\n" ); 

					if (parser.getLocalName()=="node")
					{
						//handle nodes
						nodeHandler();
					}
					else if (parser.getLocalName()=="way"){	 
						//handle ways
						wayHandler();
						if (Systemoutprint) System.out.println("Way!\n");
					}
					else if (parser.getLocalName()=="nd"){
						//handle node references in ways
						referenceHandler();
					}
					else if (parser.getLocalName()=="tag"){
						//handle tags in ways
						tagHandler();
					}
					else if (parser.getLocalName()=="bounds"){
						//handle boundary of the XLM file
						boundsHandler();
					}
					else if (parser.getLocalName()=="osm"){
						//handle general OSM info
						osmHandler();
					}
					else if (parser.getLocalName()=="relation"){
						// stop parsing file, leave while loop, actually we don't this block at the moment
						parser.close();
						break parser_loop;
					}
					break; 

				case XMLStreamConstants.CHARACTERS: 
					if ( ! parser.isWhiteSpace() ){ 
						//System.out.println( spacer + "  CHARACTERS: " + parser.getText() );
						;
					}
					break; 

				case XMLStreamConstants.END_ELEMENT:
					if (tempWay.id == 8090147) {
						tempWay.id++;
						tempWay.id--;
					}
					// Save way
					if (parser.getLocalName()=="way" && (tempWay.wayNotNeeded == false)) {
						addWay(); 						
					}
					
					//System.out.println( spacer + "END_ELEMENT: " + parser.getLocalName() ); 
					spacer.delete(spacer.length()-2, spacer.length()); 
					break; 

				default: 
					break; 
				} 
				parser.next(); 
			}
		} catch (Exception e) {
			System.err.println("Error parsing XML File: \n" + e.toString());
			return false;
		} 
		return true;
	}

	
	/**
	 * Handles OSM Nodes
	 */
	public void nodeHandler(){
		
		myOSMNode note = new myOSMNode();
				
		//read node data
		for ( int i = 0; i < parser.getAttributeCount(); i++ ) {
	    	  if (parser.getAttributeLocalName(i)=="id")
	    		  note.id = Long.valueOf(parser.getAttributeValue(i));
	    	  else if (parser.getAttributeLocalName(i)=="lat")
	    		  note.lat = Double.valueOf(parser.getAttributeValue(i)).doubleValue();
	    	  else if (parser.getAttributeLocalName(i)=="lon")
	    		  note.lon = Double.valueOf(parser.getAttributeValue(i)).doubleValue();
	    }
		
		if (note.id == 256332999) {
			note.id++;
			note.id--;
		}
		
		//nodes.add(note);
		//nodes.put(note.id, note);
		count_nodes++;
	}
	
	/**
	 * Handles OSM ways
	 */
	public void wayHandler(){
  	  	
		tempWay = new myOSMWay();
		
  	  	//now we check all Attributes of the way element
  	  	for ( int i = 0; i < parser.getAttributeCount(); i++ ) {

  	  		if (parser.getAttributeLocalName(i).equals("id")) {
  	  	
  	  	  		tempWay.id = Long.valueOf(parser.getAttributeValue(i));
  	  	  		
  				if (tempWay.id == 26796835)
  				{
  					i++;
  					i--;
  				}
  	  			
  	  		}

  	  	}
	}
	
	/**
	 * Handles XML References
	 */
	public void referenceHandler(){
		//safe all nodes that belongs to a way
		for ( int i = 0; i < parser.getAttributeCount(); i++ ) {
			if (parser.getAttributeLocalName(i)=="ref") {
				tempWay.refs.add(this.getNode(Long.valueOf(parser.getAttributeValue(i))));
			}
			else  {
				System.out.println("should never be called: referenceHandler");
			}
		}
	}
	
	/**
	 * Save OSM Way
	 */
	public void addWay()
	{	
			
		//this.ways.add(tempWay);
		//this.ways.put(tempWay.id, tempWay);
		count_ways++;

		tempWay = new myOSMWay();
		
		/**
		 * 
		//save ways for each boundary, if they cross those
		for (Boundary boundary : boundaries){
			
			//store nodes of a part way
			Vector<Long> partWayNodes = new Vector<Long>();
			Vector<Vector<Long>> partWays = new Vector<Vector<Long>>(MAX_PARTWAYS);
			
			//check if boundary contains every node, otherwise split up ways
			for (Long nodeID : wayNodes){
				if (boundary.getNodeList().containsKey(nodeID))	//start a part way if boundary contains node
					partWayNodes.add(nodeID);
				else if (partWayNodes.size() >= MIN_WAYSIZE){	//is our part way long enough?
					partWays.add(partWayNodes);
					partWayNodes = new Vector<Long>(MAX_WAYSIZE);	//new vector for next part way
				}
				else								
					partWayNodes.clear();	//otherwise delete/empty part way for next loop
			}
		
			//if last nodes builds an part way, store it
			if (partWayNodes.size() >= MIN_WAYSIZE)
				partWays.add(partWayNodes);
		
			//for all ways belongs to the way id
			for (Vector<Long> nodes : partWays){
				//increase outerNodeCount for outer ways
				boundary.getNodeList().get((Long) nodes.firstElement()).increaseOuterNodeCount();
				boundary.getNodeList().get((Long) nodes.lastElement()).increaseOuterNodeCount();
				//increase innerNodeCount for inner way nodes
				for (int i=1; i<nodes.size()-2;i++)
					((OSMNode)boundary.getNodeList().get((Long) nodes.elementAt(i))).increaseInnerNodeCount();
			}

			//insert way into data structure
			if (!partWays.isEmpty())
				boundary.getOSMWays().put(id, new OSMWay(id.longValue(), partWays, maxSpeed, oneWay,
							lanes, motorcar, highway, name, meansOfTransport));
		}
		//if (partWays.size()>1)
		//	System.out.println("Way (id="+id+") split in "+partWays.size()+" part Ways");
		*/
	}

	/**
	 * Handler for XML tags
	 */
	public void tagHandler(){
		for ( int i = 0; i < parser.getAttributeCount(); i++ ) {
			//as all tags are (k)ey / (v)alue pairs (in this order) we remember the last key and halde the
  		  	//assignment when we find a value.
  		  	if (parser.getAttributeLocalName(i).equals("k")) {  		  		
  		  		lastkey = parser.getAttributeValue(i);
  		  	}
  		  	else if (parser.getAttributeLocalName(i).equals("v")){
  		  		if (lastkey.equals("created_by") || lastkey.equals("visible")){
  		  			//ignore
  		  			lastkey="";
  		  		}	
  		  		else if (lastkey.equals("highway")){
  		  			tempWay.highway = parser.getAttributeValue(i);
  		  			if (tempWay.highway.equals("service")) {
  		  				//System.out.println("Service route, way id="+id);
  		  				tempWay.wayNotNeeded = true;	//we don't need this way
  		  			}
  		  		}
  		  		else if (lastkey.equals("motorcar")) {
  		  			tempWay.motorcar = parser.getAttributeValue(i);
  		  		}
  		  		else if (lastkey.equals("oneway")) {
  		  			
  		  			String s = parser.getAttributeValue(i);
  		  			if (s.equals("yes")) {
  		  				tempWay.onyWay = true;
  		  			} else {
  		  				tempWay.onyWay = Boolean.valueOf(s);
  		  			}
  		  			
  		  		}
  		  		else if (lastkey.equals("lanes")){
  		  			tempWay.lanes = Integer.parseInt( parser.getAttributeValue(i).split(";")[0] );
  		  		}
  		  		else if (lastkey.equals("name")){
  		  			tempWay.name = parser.getAttributeValue(i);
  		  		}
  		  		else if (lastkey.equals("railway") && parser.getAttributeValue(i).equals("tram")){
  		  			tempWay.meansOfTransport |= myOSMWay.TRAM;
  		  		}
  		  		else if (lastkey.equals("area") && parser.getAttributeValue(i).equals("yes")) {
  		  			tempWay.wayNotNeeded = true;
		  		}
  		  	}
	    	else {
				System.out.println("should never be called: tagHandler");
	    	}
  	  	}
	}
	
	/**
	 * Handles OSM tag 
	 */
	public void osmHandler(){
		//read OSM general info
		for ( int i=0; i < parser.getAttributeCount(); i++){
			if (parser.getAttributeLocalName(i).equals("version"))
				osmVersion = parser.getAttributeValue(i);
			else if (parser.getAttributeLocalName(i).equals("generator"))
				osmGenerator = parser.getAttributeValue(i);
		}
		//print these info
		System.out.println("Parsing "+xmlFile.getName()+"...\nOSM-Version: "+osmVersion+"\nGenerator: "+osmGenerator);
	}

	/**
	 * handles boundary tag
	 */
	public void boundsHandler(){
  	  //now we check all Attributes of the bounds
  	  for ( int i = 0; i < parser.getAttributeCount(); i++ ) {
  		  if (parser.getAttributeLocalName(i).equals("minlat"))
  			  osmMinLat = Double.valueOf(parser.getAttributeValue(i));
  		  else if (parser.getAttributeLocalName(i).equals("maxlat"))
  			  osmMaxLat = Double.valueOf(parser.getAttributeValue(i));
  		  else if (parser.getAttributeLocalName(i).equals("minlon"))
  			  osmMinLon = Double.valueOf(parser.getAttributeValue(i));
  		  else if (parser.getAttributeLocalName(i).equals("maxlon"))
  			  osmMaxLon = Double.valueOf(parser.getAttributeValue(i));
  		  else   
  			  System.out.println("should never be called: boundsHandler");		    	
  	  }
  	  
  	  //print min,max lat/lon of OSM-file
  	  System.out.println("OSM-file boundary min(Lat/Lon),max(Lat/Lon) : ("+
  			  			  osmMinLat+", "+osmMinLon+"),("+osmMaxLat+", "+osmMaxLon+")");
	}
}
