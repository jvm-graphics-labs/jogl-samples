#version 420 core

#define POSITION	0
#define COLOR		3
#define TEXCOORD	4
#define INSTANCE	7
#define FRAG_COLOR	0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform sampler2D diffuse;

in Vert
{
    vec2 texCoord;
} inVert;

layout(origin_upper_left) in vec4 gl_FragCoord;
layout(location = FRAG_COLOR, index = 0) out vec3 color;

vec3 texelAverage(in vec2 texCoord, in ivec2 offset)
{
	vec4 red = textureGatherOffset(diffuse, texCoord, offset, 0);
	vec4 green = textureGatherOffset(diffuse, texCoord, offset, 1);
	vec4 blue = textureGatherOffset(diffuse, texCoord, offset, 2);

	vec3 texel0 = vec3(red[0], green[0], blue[0]); 
	vec3 texel1 = vec3(red[1], green[1], blue[1]); 
	vec3 texel2 = vec3(red[2], green[2], blue[2]); 
	vec3 texel3 = vec3(red[3], green[3], blue[3]); 

	return (texel0 + texel1 + texel2 + texel3) * 0.25;
}

void main()
{
    vec2 size = textureSize(diffuse, 0) - 1;
    vec2 texCoord = inVert.texCoord * size;
    ivec2 coord = ivec2(inVert.texCoord * size);

    color = vec3(0);
    color += texelAverage(inVert.texCoord, ivec2( 8, 0));
    color += texelAverage(inVert.texCoord, ivec2( 0, 8));
    color += texelAverage(inVert.texCoord, ivec2(-8, 0));
    color += texelAverage(inVert.texCoord, ivec2( 0,-8));
    color *= 0.25f;
}
