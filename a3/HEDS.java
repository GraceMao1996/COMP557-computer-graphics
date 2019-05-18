package comp557.a3;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;



import javax.vecmath.Matrix4d;
import javax.vecmath.Matrix3d;


/**
 * Half edge data structure.
 * Maintains a list of faces (i.e., one half edge of each) to allow
 * for easy display of geometry.
 */
public class HEDS {

    /** List of faces */
    Set<Face> faces = new HashSet<Face>();
    double regularizationWeight;
    
    Queue<Edge> priorityqueue = new PriorityQueue<Edge>();
    /**
     * Constructs an empty mesh (used when building a mesh with subdivision)
     */
    public HEDS() {
        // do nothing
    }
        
    /**
     * Builds a half edge data structure from the polygon soup   
     * @param soup
     */
    
    public HEDS( PolygonSoup soup ,double weight ) {
    	regularizationWeight = weight;
        halfEdges.clear();
        faces.clear();
        
        // TODO: Objective 1: create the half edge data structure from the polygon soup    
        for(int[] face: soup.faceList) {
        	HalfEdge first = new HalfEdge();
        	HalfEdge he = new HalfEdge();
        	int i = face[0];
    	    int j = face[1];
    	    first.head = soup.vertexList.get(j);
    	    he = first;
    	    String Ename = i+","+j;
    		halfEdges.put(Ename, he); 
        // put all half edges into map halfEdges; find the next for every half edge;
    		
        	for(int index=1; index <face.length; index++) {
                i = face[index];
        	    j = face[(index+1)%3];
        	    
        	    HalfEdge next = new HalfEdge();
        	    next.head = soup.vertexList.get(j);
        	    he.next = next;
        	    he = next;
        	    
        	    Ename = i+","+j;
        	   
        		halfEdges.put(Ename, he); 
        	} 
        	he.next = first;
        	faces.add(new Face(he));
        }   
        
        // match up the twin for every halfEdge in map halfEdges;
        	Iterator<Entry<String, HalfEdge>> it = halfEdges.entrySet().iterator();
        	while(it.hasNext()){
        		Map.Entry<String, HalfEdge> entry = it.next();
        		HalfEdge he = entry.getValue();
        		String name = entry.getKey();
        		int k = name.indexOf(",");
        		String tname = name.substring(k+1)+","+name.substring(0,k);
        		
        		if (halfEdges.get(tname) != null) {    			
        			he.twin = halfEdges.get(tname);
        			he.twin.twin = he;
        			Edge e = new Edge();
        			he.e = e;
        			he.twin.e = e;
       			    e.he = he;
        		}
        		it.remove();
        	}
        CreatePriorityQueue();
      /*  while(!priorityqueue.isEmpty()) System.out.println(priorityqueue.remove().error);*/
    
        	
        // TODO: Objective 5: fill your priority queue on load
        
    }
    
    public void setRegularizationWeight(double weight) {
    	regularizationWeight = weight;
    }
    /**
     * You might want to use this to match up half edges... 
     */
    Map<String,HalfEdge> halfEdges = new TreeMap<String,HalfEdge>();
   
    
    // TODO: Objective 2, 3, 4, 5: write methods to help with collapse, and for checking topological problems
    
    //objective 2: collapse an edge;
    public void EdgeCollapse(HalfEdge he) {
    	while(!redoListHalfEdge.isEmpty()) {
    		System.out.println("redocollapse the edges in redolist");
    		redoCollapse();                               //if redolist is not empty, redo the edges in the redolist
    	}
    	if (!isCollapsible(he)) {
			System.out.println("This edge is not collapsible");
			return;
		}
   /* 	Queue<Edge> afasfd = priorityqueue.clone();
    	afasfd.*/
    	System.out.println(he.e.error);
    	/*Vertex v1 = he.head;
    	Vertex v2 = he.twin.head;
    	Vertex mid = new Vertex();*/
    	Vertex v = new Vertex();
    	
    	/*mid.p.x = (v1.p.x + v2.p.x)/2;
    	mid.p.y = (v1.p.y + v2.p.y)/2;
    	mid.p.z = (v1.p.z + v2.p.z)/2;*/
    	//v = mid;  //objective 2
    	//objective 4
    	
      
    	HalfEdge A = he.next.twin;
    	HalfEdge B = he.next.next.twin;
    	HalfEdge C = he.twin.next.next.twin;
    	HalfEdge D = he.twin.next.twin;
    	A.twin = B;
    	B.twin = A;
    	C.twin = D;
    	D.twin = C;
    	
    	priorityqueue.remove(he.e);
    	priorityqueue.remove(he.next.e);
    	priorityqueue.remove(he.next.next.e);
    
    	priorityqueue.remove(he.twin.next.e);
    	priorityqueue.remove(he.twin.next.next.e);
    	findNewVertex(he); 
        v.p.x = he.e.v.x;
        v.p.y = he.e.v.y;
        v.p.z = he.e.v.z;
      	Edge e1 = new Edge();
    	Edge e2 = new Edge();
    	e1.he = A;
    	e2.he = C;
    	A.e = e1;
    	B.e = e1;
    	C.e = e2;
    	D.e = e2;
    	
    	
    	if(faces.contains(he.leftFace)){
			faces.remove(he.leftFace);  
		}
    	if(faces.contains(he.twin.leftFace)) {
    		faces.remove(he.twin.leftFace);
    	}
    	undoList.add(he);
    
    	HalfEdge loop = A;
    	do {
    		loop.head = v;              
    		loop = loop.next.twin;
    	}while(loop!=A);
    	
    	loop = A;
    	v.Q = he.e.Q;
    	do {  		
    		//VertexQ(loop);
    		//calculate new error for loop.e
    		findNewVertex(loop);
			EdgeError(loop);                      
    		loop = loop.next.twin;
    	}while(loop!=A);
    	
    	priorityqueue.add(e1);
		priorityqueue.add(e2);
  
    	return;
    	
    }
    
    
    // 1-ring vertex of a half edge
    private Set<Vertex> Ring (HalfEdge he) {
		HashSet<Vertex> set = new HashSet<>();
	    HalfEdge first = he;
		do{
			set.add(he.head);
			he = he.next.next.twin;
		} while (he!=first);

		return set;
	}
    
    
    //objective 3: check if it is collapsible
    public Boolean isCollapsible (HalfEdge he) {
		// Check if it is a tetrahedron
		if (faces.size() <=4) {
			return false;	
		}
		//Check the 1-rings
		Collection<Vertex> set1 = Ring(he);
		Collection<Vertex> set2 = Ring(he.twin);

		int common = 0;
		for ( Vertex v : set1){
			if (set2.contains(v)) {
				common++;
			}
		}   	
		if (common > 2 ){
			return false;	
		}
		// if the edge is collapsible
		else {
			return true;
		}
	}
    
    //objective 4: find the new vertex for edge collapse
    private void findNewVertex(HalfEdge he) {
    	Vertex v1 = he.head;
    	Vertex v2 = he.twin.head;
    	
    	Matrix4d Q = new Matrix4d();
    	Matrix4d Q_reg = new Matrix4d();
    	
    	Vertex mid = new Vertex();
    	
    	mid.p.x = (v1.p.x + v2.p.x)/2;
    	mid.p.y = (v1.p.y + v2.p.y)/2;
    	mid.p.z = (v1.p.z + v2.p.z)/2;
    	
    	//compute Q
		Q_reg.setIdentity();	

		Vector4d tmpV = new Vector4d(mid.p.x,mid.p.y,mid.p.z,mid.p.x*mid.p.x+mid.p.y*mid.p.y+mid.p.z*mid.p.z);
		Q_reg.setRow(3, tmpV);
		Q_reg.setColumn(3, tmpV);
		Q_reg.mul(regularizationWeight);
	
 
		
    	//calculate Q for he.e
    	Q.add(he.head.Q,he.twin.head.Q);//he.e.Q = Qi + Qj without Qreg
    	he.e.Q = Q;
		//Q.add(he.head.Q, he.twin.head.Q);
    	Q.add(Q_reg);
    	
    	
    	//compute the new vertex
    	Matrix3d A = new Matrix3d(Q.m00,Q.m01,Q.m02,Q.m10,Q.m11,Q.m12,Q.m20,Q.m21,Q.m22);
    	Vector3d b = new Vector3d(Q.m30,Q.m31,Q.m32);
  

    	A.invert();
    
    	
    	//calculate the Optimal vertex location on collapse of the edge
    	he.e.v.x = -A.m00*b.x-A.m01*b.y-A.m02*b.z;
    	he.e.v.y = -A.m10*b.x-A.m11*b.y-A.m12*b.z;
    	he.e.v.z = -A.m20*b.x-A.m21*b.y-A.m22*b.z;   
    	he.e.v.w = 1;
    
    }
    
  
    //calculate Q for he.head
    private void VertexQ(HalfEdge he) {
    	HalfEdge he1 = he.twin;
    	Matrix4d Q = new Matrix4d();
    	do {
    		Face face = he1.leftFace;
    		//Q.add(face.K);
    		Q.add(face.K);
    		face.recomputeNormal();
            he1 = he1.next.next.twin;
    	}while(he1!=he.twin);
    	he.head.Q = Q;
    	return;
    }
    
    
  //calculate the Error metric for this edge
    private void EdgeError(HalfEdge he) {
    	Vector4d V = he.e.v;
    	Matrix4d M1 = new Matrix4d();
		Vector4d V1 = new Vector4d();
		M1.setColumn(0, V); 
		M1.mul(he.e.Q, M1); 
		M1.getColumn(0, V1); 
		he.e.error = V.dot(V1);
    }
    

	/**
	 * Set Q for all the edges and adds it to the priority queue
	 */ 
	public void CreatePriorityQueue() {
		for ( Face face : faces ) {
			HalfEdge he = face.he;
			HalfEdge he1 = he;
			do {
				VertexQ(he1);
				VertexQ(he1.twin);
				findNewVertex(he1);
				EdgeError(he1) ;
				if (!priorityqueue.contains(he1.e)){
					priorityqueue.add(he1.e);
				}
				he1 = he1.next;
			} while ( he1 != he );
		}
	}
	
	
	public HalfEdge selectEdge() {
		while(!priorityqueue.isEmpty() &&  !isCollapsible(priorityqueue.peek().he)) {
				priorityqueue.remove();	
		}	
		if(priorityqueue.isEmpty()) return null;
		return priorityqueue.peek().he;
	}
    /**
	 * Need to know both verts before the collapse, but this information is actually 
	 * already stored within the excized portion of the half edge data structure.
	 * Thus, we only need to have a half edge (the collapsed half edge) to undo
	 */
	LinkedList<HalfEdge> undoList = new LinkedList<>();
	/**
	 * To redo an undone collapse, we must know which edge to collapse.  We should
	 * likewise reuse the Vertex that was created for the collapse.
	 */
	LinkedList<HalfEdge> redoListHalfEdge = new LinkedList<>();
	LinkedList<Vertex> redoListVertex = new LinkedList<>();

    void undoCollapse() {
    	if ( undoList.isEmpty() ) return; // ignore the request
    	// TODO: Objective 6: undo the last collapse
    	// be sure to put the information on the redo list so you can redo the collapse too!
        
    	HalfEdge he = undoList.removeLast();
    	HalfEdge A = he.next.twin;
    	HalfEdge B = he.next.next.twin;
    	HalfEdge C = he.twin.next.next.twin;
    	HalfEdge D = he.twin.next.twin;
    	
    	A.twin = he.next;
    	B.twin = he.next.next;
    	C.twin = he.twin.next.next;
    	D.twin = he.twin.next;
    	
    	HalfEdge loop = A;
    	do {
    		loop.head = he.head;
    		loop.leftFace.recomputeNormal();
    		loop = loop.next.twin;
    	}while(loop!= A);
    	
    	loop = D;
    	do {
    		loop.head = he.twin.head;
    		loop.leftFace.recomputeNormal();
    		loop=loop.next.twin;
    	}while(loop!=D);
    	
    	faces.add(he.leftFace);
    	faces.add(he.twin.leftFace);
    	
    	redoListHalfEdge.add(he);
    	redoListVertex.add(he.next.twin.head);
    	return;
    	
    }
    
    void redoCollapse() {
    	if ( redoListHalfEdge.isEmpty() ) return; // ignore the request
    	
    	HalfEdge he = redoListHalfEdge.removeLast();
    	Vertex v = redoListVertex.removeLast();
    
    	undoList.add( he );  // put this on the undo list so we can undo this collapse again
        
    	// TODO: Objective 7: redo the edge collapse!
    	HalfEdge A = he.next.twin;
    	HalfEdge B = he.next.next.twin;
    	HalfEdge C = he.twin.next.next.twin;
    	HalfEdge D = he.twin.next.twin;
    	A.twin = B;
    	B.twin = A;
    	C.twin = D;
    	D.twin = C;
    	if(faces.contains(he.leftFace)){
			faces.remove(he.leftFace);  
		}
    	if(faces.contains(he.twin.leftFace)) {
    		faces.remove(he.twin.leftFace);
    	}
    	
    	HalfEdge loop = A;
    	do {
    		loop.head = v;              
    		loop = loop.next.twin;
    	}while(loop!=A);
    	return;  	
    }
      
    /**
     * Draws the half edge data structure by drawing each of its faces.
     * Per vertex normals are used to draw the smooth surface when available,
     * otherwise a face normal is computed. 
     * @param drawable
     */
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();

        // we do not assume triangular faces here        
        Point3d p;
        Vector3d n;        
        for ( Face face : faces ) {
            HalfEdge he = face.he;
            gl.glBegin( GL2.GL_POLYGON );
            n = he.leftFace.n;
            gl.glNormal3d( n.x, n.y, n.z );
            HalfEdge e = he;
            do {
                p = e.head.p;
                gl.glVertex3d( p.x, p.y, p.z );
                e = e.next;
            } while ( e != he );
            gl.glEnd();
        }
    }

}