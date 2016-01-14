#version 420 core

#define POSITION		0
#define COLOR			3
#define TEXCOORD		4
#define INSTANCE		7
#define FRAG_COLOR		0
#define TRANSFORM0	1

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

layout(binding = TRANSFORM0) uniform Transform
{
    mat4 mvp;
} transform;

layout(location = POSITION) in vec2 position;
layout(location = TEXCOORD) in vec2 texCoord;

out Block
{
    vec2 texCoord;
    float instance;
} outBlock;

void main()
{
    vec2 offset = vec2(gl_InstanceID % 5, gl_InstanceID / 5) - vec2(2, 1);
    outBlock.instance = gl_InstanceID;
    outBlock.texCoord = texCoord;
    gl_Position = transform.mvp * vec4(position + offset, 0.0, 1.0);
}
