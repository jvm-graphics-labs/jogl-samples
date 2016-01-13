#version 420 core

#define POSITION	0

#define TRANSFORM0	1

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

layout(binding = TRANSFORM0) uniform Transform
{
    mat4 mvp;
} transform;

layout(location = POSITION) in vec2 position;

out gl_PerVertex
{
    vec4 gl_Position;
};

out Block
{
    float instance;
} outBlock;

void main()
{	
    gl_Position = transform.mvp * vec4(position, float(gl_InstanceID) * 0.25 - 0.5, 1.0);
    outBlock.instance = float(gl_InstanceID);
}
