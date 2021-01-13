package Objects;
public class STPrism extends SObject{
    private float sideLength;
    private float height;

    public STPrism(){  
        super();
        init();
        update();
    }
        
    public STPrism(float sideLength, float height){
        super();
        this.sideLength = sideLength;
        this.height = height;
        update();
    }
    
    private void init(){
        this.sideLength = 1;
        this.height=1;
    }

    @Override
    protected void genData() {

        numVertices = 6;
        vertices = new float[numVertices*3];
        normals = new float[numVertices*3];
        textures = new float[numVertices*2];

        vertices[0]=0;           vertices[1]=0;           vertices[2]=0;
        vertices[3]=sideLength;  vertices[4]=0;           vertices[5]=0;
        vertices[6]=0;           vertices[7]=sideLength;  vertices[8]=0;
        vertices[9]=0;           vertices[10]=0;          vertices[11]=height;
        vertices[12]=sideLength; vertices[13]=0;          vertices[14]=height;
        vertices[15]=0;          vertices[16]=sideLength; vertices[17]=height;

        normals[0]=sideLength;  normals[1]=0;  normals[2]=0;
        normals[3]=sideLength;  normals[4]=0;  normals[5]=0;
        normals[6]=sideLength;  normals[7]=0;  normals[8]=0;
        normals[9]=sideLength;  normals[10]=0; normals[11]=0;
        normals[12]=sideLength; normals[13]=0; normals[14]=0;
        normals[15]=sideLength; normals[16]=0; normals[17]=0;

        numIndices = 25;
        indices = new int[numIndices];
        
        indices[0]=0;  indices[1]=1;  indices[2]=2;  indices[3]=0;
        indices[4]=0;  indices[5]=1;  indices[6]=3;  indices[7]=0;
        indices[8]=1;  indices[9]=4;  indices[10]=3; indices[11]=1;
        indices[12]=1; indices[13]=2; indices[14]=5; indices[15]=1;
        indices[16]=4; indices[17]=5; indices[18]=2; indices[19]=4;
        indices[20]=2; indices[21]=0; indices[22]=5; indices[23]=2;
        indices[21]=5; indices[22]=3; indices[23]=2; indices[24]=5;
    }

    public void setSideLength(float sideLength){
        this.sideLength=sideLength;
        updated=false;
    }

    public void setHeight(float height){
        this.height=height;
        updated=false;
    }

    public float getSideLength(){
        return sideLength;
    }

    public float getHeight(){
        return height;
    }
}