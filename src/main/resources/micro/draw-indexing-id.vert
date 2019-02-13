#version 420 core
#extension GL_ARB_shader_draw_parameters : require

#define POSITION		0
#define TRANSFORM0		1

layout(binding = TRANSFORM0) uniform transform
{
	mat4 MVP;
} Transform;

in int gl_DrawIDARB;
layout(location = POSITION) in vec2 Position;

out block
{
	flat int DrawID;
} Out;

out gl_PerVertex
{
	vec4 gl_Position;
};

void main()
{
	Out.DrawID = gl_DrawIDARB;
	gl_Position = Transform.MVP * vec4(Position, 0.0, 1.0);
}
