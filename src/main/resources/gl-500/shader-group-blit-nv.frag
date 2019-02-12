#version 420 core

#define POSITION	0
#define COLOR		3
#define TEXCOORD	4
#define FRAG_COLOR	0

#define TRANSFORM0	1
#define DIFFUSE		0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

layout(origin_upper_left) in vec4 gl_FragCoord;
layout(binding = 0, r32ui) uniform coherent uimage2D InvocationCount;
layout(binding = DIFFUSE) uniform sampler2D Diffuse;
layout(location = FRAG_COLOR, index = 0) out vec4 Color;

void main()
{
	//Color = texelFetch(Diffuse, ivec2(gl_FragCoord.xy) >> 3, 0);

	uint TotalInvocationCount = imageLoad(InvocationCount, ivec2(gl_FragCoord.xy) >> 3).x;

	Color = vec4(float(TotalInvocationCount) / 16.f, 0.0, 0.0, 1.0);
}

