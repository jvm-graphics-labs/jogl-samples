#version 150 core

layout(std140) uniform;

uniform material
{
    vec4 diffuse;
} Material;

out vec4 color;

void main()
{
    color = Material.diffuse;
}