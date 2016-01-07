#version 400 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;
layout(triangles, invocations = 6) in;
layout(triangle_strip, max_vertices = 4) out;

in Block
{
    vec3 color;
} inBlock[];

layout(stream = 0) out Block
{
    vec3 color;
} outBlock;

uniform mat4 mvp;

void main()
{	
    for(int i = 0; i < gl_in.length(); ++i)
    {
        outBlock.color = (vec3(gl_InvocationID + 1) / 6.0 + inBlock[i].color) / 2.0; 
        gl_Position = mvp * (gl_in[i].gl_Position + vec4(vec2(0.0), - 0.3 + float(0.1) * float(gl_InvocationID), 0.0));
        EmitVertex();
    }
    EndPrimitive();
}
