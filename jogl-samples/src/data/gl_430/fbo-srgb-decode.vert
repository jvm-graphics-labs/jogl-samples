#version 420 core

#define POSITION	0
#define COLOR		3
#define TEXCOORD	4
#define TRANSFORM0	1

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
} outBlock;

void main()
{
    outBlock.texCoord = texCoord;
    gl_Position = transform.mvp * vec4(position, 1.0);
}
