#version 150 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

const vec4 Color[4] = vec4[]
(
	vec4(1.0, 0.0, 0.0, 1.0),
	vec4(1.0, 1.0, 0.0, 1.0),
	vec4(0.0, 1.0, 0.0, 1.0),
	vec4(0.0, 0.0, 1.0, 1.0)
);

in block
{
	flat int Instance;
} In;

out vec4 FragColor;

void main()
{
	FragColor = Color[In.Instance];
}
