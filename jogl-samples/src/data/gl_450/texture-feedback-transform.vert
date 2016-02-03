#version 440 core

#define POSITION	0
#define COLOR		3
#define FRAG_COLOR	0

#define TRANSFORM0		1

precision highp float;
precision highp int;
layout(std140, column_major) uniform;
//layout(std430, column_major) buffer; AMD bug

layout(binding = TRANSFORM0) uniform Transform
{
    mat4 mvp;
} transform;

layout(location = POSITION) in vec4 position;

layout(xfb_buffer = 0, xfb_stride = 32) out;

out Block
{
    layout(xfb_buffer = 0, xfb_offset = 16) vec4 color;
} outBlock;

out gl_PerVertex
{
    layout(xfb_buffer = 0, xfb_offset = 0) vec4 gl_Position;
};

void main()
{	
    gl_Position = transform.mvp * position;
    outBlock.color = vec4(clamp(vec2(position), 0.0, 1.0), 0.0, 1.0);
}
