#version 150 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform Transform
{
    mat4 mvp[2];
} transform;

in vec2 position;

out Block
{
    flat int instance;
} outBlock;

void main()
{
    gl_Position = transform.mvp[gl_InstanceID] * vec4(position, 0.0, 1.0);
    outBlock.instance = gl_InstanceID;
}