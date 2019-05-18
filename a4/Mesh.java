package comp557.a4;

import java.util.HashMap;
import java.util.Map;
import javax.vecmath.Vector3d;
import javax.vecmath.Point3d;

public class Mesh extends Intersectable {
	
	/** Static map storing all meshes by name */
	public static Map<String,Mesh> meshMap = new HashMap<String,Mesh>();
	
	/**  Name for this mesh, to allow re-use of a polygon soup across Mesh objects */
	public String name = "";
	
	/**
	 * The polygon soup.
	 */
	public PolygonSoup soup;

	public Mesh() {
		super();
		this.soup = null;
	}			
		
	@Override
	public void intersect(Ray ray, IntersectResult result) {
		
		// TODO: Objective 9: ray triangle intersection for meshes
		for(int[] indices:soup.faceList) {
			Vector3d a=new Vector3d(soup.vertexList.get(indices[0]).p);
			Vector3d b=new Vector3d(soup.vertexList.get(indices[1]).p);
			Vector3d c=new Vector3d(soup.vertexList.get(indices[2]).p);  // three vertex of the triangle face
			Vector3d b_a = new Vector3d(b);
			b_a.sub(a);
			Vector3d c_b = new Vector3d(c);
			c_b.sub(b);
			
			Vector3d a_c = new Vector3d(a);
			a_c.sub(c);
			Vector3d normal = new Vector3d();
			normal.cross(b_a, c_b);
			//normal.normalize();   // compute the normal for every face
			Vector3d a_p = new Vector3d(a);
			a_p.sub(ray.eyePoint);
			double t = a_p.dot(normal)/(normal.dot(ray.viewDirection));
			
			Point3d f = new Point3d(ray.viewDirection);
			f.scale(t);
			
			f.add(ray.eyePoint);
			Vector3d f_a = new Vector3d(f);
			f_a.sub(a);
			Vector3d f_b = new Vector3d(f);
			f_b.sub(b);
			Vector3d f_c = new Vector3d(f);
			f_c.sub(c);
			
			
			f_a.cross(b_a, f_a);
			f_b.cross(c_b, f_b);
			f_c.cross(a_c,f_c);
			
			
			
			if(f_a.dot(normal)>0 && f_b.dot(normal)>0 && f_c.dot(normal)>0 && t>0 && t<result.t) {
				result.n =normal;
				result.t = t;
				result.p = f;
				result.material = material;
				
			}
			
		}
		
	}

}
