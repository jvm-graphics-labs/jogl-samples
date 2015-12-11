#version 150 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform sampler2D diffuse;

in vec2 gl_PointCoord;

in Block
{
    vec4 color;
} inBlock;

out vec4 color;

void main()
{
    if  (length(gl_PointCoord - 0.5) > 0.5)
        discard;

    color = inBlock.color * texture(diffuse, gl_PointCoord);
}

