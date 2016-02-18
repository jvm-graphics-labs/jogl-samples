#version 450 core

#define FRAG_COLOR	0
#define DIFFUSE		0

layout(binding = DIFFUSE) uniform sampler2D diffuse;

in Vert
{
    vec2 texCoord;
} vert;

layout(location = FRAG_COLOR, index = 0) out vec4 color;

void main()
{
    color = texture(diffuse, interpolateAtSample(vert.texCoord, gl_SampleID));
}
