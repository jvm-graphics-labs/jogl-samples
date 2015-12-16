#version 300 es
#define FRAG_COLOR	0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform sampler2D Diffuse;

in vec2 VertTexcoord;
layout(location = FRAG_COLOR) out vec4 FragColor;

void main()
{
	FragColor = texture(Diffuse, VertTexcoord);
}
