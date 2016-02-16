#version 420 core

#define POSITION		0
#define COLOR			3
#define TEXCOORD		4
#define DRAW_ID			5

#define FRAG_COLOR		0

#define TRANSFORM0		1
#define INDIRECTION		3

#define MAX_DRAW		3

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

layout(binding = INDIRECTION) uniform Indirection
{
    int transform[MAX_DRAW];
} indirection;

layout(binding = TRANSFORM0) uniform Transform
{
    mat4 mvp[MAX_DRAW];
} transform;

layout(location = POSITION) in vec2 position;
layout(location = TEXCOORD) in vec2 texCoord;
layout(location = DRAW_ID) in int drawID;

out gl_PerVertex
{
    vec4 gl_Position;
};

out Block
{
    vec2 texCoord;
    flat int drawID;
} outBlock;

void main()
{
    outBlock.drawID = drawID;
    outBlock.texCoord = texCoord.st;
    gl_Position = transform.mvp[indirection.transform[drawID]] * vec4(position, 0.0, 1.0);
}
