#version 430 core
#extension GL_ARB_shader_texture_image_samples : require

#define DIFFUSE		0
#define FRAG_COLOR	0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

layout(binding = DIFFUSE) uniform sampler2DMS diffuse;

in Block
{
    vec2 texCoord;
} inBlock;

layout(location = FRAG_COLOR, index = 0) out vec4 color;

void main()
{
    // integer UV coordinates, needed for fetching multisampled texture
    ivec2 texCoord = ivec2(textureSize(diffuse) * inBlock.texCoord);

    vec4 temp = vec4(0.0);
    float samples = textureSamples(diffuse);

    // For each of the samples
    for(int i = 0; i < samples; ++i)
        temp += texelFetch(diffuse, texCoord, i);

    color = temp / samples;
}
