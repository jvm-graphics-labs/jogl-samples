#version 420 core
#extension GL_ARB_cull_distance : require

#define POSITION	0
#define COLOR		3
#define TEXCOORD	4
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
    float gl_CullDistance[1];
};

out Block
{
    vec4 position;
    vec2 texCoord;
} outBlock;

void main()
{	
    vec4 position = transform.mvp * vec4(position, 1.0);

    outBlock.position = position;
    outBlock.texCoord = texCoord;
    gl_Position = position;
    gl_CullDistance[0] = mix(-1.0, 1.0, position.z > 2);
}
