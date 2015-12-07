#version 150 core

layout(std140) uniform;

uniform Transform
{
    mat4 mvp;
} transform;

in vec4 position;

void main()
{
    gl_Position = transform.mvp * position;
}

