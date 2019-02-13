#version 330 core
#define POSITION	0
#define TEXCOORD	4

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform mat4 MVP;

layout(location = POSITION) in vec2 Position;
layout(location = TEXCOORD) in vec2 Texcoord;

out block
{
	vec2 Texcoord;
} Out;

void main()
{	
	Out.Texcoord = Texcoord;
	gl_Position = MVP * vec4(Position, 0.0, 1.0);
}
