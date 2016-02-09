#version 150 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform mat4 mvp;

in vec4 position;

out Block
{
    vec4 color;
} outBlock;

void main()
{	
    gl_Position = mvp * position;
    outBlock.color = vec4(clamp(vec2(position), 0.0, 1.0), 0.0, 1.0);
}
