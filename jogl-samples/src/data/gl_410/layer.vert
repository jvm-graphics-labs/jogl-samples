#version 410 core

#define POSITION	0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

layout(location = POSITION) in vec2 position;

out gl_PerVertex
{
    vec4 gl_Position;
};

void main()
{	
    gl_Position = vec4(position, 0.0, 1.0);
}
