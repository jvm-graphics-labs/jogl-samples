#version 410 core

#define POSITION		0
#define COLOR			3
#define TEXCOORD		4
#define FRAG_COLOR		0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform mat4 mvp;

layout(location = POSITION) in vec2 position;
layout(location = TEXCOORD) in vec2 texCoord;
layout(location = COLOR) out vec3 vertColor;
layout(location = TEXCOORD) out vec2 vertTexCoord;

out gl_PerVertex
{
    vec4 gl_Position;
};

void main()
{
    gl_Position = mvp * vec4(position, 0.0, 1.0);
    vertTexCoord = texCoord;
    vertColor = vec3(1.0, 0.9, 0.8);
}

