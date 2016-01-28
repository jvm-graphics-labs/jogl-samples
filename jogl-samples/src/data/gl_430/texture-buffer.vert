#version 430

#define POSITION		0
#define COLOR			3
#define TEXCOORD		4
#define INSTANCE		7
#define FRAG_COLOR		0

#define TRANSFORM0		1

precision highp float;
precision highp int;
layout(std140, column_major) uniform;
layout(std430, column_major) buffer;

layout(binding = 0) uniform samplerBuffer displacement;

layout(binding = TRANSFORM0) uniform Transform
{
    mat4 mvp;
} transform;

layout(location = POSITION) in vec2 position;

out gl_PerVertex
{
    vec4 gl_Position;
};

out Block
{
    flat int instance;
} outBlock;

void main()
{	
    outBlock.instance = gl_InstanceID;
    gl_Position = transform.mvp * (vec4(position, 0.0, 0.0) + texelFetch(displacement, gl_InstanceID));
}
