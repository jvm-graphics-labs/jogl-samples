#version 400 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;
layout(triangles, invocations = 1) in;
layout(triangle_strip, max_vertices = 4) out;

in Block
{
    vec4 color;
} inBlock[];

out Block
{
    layout(stream = 0) vec4 color;
} outBlock;

void main()
{
    for(int i = 0; i < gl_in.length(); ++i)
    {
        outBlock.color = inBlock[i].color;
        gl_Position = gl_in[i].gl_Position;
        EmitVertex();
    }
    EndPrimitive();
}

