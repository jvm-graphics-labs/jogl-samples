#version 150 core

layout(std140) uniform;

const float Luminance[2] = float[2](1.0, 0.5);

in block
{
	flat int Index;
	vec4 Color;
} In;

out vec4 Color;

void main()
{
	Color = In.Color * Luminance[In.Index];
}
