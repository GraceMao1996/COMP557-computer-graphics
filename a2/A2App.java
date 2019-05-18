package comp557.a2;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.vecmath.Point2d;

import com.jogamp.opengl.DebugGL2;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.gl2.GLUT;

import comp557Demos.projDemo.Vec3Parameter;
import mintools.parameters.BooleanParameter;
import mintools.parameters.DoubleParameter;
import mintools.parameters.IntParameter;
import mintools.swing.ControlFrame;
import mintools.swing.VerticalFlowPanel;
import mintools.viewer.EasyViewer;
import mintools.viewer.FlatMatrix4d;
import mintools.viewer.Interactor;
import mintools.viewer.SceneGraphNode;
import mintools.viewer.TrackBallCamera;

/**
 * Assignment 2 - depth of field blur, and anaglyphys
 * 
 * For additional information, see the following paper, which covers
 * more on quality rendering, but does not cover anaglyphs.
 * 
 * The Accumulation Buffer: Hardware Support for High-Quality Rendering
 * Paul Haeberli and Kurt Akeley
 * SIGGRAPH 1990
 * 
 * http://http.developer.nvidia.com/GPUGems/gpugems_ch23.html
 * GPU Gems [2007] has a slightly more recent survey of techniques.
 *
 * @author Yiran Mao
 */
public class A2App implements GLEventListener, Interactor {

	/** TODO: Put your name in the window title */
	private String name = "Comp 557 Assignment 2 - Yiran Mao-260850827";
	
    /** Viewing mode as specified in the assignment */
    int viewingMode = 1;
        
    /** eye Z position in world coordinates */
    private DoubleParameter eyeZPosition = new DoubleParameter( "eye z", 0.5, 0.25, 3 ); 
    /** near plane Z position in world coordinates */
    private DoubleParameter nearZPosition = new DoubleParameter( "near z", 0.15, -0.2, 0.5 ); 
    /** far plane Z position in world coordinates */
    private DoubleParameter farZPosition  = new DoubleParameter( "far z", -0.5, -5, -0.25 ); 
    /** focal plane Z position in world coordinates */
    private DoubleParameter focalPlaneZPosition = new DoubleParameter( "focal z", 0, -1.5, 0.4 );     

    /** Samples for drawing depth of field blur */    
    private IntParameter samples = new IntParameter( "samples", 5, 1, 100 );   
    
    /** 
     * Aperture size for drawing depth of field blur
     * In the human eye, pupil diameter ranges between approximately 2 and 8 mm
     */
    private DoubleParameter aperture = new DoubleParameter( "aperture size", 0.003, 0, 0.01 );
    
    /** x eye offsets for testing (see objective 4) */         
    private DoubleParameter eyeXOffset = new DoubleParameter("eye offset in x", 0.0, -0.3, 0.3);
    /** y eye offsets for testing (see objective 4) */
    private DoubleParameter eyeYOffset = new DoubleParameter("eye offset in y", 0.0, -0.3, 0.3);
    
    private BooleanParameter drawCenterEyeFrustum = new BooleanParameter( "draw center eye frustum", true );    
    
    private BooleanParameter drawEyeFrustums = new BooleanParameter( "draw left and right eye frustums", true );
    
	/**
	 * The eye disparity should be constant, but can be adjusted to test the
	 * creation of left and right eye frustums or likewise, can be adjusted for
	 * your own eyes!! Note that 63 mm is a good inter occular distance for the
	 * average human, but you may likewise want to lower this to reduce the
	 * depth effect (images may be hard to fuse with cheap 3D colour filter
	 * glasses). Setting the disparity negative should help you check if you
	 * have your left and right eyes reversed!
	 */
    private DoubleParameter eyeDisparity = new DoubleParameter("eye disparity", 0.063, -0.1, 0.1 );

    private GLUT glut = new GLUT();
    
    private GLU glu = new GLU();
    
    private Scene scene = new Scene();
    
    public FastPoissonDisk fpd = new FastPoissonDisk();
    /**
     * Launches the application
     * @param args
     */
    public static void main(String[] args) {
        new A2App();
    }
    
    GLCanvas glCanvas;
    
    /** Main trackball for viewing the world and the two eye frustums */
    TrackBallCamera tbc = new TrackBallCamera();
    /** Second trackball for rotating the scene */
    TrackBallCamera tbc2 = new TrackBallCamera();
    
    /**
     * Creates the application
     */
    public A2App() {      
        Dimension controlSize = new Dimension(640, 640);
        Dimension size = new Dimension(640, 480);
        ControlFrame controlFrame = new ControlFrame("Controls");
        controlFrame.add("Camera", tbc.getControls());
        controlFrame.add("Scene TrackBall", tbc2.getControls());
        controlFrame.add("Scene", getControls());
        controlFrame.setSelectedTab("Scene");
        controlFrame.setSize(controlSize.width, controlSize.height);
        controlFrame.setLocation(size.width + 20, 0);
        controlFrame.setVisible(true);    
        GLProfile glp = GLProfile.getDefault();
        GLCapabilities glc = new GLCapabilities(glp);
        glCanvas = new GLCanvas( glc );
        glCanvas.setSize( size.width, size.height );
        glCanvas.setIgnoreRepaint( true );
        glCanvas.addGLEventListener( this );
        glCanvas.requestFocus();
        FPSAnimator animator = new FPSAnimator( glCanvas, 60 );
        animator.start();        
        tbc.attach( glCanvas );
        tbc2.attach( glCanvas );
        // initially disable second trackball, and improve default parameters given our intended use
        tbc2.enable(false);
        tbc2.setFocalDistance( 0 );
        tbc2.panRate.setValue(5e-5);
        tbc2.advanceRate.setValue(0.005);
        this.attach( glCanvas );        
        JFrame frame = new JFrame( name );
        frame.getContentPane().setLayout( new BorderLayout() );
        frame.getContentPane().add( glCanvas, BorderLayout.CENTER );
        frame.setLocation(0,0);        
        frame.addWindowListener( new WindowAdapter() {
            @Override
            public void windowClosing( WindowEvent e ) {
                System.exit(0);
            }
        });
        frame.pack();
        frame.setVisible( true );        
    }
    
    @Override
    public void dispose(GLAutoDrawable drawable) {
    	// nothing to do
    }
        
    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        // do nothing
    }
    
    @Override
    public void attach(Component component) {
        component.addKeyListener(new KeyAdapter() {
            @Override
            
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() >= KeyEvent.VK_1 && e.getKeyCode() <= KeyEvent.VK_7) {
                    viewingMode = e.getKeyCode() - KeyEvent.VK_1 + 1;
                    System.out.println(viewingMode);
                }
                System.out.println("hahaha");
                // only use the tbc trackball camera when in view mode 1 to see the world from
                // first person view, while leave it disabled and use tbc2 ONLY FOR ROTATION when
                // viewing in all other modes
                if ( viewingMode == 1 ) {
                	tbc.enable(true);
                	tbc2.enable(false);
	            } else {
                	tbc.enable(false);
                	tbc2.enable(true);
	            }
            }
        });
        component.setFocusable(true);
    }
    
    /**
     * @return a control panel
     */
    public JPanel getControls() {     
        VerticalFlowPanel vfp = new VerticalFlowPanel();
        
        VerticalFlowPanel vfp2 = new VerticalFlowPanel();
        vfp2.setBorder(new TitledBorder("Z Positions in WORLD") );
        vfp2.add( eyeZPosition.getSliderControls(false));        
        vfp2.add( nearZPosition.getSliderControls(false));
        vfp2.add( farZPosition.getSliderControls(false));        
        vfp2.add( focalPlaneZPosition.getSliderControls(false));     
        vfp.add( vfp2.getPanel() );
        
        vfp.add ( drawCenterEyeFrustum.getControls() );
        vfp.add ( drawEyeFrustums.getControls() );        
        vfp.add( eyeXOffset.getSliderControls(false ) );
        vfp.add( eyeYOffset.getSliderControls(false ) );        
        vfp.add ( aperture.getSliderControls(false) );
        vfp.add ( samples.getSliderControls() );        
        vfp.add( eyeDisparity.getSliderControls(false) );
        VerticalFlowPanel vfp3 = new VerticalFlowPanel();
        vfp3.setBorder( new TitledBorder("Scene size and position" ));
        vfp3.add( scene.getControls() );
        vfp.add( vfp3.getPanel() );        
        return vfp.getPanel();
    }
             
    public void init( GLAutoDrawable drawable ) {
    	drawable.setGL( new DebugGL2( drawable.getGL().getGL2() ) );
        GL2 gl = drawable.getGL().getGL2();
        gl.glShadeModel(GL2.GL_SMOOTH);             // Enable Smooth Shading
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);    // Black Background
        gl.glClearDepth(1.0f);                      // Depth Buffer Setup
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        gl.glEnable(GL.GL_LINE_SMOOTH);
        gl.glEnable(GL2.GL_POINT_SMOOTH);
        gl.glEnable(GL2.GL_NORMALIZE );
        gl.glEnable(GL.GL_DEPTH_TEST);              // Enables Depth Testing
        gl.glDepthFunc(GL.GL_LEQUAL);               // The Type Of Depth Testing To Do 
        gl.glLineWidth( 2 );                        // slightly fatter lines by default!
       
        
    }   

	// TODO: Objective 1 - adjust for your screen resolution and dimension to something reasonable.
	double screenWidthPixels = 1920;
	double screenWidthMeters = 0.34;
	double metersPerPixel = screenWidthMeters / screenWidthPixels;
  
	
	
    @Override
    public void display(GLAutoDrawable drawable) {        
        GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);            

        double w = drawable.getSurfaceWidth() * metersPerPixel;
        double h = drawable.getSurfaceHeight() * metersPerPixel;  

        
        
        // Compute the left,right,top and bottom for the grey rectangle(focal plane).
        
        double left_g = -w/2/(eyeZPosition.getValue())*(eyeZPosition.getValue() - focalPlaneZPosition.getValue());
        double right_g = w/2/(eyeZPosition.getValue())*(eyeZPosition.getValue() - focalPlaneZPosition.getValue());;
        double top_g = h/2/(eyeZPosition.getValue())*(eyeZPosition.getValue() - focalPlaneZPosition.getValue());
        double bottom_g = -h/2/(eyeZPosition.getValue())*(eyeZPosition.getValue() - focalPlaneZPosition.getValue());
        

        // Compute the left,right,top and bottom for the grey rectangle(focal plane) for the left eye.
        double left_g_l = (-w/2 -(-eyeDisparity.getValue()/2))/(eyeZPosition.getValue())*(eyeZPosition.getValue() - focalPlaneZPosition.getValue()) + (-eyeDisparity.getValue()/2);
        double right_g_l = (w/2-(-eyeDisparity.getValue()/2))/(eyeZPosition.getValue())*(eyeZPosition.getValue() - focalPlaneZPosition.getValue())+ (-eyeDisparity.getValue()/2);
        double top_g_l = h/2/(eyeZPosition.getValue())*(eyeZPosition.getValue() - focalPlaneZPosition.getValue());
        double bottom_g_l = -h/2/(eyeZPosition.getValue())*(eyeZPosition.getValue() - focalPlaneZPosition.getValue());
       

        // Compute the left,right,top and bottom for the grey rectangle(focal plane) for the right eye.
        double left_g_r = (-w/2 -(eyeDisparity.getValue()/2))/(eyeZPosition.getValue())*(eyeZPosition.getValue() - focalPlaneZPosition.getValue()) + (eyeDisparity.getValue()/2);
        double right_g_r = (w/2-(eyeDisparity.getValue()/2))/(eyeZPosition.getValue())*(eyeZPosition.getValue() - focalPlaneZPosition.getValue())+ (eyeDisparity.getValue()/2);
        double top_g_r = h/2/(eyeZPosition.getValue())*(eyeZPosition.getValue() - focalPlaneZPosition.getValue());
        double bottom_g_r = -h/2/(eyeZPosition.getValue())*(eyeZPosition.getValue() - focalPlaneZPosition.getValue());
        
        // Now draw the yellow rectangle
        gl.glDisable(GL2.GL_LIGHTING);
        gl.glBegin(GL2.GL_LINE_LOOP);
        gl.glColor3f(1f, 1f, 0.0f);
        
        gl.glVertex3d(-w/2, -h/2, 0);
        gl.glVertex3d(-w/2, h/2, 0);
        gl.glVertex3d(w/2, h/2, 0);
        gl.glVertex3d(w/2, -h/2, 0);
        
        gl.glEnd();
        gl.glEnable(GL2.GL_LIGHTING);
        
        
     
       
        
        if ( viewingMode == 1 ) {
        	// We will use a trackball camera, but also apply an 
        	// arbitrary scale to make the scene and frustums a bit easier to see
        	// (note the extra scale could have been part of the initializaiton of
        	// the tbc track ball camera, but this is eaiser)
           
        	
            tbc.prepareForDisplay(drawable);
            gl.glScaled(15,15,15);        
            
            gl.glPushMatrix();
            tbc2.applyViewTransformation(drawable); // only the view transformation
            scene.display( drawable );
            gl.glPopMatrix();
         
            // TODO: Objective 2 - draw camera frustum if drawCenterEyeFrustum is true
            if (drawCenterEyeFrustum.getValue() == true) {
            	
            	//Now draw the grey focal plane for the center eye
            	gl.glPushMatrix();
                gl.glDisable(GL2.GL_LIGHTING);
                gl.glTranslated(0, 0, focalPlaneZPosition.getValue());
                gl.glBegin(GL2.GL_LINE_LOOP);
                gl.glColor3f(0.5f, 0.5f, 0.5f);
                
                gl.glVertex3d(left_g,bottom_g, 0);
                gl.glVertex3d(left_g,top_g, 0);
                gl.glVertex3d(right_g,top_g, 0);
                gl.glVertex3d(right_g,bottom_g, 0);
                gl.glEnd();
                gl.glEnable(GL2.GL_LIGHTING);
                gl.glPopMatrix();
                
                
                
            	//Now draw the eye sphere:

            	gl.glPushMatrix();
            	gl.glDisable(GL2.GL_LIGHTING);
                gl.glColor3f(1.0f, 1.0f, 1.0f);
                gl.glTranslated(eyeXOffset.getValue(), eyeYOffset.getValue(), eyeZPosition.getValue());
                glut.glutSolidSphere(0.0125f,32,32);
                gl.glEnable(GL2.GL_LIGHTING);
                gl.glPopMatrix();
                
                
            
                FlatMatrix4d P = new FlatMatrix4d();
             	FlatMatrix4d Pinv = new FlatMatrix4d();
                // compute the left, right, top and bottom of near plane.
            	// opject 2 and 4
            	double r = (eyeZPosition.getValue() - nearZPosition.getValue())/(eyeZPosition.getValue() - focalPlaneZPosition.getValue());
            	double left = r*left_g - r*eyeXOffset.getValue();
                double right = r*right_g - r*eyeXOffset.getValue();
                double top = r*top_g - r*eyeYOffset.getValue();
                double bottom = r*bottom_g - r*eyeYOffset.getValue();
                
                
            	gl.glMatrixMode(GL2.GL_PROJECTION);
            	gl.glPushMatrix();
            	gl.glLoadIdentity();
            	
            	// Set the frustum
            	gl.glFrustum(left, right, bottom, top, eyeZPosition.getValue() - nearZPosition.getValue(),eyeZPosition.getValue() - farZPosition.getValue());
                gl.glGetDoublev(GL2.GL_PROJECTION_MATRIX, P.asArray(), 0);   
             
        		P.reconstitute();
        		Pinv.getBackingMatrix().invert(P.getBackingMatrix());
                gl.glPopMatrix();
                
                
                gl.glMatrixMode(GL2.GL_MODELVIEW);
                gl.glPushMatrix();
                
        		gl.glTranslated(0, 0, eyeZPosition.getValue());	
        		gl.glTranslated(eyeXOffset.getValue(), eyeYOffset.getValue(), 0);
        		gl.glMultMatrixd( Pinv.asArray(), 0 );    
        		
        		gl.glDisable(GL2.GL_LIGHTING);	
        	    gl.glColor3f(1,1,1);
           		glut.glutWireCube(2);
           		gl.glPopMatrix();
           		gl.glEnable(GL2.GL_LIGHTING);
            }
            
            
            
            if (drawEyeFrustums.getValue() == true) {
            	
        	 
            	//Draw the focal plane for left eye.
              	gl.glPushMatrix();
                gl.glDisable(GL2.GL_LIGHTING);
                gl.glTranslated(0, 0, focalPlaneZPosition.getValue());
                gl.glBegin(GL2.GL_LINE_LOOP);
                gl.glColor3f(0.5f, 0.5f, 0.5f);
                
                gl.glVertex3d(left_g_l,bottom_g_l, 0);
                gl.glVertex3d(left_g_l,top_g_l, 0);
                gl.glVertex3d(right_g_l,top_g_l, 0);
                gl.glVertex3d(right_g_l,bottom_g_l, 0);
                gl.glEnd();
                gl.glEnable(GL2.GL_LIGHTING);
                gl.glPopMatrix();
                
               
              //Draw the focal plane for right eye.
              	gl.glPushMatrix();
                gl.glDisable(GL2.GL_LIGHTING);
                gl.glTranslated(0, 0, focalPlaneZPosition.getValue());
                gl.glBegin(GL2.GL_LINE_LOOP);
                gl.glColor3f(0.5f, 0.5f, 0.5f);
                
                gl.glVertex3d(left_g_r,bottom_g_r, 0);
                gl.glVertex3d(left_g_r,top_g_r, 0);
                gl.glVertex3d(right_g_r,top_g_r, 0);
                gl.glVertex3d(right_g_r,bottom_g_r, 0);
                gl.glEnd();
                gl.glEnable(GL2.GL_LIGHTING);
                gl.glPopMatrix();
            	
            	//Draw the left eye and right eye
            	   
            	gl.glPushMatrix();
            	gl.glDisable(GL2.GL_LIGHTING);
                gl.glColor3f(1.0f, 0, 0);
                gl.glTranslated(-eyeDisparity.getValue()/2, 0, eyeZPosition.getValue());
                glut.glutSolidSphere(0.0125f,32,32);
                gl.glEnable(GL2.GL_LIGHTING);
                gl.glPopMatrix();
                
                
            	gl.glPushMatrix();
            	gl.glDisable(GL2.GL_LIGHTING);
                gl.glColor3f(0, 1, 1);
                gl.glTranslated(eyeDisparity.getValue()/2, 0, eyeZPosition.getValue());
                glut.glutSolidSphere(0.0125f,32,32);
                gl.glEnable(GL2.GL_LIGHTING);
                gl.glPopMatrix();
              
                
                // Draw the frustum of left and right eye
            	
                FlatMatrix4d P_l = new FlatMatrix4d();
                FlatMatrix4d Pinv_l = new FlatMatrix4d();
                FlatMatrix4d P_r = new FlatMatrix4d();
            	FlatMatrix4d Pinv_r = new FlatMatrix4d();
            	
            	double r = (eyeZPosition.getValue() - nearZPosition.getValue())/eyeZPosition.getValue();
            	double left_l = r*(-w/2) + r*eyeDisparity.getValue()/2;
                double right_l = r*(w/2) + r*eyeDisparity.getValue()/2;
                double top_l = r*(h/2);  
                double bottom_l = r*(-h/2); 
                
            	
            	double left_r = r*(-w/2) - r*eyeDisparity.getValue()/2;
                double right_r = r*w/2 - r*eyeDisparity.getValue()/2;
                double top_r = r*h/2;  
                double bottom_r = r*(-h/2); 
                
            	gl.glMatrixMode(GL2.GL_PROJECTION);
            	gl.glPushMatrix();
            	gl.glLoadIdentity();
            	
            	// Set the frustum of left eye
            	gl.glFrustum(left_l, right_l, bottom_l, top_l, eyeZPosition.getValue() - nearZPosition.getValue(),eyeZPosition.getValue() - farZPosition.getValue());
                gl.glGetDoublev(GL2.GL_PROJECTION_MATRIX, P_l.asArray(), 0);   
             
        		P_l.reconstitute();
        		Pinv_l.getBackingMatrix().invert(P_l.getBackingMatrix());
                gl.glPopMatrix();
                
                
                gl.glMatrixMode(GL2.GL_MODELVIEW);
                gl.glPushMatrix();
                
        		gl.glTranslated(0, 0, eyeZPosition.getValue());	
        		gl.glTranslated(-eyeDisparity.getValue()/2, 0, 0);
        		gl.glMultMatrixd( Pinv_l.asArray(), 0 );    
        		
        		gl.glDisable(GL2.GL_LIGHTING);	
        	    gl.glColor3f(1,0,0);
           		glut.glutWireCube(2);
           		gl.glEnable(GL2.GL_LIGHTING);
           		gl.glPopMatrix();
           		
           		gl.glMatrixMode(GL2.GL_PROJECTION);
            	gl.glPushMatrix();
            	gl.glLoadIdentity();
            	
            	// Set the frustum of right eye
            	gl.glFrustum(left_r, right_r, bottom_r, top_r, eyeZPosition.getValue() - nearZPosition.getValue(),eyeZPosition.getValue() - farZPosition.getValue());
                gl.glGetDoublev(GL2.GL_PROJECTION_MATRIX, P_r.asArray(), 0);   
             
        		P_r.reconstitute();
        		Pinv_r.getBackingMatrix().invert(P_r.getBackingMatrix());
                gl.glPopMatrix();
                
                
                gl.glMatrixMode(GL2.GL_MODELVIEW);
                gl.glPushMatrix();
                
        		gl.glTranslated(0, 0, eyeZPosition.getValue());	
        		gl.glTranslated(eyeDisparity.getValue()/2, 0, 0);
        		gl.glMultMatrixd( Pinv_r.asArray(), 0 );    
        		
        		gl.glDisable(GL2.GL_LIGHTING);	
        	    gl.glColor3f(0,1,1);
           		glut.glutWireCube(2);
           		gl.glEnable(GL2.GL_LIGHTING);
           		gl.glPopMatrix();
            	    
                }
                // TODO: Objective 6 - draw left and right eye frustums if drawEyeFrustums is true
            
        } else if ( viewingMode == 2 ) {
        	// TODO: Objective 2 - draw the center eye camera view
        	// the result of center eye image
        	tbc.enable(false);
        	double r = (eyeZPosition.getValue() - nearZPosition.getValue())/(eyeZPosition.getValue() - focalPlaneZPosition.getValue());
        	double left = r*left_g - r*eyeXOffset.getValue();
            double right = r*right_g - r*eyeXOffset.getValue();
            double top = r*top_g - r*eyeYOffset.getValue();
            double bottom = r*bottom_g - r*eyeYOffset.getValue();
            
      
        	gl.glMatrixMode(GL2.GL_PROJECTION);
        	gl.glPushMatrix();
        	gl.glLoadIdentity();
        	gl.glFrustum(left, right, bottom, top, eyeZPosition.getValue() - nearZPosition.getValue(),eyeZPosition.getValue() - farZPosition.getValue());
            
            
        	
            gl.glMatrixMode(GL2.GL_MODELVIEW);
            gl.glPushMatrix();          
            gl.glLoadIdentity();
            glu.gluLookAt( eyeXOffset.getValue(), eyeYOffset.getValue(), eyeZPosition.getValue(), 0, 0, -5, 0, 1, 0 );
     
     		scene.display( drawable );
     		gl.glPopMatrix();
     				
     		gl.glMatrixMode(GL2.GL_PROJECTION);
        	gl.glPopMatrix();
        	
        	
        	
        	
        } else if ( viewingMode == 3 ) {            
        	
        	// TODO: Objective 5 - draw center eye with depth of field blur
      
            IntParameter n = samples;	
    		
    		
    		for ( int i = 0; i < n.getValue(); i++ ){
    			    Point2d p = new Point2d();
    			    fpd.get( p, i, n.getValue() );
    			    p.scale(aperture.getValue());
    				
    				double r = (eyeZPosition.getValue() - nearZPosition.getValue())/(eyeZPosition.getValue() - focalPlaneZPosition.getValue());
        	        double left = r*left_g - r*p.x;
                    double right = r*right_g - r*p.x;
                    double top = r*top_g - r*p.y;
                    double bottom = r*bottom_g - r*p.y;
                    
                    gl.glMatrixMode(GL2.GL_PROJECTION);
                	gl.glPushMatrix();
                	gl.glLoadIdentity();
                	gl.glFrustum(left, right, bottom, top, eyeZPosition.getValue() - nearZPosition.getValue(),eyeZPosition.getValue() - farZPosition.getValue());
                    
                    gl.glMatrixMode(GL2.GL_MODELVIEW);
                    gl.glPushMatrix();          
                    gl.glLoadIdentity();
                    glu.gluLookAt( p.x, p.y, eyeZPosition.getValue(), 0, 0, -5, 0, 1, 0 );
             	    gl.glClear(GL2.GL_DEPTH_BUFFER_BIT);  
             		scene.display( drawable );
             		
                	if (i==0)
                	    gl.glAccum(GL2.GL_LOAD, 1f/n.getValue());
                	else
                		gl.glAccum(GL2.GL_ACCUM, 1f/n.getValue());
                	gl.glPopMatrix();
                	   
                	gl.glMatrixMode(GL2.GL_PROJECTION);
                	gl.glPopMatrix();      	
    			}
    	   
    		gl.glAccum( GL2.GL_RETURN, 1 );
    		
        	
        	
            
        } else if ( viewingMode == 4 ) {
        	
            // TODO: Objective 6 - draw the left eye view
        	 	
        	
        	//compute the left,right,top and bottom of near plane
        	double r = (eyeZPosition.getValue() - nearZPosition.getValue())/eyeZPosition.getValue() ;
        	double left_l = r*(-w/2) + r*eyeDisparity.getValue()/2;
            double right_l = r*(w/2) + r*eyeDisparity.getValue()/2;
            double top_l = r*(h/2);  
            double bottom_l = r*(-h/2); 
            
      
        	gl.glMatrixMode(GL2.GL_PROJECTION);
        	gl.glPushMatrix();
        	gl.glLoadIdentity();
        	gl.glFrustum(left_l, right_l, bottom_l, top_l, eyeZPosition.getValue() - nearZPosition.getValue(),eyeZPosition.getValue() - farZPosition.getValue());
            
            
        	
            gl.glMatrixMode(GL2.GL_MODELVIEW);
            gl.glPushMatrix();          
            gl.glLoadIdentity();
            glu.gluLookAt( -eyeDisparity.getValue()/2, 0, eyeZPosition.getValue(), -eyeDisparity.getValue()/2, 0, -5, 0, 1, 0 );
     
     		scene.display( drawable );
     		gl.glPopMatrix();
     				
     		gl.glMatrixMode(GL2.GL_PROJECTION);
        	gl.glPopMatrix();
        	
        	
        } else if ( viewingMode == 5 ) {  
        	
        	// TODO: Objective 6 - draw the right eye view
        	
        	// compute the left,right,top and bottom of near plane
        	double r = (eyeZPosition.getValue() - nearZPosition.getValue())/eyeZPosition.getValue();
        	double left_r = r*(-w/2) - r*eyeDisparity.getValue()/2;
            double right_r = r*w/2 - r*eyeDisparity.getValue()/2;
            double top_r = r*h/2;  
            double bottom_r = r*(-h/2); 
            
            
      
        	gl.glMatrixMode(GL2.GL_PROJECTION);
        	gl.glPushMatrix();
        	gl.glLoadIdentity();
        	gl.glFrustum(left_r, right_r, bottom_r, top_r, eyeZPosition.getValue() - nearZPosition.getValue(),eyeZPosition.getValue() - farZPosition.getValue());
            
            
        	
            gl.glMatrixMode(GL2.GL_MODELVIEW);
            gl.glPushMatrix();          
            gl.glLoadIdentity();
            glu.gluLookAt( eyeDisparity.getValue()/2, 0, eyeZPosition.getValue(), eyeDisparity.getValue()/2, 0, -5, 0, 1, 0 );
     
     		scene.display( drawable );
     		gl.glPopMatrix();
     				
     		gl.glMatrixMode(GL2.GL_PROJECTION);
        	gl.glPopMatrix();
        	                               
        } else if ( viewingMode == 6 ) {            
        	
        	// TODO: Objective 7 - draw the anaglyph view using glColouMask
        	// the left image
        	
        	gl.glColorMask( true, false, false, true );
        	gl.glClear( GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT );
        	
        	tbc.enable(false);  	
           	double r = (eyeZPosition.getValue() - nearZPosition.getValue())/eyeZPosition.getValue() ;
        	double left_l = r*(-w/2) + r*eyeDisparity.getValue()/2;
            double right_l = r*(w/2) + r*eyeDisparity.getValue()/2;
            double top_l = r*(h/2);  
            double bottom_l = r*(-h/2); 
            
      
        	gl.glMatrixMode(GL2.GL_PROJECTION);
        	gl.glPushMatrix();
        	gl.glLoadIdentity();
        	gl.glFrustum(left_l, right_l, bottom_l, top_l, eyeZPosition.getValue() - nearZPosition.getValue(),eyeZPosition.getValue() - farZPosition.getValue());
            
            
        	
            gl.glMatrixMode(GL2.GL_MODELVIEW);
            gl.glPushMatrix();          
            gl.glLoadIdentity();
            glu.gluLookAt( -eyeDisparity.getValue()/2, 0, eyeZPosition.getValue(), -eyeDisparity.getValue()/2, 0, -5, 0, 1, 0 );
     
     		scene.display( drawable );
     		gl.glPopMatrix();
     				
     		gl.glMatrixMode(GL2.GL_PROJECTION);
        	gl.glPopMatrix();
        	
        	//  draw the right eye view
        	gl.glColorMask( false, true, true, true );
        	gl.glClear( GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT );
        	
        	tbc.enable(false);  	
        	
        	double left_r = r*(-w/2) - r*eyeDisparity.getValue()/2;
            double right_r = r*w/2 - r*eyeDisparity.getValue()/2;
            double top_r = r*h/2;  
            double bottom_r = r*(-h/2); 
            
      
        	gl.glMatrixMode(GL2.GL_PROJECTION);
        	gl.glPushMatrix();
        	gl.glLoadIdentity();
        	gl.glFrustum(left_r, right_r, bottom_r, top_r, eyeZPosition.getValue() - nearZPosition.getValue(),eyeZPosition.getValue() - farZPosition.getValue());
            
            
        	
            gl.glMatrixMode(GL2.GL_MODELVIEW);
            gl.glPushMatrix();          
            gl.glLoadIdentity();
            glu.gluLookAt( eyeDisparity.getValue()/2, 0, eyeZPosition.getValue(), eyeDisparity.getValue()/2, 0, -5, 0, 1, 0 );
     
     		scene.display( drawable );
     		gl.glPopMatrix();
     				
     		gl.glMatrixMode(GL2.GL_PROJECTION);
        	gl.glPopMatrix();
        	
        	gl.glColorMask( true, true, true, false );
        	
        	
        } else if ( viewingMode == 7 ) {            
        	
        	// TODO: Bonus Ojbective 8 - draw the anaglyph view with depth of field blur
            IntParameter n = samples;	
            
            //blur of left eye
    		
            gl.glColorMask( true, false, false, true );
        	gl.glClear( GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT );
        	
    		for ( int i = 0; i < n.getValue(); i++ ){
    			    Point2d p = new Point2d();
    			    fpd.get( p, i, n.getValue() );
    			    p.scale(aperture.getValue());
    			    
    				// contain the left focal plane
    				double r = (eyeZPosition.getValue() - nearZPosition.getValue())/(eyeZPosition.getValue() - focalPlaneZPosition.getValue());
        	        double left = r*left_g_l - r*(p.x + (-eyeDisparity.getValue()/2));
                    double right = r*right_g_l - r*(p.x + (-eyeDisparity.getValue()/2)); 
                    double top = r*top_g_l - r*p.y;
                    double bottom = r*bottom_g_l - r*p.y;
                    
                    gl.glMatrixMode(GL2.GL_PROJECTION);
                	gl.glPushMatrix();
                	gl.glLoadIdentity();
                	gl.glFrustum(left, right, bottom, top, eyeZPosition.getValue() - nearZPosition.getValue(),eyeZPosition.getValue() - farZPosition.getValue());
                    
                    gl.glMatrixMode(GL2.GL_MODELVIEW);
                    gl.glPushMatrix();          
                    gl.glLoadIdentity();
                    glu.gluLookAt( p.x - eyeDisparity.getValue()/2, p.y, eyeZPosition.getValue(), 0, 0, -5, 0, 1, 0 );
             	    gl.glClear(GL2.GL_DEPTH_BUFFER_BIT);  
             		scene.display( drawable );
             		
                	if (i==0)
                	    gl.glAccum(GL2.GL_LOAD, 1f/n.getValue());
                	else
                		gl.glAccum(GL2.GL_ACCUM, 1f/n.getValue());
                	gl.glPopMatrix();
                	   
                	gl.glMatrixMode(GL2.GL_PROJECTION);
                	gl.glPopMatrix();      	
    			}
    	   
    		gl.glAccum( GL2.GL_RETURN, 1 );
    		
    		
    		

            //blur of right eye
    		
    		gl.glColorMask( false, true, true, true );
        	gl.glClear( GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT );
        	
    		for ( int i = 0; i < n.getValue(); i++ ){
			    Point2d p = new Point2d();
			    fpd.get( p, i, n.getValue() );
			    p.scale(aperture.getValue());
			 
				// contain the right focal plane
				double r = (eyeZPosition.getValue() - nearZPosition.getValue())/(eyeZPosition.getValue() - focalPlaneZPosition.getValue());
    	        double left = r*left_g_r - r*(p.x + eyeDisparity.getValue()/2);
                double right = r*right_g_r - r*(p.x + eyeDisparity.getValue()/2); 
                double top = r*top_g_r - r*p.y;
                double bottom = r*bottom_g_r - r*p.y;
                
                gl.glMatrixMode(GL2.GL_PROJECTION);
            	gl.glPushMatrix();
            	gl.glLoadIdentity();
            	gl.glFrustum(left, right, bottom, top, eyeZPosition.getValue() - nearZPosition.getValue(),eyeZPosition.getValue() - farZPosition.getValue());
                
                gl.glMatrixMode(GL2.GL_MODELVIEW);
                gl.glPushMatrix();          
                gl.glLoadIdentity();
                glu.gluLookAt( p.x + eyeDisparity.getValue()/2, p.y, eyeZPosition.getValue(), 0, 0, -5, 0, 1, 0 );
         	    gl.glClear(GL2.GL_DEPTH_BUFFER_BIT);  
         		scene.display( drawable );
         		
            	if (i==0)
            	    gl.glAccum(GL2.GL_LOAD, 1f/n.getValue());
            	else
            		gl.glAccum(GL2.GL_ACCUM, 1f/n.getValue());
            	gl.glPopMatrix();
            	   
            	gl.glMatrixMode(GL2.GL_PROJECTION);
            	gl.glPopMatrix();      	
			}
	   
		gl.glAccum( GL2.GL_RETURN, 1 );

    	gl.glColorMask( true, true, true, false );
        	
        }        
    }
    
}
