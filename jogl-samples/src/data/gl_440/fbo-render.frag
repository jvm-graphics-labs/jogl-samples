#version 420 core

#define DIFFUSE		0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

in vec4 gl_FragCoord;

in Block
{
    vec2 texCoord;
} inBlock;

layout(binding = DIFFUSE) uniform sampler2D diffuse;
layout(binding = 0, rgba8) uniform coherent image2D color;

void main()
{
    imageStore(color, ivec2(gl_FragCoord.xy), texture(diffuse, inBlock.texCoord));
}