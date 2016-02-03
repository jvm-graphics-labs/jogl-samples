#version 420 core

#define FRAG_COLOR		0
#define DIFFUSE			0
#define COLORBUFFER		1

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

layout(binding = DIFFUSE) uniform sampler2D diffuse;
layout(binding = COLORBUFFER) uniform sampler2D colorbuffer;

in vec4 gl_FragCoord;

in Block
{
    vec2 texCoord;
} inBlock;

layout(location = FRAG_COLOR, index = 0) out vec4 color;

void main()
{
    color = texture(diffuse, inBlock.texCoord.st) * 0.75 + texelFetch(colorbuffer, ivec2(gl_FragCoord.xy), 0) * 0.25;
}
