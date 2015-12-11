#version 150 core

#ifndef GEN_ERROR // If this is not declared, the compiler should generate an error
layout(triangles) in;
#endif

precision highp float;
precision highp int;
layout(std140, column_major) uniform;
layout(triangle_strip, max_vertices = 4) out;

uniform Constant
{
    vec4 color[3];
} constant;

in Block
{
    vec4 color;
} inBlock[];

out Block
{
    vec4 color;
} outBlock;

void main()
{
    for(int i = 0; i < gl_in.length(); ++i)
    {
        gl_Position = gl_in[i].gl_Position;
        outBlock.color = (inBlock[i].color + constant.color[i]) * 0.5;
        EmitVertex();
    }
    EndPrimitive();
}

