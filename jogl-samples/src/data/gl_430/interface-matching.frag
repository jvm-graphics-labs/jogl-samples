#version 430 core

#define POSITION		0
#define COLOR			3
#define FRAG_COLOR		0

struct Vertex
{
    vec4 color;
};

layout(location = 0) in Vertex stIn;

in Block
{
    vec4 color;
} blIn; 

layout(location = FRAG_COLOR, index = 0) out vec4 color;

void main()
{
    color = stIn.color + blIn.color;
}
