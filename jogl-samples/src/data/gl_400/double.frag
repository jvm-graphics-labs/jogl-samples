#version 400 core

#define FRAG_COLOR		0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform dvec4 diffuse;

layout(location = FRAG_COLOR, index = 0) out vec4 fragColor;

void main()
{
    fragColor = vec4(diffuse);
}

