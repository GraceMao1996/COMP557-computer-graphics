package comp557.a4;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * A simple sphere class.
 */
public class Sphere extends Intersectable {
    
	/** Radius of the sphere. */
	public double radius = 1;
    
	/** Location of the sphere center. */
	public Point3d center = new Point3d( 0, 0, 0 );
    
    /**
     * Default constructor
     */
    public Sphere() {
    	super();
    }
    
    /**
     * Creates a sphere with the request radius and center. 
     * 
     * @param radius
     * @param center
     * @param material
     */
    public Sphere( double radius, Point3d center, Material material ) {
    	super();
    	this.radius = radius;
    	this.center = center;
    	this.material = material;
    }
    
    @Override
    public void intersect( Ray ray, IntersectResult result ) {
    
        // TODO: Objective 2: intersection of ray with sphere
    	// point on ray: x = p + td
    	// point on sephere: : ||x - c||^2 = r^2
    	Vector3d direction = new Vector3d(ray.viewDirection); //ray direction
    	Vector3d e_c = new Vector3d(ray.eyePoint); //eye point - center   	
    	e_c.sub(center);
    	double d_e_c = direction.dot(e_c);
    	double d_d = direction.lengthSquared();
    	double inside = d_e_c*d_e_c - d_d*(e_c.lengthSquared() - radius*radius);
    	// if inside<0 no intersection
    	//if(inside<0) System.out.println("no intersection!");
    	if(inside>=0) {
    		
    		double t = (-d_e_c - Math.sqrt(inside))/d_d; //the closer one
    		if(t>0 && t<result.t) {  //t is positive, ray is a half line
    			//only if t<result.t, update result, always keeping track of the closest intersection.
    			result.t = t;
    			result.material = material;
    			result.p.set(ray.eyePoint);
    			direction.scale(t);
    			result.p.add(direction); // p+t*d
    			Vector3d normal = new Vector3d(result.p);
    			normal.sub(center);
    			normal.normalize(); //normal of the sphere
    			result.n = normal;
    		}
    		
    	}
	
    }
    
}
