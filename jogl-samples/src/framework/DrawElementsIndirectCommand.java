/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package framework;

/**
 *
 * @author GBarbieri
 */
public class DrawElementsIndirectCommand {

    public static final int SIZEOF = 5 * Integer.BYTES;
    
    public int primitiveCount;
    public int instanceCount;
    public int firstIndex;
    public int baseVertex;
    public int baseInstance;

    public DrawElementsIndirectCommand(int primitiveCount, int instanceCount, int firstIndex, int baseVertex, int baseInstance) {
        this.primitiveCount = primitiveCount;
        this.instanceCount = instanceCount;
        this.firstIndex = firstIndex;
        this.baseVertex = baseVertex;
        this.baseInstance = baseInstance;
    }
    
    public int [] toIa_() {
        return new int[]{primitiveCount, instanceCount, firstIndex, baseVertex, baseInstance};
    }
}
