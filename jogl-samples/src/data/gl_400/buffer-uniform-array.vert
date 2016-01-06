#version 400 core

#define POSITION	0
#define TRANSFORM0	1
#define TRANSFORM1	2	

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform Transform
{
    mat4 mvp;
} transform[2];

out gl_PerVertex
{
    vec4 gl_Position;
};

out Block
{
    flat int instance;
} outBlock;

layout(location = POSITION) in vec2 position;

void main()
{
    outBlock.instance = gl_InstanceID;
    gl_Position = transform[gl_InstanceID].mvp * vec4(position, 0.0, 1.0);
}
