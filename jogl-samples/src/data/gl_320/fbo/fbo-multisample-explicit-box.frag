#version 150 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform sampler2DMS diffuse;

in Block
{
    vec2 texCoord;
} inBlock;

out vec4 color;

void main()
{
    // integer UV coordinates, needed for fetching multisampled texture
    ivec2 texCoord = ivec2(textureSize(diffuse) * inBlock.texCoord);

    vec4 temp = vec4(0.0);

    // For each of the 4 samples
    for(int i = 0; i < 4; ++i)
        temp += texelFetch(diffuse, texCoord, i);

    color = temp * 0.25;
}
