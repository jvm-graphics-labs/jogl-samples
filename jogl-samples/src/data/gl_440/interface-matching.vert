#version 440 core

#define POSITION		0
#define COLOR			3
#define FRAG_COLOR		0

#define TRANSFORM0		1

precision highp float;
precision highp int;
layout(std140, column_major) uniform;
layout(std430, column_major) buffer;

layout(binding = TRANSFORM0) uniform Transform
{
    mat4 mvp;
} transform;

layout(location = POSITION) in vec2 position[2];
layout(location = COLOR) in dvec4 color;

out gl_PerVertex
{
    vec4 gl_Position;
    float gl_PointSize;
    float gl_ClipDistance[];
};

struct Vertex
{
    vec4 color;
};

layout(location = 0) out Vertex st_Out[2];

//layout(location = 0 + 1 * st_Out.length()) out block
layout(location = 0 + 1 * 2) out Block
{
    vec4 color;
    mediump float lumimance[2];
} bl_Out; 

void main()
{	
    gl_Position = transform.mvp * vec4((position[0] + position[1]) * 0.5, 0.0, 1.0);
    st_Out[0].color = vec4(color) * 0.25;
    st_Out[1].color = vec4(color) * 0.50;
    bl_Out.color = vec4(color) * 0.25;

    for(int i = 0; i < 2; ++i)
        bl_Out.lumimance[i] = 1.0 / 2.0;
}