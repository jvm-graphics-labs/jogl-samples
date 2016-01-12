#version 420 core

#define FRAG_COLOR		0
#define DIFFUSE			0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

in vec4 gl_FragCoord;
layout(binding = 0, rgba8) coherent uniform image2D diffuse;

layout(location = FRAG_COLOR, index = 0) out vec4 color;

void main()
{
    color = imageLoad(diffuse, ivec2(gl_FragCoord.xy));
}
