#version 420 core

#define POSITION	0
#define COLOR		3

#define MATERIAL	0
#define TRANSFORM0	1
#define TRANSFORM1	2	

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

layout(binding = TRANSFORM0) uniform Transform
{
    mat4 mvp;
} transform;

layout(location = POSITION) in vec2 position;
layout(location = COLOR) in vec4 color;

out gl_PerVertex
{
    vec4 gl_Position;
};

out Block
{
    vec4 color;
} outBlock;

void main()
{	
    gl_Position = transform.mvp * vec4(position, float(gl_InstanceID) * 0.25 - 0.5, 1.0);
    outBlock.color = color;
}
