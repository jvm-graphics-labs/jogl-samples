#version 420 core
#extension GL_ARB_shader_draw_parameters : require
#extension GL_ARB_shader_storage_buffer_object : require

#define DRAW_MAX		3
#define DRAW_ID			5
#define VERTEX			0
#define TRANSFORM0		1
#define INDIRECTION		3

precision highp float;
precision highp int;
layout(std140, column_major) uniform;
layout(std430, column_major) buffer; //AMD bug

layout(binding = INDIRECTION) uniform indirection
{
	int Transform[DRAW_MAX];
} Indirection;

layout(binding = TRANSFORM0) uniform transform
{
	mat4 MVP[DRAW_MAX];
} Transform;

struct vertex
{
	vec2 Position;
	vec2 Texcoord;
};

layout(std430, binding = VERTEX) buffer mesh
{
	vertex Vertex[];
} Mesh;

out gl_PerVertex
{
	vec4 gl_Position;
};

out block
{
	vec2 Texcoord;
	flat int DrawID;
} Out;

void main()
{
	Out.DrawID = gl_DrawIDARB;
	Out.Texcoord = Mesh.Vertex[gl_VertexID].Texcoord.st;
	gl_Position = Transform.MVP[Indirection.Transform[gl_DrawIDARB]] * vec4(Mesh.Vertex[gl_VertexID].Position, 0.0, 1.0);
}
