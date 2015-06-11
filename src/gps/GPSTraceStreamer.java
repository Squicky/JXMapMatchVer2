/*
 * Load GPS Tracks
 */

package gps;

import interfaces.StatusUpdate;

import java.io.*;
import java.sql.Time;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.jdesktop.swingx.mapviewer.GeoPosition;

import algorithm.GPSToLinkMatcher;
import algorithm.MatchedGPSNode;
import algorithm.ReorderedMatchedGPSNode;
import cartesian.Coordinates;

import java.util.*;

/**
 * @author Daniel Sathees Elmo
 * 
 * this class imports GPS traces from text based or GPX-XML files
 * and exports matched GPS traces/Points to text based files
 */

//TODO: improve moment where progress calculation is shown (for GPX)
//TODO: bugfix normalized timestamp

public class GPSTraceStreamer {
	
	// pattern for GPS point: timestamp, latitude, longitude -> digit(s),digit(s).digit(s),digit(s).digit(s)
	static private final Pattern gpsPattern = Pattern.compile("-?\\d+(,-?\\d+.\\d+){2}"); 
	static private final Pattern gpsSplitPattern = Pattern.compile(",");
	
	// pattern for date strings in GPX files (e.g. "2012-10-02T16:17:16Z"), we have to split at '-', 'T' and 'Z' Position
	static private final Pattern gpxDateSplitPattern = Pattern.compile("[-TZ]");
	static private final int GPX_STRING_DATE_PARTS = 4; // we must have 4 parts after splitting: 1.Year 2.Month 3.Day 4.Time(HH:MM:ss)
	
	// create date formatter for parsing date string
	static private DateFormat dateFormatter = DateFormat.getDateTimeInstance();
	
	// for GPX parsing (XML)
	static private XMLInputFactory xmlInputfactory = XMLInputFactory.newInstance();
	
	// for longitude/latitude double formating, set '.' as separator
	static private DecimalFormatSymbols dfS = DecimalFormatSymbols.getInstance();
	static { dfS.setDecimalSeparator('.');}
	// create formations for latitude (+-90) and longitude (+-180)
	static private DecimalFormat latFormat = new DecimalFormat("##.000000000", dfS);
	static private DecimalFormat lonFormat = new DecimalFormat("###.000000000", dfS);
	
	 /**
     * converts Text or GPX formated files including a trace to a GPSTrace 
     * @param filePath
     * @param statusUpdate
     * @return
     * @throws Exception
     */
    public static GPSTrace convertToGPSPath(String filePath, StatusUpdate statusUpdate) throws Exception {
    	GPSTrace gpsTrace;     					// store parsed GPS trace from file
    	File gpsTraceFile = new File(filePath);	// connect to given file
    	
    	// TEXT file
    	if (filePath.toLowerCase().endsWith(".txt")) {
    		gpsTrace =  convertToGPSPathFromTextFile(filePath, statusUpdate); 
    	}
    	// GPX XML file
    	else if (filePath.toLowerCase().endsWith(".gpx")) {
    		gpsTrace = convertToGPSPathFromGPXFile(filePath, statusUpdate);
    	}
		// otherwise throw exception
    	else {
    		throw new Exception("Not valid GPS file extension!");
    	}    	
    	
	  	// update status, work finished!
	  	statusUpdate.finished("GPS trace file \"" + gpsTraceFile.getName() + "\" with " + 
	  						   gpsTrace.getNrOfNodes() + " GPS points loaded! Boundary min(lon/lat) max (lon/lat): (" +
	  						   lonFormat.format(gpsTrace.getMinLon()) + ", " + latFormat.format(gpsTrace.getMinLat()) + ") (" +
	  						   lonFormat.format(gpsTrace.getMaxLon()) + ", " + latFormat.format(gpsTrace.getMaxLat()) + ")");
	  	/*
	  	System.out.println("GPS trace file \"" + gpsTraceFile.getName() + "\" with " + 
	  						   gpsTrace.getNrOfNodes() + " GPS points loaded! Boundary min(lon/lat) max (lon/lat): (" +
	  						   lonFormat.format(gpsTrace.getMinLon()) + ", " + latFormat.format(gpsTrace.getMinLat()) + ") (" +
	  						   lonFormat.format(gpsTrace.getMaxLon()) + ", " + latFormat.format(gpsTrace.getMaxLat()) + ")");
	  	*/
	  	
    	// return parsed GPS trace
    	return gpsTrace;
    }

    /**
     * parses text file and converts it to a GPS Path
     * an GPS Path
     * @param filePath
     * @return GPSPath
	 * @exception FileNotFoundException if GPS trace file can't be found
	 * @exception IOException if reading file occurs an error
	 * @exception NumberFormatException  if a number can't be read
	 */
    public static GPSTrace convertToGPSPathFromTextFile(String filePath, StatusUpdate statusUpdate) throws Exception {
    	try{
    		// variables
    		GPSTrace gpsTrace;
			int nrOfGPSPoints=0;
			long refTimeStamp=0;
			
			// access file and save name
			File gpsTraceFile = new File(filePath);
					
			// read file via buffered Reader due to better performance
			FileReader fReader = new FileReader(gpsTraceFile);
			BufferedReader bReader = new BufferedReader(fReader);

			// read first line
			String line = bReader.readLine();
			
			// line must be "#n" with n = Number Of GPS Points in file
			if(line.matches("#\\d+"))
				nrOfGPSPoints = Integer.parseInt(line.substring(1));
			else{
				System.out.println("Numbers of GPS Point information couldn't be read");
				bReader.close();
				throw new Exception("Numbers of GPS Point information couldn't be read");
			}
			
			// read second line
			line = bReader.readLine();
			
			// line must contain reference time stamp, ignore case sensitivity
			if(line.matches("(?i)#all Tstamps substracted by \\d+"))
				refTimeStamp = Long.parseLong(line.substring(28));
			else			
				System.out.println("Numbers of GPS Point information couldn't be read");
			
			// read third line, ignore though it contains information about GPS information syntax
			bReader.readLine();
			
			// initialize GPS path
			gpsTrace = new GPSTrace(nrOfGPSPoints, refTimeStamp);
			
			// store read data
			long timeStamp = 0;
			long prevTime = -Long.MAX_VALUE;
			double latitude = 0.0;
			double longitude = 0.0;
			
			// store read data from file
			String[] gpsData;
			
			// current read line
			int currentLineNr = 0;
			float currentProgress = 0;
			
			int count = 0;
			
			while((line = bReader.readLine()) != null){
				count++;
				
				// read line must confirm to pattern
				if (gpsPattern.matcher(line).matches() || line.startsWith("2014-")){
					gpsData = gpsSplitPattern.split(line);
					
					// read time, read latitude/longitude
					if (line.startsWith("2014-")) {
						
						Calendar c = Calendar.getInstance();
						
						String [] sdatetime = line.split(" ");
						String [] sdate = sdatetime[0].split("-");						
						String [] stime = sdatetime[1].split(".0000000,");
						stime = stime[0].split(":");
						
						c.set(Integer.parseInt(sdate[0]), 
								Integer.parseInt(sdate[1]), 
								Integer.parseInt(sdate[2]), 
								Integer.parseInt(stime[0]), 
								Integer.parseInt(stime[1]),
								Integer.parseInt(stime[2]));
						
						timeStamp = c.getTimeInMillis();
						timeStamp = timeStamp * 1000000L;
						
					} 
					else {
						timeStamp = Long.parseLong(gpsData[0]);						
					}
					
					latitude = Double.parseDouble(gpsData[1]);
					longitude = Double.parseDouble(gpsData[2]);
					
				    // check if its time is greater then previous GPS point's time
					if (timeStamp > prevTime){
						// add node to GPS Path
						if (count % 1 == 0) {
							gpsTrace.addNode(Coordinates.getCartesianX(longitude, latitude),Coordinates.getCartesianY(longitude, latitude), timeStamp);							
							prevTime = timeStamp;
						}
					}
				}
				// ignore comments
				else if (line.startsWith("#"))
					continue;
				else
					System.out.println(line+" doesn't match gps information pattern!");
				
				// update status
				currentLineNr++;
				currentProgress = ((float) currentLineNr / nrOfGPSPoints * 100);
				statusUpdate.updateStatus("reading line Nr." + currentLineNr + "...", currentProgress);
			}
			
			
			nrOfGPSPoints = count;
			
			// close reader
			bReader.close();
			fReader.close();

	    	// return created GPS path
	        return gpsTrace;
		}
		catch(FileNotFoundException e){
			System.out.println("GPS-trace file not found!");
			throw e;
		}
		catch(IOException e){
			System.out.println("Error while reading GPS-trace file!");
			throw e;
		}
		catch(NumberFormatException e){
			System.out.println("Error reading number!");
			throw e;
		}
    }
    
    /**
     * parses GPX XML file and converts it to a GPS Path
     * @param filePath
     * @param statusUpdate
     * @return
     * @throws Exception
     */
    public static GPSTrace convertToGPSPathFromGPXFile(String filePath, StatusUpdate statusUpdate) throws Exception {
    	boolean isInsideMetadata = false;	// flag to check if are we inside a meta data block
    	long refTimeStamp = 0;				// save reference time stamp of GPS trace
    	int nrOfGPSPoints = 0;				// sum of all GPS Points
    	
    	// try initialize stream reader with XML file
    	XMLStreamReader parser = createXMLStreamReader(filePath);
   
    	// update status to an undefined status, cause we don't know at this time
    	// how many track points we have to read
    	statusUpdate.updateUndefinedStatus("parsing...");
    	
    	// get time stamp and bounds
    loop_count:
    	while (parser.hasNext()) {
    		switch (parser.getEventType()) {
    			case XMLStreamConstants.START_ELEMENT:
    				
    				// notice that we entered metadata info
    				if (parser.getLocalName().equals("metadata")) {
    					//update status
    					statusUpdate.updateStatus("reading metadata...");
    					// notice we're inside meta data block
    					isInsideMetadata = true;
    				}
    				// read reference time stamp inside metadata
    				else if (parser.getLocalName().equals("time") && isInsideMetadata) {
    					//update status
    					statusUpdate.updateStatus("reading reference timestamp...");
    					// get reference time stamp
    					refTimeStamp = readGPXTimeStamp(parser);
    				}
    				// count GPS Points
    				else if (parser.getLocalName().equals("trkpt")) {
    					//update status
    					statusUpdate.updateStatus("counting trackpoints..." + nrOfGPSPoints);
    					// increase nr of read GPS points
    					nrOfGPSPoints++;
    				}
    				break;
    				
    			// leave while loop if metadata info ends
    			case XMLStreamConstants.END_ELEMENT:
    				if (parser.getLocalName().equals("trk"))
    					break loop_count;
    		}
    		// get next event
    		parser.next();
    	}					
		
		// read XML Stream from Beginning, but this read each GPS Point and add to GPSPath
    	parser.close();
		parser = createXMLStreamReader(filePath);
		
		// create new GPS path
		GPSTrace gpsTrace = new GPSTrace(nrOfGPSPoints, refTimeStamp);

		// read each track point
		double lat = 0;
		double lon = 0;
		long timeStamp = 0;
		
		// flags for parsing
		boolean isInsideTrackPointBlock = false;
		

		// current read line
		int currentTrackPoint = 0;
		float currentProgress = 0;
		
		// go through file again
	loop_reader:
		while (parser.hasNext()) {
			
			switch (parser.getEventType()) {
			
				case XMLStreamConstants.START_ELEMENT:
					
					// track point tag reached, set flag
					if (parser.getLocalName().equals("trkpt")) {
						isInsideTrackPointBlock = true;
						// read latitude and longitude
						for (int i=0; i < parser.getAttributeCount(); i++) {
							if (parser.getAttributeLocalName(i).equals("lat"))
								lat = Double.parseDouble(parser.getAttributeValue(i));
							else if (parser.getAttributeLocalName(i).equals("lon"))
								lon = Double.parseDouble(parser.getAttributeValue(i));
						}
					}
						
					// read time stamp inside, add GPS Point data to GPS trace, reset track point flag 
					else if (parser.getLocalName().equals("time") && isInsideTrackPointBlock) {
						timeStamp = readGPXTimeStamp(parser);
						gpsTrace.addNode(Coordinates.getCartesianX(lon, lat), Coordinates.getCartesianY(lon, lat), timeStamp);
						isInsideTrackPointBlock = false;
						
						// calculate progress
						currentTrackPoint++;
						currentProgress = ((float) currentTrackPoint / nrOfGPSPoints * 100);
						statusUpdate.updateStatus("reading track point " + currentTrackPoint + "/" + nrOfGPSPoints, currentProgress);
					}
					break;

				// leave while loop if first track ends
				case XMLStreamConstants.END_ELEMENT:
					if (parser.getLocalName().equals("trk")) {
						break loop_reader;
					}
			}
			
			// get next event
			parser.next();
		}
		
		// GPS trace with parsed position/time values
    	return gpsTrace;
    }
    
    private static XMLStreamReader createXMLStreamReader(String filePath) throws Exception {
    	// try initialize stream reader with XML file
    	InputStream inputStream = new FileInputStream(filePath);
    	XMLStreamReader parser;
    	try {
			parser = xmlInputfactory.createXMLStreamReader(inputStream);
		} catch (XMLStreamException e) {
			System.err.println("XML parser couldn't be created (file: " + filePath + ")");
			throw e;
		}
    	// give back instance of StAX stream reader
    	return parser;
    }
    
    /**
     * if time tag is reached, this method will extract timestamp value in milliseconds
     * @param parser
     * @return
     * @throws Exception
     */
    private static long readGPXTimeStamp(XMLStreamReader parser) throws Exception {
    	// get next tag, ignore white spaces an comments
    	while (parser.hasNext()) {
    		// next content must be characters
    		if (parser.getEventType() == XMLStreamConstants.CHARACTERS)
    			return dateInGPXToMilli(parser.getText());
    		else if ((parser.getEventType() == XMLStreamConstants.END_ELEMENT) && (parser.getLocalName().equals("time")))
    			break;
    		// get next element
    		parser.next();
    	}
    	// throw error exception
    	throw new Exception("No time character stream available inside time tag");
    }
    
    /**
     * convert date string out of GPX files to milliseconds since 1.January.1970
     * @param gpxDateString 
     * @return
     * @throws Exception
     */
    private static long dateInGPXToMilli(String gpxDateString) throws Exception {
    	// build java date class compatible string for parsing
    	String dateString;
    	// apply split pattern
		String dateStringParts[] = gpxDateSplitPattern.split(gpxDateString);
		
		// check correct amount of split parts
		if (dateStringParts.length == GPX_STRING_DATE_PARTS) {
			// rebuild compatible date string for parsing
			dateString = dateStringParts[2] + "." + dateStringParts[1] + "." + dateStringParts[0] + " " + dateStringParts[3]; 
		}
		// otherwise throw exception cause we've got a wrong formated GPX date string
		else throw new Exception("GPX date string doesn't match to format YYYY-MM-DDTHH:MM:ssZ");
		
		// parse date string
		Date date = dateFormatter.parse(dateString);
		
		// return date in milliseconds since 1.January.1970
		return date.getTime();
    }
    
    /**
     * saves all matched GPS points to text file. time stamps can be normalized if desired
     * @param gpsTrace
     * @param normalizeTimeStamp
     * @param filePath
     * @param statusUpdate
     * @return was writing progress successful?
     */
    public static boolean saveMatchedGPSTraceToFile(Vector<MatchedGPSNode> gpsNodesToMatch, long refTimeStamp, boolean normalizeTimeStamp, String filePath, StatusUpdate statusUpdate){
    
		// access file and save name
		File gpsTracefile = new File(filePath);
		
		// create offSet if user wishes to normalize exported time stamp
		long timeStampOffSet = (normalizeTimeStamp) ? refTimeStamp : 0;
		
		int nrOfMatchedNodes=0;
    	// count numbers of matched gps nodes
    	for(MatchedGPSNode matchedGPSNode : gpsNodesToMatch){
    		if (matchedGPSNode.isMatched())
    			++nrOfMatchedNodes;
    	}
    
    	try {
    		// wrap with buffered writer 
    		BufferedWriter bWriter = new BufferedWriter(new FileWriter(gpsTracefile));
    		
    		// write numbers of (matched) GPS nodes
    		bWriter.write("#" + nrOfMatchedNodes);
    		bWriter.newLine();
    		
    		// write structural info in the form of comments
    		bWriter.write("#all Tstamps substracted by " + (refTimeStamp - timeStampOffSet)); //+ gpsTrace.getTimestamp());
    		bWriter.newLine();
    		bWriter.write("#NormTstamp [ns], matched latitude, matched longitude, unmatched latitude, unmatched longitude");
    		bWriter.newLine();
    		
    		// for calculating current progress
			float currentProgress = 0;
    		int nodeCounter = 0;
    				
			// write matched GPS Points to file
    		for(MatchedGPSNode matchedGPSNode : gpsNodesToMatch){
    			// get just matched GPS Points
    			if (matchedGPSNode.isMatched()){
    				// convert to geographic position
    				GeoPosition matchedGeoPos = Coordinates.getGeoPos(matchedGPSNode.getMatchedX(), matchedGPSNode.getMatchedY());
    				GeoPosition unmatchedGeoPos = Coordinates.getGeoPos(matchedGPSNode.getX(), matchedGPSNode.getY());
    				// write line to file
    				bWriter.write((matchedGPSNode.getTimestamp() + timeStampOffSet) + ",");
    				bWriter.write(latFormat.format(matchedGeoPos.getLatitude()) + "," + lonFormat.format(matchedGeoPos.getLongitude()) + ",");
    				bWriter.write(latFormat.format(unmatchedGeoPos.getLatitude()) + "," + lonFormat.format(unmatchedGeoPos.getLongitude()));
    				bWriter.newLine();
    				
    				// increase counter
    				nodeCounter++;
    				// update current status of exporting progress
    				statusUpdate.updateStatus("Writing matched GPS node nr." + nodeCounter);
    			}
    			
    			// calculate progress and update
    			currentProgress = ((float) nodeCounter / nrOfMatchedNodes * 100);
    			statusUpdate.updateStatus(currentProgress);
    		}
    		
    		// close writer
    		bWriter.close();
    		
    		// finished
    		statusUpdate.finished(nrOfMatchedNodes + " matched GPS nodes saved to " + gpsTracefile.getName());
     	} 
    	// handle I/O error during writing operation 
    	catch (IOException e) {
     		System.out.println("Error during exporting matched GPS points!");
     		return false;
     	}
    	
    	// successful!
    	return true;
    }
    
    /**
     * saves all matched GPS points to text file. time stamps can be normalized if desired
     * @param gpsTrace
     * @param normalizeTimeStamp
     * @param filePath
     * @param statusUpdate
     * @return was writing progress successful?
     */
    public static boolean saveNMatchedGPSTraceToFile(Vector<ReorderedMatchedGPSNode> matchedGPSNodes, GPSTrace gpsTrace, boolean normalizeTimeStamp, String filePath, StatusUpdate statusUpdate){
    
		// access file and save name
		File gpsTracefile = new File(filePath);
    	
		// get starting time of  measurement
		long refTimeStamp = gpsTrace.getRefTimeStamp();
		
		// create offSet if user wishes to normalize exported time stamp
		long timeStampOffSet = (normalizeTimeStamp) ? refTimeStamp : 0;
		
		// get count of matched GPS nodes
		int nrOfMatchedNodes=matchedGPSNodes.size();
    
    	try {
    		// wrap with buffered writer 
    		BufferedWriter bWriter = new BufferedWriter(new FileWriter(gpsTracefile));
    		
    		// write numbers of (matched) GPS nodes
    		bWriter.write("#" + nrOfMatchedNodes);
    		bWriter.newLine();
    		
    		// write structural info in the form of comments
    		bWriter.write("#all Tstamps substracted by " + (refTimeStamp - timeStampOffSet)); //+ gpsTrace.getTimestamp());
    		bWriter.newLine();
    		bWriter.write("#NormTstamp [ns], matched latitude, matched longitude, unmatched latitude, unmatched longitude");
    		bWriter.newLine();
    		
    		// for calculating current progress
			float currentProgress = 0;
    		
			// write matched GPS Points to file
    		for (int i=0; i<matchedGPSNodes.size(); i++){
    			// get matched GPS Node
    			MatchedGPSNode matchedGPSNode = matchedGPSNodes.get(i);
    			
    			// get just matched GPS Points
    			if (matchedGPSNode.isMatched()){
    				// convert to geographic position
    				GeoPosition matchedGeoPos = Coordinates.getGeoPos(matchedGPSNode.getMatchedX(), matchedGPSNode.getMatchedY());
    				GeoPosition unmatchedGeoPos = Coordinates.getGeoPos(matchedGPSNode.getX(), matchedGPSNode.getY());
    				// write line to file
    				bWriter.write((gpsTrace.getNodeTimestamp(i) + timeStampOffSet) + ",");
    				bWriter.write(latFormat.format(matchedGeoPos.getLatitude()) + "," + lonFormat.format(matchedGeoPos.getLongitude()) + ",");
    				bWriter.write(latFormat.format(unmatchedGeoPos.getLatitude()) + "," + lonFormat.format(unmatchedGeoPos.getLongitude()));
    				bWriter.newLine();
    				
    				// update current status of exporting progress
    				statusUpdate.updateStatus("Writing matched GPS node nr." + i);
    			}
    			
    			// calculate progress and update
    			currentProgress = ((float) i / gpsTrace.getNrOfNodes() * 100);
    			statusUpdate.updateStatus(currentProgress);
    		}
    		
    		// close writer
    		bWriter.close();
    		
    		// finished
    		statusUpdate.finished(nrOfMatchedNodes + " matched GPS nodes saved to " + gpsTracefile.getName());
     	} 
    	// handle I/O error during writing operation 
    	catch (IOException e) {
     		System.out.println("Error during exporting matched GPS points!");
     		return false;
     	}
    	
    	// successful!
    	return true;
    }
}
