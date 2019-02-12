#version 150 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform mat4 MVP;

in vec2 Position;
in vec2 Texcoord;

out block
{
	vec2 Texcoord;
} Out;

void main()
{	
	Out.Texcoord = Texcoord;
	gl_Position = MVP * vec4(Position, float(gl_InstanceID) * 1.0 - 2.0, 1.0);
}
