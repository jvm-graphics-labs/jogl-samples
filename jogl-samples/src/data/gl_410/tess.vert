#version 410 core

#define POSITION		0
#define COLOR			3
#define FRAG_COLOR		0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform mat4 mvp;

layout(location = POSITION) in vec2 position;
layout(location = COLOR) in vec4 color;

out gl_PerVertex
{
    vec4 gl_Position;
    float gl_PointSize;
    float gl_ClipDistance[];
};

struct Vertex
{
    vec4 color;
};

layout(location = 0) out Vertex outVertex;

void main()
{	
    gl_Position = mvp * vec4(position, 0.0, 1.0);
    outVertex.color = color;
}
