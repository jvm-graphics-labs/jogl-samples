#version 420 core
#extension GL_ARB_shader_storage_buffer_object : require

#define POSITION	0
#define COLOR		3
#define TEXCOORD	4
#define FRAG_COLOR	0

#define VERTEX		0
#define TRANSFORM0		1

precision highp float;
precision highp int;
layout(std140, column_major) uniform;
layout(std430, column_major) buffer;

struct Vertex
{
    vec4 position;
    vec4 texCoord;
    vec4 color;
};

layout(binding = TRANSFORM0) uniform Transform
{
    mat4 mvp;
} transform;

layout(binding = VERTEX) buffer Mesh
{
    Vertex vertex[];
} mesh;

out gl_PerVertex
{
    vec4 gl_Position;
};

out Block
{
    vec4 texCoord;
    vec4 color;
} outBlock;

void main()
{	
    outBlock.texCoord = mesh.vertex[gl_VertexID].texCoord;
    if(gl_VertexID % 2 != 0)
        outBlock.color = vec4(1.0);
    else
        outBlock.color = mesh.vertex[gl_VertexID].color;
    gl_Position = transform.mvp * mesh.vertex[gl_VertexID].position;
}