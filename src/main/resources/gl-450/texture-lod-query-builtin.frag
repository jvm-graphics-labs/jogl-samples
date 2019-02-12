#version 450 core

#define FRAG_COLOR	0
#define DIFFUSE		0

layout(binding = DIFFUSE) uniform sampler2D Diffuse;

layout(origin_upper_left) in vec4 gl_FragCoord;

in block
{
	vec2 Texcoord;
} In;

layout(location = FRAG_COLOR, index = 0) out vec4 Color;

void main()
{
	ivec2 Size = textureSize(Diffuse, 0);
	float LevelCount = log2(max(Size.x, Size.y)) + 1.0;
	float Level = (textureQueryLod(Diffuse, In.Texcoord).y);

	//Color = vec4(vec3(Level) / LevelCount, 1.0);
	Color = vec4(dFdx(In.Texcoord) * 10.0, 0.0, 1.0);
}
