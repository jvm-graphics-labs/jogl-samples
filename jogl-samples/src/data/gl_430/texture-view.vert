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

layout(location = POSITION) in vec3 position;
layout(location = TEXCOORD) in vec2 texCoord;

out gl_PerVertex
{
    vec4 gl_Position;
};

out Block
{
    vec2 texCoord;
    mediump int instance;
} outBlock;

void main()
{	
    outBlock.texCoord = texCoord;
    outBlock.instance = int(gl_InstanceID);
    gl_Position = transform.mvp * vec4(position, 1.0);
}
