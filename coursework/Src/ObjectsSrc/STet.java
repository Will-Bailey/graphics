package Objects;
public class STet extends SObject{
    private float sideLength;
    private int slices;
    private int stacks;
        
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
        int i,j,k;
       
        // Generate vertices coordinates, normal values, and texture coordinates
        numVertices = 4; 
        numIndices = 4; 
        vertices = new float[numVertices*3];
        normals = new float[numVertices*3];
        textures = new float[numVertices*3];

        float comp1 = (float)Math.sqrt(Math.pow(sideLength, 2) - Math.pow(sideLength/2, 2));

        vertices[0]=0; vertices[1]=0; vertices[2]=0;
        vertices[3]=sideLength; vertices[4]=0; vertices[5]=0;
        vertices[6]=sideLength/2; vertices[7]=comp1; vertices[8]=0;
        vertices[9]=sideLength/2; vertices[10]=comp1/2; vertices[11]=comp1;

        normals[0]=0; normals[1]=0; normals[2]=0;
        normals[3]=0; normals[4]=0; normals[5]=0;
        normals[6]=0; normals[7]=0; normals[8]=0;
        normals[9]=0; normals[10]=0; normals[11]=0;

        indices[0]=0; indices[1]=1; indices[2]=2; indices[3]=3;

    }
}