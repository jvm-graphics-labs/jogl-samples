#version 410 core

#define POSITION	0
#define DRAW_ID		5
#define TRANSFORM0	1
#define TRANSFORM1	2

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform Transform
{
    mat4 mvp;
} transform[2];

out gl_PerVertex
{
    vec4 gl_Position;
};

out Block
{
    flat int instance;
} outBlock;

layout(location = POSITION) in vec2 position;
layout(location = DRAW_ID) in int instance;

void main()
{
    outBlock.instance = instance;
    gl_Position = transform[instance].mvp * vec4(position, 0.0, 1.0);
}
