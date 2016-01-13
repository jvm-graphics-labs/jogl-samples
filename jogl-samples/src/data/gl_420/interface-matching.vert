#version 420 core

#define POSITION		0
#define COLOR			3
#define FRAG_COLOR		0

#define COUNT 2

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform mat4 mvp;

struct Vertex
{
    vec4 color;
};

layout(location = POSITION) in vec2 position[2];
//layout(location = POSITION) in my_vertex Input;
layout(location = COLOR) in vec4 color;

out gl_PerVertex
{
    vec4 gl_Position;
    float gl_PointSize;
    float gl_ClipDistance[];
};

layout(location = 0) out Vertex stOut;

out Block
{
    vec4 color;
    float lumimance[COUNT];
} blOut; 

void main()
{
    //gl_Position = MVP * vec4((Input.Position[0] + Input.Position[1]) * 0.5, 0.0, 1.0);
    gl_Position = mvp * vec4((position[0] + position[1]) * 0.5, 0.0, 1.0);
    stOut.color = color * 0.75;
    blOut.color = color * 0.25;

    for(int i = 0; i < COUNT; ++i)
        blOut.lumimance[i] = 1.0 / float(COUNT);
}
