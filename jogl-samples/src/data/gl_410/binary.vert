#version 410 core

#define POSITION	0
#define COLOR		3
#define TEXCOORD	4

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform mat4 mvp;

layout(location = POSITION) in vec2 position;
layout(location = TEXCOORD) in vec2 texCoord;

out gl_PerVertex
{
    vec4 gl_Position;
};

out Block
{
    vec2 texCoord;
    vec3 color;
} outBlock; 

void main()
{	
    gl_Position = mvp * vec4(position, 0.0, 1.0);
    outBlock.texCoord = texCoord;
    outBlock.color = vec3(1.0, 0.9, 0.8);
}

