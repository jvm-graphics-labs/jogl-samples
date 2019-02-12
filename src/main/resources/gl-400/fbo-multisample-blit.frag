#version 400 core

#define FRAG_COLOR		0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform sampler2D Diffuse;

in vec4 gl_FragCoord;
layout(location = FRAG_COLOR, index = 0) out vec4 Color;

void main()
{
	Color = texture(Diffuse, vec2(gl_FragCoord.x, 1.0 - gl_FragCoord.y) / vec2(640, 480));
}
