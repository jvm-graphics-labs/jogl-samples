#version 150 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform usampler2D diffuse;

in Block
{
    vec2 texCoord;
} inBlock;

out vec4 color;

void main()
{
    uvec4 intColor = texture(diffuse, inBlock.texCoord);

    color = vec4(intColor) / 255.0;
}
