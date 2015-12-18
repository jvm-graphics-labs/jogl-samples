#version 330 core

#define FRAG_COLOR		0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform sampler2D diffuse;

in Block
{
    vec2 texCoord;
} inBlock;

layout(location = FRAG_COLOR, index = 0) out vec4 color0;
layout(location = FRAG_COLOR, index = 1) out vec4 color1;

void main()
{
    color0.rgba = texture(diffuse, inBlock.texCoord).rgba * 0.5;
    color1.rgba = texture(diffuse, inBlock.texCoord).bgra * 0.5;
}
