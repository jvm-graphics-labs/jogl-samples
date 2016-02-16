#version 440 core

#define FRAG_COLOR		0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;
layout(std430, column_major) buffer;

struct Vertex
{
    vec4 color;
};

layout(location = 0) in Vertex st_In;

layout(location = 0 + 1) in Block
{
    vec4 color;
} bl_In; 

layout(location = FRAG_COLOR, index = 0) out vec4 color;

void main()
{
    color = st_In.color + bl_In.color;
}