#version 420 core

#define POSITION	0
#define COLOR		3
#define TEXCOORD	4
#define FRAG_COLOR	0

#define TRANSFORM0	1

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

layout(binding = TRANSFORM0) uniform Transform
{
    mat4 mvp;
} transform;

layout(location = POSITION) in vec2 position;

out gl_PerVertex
{
    vec4 gl_Position;
};

void main()
{	
    gl_Position = transform.mvp * vec4(position.x - 1.0 + gl_InstanceID, position.y, 0.0, 1.0);
}
