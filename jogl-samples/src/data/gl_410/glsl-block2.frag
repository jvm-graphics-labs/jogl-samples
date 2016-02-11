#version 410 core

in Block
{
    vec4 color;
    vec2 texCoord;
    flat int instance;
} inBlock;

vec4 colorAddition(vec4 color)
{
    return (color + inBlock.color) * 0.5;
}
