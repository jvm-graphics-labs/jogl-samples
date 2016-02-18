#version 440 core
#extension GL_ARB_shader_group_vote : enable

#define FRAG_COLOR	0
#define DIFFUSE		0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

in vec4 gl_FragCoord;

layout(binding = DIFFUSE) uniform sampler2D diffuse;

in Block
{
    vec2 texCoord;
} inBlock;

layout(location = FRAG_COLOR, index = 0) out vec4 color;

void main()
{
    float lod = 0.0;
    if(allInvocationsARB(gl_FragCoord.y / gl_FragCoord.x < 3.0 / 4.0))
        lod = 5.0;

    color = textureLod(diffuse, inBlock.texCoord.st, lod);
}

