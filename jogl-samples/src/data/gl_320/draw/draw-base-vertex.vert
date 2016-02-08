#version 150 core

layout(std140) uniform;

uniform Transform
{
    mat4 mvp;
} transform;

in vec3 position;
in vec4 color;

out Block
{
    flat int index;
    vec4 color;
} outBlock;

void main()
{
    gl_Position = transform.mvp * vec4(position, 1.0);
    outBlock.color = color;
    outBlock.index = gl_VertexID / 4;
}
