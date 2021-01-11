import static com.jogamp.opengl.GL3.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.swing.JFrame;

import Basic.*;
import Objects.*;

import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.glu.GLU;

public class Framework extends JFrame implements KeyListener,  GLEventListener, MouseMotionListener{
    final GLCanvas canvas;
    final FPSAnimator animator=new FPSAnimator(60, true);

    private Transform T = new Transform();

    private float[] cameraPos = new float[3];
    private float[] cameraRot = new float[2];

    private int idPoint=0, numVAOs = 2;
    private int idBuffer=0, numVBOs = 2;
    private int idElement=0, numEBOs = 2;
    private int[] VAOs = new int[numVAOs];
    private int[] VBOs = new int[numVBOs];
    private int[] EBOs = new int[numEBOs];

    private int[] numElements = new int[numEBOs];
    private long vertexSize; 
    private int vPosition;
    
    private int ModelView;
    private int Projection;

    public Framework() {
        super("KeyListener Activity");
        
        cameraPos[0] = 0f;
        cameraPos[1] = 0f;
        cameraPos[2] = 0f;

        cameraRot[0] = 0f;
        cameraRot[1] = 0f;

        GLProfile glp = GLProfile.get(GLProfile.GL3);
        GLCapabilities capabilities = new GLCapabilities(glp);
        canvas = new GLCanvas(capabilities);

        add(canvas, java.awt.BorderLayout.CENTER);
        canvas.addGLEventListener(this);
        canvas.addKeyListener(this);
        canvas.addMouseMotionListener(this);
        animator.add(canvas);

        setSize(500,500);
        setVisible(true);

        animator.start();
        canvas.requestFocus();
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL3 gl = drawable.getGL().getGL3(); // Get the GL pipeline object this 
        
        gl.glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);
        gl.glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

        T.initialize();
        //T.lookAt(cameraPos[0], cameraPos[1], cameraPos[2], cameraPos[3], cameraPos[4], cameraPos[5], 0f, 1f, 0f);    
        T.scale(0.5f, 0.5f, 0.5f);
        T.rotateX(-90);
        T.translate(0, -0.4f, 0);


        gl.glUniformMatrix4fv(ModelView, 1, true, T.getTransformv(), 0);          

        idPoint=0;
        idBuffer=0;
        idElement=0;
        bindObject(gl);
        gl.glDrawElements(GL_TRIANGLES, numElements[idElement], GL_UNSIGNED_INT, 0);

        T.scale(0.5f,0.5f,0.5f);
        T.translate(0, 0.7f, 0);

        T.translate(-cameraPos[0], -cameraPos[1], -cameraPos[2]);
        T.rotateX(cameraRot[0]);
        T.rotateY(cameraRot[1]);
        
        gl.glUniformMatrix4fv( ModelView, 1, true, T.getTransformv(), 0 );          

        idPoint=1;
        idBuffer=1;
        idElement=1;
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
        SObject sphere = new SSphere(1, 40, 40);
        idPoint=0;
        idBuffer=0;
        idElement=0;
        createObject(gl, sphere);

        SObject teapot = new STeapot(2);
        idPoint=1;
        idBuffer=1;
        idElement=1;
        createObject(gl, teapot);

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

    public void createObject(GL3 gl, SObject obj) {
        float [] vertexArray = obj.getVertices();
        int [] vertexIndexs =obj.getIndices();
        numElements[idElement] = obj.getNumIndices();

        bindObject(gl);
        
        FloatBuffer vertices = FloatBuffer.wrap(vertexArray);

        vertexSize = vertexArray.length*(Float.SIZE/8);
        gl.glBufferData(GL_ARRAY_BUFFER, vertexSize, 
                vertices, GL_STATIC_DRAW);
        
        IntBuffer elements = IntBuffer.wrap(vertexIndexs);          

        long indexSize = vertexIndexs.length*(Integer.SIZE/8);
        gl.glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexSize, 
                elements, GL_STATIC_DRAW);                       
        gl.glEnableVertexAttribArray(vPosition);
        gl.glVertexAttribPointer(vPosition, 3, GL_FLOAT, false, 0, 0L);
    }

    public void bindObject(GL3 gl){
        gl.glBindVertexArray(VAOs[idPoint]);
        gl.glBindBuffer(GL_ARRAY_BUFFER, VBOs[idBuffer]);
        gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBOs[idElement]);          
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {}

    @Override
    public void keyPressed(KeyEvent e){
        char pressed = e.getKeyChar();

        float step = 0.05f;
            
        if (pressed == 'w'){
            System.out.println("camera forward");
            cameraPos[2] = cameraPos[2] + step;

        } else if (pressed == 'a'){
            System.out.println("camera left");
            cameraPos[0] = cameraPos[0] - step;

        } else if (pressed == 's'){
            System.out.println("camera back");
            cameraPos[2] = cameraPos[2] - step;

        } else if (pressed == 'd'){
            System.out.println("camera right");
            cameraPos[0] = cameraPos[0] + step;
        }
        System.out.println(Float.toString(cameraPos[0]));
    }

    @Override
    public void keyReleased(KeyEvent e) {};

    @Override
    public void keyTyped(KeyEvent e) {
        keyPressed(e);
    };

    @Override
    public void mouseDragged(MouseEvent e) {
        int dx = e.getX();
        int dy = e.getY();

        cameraRot[0] = cameraRot[0]+0.005f*dx;
        cameraRot[1] = cameraRot[0]+0.005f*dy;
    }
    @Override
    public void mouseMoved(MouseEvent e){}
    
    public static void main(String[] args) {
        new Framework();
    }
}