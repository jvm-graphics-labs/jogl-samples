#version 400 core

#define POSITION		0
#define COLOR			3
#define TEXCOORD		4

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform dmat4 mvp;

layout(location = POSITION) in vec2 position;

void main()
{	
    gl_Position = vec4(mvp * dvec4(position, 0.0, 1.0));
}

