
import Basic.*;

import static com.jogamp.opengl.GL3.*;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;

import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import javax.imageio.ImageIO;

import java.io.IOException;
import java.io.FileInputStream;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ByteBuffer;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;

import Objects.*;

public class Framework extends JFrame implements KeyListener,  GLEventListener, MouseMotionListener{
    final GLCanvas canvas;
    final FPSAnimator animator=new FPSAnimator(60, true);

    private Transform T = new Transform();

    private int xMouse;
    private int yMouse;
    private float[] cameraPos = new float[3];
    private float[] cameraRot = new float[2];

    private int idPoint=0, numVAOs = 2;
    private int idBuffer=0, numVBOs = 2;
    private int idElement=0, numEBOs = 2;
    private int[] VAOs = new int[numVAOs];
    private int[] VBOs = new int[numVBOs];
    private int[] EBOs = new int[numEBOs];

    private int[] numElements = new int[numEBOs];
    private int vPosition;
    private int vColour;
    private int vTexCoord;
    private int vNormal;

    long coordSize;
    long colourSize;
    long texSize;
    long vertexSize;
    long normalSize;

    float[] vertexCoord = {0, 0, 0, 1, 0, 0, 0, 1, 0, 1, 1, 0};
    float[] texCoord = {0, 0, 1, 0, 0, 1, 1, 1};
    float[] vertexColours = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
    int[] vertexIndexs = {0, 1, 2, 2, 1, 3};
    
    private int ModelView;
    private int NormalTransform;
    private int Projection;

    ByteBuffer texImg;
    private int texWidth, texHeight;

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
        
        T.initialize();
        T.translate(-cameraPos[0], -cameraPos[1], -cameraPos[2]);
        T.rotateX(cameraRot[0]);
        T.rotateY(cameraRot[1]);
        //T.frustum(-1f, 1f, -1f, 1f, 1f, -1f);

        T.scale(0.5f, 0.5f, 0.5f);
        T.rotateX(-90);
        T.translate(0, -0.4f, 0);

        gl.glUniformMatrix4fv(ModelView, 1, true, T.getTransformv(), 0);          
        gl.glUniformMatrix4fv(NormalTransform, 1, true, T.getInvTransformTv(), 0);
        
        idPoint=0;
        idBuffer=0;
        idElement=0;
        bindObject(gl);
        gl.glDrawElements(GL_TRIANGLES, numElements[idElement], GL_UNSIGNED_INT, 0);

        T.scale(0.5f,0.5f,0.5f);
        T.translate(0, 0.7f, 0);


        gl.glUniformMatrix4fv(ModelView, 1, true, T.getTransformv(), 0 );
        gl.glUniformMatrix4fv(NormalTransform, 1, true, T.getInvTransformTv(), 0 );

        idPoint=1;
        idBuffer=1;
        idElement=1;
        bindObject(gl);
        gl.glDrawElements(GL_TRIANGLES, numElements[idElement], GL_UNSIGNED_INT, 0);

    }
    
    @Override
    public void init(GLAutoDrawable drawable) {
        //Init GL
        GL3 gl = drawable.getGL().getGL3();
        System.out.print("GL_Version: " + gl.glGetString(GL_VERSION));
        
        //Init VAOs, VBOs and EBOs
        gl.glGenVertexArrays(numVAOs,VAOs,0);
        gl.glGenBuffers(numVBOs, VBOs,0);
        gl.glGenBuffers(numEBOs, EBOs,0);


        //Init Texture
        ShaderProg texturer = new ShaderProg(gl, "ColourTex.vert", "ColourTex.frag");
        int tProgram = texturer.getProgram();
        int program = tProgram;
        gl.glUseProgram(tProgram);
        importTexture(gl);

        //Init Shader
        ShaderProg shaderproc = new ShaderProg(gl, "Gouraud.vert", "Gouraud.frag");
        program = shaderproc.getProgram();
        gl.glUseProgram(program);

        //Init Views
        ModelView = gl.glGetUniformLocation(program, "ModelView");
        Projection = gl.glGetUniformLocation(program, "Projection");
        NormalTransform = gl.glGetUniformLocation(program, "NormalTransform");
        
        //Init Objects
        SObject sphere = new SSphere(1, 40, 40);
        idPoint=0;
        idBuffer=0;
        idElement=0;
        createObject(gl, sphere);
        runTexture(gl, tProgram);
        //runShader(gl, program);

        SObject teapot = new STeapot(2);
        idPoint=1;
        idBuffer=1;
        idElement=1;
        createObject(gl, teapot);
        runTexture(gl, tProgram);
        //runShader(gl, program);


        gl.glEnable(GL_DEPTH_TEST);
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
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
        float [] normalArray = obj.getNormals();
        int [] vertexIndexs =obj.getIndices();
        numElements[idElement] = obj.getNumIndices();

        gl.glBindVertexArray(VAOs[idPoint]);
        gl.glBindBuffer(GL_ARRAY_BUFFER, VBOs[idBuffer]);
        gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBOs[idElement]);          

        FloatBuffer data = FloatBuffer.wrap(vertexCoord);
        FloatBuffer colours = FloatBuffer.wrap(vertexColours);
        FloatBuffer textures = FloatBuffer.wrap(texCoord);
        coordSize = vertexCoord.length*(Float.SIZE/8);
        colourSize = vertexColours.length*(Float.SIZE/8);
        texSize = texCoord.length*(Float.SIZE/8);
        gl.glBufferData(GL_ARRAY_BUFFER, coordSize + colourSize + texSize, null, GL_STATIC_DRAW);
        gl.glBufferSubData( GL_ARRAY_BUFFER, 0, coordSize, data);
        gl.glBufferSubData( GL_ARRAY_BUFFER, coordSize, colourSize, colours);
        gl.glBufferSubData( GL_ARRAY_BUFFER, coordSize + colourSize, texSize, textures);

        FloatBuffer vertices = FloatBuffer.wrap(vertexArray);
        FloatBuffer normals = FloatBuffer.wrap(normalArray);
        vertexSize = vertexArray.length*(Float.SIZE/8);
        normalSize = normalArray.length*(Float.SIZE/8);
        gl.glBufferData(GL_ARRAY_BUFFER, vertexSize + normalSize, vertices, GL_STATIC_DRAW);
        gl.glBufferSubData(GL_ARRAY_BUFFER, 0, vertexSize, vertices);
        gl.glBufferSubData(GL_ARRAY_BUFFER, vertexSize, normalSize, normals);
        
        IntBuffer elements = IntBuffer.wrap(vertexIndexs);
        long indexSize = vertexIndexs.length*(Integer.SIZE/8);
        gl.glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexSize, elements, GL_STATIC_DRAW);
        gl.glEnableVertexAttribArray(vPosition);
        gl.glVertexAttribPointer(vPosition, 3, GL_FLOAT, false, 0, 0L);
    }

    public void bindObject(GL3 gl){
        gl.glBindVertexArray(VAOs[idPoint]);
        gl.glBindBuffer(GL_ARRAY_BUFFER, VBOs[idBuffer]);
        gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBOs[idElement]);          
    }

    public void runShader(GL3 gl, int program){
        vPosition = gl.glGetAttribLocation( program, "vPosition" );
        gl.glGetAttribLocation( program, "vNormal" );
        gl.glVertexAttribPointer(vPosition, 3, GL_FLOAT, false, 0, 0L);

        vNormal = gl.glGetAttribLocation(program, "vNormal");
        gl.glEnableVertexAttribArray(vNormal);
        gl.glVertexAttribPointer(vNormal, 3, GL_FLOAT, false, 0, vertexSize);
        
        float[] lightPosition = {100.0f, 100.0f, 100.0f, 0.0f};
        Vec4 lightAmbient = new Vec4(1.0f, 1.0f, 1.0f, 1.0f);
        Vec4 lightDiffuse = new Vec4(1.0f, 1.0f, 1.0f, 1.0f);
        Vec4 lightSpecular = new Vec4(1.0f, 1.0f, 1.0f, 1.0f);

        //Brass material
        Vec4 materialAmbient = new Vec4(0.329412f, 0.223529f, 0.027451f, 1.0f);
        Vec4 materialDiffuse = new Vec4(0.780392f, 0.568627f, 0.113725f, 1.0f);
        Vec4 materialSpecular = new Vec4(0.992157f, 0.941176f, 0.807843f, 1.0f);
        float  materialShininess = 27.8974f;
        
        Vec4 ambientProduct = lightAmbient.times(materialAmbient);
        float[] ambient = ambientProduct.getVector();
        Vec4 diffuseProduct = lightDiffuse.times(materialDiffuse);
        float[] diffuse = diffuseProduct.getVector();
        Vec4 specularProduct = lightSpecular.times(materialSpecular);
        float[] specular = specularProduct.getVector();

        gl.glUniform4fv( gl.glGetUniformLocation(program, "AmbientProduct"), 1, ambient, 0);
        gl.glUniform4fv( gl.glGetUniformLocation(program, "DiffuseProduct"), 1, diffuse, 0);
        gl.glUniform4fv( gl.glGetUniformLocation(program, "SpecularProduct"), 1, specular, 0);
        
        gl.glUniform4fv( gl.glGetUniformLocation(program, "LightPosition"), 1, lightPosition, 0);
        gl.glUniform1f( gl.glGetUniformLocation(program, "Shininess"), materialShininess);
    }

    public void runTexture(GL3 gl, int tProgram){
        vColour = gl.glGetAttribLocation(tProgram, "vColour");
        gl.glEnableVertexAttribArray(vColour);
        gl.glVertexAttribPointer(vColour, 3, GL_FLOAT, false, 0, coordSize);

        vTexCoord = gl.glGetAttribLocation(tProgram, "vTexCoord");
        gl.glEnableVertexAttribArray( vTexCoord );
        gl.glVertexAttribPointer( vTexCoord, 2, GL_FLOAT, false, 0, coordSize+colourSize);
        
        gl.glUniform1i( gl.glGetUniformLocation(tProgram, "tex"), 0 );
    }

    public void importTexture(GL3 gl){
        try {
                texImg = readImage("WelshDragon.jpg");
            } catch (IOException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            }

        gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, texWidth, texHeight, 0, GL_BGR, GL_UNSIGNED_BYTE, texImg);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    }

    private ByteBuffer readImage(String filename) throws IOException {

            ByteBuffer imgbuf;
            BufferedImage img = ImageIO.read(new FileInputStream(filename));

            texWidth = img.getWidth();
            texHeight = img.getHeight();
            DataBufferByte datbuf = (DataBufferByte) img.getData().getDataBuffer();
            imgbuf = ByteBuffer.wrap(datbuf.getData());
            return imgbuf;
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
        int x = e.getX();
        int y = e.getY();

        cameraRot[0] += (y-yMouse);
        cameraRot[1] += (x-xMouse);
        
        xMouse = x;
        yMouse = y;
    }

    @Override
    public void mouseMoved(MouseEvent e){
        xMouse = e.getX();
        yMouse = e.getY();
    }
    
    public static void main(String[] args) {
        new Framework();
    }
}