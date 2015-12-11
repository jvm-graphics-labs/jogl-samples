#version 150

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform samplerBuffer diffuse;

in Block
{
    flat int instance;
} inBlock;

out vec4 color;

void main()
{
    color = texelFetch(diffuse, inBlock.instance);
}
