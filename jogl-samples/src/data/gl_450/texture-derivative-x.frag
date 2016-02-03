#version 430 core
#extension GL_ARB_derivative_control : require

#define FRAG_COLOR	0
#define DIFFUSE		0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

layout(binding = DIFFUSE) uniform sampler2D diffuse;

in Block
{
    vec2 texCoord;
} inBlock;

layout(location = FRAG_COLOR, index = 0) out vec4 color;

float textureLevel(in sampler2D sampler, in vec2 texCoord)
{
    vec2 textSize = vec2(textureSize(sampler, 0));

    float levelCount = max(log2(textSize.x), log2(textSize.y));

    vec2 dx = dFdxCoarse(texCoord * textSize);
    vec2 dy = dFdyCoarse(texCoord * textSize);
    float d = max(dot(dx, dx), dot(dy, dy));

    d = clamp(d, 1.0, pow(2, (levelCount - 1) * 2));

    return 0.5 * log2(d);
}

void main()
{
    float level = textureLevel(diffuse, inBlock.texCoord);

    color = textureLod(diffuse, inBlock.texCoord, level);
}
