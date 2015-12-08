#version 150 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform Transform
{
    mat4 mvp;
} transform;

struct Vertex
{
    vec2 position;
    vec2 texCoord;
};

in Vertex vertex;

out Block
{
    vec2 texCoord;
} outBlock;

out gl_PerVertex
{
    vec4 gl_Position;
};

void main()
{	
    outBlock.texCoord = vertex.texCoord;
    gl_Position = transform.mvp * vec4(vertex.position, 0.0, 1.0);
}
