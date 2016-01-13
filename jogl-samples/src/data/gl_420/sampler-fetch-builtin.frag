#version 420 core

#define FRAG_COLOR	0
#define DIFFUSE		0

layout(binding = DIFFUSE) uniform sampler2D diffuse;

layout(origin_upper_left) in vec4 gl_FragCoord;

in Block
{
    vec2 texCoord;
} inBlock;

layout(location = FRAG_COLOR, index = 0) out vec4 color;

void main()
{
    color = texture(diffuse, inBlock.texCoord.st);
}
