#version 410 core

#define POSITION		0
#define COLOR			3
#define FRAG_COLOR		0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

struct Vertex
{
    vec4 color;
};

layout(location = 0) in Vertex inVertex;
layout(location = FRAG_COLOR, index = 0) out vec4 fragColor;

void main()
{
    fragColor = inVertex.color;
}
