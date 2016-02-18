#version 420 core

#define POSITION	0
#define COLOR		3
#define TEXCOORD	4
#define FRAG_COLOR	0
#define TRANSFORM0	1
#define DIFFUSE		0

layout(binding = TRANSFORM0) uniform Transform
{
    mat4 mvp;
} transform;

layout(location = POSITION) in vec2 position;
layout(location = TEXCOORD) in vec2 texCoord;

out Vert
{
    vec2 texCoord;
} vert;

out gl_PerVertex
{
    vec4 gl_Position;
};

void main()
{	
    vert.texCoord = texCoord;
    gl_Position = transform.mvp * vec4(position, 0.0, 1.0);
}

