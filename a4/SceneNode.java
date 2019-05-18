package comp557.a4;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.vecmath.Matrix4d;

import comp557.a4.IntersectResult;
import comp557.a4.Intersectable;
import comp557.a4.Ray;

/**
 * The scene is constructed from a hierarchy of nodes, where each node
 * contains a transform, a material definition, some amount of geometry, 
 * and some number of children nodes.  Each node has a unique name so that
 * it can be instanced elsewhere in the hierarchy (provided it does not 
 * make loops. 
 * 
 * Note that if the material (inherited from Intersectable) for a scene 
 * node is non-null, it should override the material of any child.
 * 
 */
public class SceneNode extends Intersectable {
	
	/** Static map for accessing scene nodes by name, to perform instancing */
	public static Map<String,SceneNode> nodeMap = new HashMap<String,SceneNode>();
	
    public String name;
   
    /** Matrix transform for this node */
    public Matrix4d M;
    
    /** Inverse matrix transform for this node */
    public Matrix4d Minv;
    
    /** Child nodes */
    public List<Intersectable> children;
    
    /**
     * Default constructor.
     * Note that all nodes must have a unique name, so that they can used as an instance later on.
     */
    public SceneNode() {
    	super();
    	this.name = "";
    	this.M = new Matrix4d();
    	this.Minv = new Matrix4d();
    	this.children = new LinkedList<Intersectable>();
    }
           
    @Override
    public void intersect(Ray ray, IntersectResult result) {

    	// TODO: Objective 7: implement hierarchy with instances

    	// this is not going to work, but might help you get
    	// started with some scenes...
    	Ray rayTemp = new Ray(ray.eyePoint,ray.viewDirection);

    	Minv.transform(rayTemp.eyePoint);
    	Minv.transform(rayTemp.viewDirection); 
    	//transform by Minv
    	
    	
    	for ( Intersectable c : children ) {
    		IntersectResult resultTemp = new IntersectResult();
            c.intersect( rayTemp, resultTemp ); 	
	    	if(resultTemp.t > 0.00001 && resultTemp.t<result.t) { //the closest one
		    	
		    		M.transform(resultTemp.n);
		    		M.transform(resultTemp.p);	    		
		    		result.n.set(resultTemp.n); ;
		    		result.p.set(resultTemp.p);	    	
		    		result.t = resultTemp.t;
		    		result.material = resultTemp.material;			
		    	}
    	}
    	if (this.material != null) result.material = this.material;
    	result.n.normalize();
        		
    }
    
}
