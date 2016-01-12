#version 420 core

#define POSITION	0
#define TRANSFORM0	1

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform int instance;

layout(binding = TRANSFORM0) uniform Transform
{
    mat4 mvp[2];
} transform;

out gl_PerVertex
{
    vec4 gl_Position;
};

layout(location = POSITION) in vec2 position;

void main()
{
    gl_Position = transform.mvp[instance] * vec4(position, 0.0, 1.0);
}
