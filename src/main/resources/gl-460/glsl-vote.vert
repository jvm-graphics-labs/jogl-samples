#version 440 core
#extension GL_ARB_shader_storage_buffer_object : require

#define TRANSFORM0	1
#define VERTEX		0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;
// layout(std430, column_major) buffer; AMD bug

layout(binding = TRANSFORM0) uniform transform
{
	mat4 MVP;
	mat4 MV;
} Transform;

struct vertex
{
	vec2 Position;
};

layout(std430, binding = VERTEX) buffer mesh
{
	vertex Vertex[];
} Mesh;

out gl_PerVertex
{
	vec4 gl_Position;
	float gl_PointSize;
};

void main()
{
	vec4 Position = vec4(Mesh.Vertex[gl_VertexID].Position, 0.0, 1.0);
	gl_Position = Transform.MVP * Position;
	gl_PointSize = 256.0 / -(Transform.MV * Position).z;
}
