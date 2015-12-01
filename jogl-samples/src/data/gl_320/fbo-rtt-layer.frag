#version 150 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform sampler2DArray diffuse;
uniform int layer;

in Block
{
    vec2 texCoord;
} inBlock;

out vec4 color;

void main()
{
    color = texture(diffuse, vec3(inBlock.texCoord, float(layer)));
}
