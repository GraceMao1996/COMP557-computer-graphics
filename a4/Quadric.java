package comp557.a4;


import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Class for a Elliptical paraboloid quadric.
 * //x^2/a_a + y^2/b_b = 2z
 */
public class Quadric extends Intersectable {
    

	

    
    /**
     * Default constructor
     */
    public Quadric() {
    	super();
    }

    public float a_a=1;
    public float b_b=1;
    
    @Override
    public void intersect( Ray ray, IntersectResult result ) { 
    	
    	

    	Vector3d d=new Vector3d(ray.viewDirection);
    	Vector3d e=new Vector3d(ray.eyePoint);
    	//x^2/a_a + y^2/b_b = 2z
    	//to calculate t:
    	//At^2+Bt+C=0
    	double A,B,C,t;
    	A=b_b*d.x*d.x+a_a*d.y*d.y;
    	B=b_b*2*e.x*d.x+a_a*2*e.y*d.y-2*a_a*b_b*d.z;
    	C=b_b*e.x*e.x+a_a*e.y*e.y-2*a_a*b_b*e.z;
    	double inside=B*B-4*A*C;
    	if(inside>=0 && A!=0){
    		
    		double root=Math.sqrt(inside);
        	double t1=(-B+root)/A/2;
        	double t2=(-B-root)/A/2;
        	
        	if(t2>0)
        		t=t2;
        	else
        		t=t1;
        	if(t<result.t&&t>0){
           	result.material=material;
       		result.t=t;
       		result.p.set(ray.viewDirection);
           	result.p.scale(t);
           	result.p.add(ray.eyePoint);

           	result.n=new Vector3d();
           	result.n.x=2/a_a*result.p.x;
           	result.n.y=2/b_b*result.p.y;
           	result.n.z=-2;
           	result.n.normalize();
           	
        	}

    	
		}
	}
}
    	