#version 440 core

#define POSITION	0
#define COLOR		3
#define FRAG_COLOR	0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;
//layout(std430, column_major) buffer; AMD bug

layout(location = POSITION) in vec4 Position;
layout(location = COLOR) in vec4 Color;

out block
{
	vec4 Color;
} Out;

out gl_PerVertex
{
	vec4 gl_Position;
};

void main()
{	
	gl_Position = Position;
	Out.Color = Color;
}

