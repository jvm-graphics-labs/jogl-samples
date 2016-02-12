/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package framework;

import static com.jogamp.opengl.GL.GL_FALSE;
import static com.jogamp.opengl.GL.GL_FLOAT;

/**
 *
 * @author GBarbieri
 */
public class VertexAttrib {

    public int enabled;
    public int binding;
    public int size;
    public int stride;
    public int type;
    public boolean normalized;
    public int integer;
    public int long_;
    public int divisor;
    public int pointer;

    public VertexAttrib(int enabled, int binding, int size, int stride, int type, boolean normalized, int integer, 
            int long_, int divisor, int pointer) {
        
        this.enabled = enabled;
        this.binding = binding;
        this.size = size;
        this.stride = stride;
        this.type = type;
        this.normalized = normalized;
        this.integer = integer;
        this.long_ = long_;
        this.divisor = divisor;
        this.pointer = pointer;
    }

    public VertexAttrib() {
        
        enabled = GL_FALSE;
        binding = 0;
        size = 4;
        stride = 0;
        type = GL_FLOAT;
        normalized = false;
        integer = GL_FALSE;
        long_ = GL_FALSE;
        divisor = 0;
        pointer = 0;
    }

    public boolean isEqual(VertexAttrib vertexAttrib) {
        
        return enabled == vertexAttrib.enabled
                && size == vertexAttrib.size
                && stride == vertexAttrib.stride
                && type == vertexAttrib.type
                && normalized == vertexAttrib.normalized
                && integer == vertexAttrib.integer
                && long_ == vertexAttrib.long_;
    }
}
