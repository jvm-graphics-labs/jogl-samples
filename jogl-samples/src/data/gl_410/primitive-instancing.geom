#version 410 core

#define POSITION	0
#define COLOR		3
#define TEXCOORD	4
#define FRAG_COLOR	0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;
layout(triangles, invocations = 5) in;
layout(triangle_strip, max_vertices = 4) out;

in gl_PerVertex
{
    vec4 gl_Position;
    float gl_PointSize;
    float gl_ClipDistance[];
} gl_in[];

layout(location = COLOR) in vec4 color[];

out gl_PerVertex 
{
    vec4 gl_Position;
    float gl_PointSize;
    float gl_ClipDistance[];
};

layout(location = COLOR) out vec4 geomColor;

uniform mat4 mvp;

void main()
{	
    for(int i = 0; i < gl_in.length(); ++i)
    {
        gl_Position = mvp * (gl_in[i].gl_Position + vec4(vec2(0.0), - 0.5 + 0.25 * float(gl_InvocationID), 0.0));
        geomColor = (vec4(gl_InvocationID + 1) / 6.0 + color[i]) / 2.0; 
        EmitVertex();
    }
    EndPrimitive();
}

