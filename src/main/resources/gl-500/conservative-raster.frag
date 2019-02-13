#version 450 core
#extension GL_NV_sample_mask_override_coverage : require

#define POSITION	0
#define COLOR		3
#define FRAG_COLOR	0

#define MATERIAL	0
#define TRANSFORM0	1

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

layout(binding = MATERIAL) uniform material
{
	vec4 Diffuse;
} Material;

//in int gl_SampleMaskIn[];

layout(location = FRAG_COLOR, index = 0) out vec4 Color;

//layout(override_coverage) out int gl_SampleMask[];

void main()
{
	Color = Material.Diffuse;

	//gl_SampleMask[0] = (gl_SampleMaskIn[0] & 255) >= 2 ? 255 : 0;
	//gl_SampleMask[0] = gl_SampleMaskIn[0];
}
