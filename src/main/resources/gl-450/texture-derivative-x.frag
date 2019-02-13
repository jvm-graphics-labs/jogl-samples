#version 430 core
#extension GL_ARB_derivative_control : require

#define FRAG_COLOR	0
#define DIFFUSE		0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

layout(binding = DIFFUSE) uniform sampler2D Diffuse;

in block
{
	vec2 Texcoord;
} In;

layout(location = FRAG_COLOR, index = 0) out vec4 Color;

float textureLevel(in sampler2D Sampler, in vec2 Texcoord)
{
	vec2 TextureSize = vec2(textureSize(Sampler, 0));

	float LevelCount = max(log2(TextureSize.x), log2(TextureSize.y));

	vec2 dx = dFdxCoarse(Texcoord * TextureSize);
	vec2 dy = dFdyCoarse(Texcoord * TextureSize);
	float d = max(dot(dx, dx), dot(dy, dy));

	d = clamp(d, 1.0, pow(2, (LevelCount - 1) * 2));

	return 0.5 * log2(d);
}

void main()
{
	float Level = textureLevel(Diffuse, In.Texcoord);

	Color = textureLod(Diffuse, In.Texcoord, Level);
}
