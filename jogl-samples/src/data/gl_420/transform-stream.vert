#version 420 core

#define POSITION	0
#define COLOR		3
#define FRAG_COLOR	0

uniform mat4 mvp;

layout(location = POSITION) in vec4 position;

out gl_PerVertex
{
    vec4 gl_Position;
};

out Block
{
    vec4 color;
} outBlock;

void main()
{	
    gl_Position = mvp * position;
    outBlock.color = vec4(clamp(vec2(position), 0.0, 1.0), 0.0, 1.0);
}
