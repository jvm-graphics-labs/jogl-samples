#version 410 core

#define POSITION	0
#define COLOR		3
#define TEXCOORD	4
#define FRAG_COLOR	0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

layout(location = POSITION) in vec2 position;
layout(location = COLOR) in vec4 color;
layout(location = COLOR) out vec4 vertColor;

out gl_PerVertex
{
    vec4 gl_Position;
};

void main()
{	
    gl_Position = vec4(position, 0.0, 1.0);
    vertColor = color;
}

