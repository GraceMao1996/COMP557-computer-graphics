package comp557.a4;

import javax.vecmath.Vector3d;

/**
 * Class for a plane at y=0.
 * 
 * This surface can have two materials.  If both are defined, a 1x1 tile checker 
 * board pattern should be generated on the plane using the two materials.
 */
public class Plane extends Intersectable {
    
	/** The second material, if non-null is used to produce a checker board pattern. */
	Material material2;
	
	/** The plane normal is the y direction */
	public static final Vector3d n = new Vector3d( 0, 1, 0 );
    
    /**
     * Default constructor
     */
    public Plane() {
    	super();
    }

        
    @Override
    public void intersect( Ray ray, IntersectResult result ) {
    
        // TODO: Objective 4: intersection of ray with plane
    	//attention! the plane is at y=0
    	//r = p +td  ry = 0 py+tdy=0
    	double t = -ray.eyePoint.y/ray.viewDirection.y;
    	if(t>0 && t<result.t) {
    		result.t = t;
			result.material = material;
			result.p.set(ray.eyePoint);
			Vector3d direction = new Vector3d(ray.viewDirection);
			direction.scale(t);
			result.p.add(direction); // p+t*d
			result.n.set(n); 
		
			int x=(int)Math.round(result.p.x);
        	int z=(int)Math.round(result.p.z);
        	if(material2!=null && (x+z)%2==0)
    			result.material=material2;
    		
    	}
    	
    }
    
}
