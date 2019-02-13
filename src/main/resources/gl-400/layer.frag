#version 400 core

#define FRAG_COLOR		0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

const vec4 Color[4] = vec4[]
(
	vec4(1.0, 0.0, 0.0, 1.0),
	vec4(1.0, 1.0, 0.0, 1.0),
	vec4(0.0, 1.0, 0.0, 1.0),
	vec4(0.0, 0.0, 1.0, 1.0)
);

flat in int GeomInstance;

layout(location = FRAG_COLOR, index = 0) out vec4 FragColor;

void main()
{
	FragColor = Color[GeomInstance];
}
