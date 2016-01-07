#version 400 core
#extension GL_NV_gpu_shader5 : require

#define FRAG_COLOR	0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform sampler2D diffuse[2];

in Vert
{
    vec2 texCoord;
} inVert;

layout(location = FRAG_COLOR, index = 0) out vec4 color;

void main()
{
    int index = (int(inVert.texCoord.x * 8) + int(inVert.texCoord.y * 8)) % 2;
    color = texture(diffuse[index], inVert.texCoord);
}
