#version 420 core
#extension GL_ARB_shader_storage_buffer_object : require

// Attributes
#define VERTEX		0
// Uniforms
#define TRANSFORM0      1

precision highp float;
precision highp int;
layout(std140, column_major) uniform;
layout(std430, column_major) buffer;

struct Vertex
{
    vec2 position;
    vec2 texCoord;
};

layout(location = VERTEX) in Vertex vertex;

layout(binding = TRANSFORM0) uniform Transform
{
    mat4 mvp;
} transform;

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
    outBlock.texCoord = vertex.texCoord;
    gl_Position = transform.mvp * vec4(vertex.position, 0, 1);
}