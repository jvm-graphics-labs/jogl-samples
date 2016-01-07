#version 400 core

#define POSITION		0
#define COLOR			3
#define TEXCOORD		4

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform mat4 mvp;
uniform float displacement;

layout(location = POSITION) in vec2 position;
layout(location = TEXCOORD) in vec2 texCoord;

out Block
{
    vec2 texCoord;
} outBlock;

void main()
{	
    outBlock.texCoord = texCoord;
    gl_Position = mvp * vec4(position.x + displacement, position.y, 0.0, 1.0);
}

