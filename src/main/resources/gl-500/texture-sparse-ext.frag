#version 450 core
#extension GL_EXT_sparse_texture2 : enable

#define DIFFUSE			0
#define FRAG_COLOR		0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

layout(binding = DIFFUSE) uniform sampler2DArray Diffuse;

in block
{
	vec2 Texcoord;
} In;

layout(location = FRAG_COLOR, index = 0) out vec4 Color;

void main()
{
	int Resisdency = sparseTextureEXT(Diffuse, vec3(In.Texcoord.st, 0.0), Color);
	if (!sparseTexelsResidentEXT(Resisdency))
		discard;
}
