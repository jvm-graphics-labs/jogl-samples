#version 330 core

#define ATTR_POSITION	0
#define ATTR_TEXCOORD	4

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform mat4 mvp;

layout(location = ATTR_POSITION) in vec2 position;
layout(location = ATTR_TEXCOORD) in vec2 texCoord;

out Block
{
    vec2 texCoord;
} outBlock;

void main()
{	
    outBlock.texCoord = texCoord;
    gl_Position = mvp * vec4(position, 0.0, 1.0);
}
