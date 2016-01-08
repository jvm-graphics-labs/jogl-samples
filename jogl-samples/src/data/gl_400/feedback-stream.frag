#version 400 core

#define POSITION	0
#define COLOR		3
#define FRAG_COLOR	0

in Block
{
    vec4 color;
} inBlock;

layout(location = FRAG_COLOR, index = 0) out vec4 color;

void main()
{
    color = inBlock.color;
}

