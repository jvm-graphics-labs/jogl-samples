#version 410 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;
layout(triangles, invocations = 4) in;
layout(triangle_strip, max_vertices = 3) out;

in gl_PerVertex
{
    vec4 gl_Position;
} gl_in[];

out Block
{
    flat int instance;
} outBlock;

uniform mat4 mvp;

void main()
{	
    for(int i = 0; i < gl_in.length(); ++i)
    {
        gl_Position = mvp * gl_in[i].gl_Position;
        gl_Layer = gl_InvocationID;
        outBlock.instance = gl_InvocationID;
        EmitVertex();
    }

    EndPrimitive();
}

