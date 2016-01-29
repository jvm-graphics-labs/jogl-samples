#version 420 core
#extension GL_ARB_texture_query_levels : require
#extension GL_ARB_fragment_layer_viewport : require

#define POSITION	0
#define COLOR		3
#define TEXCOORD	4
#define FRAG_COLOR	0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

layout(binding = 0) uniform sampler2DArray diffuse[2];

vec4 textureNearest(in sampler2DArray sampler, in vec2 texCoord) {

    int lodNearest = int(round(textureQueryLod(sampler, texCoord).x));

    ivec2 textSize = textureSize(sampler, lodNearest).xy;
    ivec2 texelCoord = ivec2(textSize * texCoord);

    return texelFetch(sampler, ivec3(texelCoord, 0), lodNearest);
}

vec4 fetchBilinear(in sampler2DArray sampler, in vec2 interpolant, in ivec2 texelCoords[4], in int lod) {

    vec4 texel00 = texelFetch(sampler, ivec3(texelCoords[0], 0), lod);
    vec4 texel10 = texelFetch(sampler, ivec3(texelCoords[1], 0), lod);
    vec4 texel11 = texelFetch(sampler, ivec3(texelCoords[2], 0), lod);
    vec4 texel01 = texelFetch(sampler, ivec3(texelCoords[3], 0), lod);

    vec4 texel0 = mix(texel00, texel01, interpolant.y);
    vec4 texel1 = mix(texel10, texel11, interpolant.y);
    return mix(texel0, texel1, interpolant.x);
}

vec4 textureBilinear(in sampler2DArray sampler, in vec2 texCoord) {

    int lod = int(round(textureQueryLod(sampler, texCoord).x));

    ivec2 size = textureSize(sampler, lod).xy;
    vec2 texelCoord = texCoord * size - 0.5;
    ivec2 texelIndex = ivec2(texelCoord);

    ivec2 texelCoords[] = ivec2[4](
            texelIndex + ivec2(0, 0),
            texelIndex + ivec2(1, 0),
            texelIndex + ivec2(1, 1),
            texelIndex + ivec2(0, 1));

    return fetchBilinear(
            sampler, 
            fract(texelCoord), 
            texelCoords, 
            lod);
}

vec4 textureBilinearLod(in sampler2DArray sampler, in vec2 texCoord, in int lod) {

    ivec2 size = textureSize(sampler, lod).xy;
    vec2 texelCoord = texCoord * size - 0.5;
    ivec2 texelIndex = ivec2(texelCoord);

    ivec2 texelCoords[] = ivec2[4](
            texelIndex + ivec2(0, 0),
            texelIndex + ivec2(1, 0),
            texelIndex + ivec2(1, 1),
            texelIndex + ivec2(0, 1));

    return fetchBilinear(
            sampler, 
            fract(texelCoord), 
            texelCoords, 
            lod);
}

vec4 textureTrilinear(in sampler2DArray sampler, in vec2 texCoord) {

    float lod = textureQueryLod(sampler, texCoord).x;

    int lodMin = int(floor(lod));
    int lodMax = int(ceil(lod));

    vec4 texelMin = textureBilinearLod(sampler, texCoord, lodMin);
    vec4 texelMax = textureBilinearLod(sampler, texCoord, lodMax);

    return mix(texelMin, texelMax, fract(lod));
}

in Block
{
    vec2 texCoord;
    flat mediump int instance;
} inBlock;

layout(location = FRAG_COLOR, index = 0) out vec4 color;

void main()
{
    if(textureQueryLevels(diffuse[gl_ViewportIndex]) == 1)
        color = textureNearest(diffuse[gl_ViewportIndex], inBlock.texCoord.st);
    else
        color = textureTrilinear(diffuse[gl_ViewportIndex], inBlock.texCoord.st);
}
