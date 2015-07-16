package myOSM;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class myDataset {

	public long timestamp = -1;
	public int datarate = -1;
	public double delay = -1;
	public double loss_rate = -1;
	
	
	public static ArrayList<myDataset> loadDatasetsUp(String FilePath) {
		
		ArrayList<myDataset> list = new ArrayList<myDataset>();
		
		String line = "";
		try {
			BufferedReader bReader = new BufferedReader( new InputStreamReader( new FileInputStream( new File( FilePath ) ), "UTF-8" ));
			
			line = bReader.readLine();
			
			int columnNrDataRate = -1;
			int columnNrDelay = -1;
			int columnNrTimestamp = -1;
			int columnNrLossRate = -1;
			
			if (line != null) {
				String [] lines = line.split(",");
				
				for (int i = 0; i < lines.length; i++) {
					
					if (lines[i].equals("data rate [Byte/s]")) {
						
						columnNrDataRate = i;
					
					} else if (lines[i].equals("delay [s]")) {
					
						columnNrDelay = i;
					
					} else if (lines[i].equals("first ttx [ns]")) {
					
						columnNrTimestamp = i;
					
					} else if (lines[i].equals("loss rate")) {
					
						columnNrLossRate = i;
					
					} 
					
				}
				
				list = loadDatasetsUp(bReader, columnNrDataRate, columnNrDelay, columnNrTimestamp, columnNrLossRate);
				
			}
			
			bReader.close();
			
			return list;
			
		} catch (Exception e) {			
			System.out.println("Error: loadGetEdges: \n" + line + "\n" + e.toString());
		}		
		
		return list;
		
	}
	
	public static ArrayList<myDataset> loadDatasetsDown(String FilePath) {
		
		ArrayList<myDataset> list = new ArrayList<myDataset>();
		
		String line = "";
		try {
			BufferedReader bReader = new BufferedReader( new InputStreamReader( new FileInputStream( new File( FilePath ) ), "UTF-8" ));
			
			line = bReader.readLine();
			
			int columnNrDataRate = -1;
			int columnNrDelay = -1;
			int columnNrTimestamp = -1;
			int columnNrLossRate = -1;
			
			if (line != null) {
				String [] lines = line.split(",");
				
				for (int i = 0; i < lines.length; i++) {
					
					if (lines[i].equals("data rate [Byte/s]")) {
						
						columnNrDataRate = i;
					
					} else if (lines[i].equals("delay [s]")) {
					
						columnNrDelay = i;
					
					} else if (lines[i].equals("first trx [ns]")) {
					
						columnNrTimestamp = i;
					
					} else if (lines[i].equals("loss rate")) {
					
						columnNrLossRate = i;
					
					} 
					
				}
				
				list = loadDatasetsUp(bReader, columnNrDataRate, columnNrDelay, columnNrTimestamp, columnNrLossRate);
				
			}
			
			bReader.close();
			
			return list;
			
		} catch (Exception e) {			
			System.out.println("Error: loadGetEdges: \n" + line + "\n" + e.toString());
		}		
		
		return list;
		
	}
	
	private static ArrayList<myDataset> loadDatasetsUp(BufferedReader bReader, int columnNrDataRate, int columnNrDelay, int columnNrTimestamp, int columnNrLossRate) {
		
		ArrayList<myDataset> list = new ArrayList<myDataset>();
		
		String line = "";
		
		try {
			line = bReader.readLine();	
			
			while (line != null) {
				
				myDataset d = new myDataset();
				
				String[] lines = line.split(",");
				
				try {
					d.datarate = Integer.parseInt(lines[columnNrDataRate]);					
				} catch (Exception e) {
					d.datarate = -1;
				}
				
				try {
					d.delay = Double.parseDouble(lines[columnNrDelay]);		
				} catch (Exception e) {
					d.delay = -1;
				}
				
				try {
					d.timestamp = Long.parseLong(lines[columnNrTimestamp]);				
				} catch (Exception e) {
					d.timestamp = -1;
				}
				
				try {
					d.loss_rate = Double.parseDouble(lines[columnNrLossRate]);				
				} catch (Exception e) {
					d.loss_rate = -1;
				}
				
				list.add(d);
				
				line = bReader.readLine();	
			}		
		} catch(Exception e) {
			System.out.println("Error: loadGetEdges: \n" + line + "\n" + e.toString());
		}
		
		return list;
	}

}
