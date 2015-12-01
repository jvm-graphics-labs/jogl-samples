#version 150 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform sampler2D diffuse;
uniform sampler2DShadow shadow;

in Block
{
    vec4 color;
    vec4 shadowCoord;
} inBlock;

out vec4 color;

void main()
{
    vec4 shadowCoord = inBlock.shadowCoord;
    shadowCoord.z -= 0.005;

    vec4 diffuse = inBlock.color;

    float visibility = mix(0.5, 1.0, texture(shadow, shadowCoord.xyz));

    color = visibility * diffuse;
}
