#version 150 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform sampler2D diffuse;

in Block
{
    vec2 texCoord;
} inBlock;

out vec4 color;

void main()
{
    if(length(inBlock.texCoord - 0.5) > 0.5)
        discard;

    color = texture(diffuse, inBlock.texCoord);
}
