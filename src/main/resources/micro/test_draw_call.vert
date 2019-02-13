#version 330 core
#extension GL_ARB_shading_language_420pack : require

#define POSITION		0
#define COLOR			3
#define TEXCOORD		4
#define INSTANCE		7
#define FRAG_COLOR		0
#define TRANSFORM0		1

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

layout(binding = TRANSFORM0) uniform transform
{
	mat4 MVP;
} Transform;

layout(location = POSITION) in vec3 Position;
layout(location = COLOR) in vec4 Color;

out gl_PerVertex
{
	vec4 gl_Position;
};

out block
{
	vec4 Color;
} Out;

mat4 ortho(float left, float right, float bottom, float top)
{
	mat4 m = mat4(1.0);
	
	m[0][0] = 2.0 / (right - left);
	m[1][1] = 2.0 / (top - bottom);
	m[2][2] = - 1.0;
	m[3][0] = - (right + left) / (right - left);
	m[3][1] = - (top + bottom) / (top - bottom);
	
	return m;
}

void main()
{	
	Out.Color = Color;
	
	//gl_Position = ortho(0.0, 320.0, 0.0, 240.0) * vec4(Position, 1);
	gl_Position = Transform.MVP * vec4(Position, 1);
}
