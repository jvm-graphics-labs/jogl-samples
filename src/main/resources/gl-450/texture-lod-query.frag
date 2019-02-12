#version 420 core

#define FRAG_COLOR	0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

layout(binding = 0) uniform sampler2D Diffuse;

layout(origin_upper_left) in vec4 gl_FragCoord;

in block
{
	vec2 Texcoord;
} In;

layout(location = FRAG_COLOR, index = 0) out vec4 Color;

float textureLevel(in sampler2D Sampler, in vec2 Texcoord);
float textureLevelArea(in sampler2D Sampler, in vec2 Texcoord);

void main()
{
	ivec2 Size = textureSize(Diffuse, 0);
	float LevelCount = log2(max(Size.x, Size.y)) + 1.0;
	float Level = textureLevelArea(Diffuse, In.Texcoord);

	Color = vec4(vec3(Level / LevelCount), 1.0);
}
