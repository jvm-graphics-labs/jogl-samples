#version 400 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform sampler2DShadow Shadow;

in block
{
	vec4 Color;
	vec4 ShadowCoord;
} In;

out vec4 Color;

void main()
{
	vec4 ShadowCoord = In.ShadowCoord;
	ShadowCoord.z -= 0.005;

	vec4 Gather = textureGather(Shadow, ShadowCoord.xy, ShadowCoord.z);
	float Texel00 = Gather.w;
	float Texel10 = Gather.z;
	float Texel11 = Gather.y;
	float Texel01 = Gather.x;

	vec2 ShadowSize = textureSize(Shadow, 0);
	vec2 TexelCoord = ShadowCoord.xy * ShadowSize;
	vec2 SampleCoord = fract(TexelCoord + 0.5);

	float Texel0 = mix(Texel00, Texel01, SampleCoord.y);
	float Texel1 = mix(Texel10, Texel11, SampleCoord.y);
	float Visibility = mix(Texel0, Texel1, SampleCoord.x);

	Color = vec4(mix(vec4(0.5), vec4(1.0), Visibility) * In.Color);
}
