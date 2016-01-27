#version 420 core

#define POSITION	0
#define COLOR		3
#define TEXCOORD	4
#define FRAG_COLOR	0

#define VERTEX		0
#define TRANSFORM0		1

#define SAMPLER_DIFFUSE			0 
#define SAMPLER_POSITION		4 
#define SAMPLER_TEXCOORD		5 
#define SAMPLER_COLOR			6 

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

layout(binding = TRANSFORM0) uniform Transform
{
    mat4 mvp;
} transform;

layout(binding = SAMPLER_POSITION) uniform samplerBuffer position;
layout(binding = SAMPLER_TEXCOORD) uniform samplerBuffer texCoord;
layout(binding = SAMPLER_COLOR) uniform samplerBuffer color;

out gl_PerVertex
{
    vec4 gl_Position;
};

out Block
{
    vec4 texCoord;
    vec4 color;
} outBlock;

void main()
{	
    outBlock.texCoord = texelFetch(texCoord, gl_VertexID);
    outBlock.color = texelFetch(color, gl_VertexID);
    gl_Position = texelFetch(position, gl_VertexID);
}
