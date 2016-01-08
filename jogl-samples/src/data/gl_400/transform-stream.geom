#version 400 core

#define POSITION	0
#define COLOR		3
#define FRAG_COLOR	0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;
layout(triangles) in;
layout(triangle_strip, max_vertices = 3) out;

in Block
{
    vec4 color;
} inBlock[];

out Block
{
    layout(stream = 0) vec4 color;
} outBlock;

out gl_PerVertex 
{
    vec4 gl_Position;
    float gl_PointSize;
    float gl_ClipDistance[];
};

void main()
{
    for(int i = 0; i < gl_in.length(); ++i)
    {
        outBlock.color = inBlock[i].color;
        gl_Position = gl_in[i].gl_Position;
        EmitStreamVertex(0);
    }
    EndStreamPrimitive(0);
}
