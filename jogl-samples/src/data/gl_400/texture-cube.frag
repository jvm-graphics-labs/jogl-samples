#version 400 core

#define FRAG_COLOR	0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform samplerCubeArray environment;

in Block
{
    vec3 refl;
} inBlock;

layout(location = FRAG_COLOR, index = 0) out vec4 color;

void main()
{
    color = texture(environment, vec4(inBlock.refl, 0.0));
}
