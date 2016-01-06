#version 400 core

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

void main()
{	
    gl_Position = transform.depthMvp * vec4(position, 1.0);
}
