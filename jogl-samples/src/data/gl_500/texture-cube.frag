#version 420 core

#define POSITION	0
#define COLOR		3
#define TEXCOORD	4
#define REFLECT		6
#define FRAG_COLOR	0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

layout(binding = 0) uniform samplerCubeArray environment;

in Block
{
    vec3 reflect_;
} inBlock;

layout(location = FRAG_COLOR, index = 0) out vec4 color;

void main()
{
    color = texture(environment, vec4(inBlock.reflect_, 0.0));
}
