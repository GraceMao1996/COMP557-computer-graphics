package comp557.a4;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * A simple box class. A box is defined by it's lower (@see min) and upper (@see max) corner. 
 */
public class Box extends Intersectable {

	public Point3d max;
	public Point3d min;
	
    /**
     * Default constructor. Creates a 2x2x2 box centered at (0,0,0)
     */
    public Box() {
    	super();
    	this.max = new Point3d( 1, 1, 1 );
    	this.min = new Point3d( -1, -1, -1 );
    }	

	@Override
	public void intersect(Ray ray, IntersectResult result) {
		// TODO: Objective 6: intersection of Ray with axis aligned box
		// the intersection of 3 slabs
		double txmin, txmax, txlow, txhigh, tymin, tymax, tylow, tyhigh, tzmin, tzmax, tzlow, tzhigh, tmin, tmax;
		txmin = (min.x - ray.eyePoint.x)/ray.viewDirection.x;
		txmax = (max.x - ray.eyePoint.x)/ray.viewDirection.x;
		txlow = Double.min(txmin, txmax);
		txhigh = Double.max(txmin, txmax);
		
		tymin = (min.y - ray.eyePoint.y)/ray.viewDirection.y;
		tymax = (max.y - ray.eyePoint.y)/ray.viewDirection.y;
		tylow = Double.min(tymin, tymax);
		tyhigh = Double.max(tymin, tymax);
		
		tzmin = (min.z - ray.eyePoint.z)/ray.viewDirection.z;
		tzmax = (max.z - ray.eyePoint.z)/ray.viewDirection.z;
		tzlow = Double.min(tzmin, tzmax);
		tzhigh = Double.max(tzmin, tzmax);
		
		tmin = Double.max(txlow, tylow);
		tmin = Double.max(tmin, tzlow);
		tmax = Double.min(txhigh, tyhigh);
		tmax = Double.min(tmax, tzhigh);
		
		if(tmin<tmax && tmin>0){ //if tmax<tmin then no intersection!		
			result.material=material;
			result.t=tmin;
			result.p =new Point3d(ray.viewDirection);
			result.p.scale(tmin);
			result.p.add(ray.eyePoint);
			
			final double epsilon=0.00001;		//attention! a tiny distance!!
            // calculate the normal   
			if(Math.abs(result.p.x-min.x)<epsilon){
				result.n=new Vector3d(-1,0,0);
			}else if(Math.abs(result.p.x-max.x)<epsilon){
				result.n=new Vector3d(1,0,0);
			}else if(Math.abs(result.p.y-min.y)<epsilon){
				result.n=new Vector3d(0,-1,0);
			}else if(Math.abs(result.p.y-max.y)<epsilon){
				result.n=new Vector3d(0,1,0);
			}else if(Math.abs(result.p.z-min.z)<epsilon){
				result.n=new Vector3d(0,0,-1);
			}else if(Math.abs(result.p.z-max.z)<epsilon){
				result.n=new Vector3d(0,0,1);
			}
		}		
	}	
}

