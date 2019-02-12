#version 150 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform mat4 MVP;

in vec4 Position;

out block
{
	vec4 Color;
} Out;

void main()
{	
	gl_Position = MVP * Position;
	Out.Color = vec4(clamp(vec2(Position), 0.0, 1.0), 0.0, 1.0);
}
