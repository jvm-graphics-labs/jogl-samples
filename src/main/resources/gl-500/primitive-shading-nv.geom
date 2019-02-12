#version 450 core
#extension GL_NV_geometry_shader_passthrough : require

#ifndef GEN_ERROR // If this is not declared, the compiler should generate an error
layout(triangles) in;
#endif

precision highp float;
precision highp int;
layout(std140, column_major) uniform;
layout(triangle_strip, max_vertices = 4) out;

layout(passthrough) in gl_PerVertex
{
	vec4 gl_Position;
} gl_in[];

layout(passthrough) in block
{
	vec4 Color;
} In[];

void main()
{

}

