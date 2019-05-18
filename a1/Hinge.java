//Yiran Mao 260850827

package comp557.a1;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import javax.vecmath.Tuple3d;
import mintools.parameters.DoubleParameter;

public class Hinge extends DAGNode {


	DoubleParameter r;

	private Tuple3d pos, axis;
	public void setPosition(Tuple3d t) {
		this.pos = t;
	}
	
	public void setAxis(Tuple3d t) {
		this.axis = t;
	}
	public Hinge( String name ) {
		super(name);
		dofs.add( r = new DoubleParameter( name+" rx", 0, -180, 0 ) );		
		
	}
	
	@Override
	public void display(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
	    gl.glPushMatrix();
	    if(pos != null) gl.glTranslated(pos.x, pos.y, pos.z);
	    if(axis != null)gl.glRotated(r.getValue(), axis.x, axis.y, axis.z);
	   
	    
		super.display(drawable);
		gl.glPopMatrix();
		// TODO: Objective 1: implement the HingeJoint display method
		
	
	}

	
}
