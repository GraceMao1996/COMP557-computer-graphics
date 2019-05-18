//Yiran Mao 260850827
package comp557.a1;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import javax.vecmath.Tuple3d;
import mintools.parameters.DoubleParameter;

public class BallXYZ extends DAGNode {

	DoubleParameter rx;
	DoubleParameter ry;
	DoubleParameter rz;
	private Tuple3d pos;
	public void setPosition(Tuple3d t) {
		this.pos = t;
	}
	
	public BallXYZ( String name ) {
		super(name);
		dofs.add( rx = new DoubleParameter( name+" rx", 0, -90, 90 ) );		
		dofs.add( ry = new DoubleParameter( name+" ry", 0, -90, 90 ) );
		dofs.add( rz = new DoubleParameter( name+" rz", 0, -90, 90 ) );
	}
	
	@Override
	public void display(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
	    gl.glPushMatrix();
	    if(pos != null)gl.glTranslated(pos.x, pos.y, pos.z);
	    gl.glRotated(rx.getValue(), 1, 0, 0);
	    gl.glRotated(ry.getValue(), 0, 1, 0);
	    gl.glRotated(rz.getValue(), 0, 0, 1);
	    
		super.display(drawable);
		gl.glPopMatrix();
		// TODO: Objective 1: implement the BallJoint display method
		
	
	}

	
}
