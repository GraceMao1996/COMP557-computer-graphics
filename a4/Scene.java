package comp557.a4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.vecmath.Color3f;
import javax.vecmath.Color4f;
import javax.vecmath.Point2d;
import javax.vecmath.Vector3d;

import comp557.a2.FastPoissonDisk;

import javax.vecmath.Point3d;

/**
 * Simple scene loader based on XML file format.
 */
public class Scene {
    
    /** List of surfaces in the scene */
    public List<Intersectable> surfaceList = new ArrayList<Intersectable>();
	
	/** All scene lights */
	public Map<String,Light> lights = new HashMap<String,Light>();

    /** Contains information about how to render the scene */
    public Render render;
    
    /** The ambient light colour */
    public Color3f ambient = new Color3f();
    


    /** 
     * Default constructor.
     */
    public FastPoissonDisk fpd = new FastPoissonDisk();
    public Scene() {
    	this.render = new Render();
    }
    
    /**
     * renders the scene
     */
    
    public void render(boolean showPanel) {
    	
        Camera cam = render.camera; 
        int w = cam.imageSize.width;
        int h = cam.imageSize.height;
        
        render.init(w, h, showPanel);
        
        Thread_render T1 = new Thread_render(this,cam,w,h,1);
        Thread_render T2 = new Thread_render(this,cam,w,h,2);   //bonus 3: multiple-threaded parallelization
        T1.start();
        T2.start();
        
        
        
        // wait for render viewer to close
        render.waitDone();
        // save the final render image
        render.save();
        
    
    } 
    
    
    
    class Thread_render extends Thread {
    	int h;
    	int w;
    	Scene scene;
    	Camera cam;
    	int Thread_number;
    	
    	Thread_render(Scene scene, Camera cam, int w, int h, int Thread_number){
    		this.scene = scene;
    		this.cam = cam;
    		this.w= w;
    		this.h = h;
    		this.Thread_number = Thread_number;
    	}
    	
    	public void run(){
    		
            int c_samples = 1;//20;
            double aperture = 0;//0.3;          // for depth of field
            double focus_lengh = 0.7;     //focus_lengh = distance_focalplane/ distance_screenplane
            int w_min, w_max;
	        if(Thread_number==1) {
	        	w_min = 0;
	        	w_max = w/2;
	        }else{
	        	w_min = w/2;
	        	w_max = w;
	        }
            
    		
            for ( int i = 0; i < h && !render.isDone(); i++ ) {
                for ( int j = w_min; j < w_max && !render.isDone(); j++ ) {
                	Color4f color = new Color4f();
                	
                	// the subpixels are defined in sub_x rows and sub_y colums and has a sub_remainder
                	
                	// TODO: Objective 1: generate a ray (use the generateRay method) 	
                	// TODO: Objective 8: do antialiasing by sampling more than one ray per pixel
    			    int samples = render.samples;
    			    
                	int sub_x = (int)Math.sqrt(samples);
                	int sub_y = samples/sub_x;
                	int sub_remainder = samples - sub_x*sub_y;
                	
               
                	double sub_x_l = (double)1.0/sub_y;
                	double sub_y_l = (double)1.0/(sub_x+1);
                 	System.out.println(sub_x_l);
                	System.out.println(sub_y_l);
                	
                 	double jitterx=sub_x_l/2;
                	double jittery=sub_y_l/2;
                	
                    for (int x = 0; x< sub_x+1; x++) { 
                    	int y_max;
                    	if (x==sub_x) {
                    		y_max = sub_remainder;
                    	}
                    	else y_max = sub_y;
                		for (int y = 0; y<y_max; y++) {
                			
                			if(render.jitter) {
                				jitterx = Math.random()*sub_x_l;
                				jittery = Math.random()*sub_y_l;
                			}
                			
                			double xOffset = x*sub_x_l+jitterx - 0.5;
                			double yOffset = y*sub_y_l +jittery - 0.5;
                			Ray ray = new Ray();
                			Color4f sub_color = new Color4f();
                			generateRay(i, j, new double[]{yOffset,xOffset}, cam, ray);
                			Point3d focus_point = new Point3d(ray.viewDirection);
                			focus_point.scale(focus_lengh);
                			focus_point.add(ray.eyePoint);
                		    for(int n = 0; n<c_samples;n++) {  //bonus1: depth of field blur, multiple cameras!
                		    	
                		    	
                		    	Color4f sub_color2 = new Color4f(render.bgcolor.x,render.bgcolor.y,render.bgcolor.z,0); 
             					Point2d p = new Point2d();
             					fpd.get(p, n, c_samples);
             					p.scale(aperture);
             				    
             				    //System.out.println(p.x);
             				    Camera sub_cam = new Camera();
             				    sub_cam.from.z = cam.from.z;
             				    sub_cam.from.x = cam.from.x + p.x;
             				    sub_cam.from.y = cam.from.y + p.y;
             				    
             				    Ray sub_ray = new Ray();
             				    sub_ray.eyePoint = sub_cam.from;
             				    sub_ray.viewDirection = new Vector3d(focus_point);
             				    sub_ray.viewDirection.sub(sub_cam.from);
             				    //sub_ray.viewDirection.normalize();
             				    
                			
                			// TODO: Objective 2: test for intersection with scene surfaces
    	            			IntersectResult result = new IntersectResult();                       	
    	                    	for(Intersectable surface:surfaceList) {
    	                    		surface.intersect(sub_ray, result);
    	                    	}
    	                    	
    	                    	// TODO: Objective 3: compute the shaded result for the intersection point (perhaps requiring shadow rays)
    	                    	
    	                    	if (result.t < Double.POSITIVE_INFINITY)   //if has intersection then compute the shaded
    	                    		//color.set(1,1,1,1); //without shading
    	                    		sub_color2 = GetColor(sub_ray,result);
    	                    		
    	                    	sub_color.add(sub_color2);
    		                    	
    		            	}	
                		    sub_color.scale((float)1.0/c_samples);
                		    color.add(sub_color);
                		    
                		}
                    }
                   
    	            color.scale((float)1.0/samples);
            		color.clampMax(1);
                	int r = (int)(255*color.x);
                    int g = (int)(255*color.y);
                    int b = (int)(255*color.z);
                    int a = 255;
                    int argb = (a<<24 | r<<16 | g<<8 | b);    
                    
                    // update the render image
                    render.setPixel(j, i, argb);
    	            
                    
                }
            }
    		
    	}
    	
    }
    
    /**
     * Generate a ray through pixel (i,j).
     * 
     * @param i The pixel row.
     * @param j The pixel column.
     * @param offset The offset from the center of the pixel, in the range [-0.5,+0.5] for each coordinate. 
     * @param cam The camera.
     * @param ray Contains the generated ray.
     */
	public static void generateRay(final int i, final int j, final double[] offset, final Camera cam, Ray ray) {
		
		// TODO: Objective 1: generate rays given the provided parmeters
		// calculate u,v,w for eye(camera) using eye position, look at and up
		
		double wx = cam.from.x - cam.to.x, wy = cam.from.y - cam.to.y, wz = cam.from.z - cam.to.z;
		Vector3d w = new Vector3d(wx,wy,wz);
		double distance = w.length();   // distance from camera to the image plane
		w.normalize();  // normalize w
		Vector3d u = new Vector3d();
		Vector3d v = new Vector3d();
		u.cross(cam.up, w);
		u.normalize();
		v.cross(u, w);
		v.normalize();
		// using distance and fovy to calculate left,right,top and bottom of the image plane
		double top =distance*(Math.tan(Math.toRadians(cam.fovy/2.0))); 
		double bottom = -top;
		double right = top*((double)cam.imageSize.width/cam.imageSize.height);
		double left = -right;
		
		//calculate ray direction
		//𝐬=𝐞+𝑢𝐮+𝑣𝐯−𝑑𝐰
		//𝐩=𝐞   𝐝=𝐬−𝐞   r(t)=𝐩+𝑡𝐝
		//to calculate from pixel to image: 𝑢=𝑙+(𝑟−𝑙)(𝑖+0.5)/𝑛𝑥𝑣=𝑏+(𝑡−𝑏)(𝑗+0.5)/𝑛𝑦
		u.scale(left+(right-left)*(j+0.5+offset[1])/cam.imageSize.width); //note j here is for width
		v.scale(bottom+(top-bottom)*(i+0.5+offset[0])/cam.imageSize.height);
		w.scale(distance);
		Vector3d direction = new Vector3d(u);
		direction.add(v);
		direction.sub(w);
		
		ray.eyePoint = new Point3d(cam.from);
		ray.viewDirection = direction;				
	}
	
	
	private Color4f GetColor(final Ray ray, final IntersectResult result) {
	    	Color4f color = new Color4f();
	    	//firstly add the ambient shading
	    	Color4f Ambient =new Color4f(ambient.x*result.material.diffuse.x,
    				ambient.y*result.material.diffuse.y,
    				ambient.z*result.material.diffuse.z,0);
	    	color = Ambient;
	    	
	    	for(String key: lights.keySet()) {   //every light in lights set
	    		Light original_light = lights.get(key);
	    		int l_samples = 1;
	    		double l_distance = 0;
	    		
	    		if (original_light.type.equals("area")) {   //bonus2:area lights, soft shadow!
	    			l_samples = 5;
	    	     	l_distance = 0.5;
	    		}
	    		
	    		for(int lx=0;lx<l_samples;lx++) {
	    			for (int lz=0; lz<l_samples;lz++) {
		    			Light light = new Light();
		    			light.power = original_light.power/(l_samples*l_samples);
		    			light.color = original_light.color;
		    			
		    			light.type = "point";
		    			light.from.x = original_light.from.x + lx*l_distance - (l_samples/2)*l_distance;
		    			light.from.z = original_light.from.z + lz*l_distance - (l_samples/2)*l_distance;
		    			light.from.y = original_light.from.y;
		    			if(!inShadow(result, light, surfaceList, new IntersectResult(), new Ray())){
				    		Vector3d l = new Vector3d(light.from);
				    		l.sub(result.p); 
				    		l.normalize();//light vector
				    		
				    		// calculate lambertian
				    		//ld = kd*I*max(0,n dot l)
				    		
				    		double n_l = result.n.dot(l);  //n dot l
				    		if (n_l<0) n_l=0;//take max(0,n dot l)
				    		n_l = n_l*light.power;
				    		Color4f lambertian = new Color4f(result.material.diffuse.x*light.color.x,
				    				result.material.diffuse.y*light.color.y,
				    				result.material.diffuse.z*light.color.z,1);
				    		lambertian.scale((float)(n_l));
				    		color.add(lambertian);	
				    		
				    		//calculate specular
				    		Vector3d h = new Vector3d(l);
				    		Vector3d v = ray.viewDirection;
				    		v.normalize();
				    		v.scale(-1); //attention! here v is p point to camera
				    		h.add(v);
				    		h.normalize();  //bisector vector
				    		double n_h = result.n.dot(h);
				    		if (n_h<0) n_h=0;
				    		n_h = Math.pow(n_h, result.material.shinyness);
				    		n_h = n_h*light.power;
				    		Color4f specular = new Color4f(result.material.specular.x*light.color.x,
				    				result.material.specular.y*light.color.y,
				    				result.material.specular.z*light.color.z,1);
				    		specular.scale((float)(n_h));
				    		color.add(specular);
				    	
	    		}
	    		
	       }	
	     }
	   }
	    	
	    	return color;
	    	
	    }

	/**
	 * Shoot a shadow ray in the scene and get the result.
	 * 
	 * @param result Intersection result from raytracing. 
	 * @param light The light to check for visibility.
	 * @param root The scene node.
	 * @param shadowResult Contains the result of a shadow ray test.
	 * @param shadowRay Contains the shadow ray used to test for visibility.
	 * 
	 * @return True if a point is in shadow, false otherwise. 
	 */
	public static boolean inShadow(final IntersectResult result, final Light light, final List<Intersectable> surfaces, IntersectResult shadowResult, Ray shadowRay) {
		
		// TODO: Objective 5: check for shadows and use it in your lighting computation
		
		shadowRay.viewDirection.set(light.from);
		shadowRay.viewDirection.sub(result.p);
		
		// The shadow ray's origin is the intersection point plus an epsilon offset
		// to avoid detecting self-intersections.
		Point3d temp = new Point3d(shadowRay.viewDirection);
		temp.scale(0.00001); // shadow rays start a tiny distance from the surface
		temp.add(result.p);
		shadowRay.eyePoint=new Point3d(temp);
		
		//Check if the shadow ray intersects a surface
		for(Intersectable surface:surfaces) {
			surface.intersect(shadowRay, shadowResult);
		}
		if (shadowResult.t<1 && shadowResult.t>0) return true;   
		return false;
		
		
	}    
}
