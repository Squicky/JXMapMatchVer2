package myOSM;

import java.util.LinkedList;
import java.util.List;

public class myOSMWay {

	//means of transport constants
	public static final int DEFAULT=0x00;
	public static final int CAR=0x01;
	public static final int TRAM=0x02;	
	
	private static String [] highwayTypes = {"motorway","motorway_link","motorway_junction","trunk","trunk_link",
		"primary","primary_link","primary_trunk","secondary","secondary_link",
		"tertiary","tertiary_link","unclassified","unsurfaced","track",
		"residential","living_street","service","road","raceway",
		"xxx","xxx","xxx","xxx","xxx",
		"xxx","xxx","xxx","xxx","xxx", //intentionally left blank
		"steps","bridleway","cycleway","footway","pedestrian",
		"bus_guideway","path","xxx","xxx","xxx",
		"xxx","xxx","xxx","xxx","xxx",
		"xxx","xxx","xxx","xxx","xxx",};
	
	public long id = -1;
	
	public List<myOSMNode> refs = new LinkedList<myOSMNode>();

	public List<myOSMWayPart> WayParts = new LinkedList<myOSMWayPart>();
	
	public String name = "";
	
	public boolean onyWay = false;
	
	public int lanes = 1;

	public String motorcar = "";
	
	public String highway = "";

	public int maxSpeed = -1;
	
	public boolean wayNotNeeded = false;
	
	public int meansOfTransport = myOSMWay.DEFAULT;

	public int carPermission; //0 = notallowed, 1 = restricted, 2 = allowed
	
	public void setWayParts() {
		for (int i = 0; i < (refs.size() - 1); i++) {
			myOSMWayPart wp = new myOSMWayPart(refs.get(i), refs.get(i+1), this, i, false);
			WayParts.add(wp);
		}

		int j = 0;
		if (onyWay == false) {
			for (int i = (refs.size() - 1); i >=1 ; i--) {
				myOSMWayPart wp = new myOSMWayPart(refs.get(i), refs.get(i-1), this, j, true);
				j++;
				WayParts.add(wp);				
			}
		}
	}
	
	public void setCountAndXYOfNotes() {
		if (refs.size() > 0) {
			refs.get(0).countIsStartOfWay++;
			
			refs.get(refs.size() - 1).countIsEndOfWay++;
			
			for (int i = 1; i < (refs.size() - 1); i++) {
				refs.get(i).countIsInnerNoteofWay++;				
			}
			
			for (int i = 0; i < refs.size(); i++) {
				refs.get(i).setXY();
			}

		}
	}
	
	public void set_meansOfTransport() {
		//transform Highway string to integer
		int highwayType = highwayType(highway);
		
		//check carPermission
		carPermission = carPermission(highwayType, motorcar, id);
		
		//check car permission to set flag
		if (carPermission!=0)
			this.meansOfTransport |= myOSMWay.CAR;		
	}
	
	public boolean getMeansOfTransportPermission(final int transportFlag)
	{
		//check if bit for this means of transport is set
		return ((meansOfTransport & transportFlag) != 0);
	}
	
	public static int highwayType(String highway){
		for (int i=0;i<highwayTypes.length;i++){
			if (highwayTypes[i].equals(highway))
				return i;
		}
		//System.out.println("Warning, unknown highway type: "+highway);
		return -1;
	}

	/**
	 * Returns a value that shows if a car may drive on this way.
	 * 
	 * @param highwayType
	 * @param motorcar
	 * @param id
	 * @return 0 = not allowed, 1 = restricted, 2 = allowed
	 */
	public static int carPermission(int highwayType, String motorcar,long id){
		// highway types usually intended for car
		if (highwayType>=0 && highwayType<=29)
		{
			if (motorcar.equals("") || motorcar.equals("yes") || motorcar.equals("designated") || motorcar.equals("official")) 
				return 2;
			else if (motorcar.equals("private") || motorcar.equals("permissive") || motorcar.equals("unknown") ||
					motorcar.equals("restricted") || motorcar.equals("destination") || motorcar.equals("customer") ||
					motorcar.equals("delivery") || motorcar.equals("agricultural") || motorcar.equals("forestry")){
				return 1;
			}
			else if (motorcar.equals("no"))
				return 0;
			else {
				System.out.println("Illegal motorcar/highway combination in way-id:"+id+
						" highway="+highwayTypes[highwayType]+" motorcar="+motorcar);
				return 0;
			}
		}
		// motor-driven vehicles can't drive on steps!
		else if (highwayType==30)
		{
			if (motorcar.equals("") || motorcar.equals("no"))
				return 0;
			else
			{
				System.out.println("Illegal motorcar/highway combination in way-id:"+id+
						" highway="+highwayTypes[highwayType]+" motorcar="+motorcar);
				return 0;
			}	
		}
		// highway types not designed for cars primarily
		else if (highwayType >= 31 && highwayType <=49)
		{
			if (motorcar.equals("") || motorcar.equals("no"))
				return 0;
			else if (motorcar.equals("yes") || motorcar.equals("designated") || motorcar.equals("official"))
				return 2;
			else if  (motorcar.equals("private") || motorcar.equals("permissive") || motorcar.equals("unknown") ||
					motorcar.equals("restricted") || motorcar.equals("destination") || motorcar.equals("customer") ||
					motorcar.equals("delivery") || motorcar.equals("agricultural") || motorcar.equals("forestry"))
				return 1;
			else 
			{
				System.out.println("Unhandled motorcar/highway combination in way-id:"+id+
						" highway="+highwayTypes[highwayType]+" motorcar="+motorcar);
				return 0;
			}
		}
		//else
		return 0;
	}
	

}