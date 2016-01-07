#version 400 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;
layout(triangles, invocations = 1) in;
layout(triangle_strip, max_vertices = 4) out;

struct Vertex
{
    vec4 color;
};

in Vertex eval[];
out Vertex geom;

void main()
{	
    for(int i = 0; i < gl_in.length(); ++i)
    {
        gl_Position = gl_in[i].gl_Position;
        geom.color = eval[i].color;
        EmitVertex();
    }
    EndPrimitive();
}

