#version 330 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform Transform
{
    mat4 mvp;
} transform;

in vec4 position;

void main()
{	
    gl_Position = transform.mvp * position;
}

