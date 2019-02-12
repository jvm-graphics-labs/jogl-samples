#version 400 core

#define FRAG_COLOR	0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform sampler2DArray Diffuse;

in block
{
	vec2 Texcoord;
} In;

layout(location = FRAG_COLOR, index = 0) out vec4 Color;

vec4 trilinearLod(in sampler2DArray Sampler, in int Layer, in float Level, in vec2 Texcoord)
{
	int LevelMin = int(ceil(Level));
	int LevelMax = int(floor(Level));
	vec2 SizeMin = textureSize(Sampler, LevelMin).xy - 1;
	vec2 SizeMax = textureSize(Sampler, LevelMax).xy - 1;
	vec2 TexcoordMin = Texcoord * SizeMin;
	vec2 TexcoordMax = Texcoord * SizeMax;
	ivec3 CoordMin = ivec3(Texcoord * SizeMin, Layer);
	ivec3 CoordMax = ivec3(Texcoord * SizeMax, Layer);

	vec4 TexelMin00 = texelFetch(Sampler, CoordMin + ivec3(0, 0, Layer), LevelMin);
	vec4 TexelMin10 = texelFetch(Sampler, CoordMin + ivec3(1, 0, Layer), LevelMin);
	vec4 TexelMin11 = texelFetch(Sampler, CoordMin + ivec3(1, 1, Layer), LevelMin);
	vec4 TexelMin01 = texelFetch(Sampler, CoordMin + ivec3(0, 1, Layer), LevelMin);
	
	vec4 TexelMax00 = texelFetch(Sampler, CoordMax + ivec3(0, 0, Layer), LevelMax);
	vec4 TexelMax10 = texelFetch(Sampler, CoordMax + ivec3(1, 0, Layer), LevelMax);
	vec4 TexelMax11 = texelFetch(Sampler, CoordMax + ivec3(1, 1, Layer), LevelMax);
	vec4 TexelMax01 = texelFetch(Sampler, CoordMax + ivec3(0, 1, Layer), LevelMax);
	
	vec4 TexelMin0 = mix(TexelMin00, TexelMin01, fract(TexcoordMin.y));
	vec4 TexelMin1 = mix(TexelMin10, TexelMin11, fract(TexcoordMin.y));
	vec4 TexelMin  = mix(TexelMin0, TexelMin1, fract(TexcoordMin.x));
	
	vec4 TexelMax0 = mix(TexelMax00, TexelMax01, fract(TexcoordMax.y));
	vec4 TexelMax1 = mix(TexelMax10, TexelMax11, fract(TexcoordMax.y));
	vec4 TexelMax  = mix(TexelMax0, TexelMax1, fract(TexcoordMax.x));

	return mix(TexelMax, TexelMin, fract(Level));
}

void main()
{
	vec2 Level = textureQueryLod(Diffuse, In.Texcoord);
	Color = trilinearLod(Diffuse, 0, Level.x, In.Texcoord);
}
