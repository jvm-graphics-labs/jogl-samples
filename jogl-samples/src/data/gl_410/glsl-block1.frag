#version 410 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform sampler2D diffuse;

vec4 colorAddition(vec4 color);

in Block
{
    vec4 color;
    vec2 texCoord;
    flat int instance;
} inBlock;

out vec4 color;

void main()
{
    color = texture(diffuse, inBlock.texCoord) * 0.75 + colorAddition(inBlock.color) * 0.25;
}
