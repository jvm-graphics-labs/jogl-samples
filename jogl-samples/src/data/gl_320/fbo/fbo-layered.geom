#version 150 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;
layout(triangles) in;
layout(triangle_strip, max_vertices = 12) out;

uniform mat4 mvp;

out Block
{
    flat int instance;
} outBlock;

void main()
{	
    for(int layer = 0; layer < 4; ++layer)
    {
        gl_Layer = layer;

        for(int i = 0; i < gl_in.length(); ++i)
        {
            gl_Position = mvp * gl_in[i].gl_Position;
            outBlock.instance = layer;
            EmitVertex();
        }
        EndPrimitive();
    }
}

