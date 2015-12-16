#version 150 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform sampler2D diffuse;

in Block
{
    vec2 texCoord;
} inBlock;

out vec4 color;

vec4 catmullRom(in vec4 a, in vec4 b, in vec4 c, in vec4 d, in float s)
{
	mat4 catmullRomMat = mat4(
		vec4(-1.0, 2.0,-1.0, 0.0), 
		vec4( 3.0,-5.0, 0.0, 2.0),
		vec4(-3.0, 4.0, 1.0, 0.0),
		vec4( 1.0,-1.0, 0.0, 0.0));

	vec4 expo = vec4(s * s * s, s * s, s, 1);

	return 0.5 * expo * catmullRomMat * mat4(
		a[0], b[0], c[0], d[0],
		a[1], b[1], c[1], d[1],
		a[2], b[2], c[2], d[2],
		a[3], b[3], c[3], d[3]);
}

vec4 textureCatmullrom(in sampler2D sampler, in vec2 texCoord)
{
	ivec2 textSize = textureSize(sampler, 0) - ivec2(1);
	ivec2 texelCoord = ivec2(textSize * texCoord);

	vec4 texel00 = texelFetchOffset(sampler, texelCoord, 0, ivec2(-1,-1));
	vec4 texel10 = texelFetchOffset(sampler, texelCoord, 0, ivec2( 0,-1));
	vec4 texel20 = texelFetchOffset(sampler, texelCoord, 0, ivec2( 1,-1));
	vec4 texel30 = texelFetchOffset(sampler, texelCoord, 0, ivec2( 2,-1));

	vec4 texel01 = texelFetchOffset(sampler, texelCoord, 0, ivec2(-1, 0));
	vec4 texel11 = texelFetchOffset(sampler, texelCoord, 0, ivec2( 0, 0));
	vec4 texel21 = texelFetchOffset(sampler, texelCoord, 0, ivec2( 1, 0));
	vec4 texel31 = texelFetchOffset(sampler, texelCoord, 0, ivec2( 2, 0));

	vec4 texel02 = texelFetchOffset(sampler, texelCoord, 0, ivec2(-1, 1));
	vec4 texel12 = texelFetchOffset(sampler, texelCoord, 0, ivec2( 0, 1));
	vec4 texel22 = texelFetchOffset(sampler, texelCoord, 0, ivec2( 1, 1));
	vec4 texel32 = texelFetchOffset(sampler, texelCoord, 0, ivec2( 2, 1));

	vec4 texel03 = texelFetchOffset(sampler, texelCoord, 0, ivec2(-1, 2));
	vec4 texel13 = texelFetchOffset(sampler, texelCoord, 0, ivec2( 0, 2));
	vec4 texel23 = texelFetchOffset(sampler, texelCoord, 0, ivec2( 1, 2));
	vec4 texel33 = texelFetchOffset(sampler, texelCoord, 0, ivec2( 2, 2));

	vec2 splineCoord = fract(textSize * texCoord);

	vec4 row0 = catmullRom(texel00, texel10, texel20, texel30, splineCoord.x);
	vec4 row1 = catmullRom(texel01, texel11, texel21, texel31, splineCoord.x);
	vec4 row2 = catmullRom(texel02, texel12, texel22, texel32, splineCoord.x);
	vec4 row3 = catmullRom(texel03, texel13, texel23, texel33, splineCoord.x);

	return catmullRom(row0, row1, row2, row3, splineCoord.y);
}

void main()
{
    color = textureCatmullrom(diffuse, inBlock.texCoord);
}

