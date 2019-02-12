#version 450 core
#extension GL_NV_shader_thread_group : require
#define POSITION	0

#define TRANSFORM0	1

out gl_PerVertex
{
	vec4 gl_Position;
};

out perVertex
{
	flat uint ExecutionUnit;
} PerVertex;

layout(binding = TRANSFORM0) uniform transform
{
	mat4 MVP;
} Transform;

layout(location = POSITION) in vec2 Position;

void main()
{
	gl_Position = Transform.MVP * vec4(Position, 0.0, 1.0);
	PerVertex.ExecutionUnit = gl_SMIDNV;
}
