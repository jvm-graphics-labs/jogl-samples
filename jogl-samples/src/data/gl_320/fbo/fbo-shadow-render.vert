#version 150 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform Transform
{
    mat4 mvp;
    mat4 depthMvp;
    mat4 depthBiasMvp;
} transform;

in vec3 position;
in vec4 color;

out Block
{
    vec4 color;
    vec4 shadowCoord;
} outBlock;

void main()
{
    gl_Position = transform.mvp * vec4(position, 1.0);
    outBlock.shadowCoord = transform.depthBiasMvp * vec4(position, 1.0);
    outBlock.color = color;
}
