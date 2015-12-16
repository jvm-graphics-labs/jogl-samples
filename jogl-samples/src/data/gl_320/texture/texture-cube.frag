#version 150 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform samplerCube environment;

in Block
{
    vec3 refl;
} inBlock;

out vec4 color;

void main()
{
    color = texture(environment, inBlock.refl);
}
