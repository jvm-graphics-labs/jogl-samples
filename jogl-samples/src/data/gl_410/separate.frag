#version 410 core

#define POSITION		0
#define COLOR			3
#define TEXCOORD		4
#define FRAG_COLOR		0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform sampler2D diffuse;

layout(location = COLOR) in vec3 color;
layout(location = TEXCOORD) in vec2 texCoord;
layout(location = FRAG_COLOR, index = 0) out vec4 fragColor;

void main()
{
    fragColor = texture(diffuse, texCoord) * vec4(color, 1.0);
}   
