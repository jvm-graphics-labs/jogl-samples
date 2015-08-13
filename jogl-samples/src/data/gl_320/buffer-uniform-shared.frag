#version 150 core

layout(std140) uniform;

uniform Material
{
    vec4 diffuse;
} material;

out vec4 color;

void main()
{
    color = material.diffuse;
}