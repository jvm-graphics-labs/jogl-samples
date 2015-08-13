#version 150 core

layout(std140, column_major) uniform;

uniform Transform
{
    mat4 mvp;
} transform;

in vec2 position;

void main()
{	
    gl_Position = transform.mvp * vec4(position, 0.0, 1.0);
}