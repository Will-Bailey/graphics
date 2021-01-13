package Objects;
public class STet extends SObject{
    private float sideLength;

    public STet(){  
        super();
        init();
        update();
    }
        
    public STet(float sideLength){
        super();
        this.sideLength = sideLength;
        update();
    }
    
    private void init(){
        this.sideLength = 1;
    }

    @Override
    protected void genData() {
       
        // Generate vertices coordinates, normal values, and texture coordinates
        numVertices = 10; 
        vertices = new float[numVertices*3];
        normals = new float[numVertices*3];
        textures = new float[numVertices*2];

        float comp = (float)Math.sqrt(Math.pow(sideLength, 2) - Math.pow(sideLength/2, 2));

        vertices[0]=0;                 vertices[1]=0;           vertices[2]=0;
        vertices[3]=sideLength*(1/2);  vertices[4]=0;           vertices[5]=0;
        vertices[6]=sideLength;        vertices[7]=0;           vertices[8]=0;
        vertices[9]=0;                 vertices[10]=comp*(1/2); vertices[11]=0;
        vertices[12]=0;                vertices[13]=comp;       vertices[14]=0;
        vertices[15]=0;                vertices[16]=comp;       vertices[17]=0;
        vertices[18]=sideLength*(1/4); vertices[19]=comp*(1/4); vertices[20]=comp*(1/2);
        vertices[21]=sideLength*(3/4); vertices[22]=comp*(1/4); vertices[23]=comp*(1/2);
        vertices[24]=sideLength*(3/4); vertices[25]=comp*(3/4); vertices[26]=comp*(1/2);
        vertices[27]=sideLength*(1/2); vertices[27]=comp*(1/2); vertices[28]=comp;

        normals[0]= comp; normals[1]=0;  normals[2]=0;
        normals[3]= comp; normals[4]=0;  normals[5]=0;
        normals[6]= comp; normals[7]=0;  normals[8]=0;
        normals[9]= comp; normals[10]=0; normals[11]=0;

        numIndices = 5; 
        indices[0]=0; indices[1]=1; indices[2]=2; indices[3]=0;
        indices[4]=0; indices[5]=1; indices[6]=3; indices[7]=0;
        indices[8]=1; indices[9]=2; indices[10]=3; indices[11]=1;
        indices[12]=2; indices[13]=3; indices[14]=0; indices[15]=2;
    }
}