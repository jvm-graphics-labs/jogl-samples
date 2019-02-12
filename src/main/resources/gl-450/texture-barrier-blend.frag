#version 420 core

#define FRAG_COLOR		0
#define DIFFUSE			0
#define COLORBUFFER		1

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

layout(binding = DIFFUSE) uniform sampler2D Diffuse;
layout(binding = COLORBUFFER) uniform sampler2D Colorbuffer;

in vec4 gl_FragCoord;

in block
{
	vec2 Texcoord;
} In;

layout(location = FRAG_COLOR, index = 0) out vec4 Color;

void main()
{
	Color = texture(Diffuse, In.Texcoord.st) * 0.75 + texelFetch(Colorbuffer, ivec2(gl_FragCoord.xy), 0) * 0.25;
}
