#version 150 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

const vec4 color[4] = vec4[]
(
    vec4(1.0, 0.0, 0.0, 1.0),
    vec4(1.0, 1.0, 0.0, 1.0),
    vec4(0.0, 1.0, 0.0, 1.0),
    vec4(0.0, 0.0, 1.0, 1.0)
);

in Block
{
    flat int instance;
} inBlock;

out vec4 fragColor;

void main()
{
    fragColor = color[inBlock.instance];
}
