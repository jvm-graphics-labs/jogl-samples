#version 420 core

#define FRAG_COLOR	0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

in vec4 gl_FragCoord;

layout(binding = 0) uniform sampler2D diffuse;
layout(binding = 1, r32f) writeonly uniform imageBuffer depth;

uvec2 pickingCoord = uvec2(320, 240);

in Block
{
    vec2 texCoord;
} inBlock;

layout(location = FRAG_COLOR, index = 0) out vec4 color;

void main()
{
    if(all(equal(pickingCoord, uvec2(gl_FragCoord.xy))))
    {
        imageStore(depth, 0, vec4(gl_FragCoord.z, 0, 0, 0));
        color = vec4(1, 0, 1, 1);
    }
    else
        color = texture(diffuse, inBlock.texCoord.st);
}
