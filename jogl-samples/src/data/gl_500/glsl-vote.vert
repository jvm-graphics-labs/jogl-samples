#version 440 core
#extension GL_ARB_shader_storage_buffer_object : require

#define TRANSFORM0	1
#define VERTEX		0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;
// layout(std430, column_major) buffer; AMD bug

layout(binding = TRANSFORM0) uniform Transform
{
    mat4 mvp;
} transform;

struct Vertex
{
    vec2 position;
    vec2 texCoord;
};

layout(std430, binding = VERTEX) buffer Mesh
{
    Vertex vertex[];
} mesh;

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
    outBlock.texCoord = mesh.vertex[gl_VertexID].texCoord;
    gl_Position = transform.mvp * vec4(mesh.vertex[gl_VertexID].position, 0.0, 1.0);
}
