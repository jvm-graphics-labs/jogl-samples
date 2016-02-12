#version 420 core

#define FRAG_COLOR	0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

layout(binding = 0) uniform sampler2D diffuse;

layout(origin_upper_left) in vec4 gl_FragCoord;

in Block
{
    vec2 texCoord;
} inBlock;

layout(location = FRAG_COLOR, index = 0) out vec4 color;

vec4 textureTrilinear(in sampler2D sampler, in vec2 texCoord);
vec4 textureBicubicLod(in sampler2D sampler, in vec2 texCoord, in int lod);

void main()
{
    //color = textureTrilinear(diffuse, inBlock.texCoord);
    color = textureBicubicLod(diffuse, inBlock.texCoord, 0);
}
