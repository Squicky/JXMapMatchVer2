package myOSM;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.Vector;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

public class myOSMMap {

	public Map<Long, myOSMNode> nodes = new HashMap<Long, myOSMNode>();
	public long count_nodes = 0;
	
	public Map<Integer, myOSMWay> ways = new HashMap<Integer, myOSMWay>();
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
	public File osmFile;
	
	//spacer to check XML formation
	private StringBuilder spacer = new StringBuilder();

	private myOSMWay tempWay = new myOSMWay(this);

	private String lastkey = ""; 
	
	private int parseXML_status = 0;
	private Map<Integer, Long> nodeIdsOfWay = new HashMap<Integer, Long>();
	private TreeSet<Long> neededNodesIds = new TreeSet<Long>();
	private boolean isBuildingWay = false;
	
	int anzahl_ways = 0;
	int anzahl_ways_Building = 0;
	int anzahl_ways_Car = 0;
	
	Map<Long, Map<Integer, myEdge>> edges = new HashMap<Long, Map<Integer, myEdge>>();
	
	ArrayList<myDataset> DatasetsUp = new ArrayList<myDataset>();
	ArrayList<myDataset> DatasetsDown = new ArrayList<myDataset>();
	
	public myOSMMap(File _xmlFile, String netFilePath, String DatasetFolderPath) {

		osmFile = _xmlFile;
		
		try {
			
			DatasetsUp = myDataset.loadDatasetsUp(DatasetFolderPath + "upstream-data.csv");
			
			DatasetsDown = myDataset.loadDatasetsDown(DatasetFolderPath + "downstream-data.csv");
			
			Map<Integer, myEdge> edgesTemp = myEdge.loadGetEdges(netFilePath);
			
			for (int i = 0 ; i < edgesTemp.size(); i++) {
				
				myEdge e = edgesTemp.get(i);
				
				if (edges.containsKey(e.osmWayId)) {
					
					Map<Integer, myEdge> me = edges.get(e.osmWayId);
					
					me.put(me.size(), e);
					
					edges.put(e.osmWayId, me);
				} else {
					Map<Integer, myEdge> me = new HashMap<Integer, myEdge>();
					
					me.put(me.size(), e);
					
					edges.put(e.osmWayId, me);
				}
				
			}
			
			parser = factory.createXMLStreamReader( new FileInputStream( osmFile));
		} catch (Exception e) {
			System.err.println("Error: myOSMMap(...) " + e.toString());
		}
		
		parseXML_status = 0;
		parseXML(true);
		
		try {
			parser = factory.createXMLStreamReader( new FileInputStream( osmFile));
		} catch (Exception e) {
			System.err.println("Error: " + e.toString());
		}
		parseXML_status = 1;
		nodeIdsOfWay.clear();
		isBuildingWay = false;
		parseXML(false);

	}
	
	/*
	public StreetMap getSteetMap() {
		
		StreetMap streetMap = new StreetMap(getNrOfAllWayParts(), nodes.size(), "");
        
        //set boundary
        streetMap.setMinX(Coordinates.getCartesianX(osmMinLon, osmMinLat));
        streetMap.setMinY(Coordinates.getCartesianY(osmMinLon, osmMinLat));
        streetMap.setMaxX(Coordinates.getCartesianX(osmMaxLon, osmMaxLat));
        streetMap.setMaxY(Coordinates.getCartesianY(osmMaxLon, osmMaxLat));
		
        myOSMWayPart wp;
        myOSMWay w;
        StreetLink sl;
		for (int i = 0; i < ways.size(); i++) {
		
			w = ways.get(i);
			
			for (int j = 0; j < w.WayParts.length; j++) {
				
				wp = w.WayParts[j];
				
				sl = streetMap.addLink(wp.startNode.x, wp.startNode.y, wp.endNode.x, wp.endNode.y, wp.parentWay.id, wp.startNode.id, wp.endNode.id);
				
				if (sl != null) {

					wp.streetLink = sl;
					
					if (wp.isBackDirection) {
						sl.myWayPartBackDirection = wp;
						
						if (sl.myWayPart == null) {
							sl.myWayPart = null;
						}
					} else {
						sl.myWayPart = wp;
					}
					
					if (58926 == sl.ObjID) {
						System.out.print(sl.ObjID + " " + sl.myid + " ");
						if (sl.myWayPart == null) {
							System.out.print("  0  ");
						} else {
							System.out.print(sl.myWayPart.ObjID + " ");
						}
						
						if (sl.myWayPartBackDirection == null) {
							System.out.println("  0  " + wp.ObjID + " " + wp.parentWay.id);
						} else {
							System.out.println(sl.myWayPartBackDirection.ObjID + " " + wp.ObjID + " " + wp.parentWay.id);
						}
					}
					
					if (wp.isBackDirection) {
						if (wp.streetLink.myWayPartBackDirection != wp) {
							System.out.println("Error: getSteetMap");
//							System.exit(-1);							
						}
						
						if (wp.startNode.id != wp.streetLink.endNodeId || wp.endNode.id != wp.streetLink.startNodeId) {
							System.out.println("Error: getSteetMap");
//							System.exit(-1);
						}
					} else {
						
						if (wp.streetLink.myWayPart != wp) {
							System.out.println("Error: getSteetMap");
//							System.exit(-1);														
						}
						
						if (wp.startNode.id != wp.streetLink.startNodeId || wp.endNode.id != wp.streetLink.endNodeId) {
							System.out.println("Error: getSteetMap");
//							System.exit(-1);
						}
					}
					
					
				} else {
					sl = streetMap.addLink(wp.startNode.x, wp.startNode.y, wp.endNode.x, wp.endNode.y, wp.parentWay.id, wp.startNode.id, wp.endNode.id);					
				}
				
			}
			
		}
        
        return streetMap;
	}
	*/
	
	public myDataset getDatasetUp (long Timestamp) {
		
		Timestamp = Timestamp * 1000000000L;
		
		for (int i = 0; i < DatasetsUp.size(); i++) {
			
			if (Timestamp <= DatasetsUp.get(i).timestamp) {
				return DatasetsUp.get(i);
			}
			
		}
		
		return null;
	}
	
	public myDataset getDatasetDown (long Timestamp) {
		
		Timestamp = Timestamp * 1000000000L;
		
		for (int i = 0; i < DatasetsDown.size(); i++) {
			
			if (Timestamp <= DatasetsDown.get(i).timestamp) {
				return DatasetsDown.get(i);
			}
			
		}
		
		return null;
	}
	
	public int getNrOfAllWayParts() {
		
		int z = 0;
		
		for(int i = 0; i < ways.size(); i++) {
				z = z + ways.get(i).WayParts.length;	
		}
		
		return z;
	}
	
	/*
	public void linkToStreetMap(StreetMap sm) {

		for (int i=0; i < ways.size(); i++) {
			
			myOSMWay w = ways.get(i);
    		
			for (int j=0; j < w.WayParts.length; j++) {
				
				myOSMWayPart wp = w.WayParts[j];
				
//				for (int k=0; k < sm.getNrOfLinks(); k++) {
					
					//StreetLink sl = sm.getLinks()[k];
				//StreetLink sl = sm.getStreetLink(wp.startNode.id, wp.endNode.id, wp.parentWay.id);
			
					
					if (sl == null || wp == null) {
						System.out.println("Error: linkToStreetMap: (sl == null || wp == null)");
						System.exit(-1);
					} else {
						
						if (sl.myid == wp.parentWay.id) {
							if (wp.isBackDirection == false) {
//								sl.myWayPart = wp;
//								wp.streetLink = sl;
							} else {
//								sl.myWayPartBackDirection = wp;
//								wp.streetLink = sl;
//								wp.streetLink.myWayPartBackDirection = wp;
							}
						}
					}
					
					
					
					if (sl.myid == wp.parentWay.id) {
						if (
								sl.getStartX() == wp.startNode.x && 
								sl.getStartY() == wp.startNode.y && 
								sl.getEndX() == wp.endNode.x && 
								sl.getEndY() == wp.endNode.y && 
								sl.startNodeId == wp.startNode.id && 
								sl.endNodeId == wp.endNode.id 
								&& wp.isBackDirection == false
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
								sl.endNodeId == wp.startNode.id 
								//&& wp.isBackDirection == true
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
					
//				}
				
			}
		
		}
		
		myOSMWayPart wp = null;
		myOSMWay w = null;
		
		for (int i=0; i < ways.size(); i++) {
			
			w = ways.get(i);
    		
			for (int j=0; j < w.WayParts.length; j++) {
				
				wp = w.WayParts[j];

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
		
		//StreetLink sl = null;
		
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
	*/
		
	public void removeUnusedNotesAndWaysAndSetWayParts() {
		
		for(int i = (ways.size() - 1); i >= 0 ; i--) {
				ways.get(i).setCountAndXYOfNotes();
		}
		
		/*
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
		*/
		
		/*
		for (int i = nodes.size() -1; i >= 0; i--) {
			if (nodes.get(i).countIsEndOfWay == 0 && nodes.get(i).countIsInnerNoteofWay == 0 && nodes.get(i).countIsStartOfWay == 0) {
				nodes.remove(i);
			}
		}
		*/

	}
	
	public myOSMNode getNode(long nodeID) {
			return nodes.get(nodeID);
		
		/*
		for (myOSMNode note : nodes) {
			if (note.id == nodeID) {
				return note;
			}
		}
		
		
		return null;
		*/
	}
	
	/**
	 * Parses the XML file to a dynamic osmData Datastructure
	 * @return
	 */
	private boolean parseXML(boolean showOsmInfo){		
		anzahl_ways = 0;
		anzahl_ways_Building = 0;
		anzahl_ways_Car = 0;
		

		long abc = 0;
		
		//start reading xml data via "stream"
		try {

			parser_loop:

				
			while ( parser.hasNext() ) 
			{ 
				abc++;
				
//				abc = ;
				if ((abc % 1000000) == 0) {
					System.out.println((new GregorianCalendar()).getTime().toString() + " | " + abc + " | n: " + count_nodes + " | w: " + count_ways);
				}


				/*
				if (abc == 4966213) {
					System.out.print((new GregorianCalendar()).getTime().toString() + " | " + abc );
					System.out.println(" |n: " + count_nodes + " | w: " + count_ways);
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

					if ( parser.getLocalName()=="node") {
						//handle nodes
						nodeHandler();
					}
					else if (parser.getLocalName()=="way") {
						//handle ways
						wayHandler();
						if (Systemoutprint) System.out.println("Way!\n");
					}
					else if (parser.getLocalName()=="nd") {
						//handle node references in ways
						referenceHandler();
					}
					else if (parser.getLocalName()=="tag" ) {
						//handle tags in ways
						tagHandler();
					}
					else if (parser.getLocalName()=="bounds") {
						//handle boundary of the XLM file
						boundsHandler( showOsmInfo );
					}
					else if (parser.getLocalName()=="osm") {
						//handle general OSM info
						osmHandler( showOsmInfo );
					}
					else if (parser.getLocalName()=="relation") {
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
			System.err.println("Error parsing XML File: \n" + e.toString() + "\n" + abc);
			return false;
		} 
		return true;
	}

	/**
	 * Handles OSM Nodes
	 */
	public void nodeHandler(){
		
		myOSMNode node = new myOSMNode();
	
		//read node data
		for ( int i = 0; i < parser.getAttributeCount(); i++ ) {
	    	  if (parser.getAttributeLocalName(i)=="id")
	    		  node.id = Long.valueOf(parser.getAttributeValue(i));
	    	  else if (parser.getAttributeLocalName(i)=="lat")
	    		  node.lat = Double.valueOf(parser.getAttributeValue(i)).doubleValue();
	    	  else if (parser.getAttributeLocalName(i)=="lon")
	    		  node.lon = Double.valueOf(parser.getAttributeValue(i)).doubleValue();
	    }
		
		if (this.parseXML_status == 1) {
			if (this.neededNodesIds.contains(node.id)) {
				//nodes.add(note);
				nodes.put(node.id, node);
			}
		}
		count_nodes++;
	}
	
	/**
	 * Handles OSM ways
	 */
	public void wayHandler(){
  	  	
		tempWay = new myOSMWay(this);
		nodeIdsOfWay.clear();
		this.isBuildingWay = false;
		
  	  	//now we check all Attributes of the way element
  	  	for ( int i = 0; i < parser.getAttributeCount(); i++ ) {

  	  		if (parser.getAttributeLocalName(i).equals("id")) {
  	  	
  	  	  		tempWay.id = Long.valueOf(parser.getAttributeValue(i));
  	  			
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
				long l = Long.valueOf(parser.getAttributeValue(i));
				nodeIdsOfWay.put(nodeIdsOfWay.size(),  l);
			}
			else  {
				System.out.println("should never be called: referenceHandler");
				System.exit(-1);
			}
		}
	}
	
	/**
	 * Save OSM Way
	 */
	public void addWay()
	{
		anzahl_ways++;
		if (this.isBuildingWay == true) {

			anzahl_ways_Building++;
			
		} else {
			
			tempWay.set_meansOfTransport();
			
			if (tempWay.getMeansOfTransportPermission(myOSMWay.CAR)) {
				
				anzahl_ways_Car++;

				if (this.parseXML_status == 0) {
					for (int i = 0; i < nodeIdsOfWay.size(); i++) {
						long l = nodeIdsOfWay.get(i);
						this.neededNodesIds.add(l);
					}
				} else if (this.parseXML_status == 1) {
					
					if (nodeIdsOfWay.size() <= 1) {
						System.out.println("Error: Way has only " + nodeIdsOfWay.size() + " refs");
						System.exit(-1);
					}
					
					tempWay.refs = new myOSMNode[nodeIdsOfWay.size()];

					for (int i = 0; i < nodeIdsOfWay.size(); i++) {
						long l = nodeIdsOfWay.get(i);
						
						myOSMNode n = this.nodes.get(l);
						
						if (n == null) {
							System.out.println("Error: n == null: addWay");
							System.exit(-1);
						} else {
							tempWay.refs[i] = n;
						}
					}
					
					tempWay.setWayParts();
					tempWay.map = this;
					this.ways.put(this.ways.size(), tempWay);
				}
			}
		}
		
		count_ways++;

		tempWay = new myOSMWay(this);
		this.isBuildingWay = false;
		this.nodeIdsOfWay.clear();
		
	}

	/**
	 * Handler for XML tags
	 */
	public void tagHandler() {
		
		for ( int i = 0; i < parser.getAttributeCount(); i++ ) {
			//as all tags are (k)ey / (v)alue pairs (in this order) we remember the last key and halde the
  		  	//assignment when we find a value.
  		  	if (parser.getAttributeLocalName(i).equals("k")) {		  		
  		  		lastkey = parser.getAttributeValue(i);
  		  	}
  		  	else if (parser.getAttributeLocalName(i).equals("v")){
  		  		if (lastkey.equals("created_by") || lastkey.equals("visible")) {
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
  		  		else if (lastkey.equals("building")) {
  		  			if (parser.getAttributeValue(i).equals("yes")) {
  		  				this.isBuildingWay = true;
  		  			}
  		  		}
  		  		else if (lastkey.equals("oneway")) {
  		  			String s = parser.getAttributeValue(i);
  		  			
  		  			if (s.equals("yes") || s.equals("-1")) {
  		  				tempWay.onyWay = true;
  		  			} else {
  		  				tempWay.onyWay = Boolean.valueOf(s);
  		  			}
  		  		}
  		  		else if (lastkey.equals("lanes")){
  		  			try {
  	  		  			tempWay.lanes = Integer.parseInt( parser.getAttributeValue(i).split(";")[0] );  		  				
  		  			} catch (NumberFormatException e) {
  		  				tempWay.lanes = 1;
  		  			}
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
	public void osmHandler(boolean showOsmInfo){
		//read OSM general info
		for ( int i=0; i < parser.getAttributeCount(); i++){
			if (parser.getAttributeLocalName(i).equals("version"))
				osmVersion = parser.getAttributeValue(i);
			else if (parser.getAttributeLocalName(i).equals("generator"))
				osmGenerator = parser.getAttributeValue(i);
		}
		//print these info
		if (showOsmInfo) {
			System.out.println("Parsing "+osmFile.getName()+"...\nOSM-Version: "+osmVersion+"\nGenerator: "+osmGenerator);
		}
		
	}

	/**
	 * handles boundary tag
	 */
	public void boundsHandler(boolean showOsmInfo){
		
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
  	  
  	  	if (showOsmInfo) {
  	  		System.out.println("OSM-file boundary min(Lat/Lon),max(Lat/Lon) : ("+
  			  			  osmMinLat+", "+osmMinLon+"),("+osmMaxLat+", "+osmMaxLon+")");
  	  	}  
	}
	
    public Vector<myOSMWayPart> getStreetLinksVector() {
    	// save street link inside this vector
    	Vector<myOSMWayPart> streetLinksVector = new Vector<myOSMWayPart>();
    	
    	// convert street links array to vector and resize it to real size
		for (int i=0; i < ways.size(); i++) {

			myOSMWay w = ways.get(i);
			
	    	Collections.addAll(streetLinksVector, w.WayParts);
	    	streetLinksVector.setSize(this.getNrOfAllWayParts());
    		
		}
    	
    	// return converted street links as vector
    	return streetLinksVector;
    }
    
    public myOSMWayPart getLink(int i){
        if (i<0) i=0;
        if (i<this.getNrOfAllWayParts()) return getStreetLinksVector().get(i);
        return null;
    }
    
    public myOSMWayPart getLink(long i){
    	return getLink((int) i);
    }
}

