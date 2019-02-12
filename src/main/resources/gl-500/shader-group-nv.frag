#version 450 core
#extension GL_NV_shader_thread_group : require

#define POSITION	0
#define COLOR		3
#define TEXCOORD	4
#define FRAG_COLOR	0
#define TRANSFORM0	1
#define DIFFUSE		0

layout(binding = 0, r32ui) uniform coherent uimage2D InvocationCount;

layout(early_fragment_tests) in;

in block
{
	vec2 Texcoord;
} In;
layout(origin_upper_left) in vec4 gl_FragCoord;

layout(location = FRAG_COLOR, index = 0) out vec4 Color;

vec4 computeHelperColor()
{
	vec4 Helper = mix(vec4(0.0), vec4(1.0, 0.0, 0.0, 1.0) * 0.25, gl_HelperInvocation);

	vec4 HelperColor0 = quadSwizzle0NV(Helper);
	vec4 HelperColor1 = quadSwizzle1NV(Helper);
	vec4 HelperColor2 = quadSwizzle2NV(Helper);
	vec4 HelperColor3 = quadSwizzle3NV(Helper);

	return HelperColor0 + HelperColor1 + HelperColor2 + HelperColor3;
}

void main()
{
	float LocalHelper = mix(0.0, 1.0, gl_HelperInvocation);

	float LocalInvocation0 = quadSwizzle0NV(LocalHelper);
	float LocalInvocation1 = quadSwizzle1NV(LocalHelper);
	float LocalInvocation2 = quadSwizzle2NV(LocalHelper);
	float LocalInvocation3 = quadSwizzle3NV(LocalHelper);

	float LocalInvocationCount = LocalInvocation0 + LocalInvocation1 + LocalInvocation2 + LocalInvocation3 + 1.0;

	imageAtomicAdd(InvocationCount, ivec2(gl_FragCoord.xy), uint(LocalInvocationCount));

	Color = computeHelperColor();
}
