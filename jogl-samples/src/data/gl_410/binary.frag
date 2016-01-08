#version 410 core

#define FRAG_COLOR	0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform sampler2D diffuse;

in Block
{
    vec2 texCoord;
    vec3 color;
} inBlock; 

layout(location = FRAG_COLOR, index = 0) out vec4 color;

void main()
{
    color = texture(diffuse, inBlock.texCoord) * vec4(inBlock.color, 1.0);
}
