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

in Block
{
    vec2 texCoord;
} inBlock[];

out Block
{
    vec2 texCoord;
    flat int instance;
} outBlock;

uniform mat4 mvp;

void main()
{	
    for(int i = 0; i < gl_in.length(); ++i)
    {
        gl_Layer = gl_InvocationID;
        gl_ViewportIndex = gl_InvocationID;
        gl_Position = mvp * gl_in[i].gl_Position;
        outBlock.instance = gl_InvocationID;
        outBlock.texCoord = inBlock[i].texCoord;
        EmitVertex();
    }

    EndPrimitive();
}

