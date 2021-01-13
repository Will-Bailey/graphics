
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
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import javax.imageio.ImageIO;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

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

    private int idPoint=0, numVAOs = 3;
    private int idBuffer=0, numVBOs = 3;
    private int idElement=0, numEBOs = 3;
    private int[] VAOs = new int[numVAOs];
    private int[] VBOs = new int[numVBOs];
    private int[] EBOs = new int[numEBOs];

    private Texture texture;
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
    long indexSize;

    float[] vertexCoord = {0, 0, 0, 1, 0, 0, 0, 1, 0, 1, 1, 0};
    float[] texCoord = {0, 0, 1, 0, 0, 1, 1, 1};
    //float[] vertexColours = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
    float[] vertexColours = {0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1};
    int[] vertexIndexs = {0, 1, 2, 2, 1, 3};
    
    private int ModelView;
    private int NormalTransform;
    private int Projection;

    ByteBuffer texImg;
    private int texWidth, texHeight;

    public Framework() {
        super("KeyListener Activity");
        
        cameraPos[0] = 0f;
        cameraPos[1] = -10f;
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
        T.rotateZ(cameraRot[1]);

        T.scale(0.3f, 0.3f, 0.3f);
        T.rotateX(-90);
        T.translate(0, -0.4f, 0f);
        gl.glUniformMatrix4fv(ModelView, 1, true, T.getTransformv(), 0);          
        gl.glUniformMatrix4fv(NormalTransform, 1, true, T.getInvTransformTv(), 0);
        idPoint=1;
        idBuffer=1;
        idElement=1;
        bindObject(gl);
        gl.glDrawElements(GL_TRIANGLES, numElements[idElement], GL_UNSIGNED_INT, 0);
        
        T.translate(0, 0.7f, 0);
        gl.glUniformMatrix4fv(ModelView, 1, true, T.getTransformv(), 0);          
        gl.glUniformMatrix4fv(NormalTransform, 1, true, T.getInvTransformTv(), 0);
        idPoint=0;
        idBuffer=0;
        idElement=0;
        bindObject(gl);
        gl.glDrawElements(GL_TRIANGLES, numElements[idElement], GL_UNSIGNED_INT, 0);

        T.translate(0, 0.5f, 0);
        gl.glUniformMatrix4fv(ModelView, 1, true, T.getTransformv(), 0);          
        gl.glUniformMatrix4fv(NormalTransform, 1, true, T.getInvTransformTv(), 0);
        idPoint=2;
        idBuffer=2;
        idElement=2;
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

        //Init Shader
        ShaderProg shaderproc = new ShaderProg(gl, "Combine.vert", "Combine.frag");
        int program = shaderproc.getProgram();
        gl.glUseProgram(program);

        //Init Texture
        importTexture(gl);

        //Init Views
        ModelView = gl.glGetUniformLocation(program, "ModelView");
        Projection = gl.glGetUniformLocation(program, "Projection");
        NormalTransform = gl.glGetUniformLocation(program, "NormalTransform");
        
        //Init Objects
        SObject tPrism = new STPrism(1f, 1f);
        idPoint=0;
        idBuffer=0;
        idElement=0;
        createObject(gl, tPrism);
        runShader(gl, program);

        SObject teapot = new STeapot(2);
        idPoint=1;
        idBuffer=1;
        idElement=1;
        createObject(gl, teapot);
        runShader(gl, program);

        SObject sphere = new SSphere(1);
        idPoint=2;
        idBuffer=2;
        idElement=2;
        createObject(gl, sphere);
        runShader(gl, program);

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
            float a = (float) w/ h;
            T.perspective(60, a, 0.1f, 1000);
            
            // Convert right-hand to left-hand coordinate system
            T.reverseZ();
            gl.glUniformMatrix4fv(Projection, 1, true, T.getTransformv(), 0 );         

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
        indexSize = vertexIndexs.length*(Integer.SIZE/8);
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
        gl.glEnableVertexAttribArray(vPosition);
        gl.glVertexAttribPointer(vPosition, 3, GL_FLOAT, false, 0, 0L);

        vNormal = gl.glGetAttribLocation(program, "vNormal");
        gl.glEnableVertexAttribArray(vNormal);
        gl.glVertexAttribPointer(vNormal, 3, GL_FLOAT, false, 0, vertexSize);
        
        vColour = gl.glGetAttribLocation(program, "vColour");
        gl.glEnableVertexAttribArray(vColour);
        gl.glVertexAttribPointer(vColour, 3, GL_FLOAT, false, 0, vertexSize+coordSize);

        vTexCoord = gl.glGetAttribLocation(program, "vTexCoord");
        gl.glEnableVertexAttribArray( vTexCoord );
        gl.glVertexAttribPointer( vTexCoord, 2, GL_FLOAT, false, 0, vertexSize+coordSize+colourSize);

        float[] lightPosition = {100.0f, 100.0f, 100.0f, 0.0f};
        Vec4 lightAmbient = new Vec4(1.0f, 1.0f, 1.0f, 1.0f);
        Vec4 lightDiffuse = new Vec4(1.0f, 1.0f, 1.0f, 1.0f);
        Vec4 lightSpecular = new Vec4(1.0f, 1.0f, 1.0f, 1.0f);

        Vec4 materialAmbient = new Vec4(0.25f, 0.25f, 0.25f, 1f);
        Vec4 materialDiffuse = new Vec4(0.4f, 0.2368f, 0.1036f, 1f);
        Vec4 materialSpecular = new Vec4(0.774597f, 0.4585161f, 0.200621f, 1f);
        float  materialShininess = 76.8f;
        
        Vec4 ambientProduct = lightAmbient.times(materialAmbient);
        float[] ambient = ambientProduct.getVector();
        Vec4 diffuseProduct = lightDiffuse.times(materialDiffuse);
        float[] diffuse = diffuseProduct.getVector();
        Vec4 specularProduct = lightSpecular.times(materialSpecular);
        float[] specular = specularProduct.getVector();

        gl.glUniform4fv(gl.glGetUniformLocation(program, "AmbientProduct"), 1, ambient, 0);
        gl.glUniform4fv(gl.glGetUniformLocation(program, "DiffuseProduct"), 1, diffuse, 0);
        gl.glUniform4fv(gl.glGetUniformLocation(program, "SpecularProduct"), 1, specular, 0);
        
        gl.glUniform4fv(gl.glGetUniformLocation(program, "LightPosition"), 1, lightPosition, 0);
        gl.glUniform1f(gl.glGetUniformLocation(program, "Shininess"), materialShininess);

        gl.glUniform1i(gl.glGetUniformLocation(program, "tex"), 0 );
    }

    public void importTexture(GL3 gl){
        try {
            texture = TextureIO.newTexture(new File("hex.jpg"), false);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }

        //texture.setTexParameteri(gl, GL_TEXTURE_WRAP_S, GL_REPEAT);
        //texture.setTexParameteri(gl, GL_TEXTURE_WRAP_T, GL_REPEAT);
        //texture.setTexParameteri(gl, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        //texture.setTexParameteri(gl, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

        texCoord[0] = texture.getImageTexCoords().left(); 
        texCoord[1] = texture.getImageTexCoords().bottom();
        texCoord[2] = texture.getImageTexCoords().right();    
        texCoord[3] = texture.getImageTexCoords().bottom();
        texCoord[4] = texture.getImageTexCoords().left(); 
        texCoord[5] = texture.getImageTexCoords().top();
        texCoord[6] = texture.getImageTexCoords().right();    
        texCoord[7] = texture.getImageTexCoords().top();

        //gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, texWidth, texHeight, 0, GL_BGR, GL_UNSIGNED_BYTE, texImg);
        //gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {}

    @Override
    public void keyPressed(KeyEvent e){
        char pressed = e.getKeyChar();

        double step = 0.05;
            
        if (pressed == 'w'){
            cameraPos[0] = cameraPos[0] + (float) (step*Math.sin(Math.toRadians(cameraRot[1])));
            cameraPos[1] = cameraPos[1] + (float) (step*Math.cos(Math.toRadians(cameraRot[0]))*Math.cos(Math.toRadians(cameraRot[1]))); 
            cameraPos[2] = cameraPos[2] - (float) (step*Math.sin(Math.toRadians(cameraRot[0])));

        } else if (pressed == 'a'){
            cameraPos[1] = cameraPos[1] + (float) (step*Math.sin(Math.toRadians(cameraRot[1])));
            cameraPos[0] = cameraPos[0] - (float) (step*Math.cos(Math.toRadians(cameraRot[0]))*Math.cos(Math.toRadians(cameraRot[1]))); 

        } else if (pressed == 's'){
            cameraPos[0] = cameraPos[0] - (float) (step*Math.sin(Math.toRadians(cameraRot[1])));
            cameraPos[1] = cameraPos[1] - (float) (step*Math.cos(Math.toRadians(cameraRot[0]))*Math.cos(Math.toRadians(cameraRot[1]))); 
            cameraPos[2] = cameraPos[2] + (float) (step*Math.sin(Math.toRadians(cameraRot[0])));

        } else if (pressed == 'd'){
            cameraPos[1] = cameraPos[1] - (float) (step*Math.sin(Math.toRadians(cameraRot[1])));
            cameraPos[0] = cameraPos[0] + (float) (step*Math.cos(Math.toRadians(cameraRot[0]))*Math.cos(Math.toRadians(cameraRot[1]))); 

        } else if (pressed == 'r'){
            cameraPos[0] = 0f;
            cameraPos[1] = -10f;
            cameraPos[2] = 0f;

            cameraRot[0] = 0f;
            cameraRot[1] = 0f;
        }
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

        if (cameraRot[0]> 90){
            cameraRot[0]= 90;}
        if (cameraRot[0]<-90){
            cameraRot[0]=-90;}

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