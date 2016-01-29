#version 420 core

#include texture-storage.glsl
#line 5

layout(binding = DIFFUSE) uniform sampler2DArray diffuse;

in Block
{
    vec2 texCoord;
} inBlock;

layout(location = FRAG_COLOR, index = 0) out vec4 color;

void main()
{
    color = texture(diffuse, vec3(inBlock.texCoord.st, 0.0));
}
