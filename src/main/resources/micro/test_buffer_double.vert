#version 430 core
#extension GL_ARB_shading_language_420pack : require

#define POSITION		0
#define COLOR			3
#define TRANSFORM0		1

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

layout(binding = TRANSFORM0) uniform transform
{
	mat4 MVP;
} Transform;

layout(location = 0) in dvec4 Position;
layout(location = 1) in dvec4 Color[12];

out gl_PerVertex
{
	vec4 gl_Position;
};

out block
{
	vec4 Color;
} Out;

void main()
{
	dvec4 Temp = dvec4(0.0);
	for(int i = 0; i < 12; ++i)
		Temp += Color[i];

	Out.Color = vec4(Temp);

	gl_Position = Transform.MVP * vec4(Position);
}
