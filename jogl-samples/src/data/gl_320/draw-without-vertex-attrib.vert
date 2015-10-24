#version 150 core

layout(std140) uniform;

uniform Transform
{
    mat4 mvp;
} transform;

const int vertexCount = 6;
const vec2 position[vertexCount] = vec2[](
	vec2(-1.0,-1.0),
	vec2( 1.0,-1.0),
	vec2( 1.0, 1.0),
	vec2(-1.0,-1.0),
	vec2( 1.0, 1.0),
	vec2(-1.0, 1.0));

void main()
{	
    gl_Position = transform.mvp * vec4(position[gl_VertexID], 0.0, 1.0);
}

