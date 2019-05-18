//Yiran Mao 260850827

package comp557.a1;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.util.gl2.GLUT;


import javax.vecmath.Tuple3d;
public class BodyBox extends DAGNode {




   private Tuple3d centre, scale, color;
   private static GLUT glut = new GLUT();
   public BodyBox( String name ) {
	   super(name);
   }
   
   public void setCentre(Tuple3d t) {   
	   this.centre = t;
   }
   
   public void setScale(Tuple3d t) {
	   this.scale = t;
   }
   
   public void setColor(Tuple3d t) {
	   this.color = t;
   }
   @Override
   public void display( GLAutoDrawable drawable ) {
      GL2 gl = drawable.getGL().getGL2();
      gl.glPushMatrix();
      if(centre != null ) gl.glTranslated( centre.x, centre.y, centre.z ); 
      if(scale != null) gl.glScaled(scale.x,scale.y,scale.z);
      if(color != null) gl.glColor3d(color.x, color.y, color.z);
      
      glut.glutSolidCube(1);
      super.display(drawable);
      gl.glPopMatrix();
   }
 
	
}
