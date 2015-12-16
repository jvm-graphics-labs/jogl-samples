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

float textureLevel(in sampler2D sampler, in vec2 texCoord)
{
    vec2 textSize = vec2(textureSize(sampler, 0));

    float levelCount = max(log2(textSize.x), log2(textSize.y));

    vec2 dx = dFdx(texCoord * textSize);
    vec2 dy = dFdy(texCoord * textSize);
    float d = max(dot(dx, dx), dot(dy, dy));

    d = clamp(d, 1.0, pow(2, (levelCount - 1) * 2));

    return 0.5 * log2(d);
}

void main()
{
    float level = textureLevel(diffuse, inBlock.texCoord);

    color = textureLod(diffuse, inBlock.texCoord, level);
}
