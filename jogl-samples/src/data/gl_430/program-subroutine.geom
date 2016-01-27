#version 420 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;
layout(triangles, invocations = 1) in;
layout(triangle_strip, max_vertices = 3) out;

in gl_PerVertex
{
    vec4 gl_Position;
} gl_in[];

in Block
{
    vec2 texCoord;
} inBlock[];

out gl_PerVertex
{
    vec4 gl_Position;
};

out Block
{
    vec2 texCoord;
} outBlock;

void main()
{	
    for(int i = 0; i < gl_in.length(); ++i)
    {
        gl_Position = gl_in[i].gl_Position;
        outBlock.texCoord = inBlock[i].texCoord;
        EmitVertex();
    }
    EndPrimitive();
}

