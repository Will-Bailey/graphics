package Supp;

import static com.jogamp.opengl.GL3.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.swing.JFrame;

import Basic.*;
import Objects.*;

java.awt.event.KeyListener

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.glu.GLU;

public class Renderer implements GLEventListener, KeyListener {

    private Transform T = new Transform(); //model_view transform

    //VAOs and VBOs parameters
    private int idPoint=0, numVAOs = 2;
    private int idBuffer=0, numVBOs = 2;
    private int idElement=0, numEBOs = 2;
    private int[] VAOs = new int[numVAOs];
    private int[] VBOs = new int[numVBOs];
    private int[] EBOs = new int[numEBOs];

    //Model parameters
    private int[] numElements = new int[numEBOs];
    private long vertexSize; 
    private int vPosition;
    
    //Transformation parameters
    private int ModelView;
    private int Projection; 

    @Override
    public void display(GLAutoDrawable drawable) {
        GL3 gl = drawable.getGL().getGL3(); // Get the GL pipeline object this 
        
        gl.glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);

        gl.glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        
        T.initialize();
        T.lookAt(0f, 0f, 0f, 0f, 0f, -100f, 0f, 1f, 0f);        

        gl.glUniformMatrix4fv( ModelView, 1, true, T.getTransformv(), 0 );          

        idPoint=0;
        idBuffer=0;
        idElement=0;
        bindObject(gl);
        gl.glDrawElements(GL_TRIANGLES, numElements[idElement], GL_UNSIGNED_INT, 0);    

    }

    
    @Override
    public void init(GLAutoDrawable drawable) {
        GL3 gl = drawable.getGL().getGL3(); // Get the GL pipeline object this 

        System.out.print("GL_Version: " + gl.glGetString(GL_VERSION));
        
        gl.glEnable(GL_CULL_FACE); 

        //compile and use the shader program
        ShaderProg shaderproc = new ShaderProg(gl, "TransProj.vert", "TransProj.frag");
        int program = shaderproc.getProgram();
        gl.glUseProgram(program);

        // Initialize the vertex position and normal attribute in the vertex shader    
        vPosition = gl.glGetAttribLocation( program, "vPosition" );
        gl.glGetAttribLocation( program, "vNormal" );

        // Get connected with the ModelView, NormalTransform, and Projection matrices
        // in the vertex shader
        ModelView = gl.glGetUniformLocation(program, "ModelView");
        Projection = gl.glGetUniformLocation(program, "Projection");

        
        // Generate VAOs, VBOs, and EBOs
        gl.glGenVertexArrays(numVAOs,VAOs,0);
        gl.glGenBuffers(numVBOs, VBOs,0);
        gl.glGenBuffers(numEBOs, EBOs,0);

        //create the first object: a teapot
        SObject teapot = new STeapot();
        idPoint=0;
        idBuffer=0;
        idElement=0;
        createObject(gl, teapot);

        //******************************************
        //create the second object: a teapot
        //using the STeapot class provided
                         
        // This is necessary. Otherwise, the The color on back face may display 
//          gl.glDepthFunc(GL_LESS);
        gl.glEnable(GL_DEPTH_TEST);         
    }
    
    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int w,
            int h) {

        GL3 gl = drawable.getGL().getGL3(); // Get the GL pipeline object this 
        
        gl.glViewport(x, y, w, h);

        T.initialize();

        //projection
        if(h<1){h=1;}
        if(w<1){w=1;}           
        float a = (float) w/ h;   //aspect 
        if (w < h) {
            T.ortho(-1, 1, -1/a, 1/a, -1, 1);
        }
        else{
            T.ortho(-1*a, 1*a, -1, 1, -1, 1);
        }
        
        // Convert right-hand to left-hand coordinate system
        T.reverseZ();
        gl.glUniformMatrix4fv( Projection, 1, true, T.getTransformv(), 0 );         

    }
    
    @Override
    public void dispose(GLAutoDrawable drawable) {
        // TODO Auto-generated method stub
        
    }
    
    public void createObject(GL3 gl, SObject obj) {
        float [] vertexArray = obj.getVertices();
        int [] vertexIndexs =obj.getIndices();
        numElements[idElement] = obj.getNumIndices();

        bindObject(gl);
        
        FloatBuffer vertices = FloatBuffer.wrap(vertexArray);

        // Create an empty buffer with the size we need 
        // and a null pointer for the data values
        vertexSize = vertexArray.length*(Float.SIZE/8);
        gl.glBufferData(GL_ARRAY_BUFFER, vertexSize, 
                vertices, GL_STATIC_DRAW); // pay attention to *Float.SIZE/8
        
        // Load the real data separately.  We put the colors right after the vertex coordinates,
        // so, the offset for colors is the size of vertices in bytes
//          gl.glBufferSubData( GL_ARRAY_BUFFER, 0, vertexSize, vertices );

        IntBuffer elements = IntBuffer.wrap(vertexIndexs);          

        long indexSize = vertexIndexs.length*(Integer.SIZE/8);
        gl.glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexSize, 
                elements, GL_STATIC_DRAW); // pay attention to *Float.SIZE/8                        
        gl.glEnableVertexAttribArray(vPosition);
        gl.glVertexAttribPointer(vPosition, 3, GL_FLOAT, false, 0, 0L);
//          gl.glEnableVertexAttribArray(vNormal);
//          gl.glVertexAttribPointer(vNormal, 3, GL_FLOAT, false, 0, vertexSize);
    }

    public void bindObject(GL3 gl){
        gl.glBindVertexArray(VAOs[idPoint]);
        gl.glBindBuffer(GL_ARRAY_BUFFER, VBOs[idBuffer]);
        gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBOs[idElement]);          
    };
}