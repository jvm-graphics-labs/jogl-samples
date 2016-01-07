#version 400 core

#define POSITION		0
#define COLOR			3
#define TEXCOORD		4

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform mat4 mvp;

struct Vertex
{
    vec4 color;
};

layout(location = POSITION) in vec2 position;
layout(location = COLOR) in vec4 color;

out Vertex vert;

void main()
{	
    gl_Position = mvp * vec4(position, 0.0, 1.0);
    vert.color = color;
}
