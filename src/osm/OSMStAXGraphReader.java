 /*
 * this class loads routing graph from a file which represents a StreetMap
 */

package osm;

import interfaces.StatusUpdate;

import java.io.*;
import java.util.Vector;
import cartesian.*;

/**
 * @author Tob
 * @author Daniel Sathees Elmo
 */

public class OSMStAXGraphReader {
    /**
     * loads file "path" and converts its graph into StreetMap
     * @param fullFilePath
     * @return StreetMap
     */
    public static StreetMap convertToStreetMap(String fullFilePath, StatusUpdate statusUpdate) throws Exception {
        int i=0,k=0,l=0;
        int large=0;
        
        int NrOfNodes=0;
        int NrOfEdges=0;

        // Nr Of all Nodes and Edges
        //(NrOfNodes + all nodes between links (for large maps))
        //(NrOfEdges + all edges between links (for large maps))
        int NrOfAllNodes=0;
        int NrOfAllEdges=0;
        
        double minLat;
        double maxLat;
        double minLon;
        double maxLon;
        
        // store lon and lat for every node
        double [] lon;
        double [] lat;
        
        long [] node_id;

        double prevLon;
        double prevLat;
        
        long prevNodeId;

        //store nr of edges for every node
        int[] nodeEdgeSizes;

        // extra information about a link
        long[] edge_id;
        int[] osmMaxSpeed;
        int[] carPermission;
        int[] lanes;
        int[] highwayType;
        String[] name;
        int[] length;
        int[][] betweenDistFromStart;
        
        // store target node for every edge
        int[] targetNode;

        int[] startNode;

        //store nr of between nodes for every edge
        int[] betweenSizes;
        

        // store edges for every node in this vector
        Vector edges = new Vector();
        int[] edge;
        
        // store between node's lon and lat in vectors
        Vector betweenNodesLon = new Vector();
        Vector betweenNodesLat = new Vector();
        Vector betweenNodesId = new Vector();
        
        double [] betweenLat;
        double [] betweenLon;
        long [] betweenId;

        //street map
        StreetMap streetMap = null;
        
        //for status update
        float currentProgressPercent;
        int sumElements;
        int currentElement=0;
        
        try {
        	File streetMapFile= new File(fullFilePath);
       
            if (streetMapFile.exists()){
                FileInputStream fis = new FileInputStream(streetMapFile);
                DataInputStream dis = new DataInputStream(fis);

                int as = dis.available();
                
                large = dis.readInt();                
                NrOfNodes = dis.readInt();
                NrOfEdges = dis.readInt();
                minLat = dis.readDouble();
                maxLat = dis.readDouble();
                minLon = dis.readDouble();
                
                maxLon = dis.readDouble();
                
                //sum of elements to read (x2 because once for reading, once for adding)
                //sumElements = (NrOfNodes * 2) + NrOfEdges;
                // for progression of first step: read all nodes
                sumElements = NrOfNodes;

                // store nr of edges for every node in array nodesEdges
                nodeEdgeSizes = new int[NrOfNodes];
                // store lon and lat for every node
                lon = new double[NrOfNodes];
                lat = new double[NrOfNodes];
                
                node_id = new long[NrOfNodes];
                
                // add number of "between nodes" to number of all nodes
                NrOfAllNodes = NrOfNodes;
                // do the same with edges
                NrOfAllEdges = NrOfEdges;

                startNode = new int[NrOfEdges];
                
                // open routing graph
                for (i=0;i<NrOfNodes;i++){
                	
                    //read nodes
                	node_id[i] = dis.readLong();;
                	lon[i] = dis.readDouble();
                    lat[i] = dis.readDouble();
                    nodeEdgeSizes[i] = dis.readInt();

                    long ll = node_id[i];
                    if (38417819 == ll || 1429164909 == ll || 1429164908 == ll || 60601938 == ll) {
                    	node_id[i]++;
                    	node_id[i]--;
                    }
                    
                    //use array to store each edge in vector edges
                    edge = new int[nodeEdgeSizes[i]];
                    
                    for (k=0;k<nodeEdgeSizes[i];k++){
                    	int a = dis.readInt();
                        edge[k] = a;
                        startNode[edge[k]] = i;
                    }
                    edges.add(edge);
                    
                    //calculate current progress and update status via interface
                    currentElement++;
                    currentProgressPercent = ((float) currentElement / sumElements * 100);
                    statusUpdate.updateStatus("Node Nr." + i + " read", currentProgressPercent);
                }
                
                // for progression of second step: read all edges, reset counter
                sumElements = NrOfEdges;
                currentElement = 0;

                // store number of "between links" one edge has
                betweenSizes = new int[NrOfEdges];
                // save every edge's target node
                targetNode = new int[NrOfEdges];

                                
                edge_id = new long[NrOfEdges];
                osmMaxSpeed = new int[NrOfEdges];
                carPermission = new int[NrOfEdges];
                lanes = new int[NrOfEdges];
                highwayType = new int[NrOfEdges];
                name = new String[NrOfEdges];
                length = new int[NrOfEdges];
                
                betweenDistFromStart = new int[NrOfEdges][1];
                
                //read edges
                for (i=0;i<NrOfEdges;i++){
                	edge_id[i] = dis.readLong();
                	
                	if (edge_id[i] == 8090147) {
                		edge_id[i]++;
                		edge_id[i]--;
                	}
                	
                    osmMaxSpeed[i] = dis.readInt();
                    carPermission[i] = dis.readInt();
                    lanes[i] = dis.readInt();
                    highwayType[i] = dis.readInt();
                    name[i] = dis.readUTF();
                    targetNode[i] = dis.readInt();
                    length[i] = dis.readInt();

                    if (large == 1){// if graph is large store "between links"

                        betweenSizes[i] = dis.readInt();
                        betweenId = new long[betweenSizes[i]];
                        betweenLon = new double[betweenSizes[i]];
                        betweenLat = new double[betweenSizes[i]];

                        betweenDistFromStart[i] = new int[betweenSizes[i]];
                                               
                        for (k=0;k<betweenSizes[i];k++){
                        	long idd = dis.readLong();
                        	
                        	betweenId[k] = idd;
                        	
                            betweenLon[k] = dis.readDouble();
                            betweenLat[k] = dis.readDouble();
                            betweenDistFromStart[i][k] = dis.readInt();
                            NrOfAllNodes++;
                            NrOfAllEdges++;
                        }
                        // increase nr of all edges by one
                        // (n between nodes means n+1 links)
                        NrOfAllEdges++;
                        betweenNodesId.add(betweenId);
                        betweenNodesLon.add(betweenLon);
                        betweenNodesLat.add(betweenLat);
                    }
                    
                    //calculate current progress and update status via interface
                    currentElement++;
                    currentProgressPercent = ((float) currentElement / sumElements * 100);
                    statusUpdate.updateStatus("Edge Nr." + i + " read", currentProgressPercent);
                }
                
                for (i=0;i<NrOfEdges;i++){
//                	System.out.println("start: " + startNode[i] + " target: " + targetNode[i]);
                }
                
                /*
                System.out.println(" Nr of Nodes: " +NrOfNodes);
                System.out.println(" Nr of All nodes: " +NrOfAllNodes);
                System.out.println(" Nr of Edges: " +NrOfEdges);
                System.out.println(" Nr of All Edges: " +NrOfAllEdges);
                */
                
                // for progression of thirs and last step: read all edges, reset counter
                sumElements = NrOfNodes;
                currentElement = 0;

                // create new StreetMap to store edges and nodes into it
                streetMap = new StreetMap(NrOfAllEdges,NrOfAllNodes, streetMapFile);
                
                //set boundary
                int ii = Coordinates.getCartesianX(minLon, minLat);
                streetMap.setMinX(ii);
                streetMap.setMinY(Coordinates.getCartesianY(minLon, minLat));
                streetMap.setMaxX(Coordinates.getCartesianX(maxLon, maxLat));
                streetMap.setMaxY(Coordinates.getCartesianY(maxLon, maxLat));

                // add StreetLinks to StreetMap
                for (k=0; k < NrOfNodes;k++){// search in nodes for edges

                    //store edges which belong to current node in array edge
                    edge = (int[])edges.elementAt(k);

                    for (i=0; i < nodeEdgeSizes[k];i++){// for each edge

                        //add Edge to StreetMap
                        if (large == 1){
                            // in this case take care of "between links"
                            prevLon=lon[k];
                            prevLat=lat[k];
                            
                            prevNodeId = node_id[k];
                            
                            betweenLon = (double[])betweenNodesLon.elementAt(edge[i]);
                            betweenLat = (double[])betweenNodesLat.elementAt(edge[i]);
                            betweenId = (long[])betweenNodesId.elementAt(edge[i]);
                            
                            for (l=0; l < betweenSizes[edge[i]];l++){
                                // add edge to StreetMap
                                streetMap.addLink(
                                        Coordinates.getCartesianX(prevLon, prevLat)
                                       ,Coordinates.getCartesianY(prevLon, prevLat)
                                       ,Coordinates.getCartesianX(betweenLon[l]
                                                                 ,betweenLat[l])
                                       ,Coordinates.getCartesianY(betweenLon[l]
                                                                 ,betweenLat[l])
                                       ,edge_id[edge[i]], prevNodeId, betweenId[l]
                                );
                                
//                                System.out.print(prevLon + "," + prevLat + " --> " + betweenLon[l] + "," + betweenLat[l]);
//                                System.out.println( " ||| " + Coordinates.getCartesianX(prevLon, prevLat) + "," + Coordinates.getCartesianY(prevLon, prevLat) + " --> " + Coordinates.getCartesianX(betweenLon[l],betweenLat[l]) + "," + Coordinates.getCartesianY(betweenLon[l],betweenLat[l]));
                                
                                
                                prevLon = betweenLon[l];
                                prevLat = betweenLat[l];
                                prevNodeId = betweenId[l];

                            }
                            
                            long l1 = edge_id[edge[i]];
                            long l2 = node_id[targetNode[edge[i]]];
                            
                            streetMap.addLink(
                                    Coordinates.getCartesianX(prevLon, prevLat)
                                   ,Coordinates.getCartesianY(prevLon, prevLat)
                                   ,Coordinates.getCartesianX(lon[targetNode[edge[i]]] 
                                                             ,lat[targetNode[edge[i]]])
                                   ,Coordinates.getCartesianY(lon[targetNode[edge[i]]]
                                                             ,lat[targetNode[edge[i]]])
                                   ,l1, prevNodeId, l2
                            );
                            
//                            System.out.print(prevLon + "," + prevLat + " --> " + lon[targetNode[edge[i]]] + "," + lat[targetNode[edge[i]]]);
//                            System.out.println( " ||| " + Coordinates.getCartesianX(prevLon, prevLat) + "," + Coordinates.getCartesianY(prevLon, prevLat) + " --> " + Coordinates.getCartesianX(lon[targetNode[edge[i]]] ,lat[targetNode[edge[i]]]) + "," + Coordinates.getCartesianY(lon[targetNode[edge[i]]],lat[targetNode[edge[i]]]));



                        }else{
                            // add edge to StreetMap
                            // edge starts with current node ends with target node
                            streetMap.addLink(
                                    Coordinates.getCartesianX(lon[k], lat[k])
                                   ,Coordinates.getCartesianY(lon[k], lat[k])
                                   ,Coordinates.getCartesianX(lon[targetNode[edge[i]]]
                                                             ,lat[targetNode[edge[i]]])
                                   ,Coordinates.getCartesianY(lon[targetNode[edge[i]]]
                                                             ,lat[targetNode[edge[i]]])
                                   ,edge_id[edge[i]], node_id[k], node_id[targetNode[edge[i]]]
                            );
                            
//                            System.out.print(lon[k] + "," + lat[k] + " --> " + lon[targetNode[edge[i]]] + "," + lat[targetNode[edge[i]]]);
//                            System.out.println( " ||| " + Coordinates.getCartesianX(lon[k], lat[k]) + "," + Coordinates.getCartesianY(lon[k], lat[k]) + " --> " + Coordinates.getCartesianX(lon[targetNode[edge[i]]],lat[targetNode[edge[i]]]) + "," + Coordinates.getCartesianY(lon[targetNode[edge[i]]],lat[targetNode[edge[i]]]));

                        }

                    }
                    
                    // calculate current progress and update status via interface
                    currentElement++;
                    currentProgressPercent = ((float) currentElement / sumElements * 100);
                    statusUpdate.updateStatus("Node Nr." + k + " adding to street map", currentProgressPercent);
                }
                
                /*
                // print all nodes
                for (k=0; k < NrOfNodes;k++){

                    System.out.println(k+" pos: " +lon[k]+" , "+lat[k]);
                    System.out.println(k+" Nr of Edges: " +nodesEdges[k]);

                    edge = (int[])edges.elementAt(k);

                    for (i=0; i < nodesEdges[k];i++){
                        System.out.println(k+" has Edge: " +edge[i]);

                    }
                }
                // print edges
                for (k=0; k < NrOfEdges;k++){
                    System.out.println(k+" Target: " +targetNode[k]);
                    
                    if (large == 1){
                        System.out.println(k+" Nr of Betweens: " +between[k]);

                        betweenLon = (double[])betweenNodesLon.elementAt(k);
                        betweenLat = (double[])betweenNodesLat.elementAt(k);
                        for (i=0; i < between[k];i++){
                            System.out.println(k+" between pos: " +betweenLon[i]+" , "+betweenLat[i]);

                        }
                    }
                }
                 */
                
                // close resources
                dis.close();
                fis.close();
                
                //progress finished
                statusUpdate.finished("Routing Graph with " + NrOfAllNodes + " nodes and overall " + NrOfAllEdges +
                					  " read");
            }
        }
        catch(FileNotFoundException e){
			System.out.println("GPS-trace file not found!");
			throw e;
		}
        catch (IOException e)
        {
            System.out.println("I/O error occured while reading graph");
            // discard data if error occured
            streetMap = null;
            throw e;
        }
     
        return streetMap;
    }

    /**
     * load file "path" and print its routing graph
     * @param path
     */
    public void printOSMStAXGraph(String path) {
        int i=0,k=0;
        int large=0;
        int NrOfNodes;
        int NrOfEdges;
        double minLat;
        double maxLat;
        double minLon;
        double maxLon;

        double longitude;
        double latitude;
        int NodeEdges;
        int EdgeValue;
        
        int osmMaxSpeed; 
        int carPermission;
        int lanes;
        int highwayType;
        String name;
        int targetNode;
        int length;
        int between;
        int betweenDistFromStart;
        double betweenLat;
        double betweenLon;

        File f= new File(path);
        try {
            if (f.exists()){
                FileInputStream fis = new FileInputStream(f);
                DataInputStream dis = new DataInputStream(fis);

                large = dis.readInt();
                NrOfNodes = dis.readInt();
                NrOfEdges = dis.readInt();
                minLat = dis.readDouble();
                maxLat = dis.readDouble();
                minLon = dis.readDouble();
                maxLon = dis.readDouble();

                System.out.println("large: "+large);
                System.out.println("NrOfNodes: "+NrOfNodes);
                System.out.println("NrOfEdges: "+NrOfEdges);
                System.out.println("minLat: "+minLat);
                System.out.println("maxLat: "+maxLat);
                System.out.println("minLon: "+minLon);
                System.out.println("maxLon: "+maxLon);

                // open routing graph
                for (i=0;i<NrOfNodes;i++){
                    //Read Nodes
                    longitude = dis.readDouble();
                    latitude = dis.readDouble();
                    NodeEdges = dis.readInt();
                    System.out.println("Node "+i+" longitude: "+longitude);
                    System.out.println("Node "+i+" latitude: "+latitude);
                    System.out.println("Node "+i+" NodeEdges: "+NodeEdges);
                    for (k=0;k<NodeEdges;k++){
                        EdgeValue = dis.readInt();
                        System.out.println("EdgeValue: "+EdgeValue);
                    }

                }
                for (i=0;i<NrOfEdges;i++){
                    osmMaxSpeed = dis.readInt();
                    carPermission = dis.readInt();
                    lanes = dis.readInt();
                    highwayType = dis.readInt();
                    name = dis.readUTF();
                    targetNode = dis.readInt();
                    length = dis.readInt();

                    System.out.println("Edge "+i+" osmMaxSpeed: "+osmMaxSpeed);
                    System.out.println("Edge "+i+" carPermission: "+carPermission);
                    System.out.println("Edge "+i+" lanes: "+lanes);
                    System.out.println("Edge "+i+" highwayType: "+highwayType);
                    System.out.println("Edge "+i+" name: "+name);
                    System.out.println("Edge "+i+" targetNode: "+targetNode);
                    System.out.println("Edge "+i+" length: "+length);
                    if (large == 1){

                        between = dis.readInt();
                        System.out.println("Edge "+i+" Edges between: "+between);

                        for (k=0;k<between;k++){
                            betweenLon = dis.readDouble();
                            betweenLat = dis.readDouble();
                            betweenDistFromStart = dis.readInt();
                            //System.out.println("Pos: "+betweenLon+" , "+betweenLat);
                            System.out.println("betweenLon: "+betweenLon);
                            System.out.println("betweenLat: "+betweenLat);
                            System.out.println("betweenDistFromStart: "+betweenDistFromStart);
                        }
                    }
                }
                dis.close();
                fis.close();
            }else{
                System.out.println("file "+path+" does not exist!");
            }
        }
        catch (IOException e)
        {
            System.out.println("Error occured while reading graph");
        }
    }
}
