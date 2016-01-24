#version 420 core

#define FRAG_COLOR	0

layout(binding = 0) uniform sampler2D diffuse;

in vec4 gl_FragCoord;
layout(location = FRAG_COLOR, index = 0) out vec4 color;

void main()
{
    color = texelFetch(diffuse, ivec2(gl_FragCoord.xy * 0.125), 0);
}
