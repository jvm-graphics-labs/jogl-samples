#version 420 core

#define FRAG_COLOR	0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

in vec4 gl_FragCoord;

layout(binding = 0) uniform sampler2D Diffuse;
layout(binding = 1, r32f) writeonly uniform imageBuffer Depth;

/*
layout(binding = PICKING) uniform picking
{
	uvec2 Coord;
} Picking;
*/

uvec2 PickingCoord = uvec2(320, 240);

in block
{
	vec2 Texcoord;
} In;

layout(location = FRAG_COLOR, index = 0) out vec4 Color;

void main()
{
	if(all(equal(PickingCoord, uvec2(gl_FragCoord.xy))))
	{
		imageStore(Depth, 0, vec4(gl_FragCoord.z, 0, 0, 0));
		Color = vec4(1, 0, 1, 1);
	}
	else
		Color = texture(Diffuse, In.Texcoord.st);
}
