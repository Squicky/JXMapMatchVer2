package myOSM;

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
	
//	public List<myOSMNode> refs = new LinkedList<myOSMNode>();
	public myOSMNode[] refs = null;
	
	
//	public List<myOSMWayPart> WayParts = new LinkedList<myOSMWayPart>();
	public myOSMWayPart[] WayParts = new myOSMWayPart[0];
	
	public String name = "";
	
	public boolean onyWay = false;
	
	public int lanes = 1;

	public String motorcar = "";
	
	public String highway = "";

	public int maxSpeed = -1;
	
	public boolean wayNotNeeded = false;
	
	public int meansOfTransport = myOSMWay.DEFAULT;

	public int carPermission; //0 = notallowed, 1 = restricted, 2 = allowed
	
	public int minX = Integer.MAX_VALUE;
	public int minY = Integer.MAX_VALUE;
	public int maxX = Integer.MIN_VALUE;
	public int maxY = Integer.MIN_VALUE;
	
	public void setWayParts() {
		
		if (onyWay == false) {
			WayParts = new myOSMWayPart[(refs.length - 1) * 2];
		} else {
			WayParts = new myOSMWayPart[(refs.length - 1)];
		}
		
		int k;
		for (k = 0; k < (refs.length - 1); k++) {
			WayParts[k] =  new myOSMWayPart(refs[k], refs[k+1], this, k, false);
			
			if (WayParts[k].startNode.x < minX) {
				minX = WayParts[k].startNode.x;
			} else if (maxX < WayParts[k].startNode.x) {
				maxX = WayParts[k].startNode.x;
			}
			
			if (WayParts[k].startNode.y < minY) {
				minY = WayParts[k].startNode.y;
			} else if (maxY < WayParts[k].startNode.y) {
				maxY = WayParts[k].startNode.y;
			}
		}

		if (k > 0) {
			k--;

			if (WayParts[k].endNode.x < minX) {
				minX = WayParts[k].endNode.x;
			} else if (maxX < WayParts[k].endNode.x) {
				maxX = WayParts[k].endNode.x;
			}
			
			if (WayParts[k].endNode.y < minY) {
				minY = WayParts[k].endNode.y;
			} else if (maxY < WayParts[k].endNode.y) {
				maxY = WayParts[k].endNode.y;
			}

			k++;
		}

		
		int j = 0;
		if (onyWay == false) {
			for (int i = (refs.length - 1); i >=1 ; i--) {
				myOSMWayPart wp = new myOSMWayPart(refs[i], refs[i-1], this, j, true);
				j++;
				WayParts[k] = wp;
				k++;
			}
		}
		
		
	}
	
	public void setCountAndXYOfNotes() {
		if (refs.length > 0) {
			refs[0].countIsStartOfWay++;
			
			refs[refs.length - 1].countIsEndOfWay++;
			
			for (int i = 1; i < (refs.length - 1); i++) {
				refs[i].countIsInnerNoteofWay++;				
			}
			
			for (int i = 0; i < refs.length; i++) {
				refs[i].setXY();
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
