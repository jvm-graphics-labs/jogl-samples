#version 150 core

layout(std140) uniform;

const float luminance[2] = float[2](1.0, 0.5);

in Block
{
    flat int index;
    vec4 color;
} inBlock;

out vec4 color;

void main()
{
    color = inBlock.color * luminance[inBlock.index];
}