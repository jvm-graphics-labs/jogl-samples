#version 150 core

precision highp float;
precision highp int;
layout(std140) uniform;

uniform Material
{
    vec4 diffuse[2];
} material;

in Block
{
    flat int instance;
} inBlock;

out vec4 color;

void main()
{
    color = material.diffuse[inBlock.instance];
}