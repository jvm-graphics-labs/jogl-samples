#version 420 core
#extension GL_ARB_bindless_texture : require

#define FRAG_COLOR	0
#define MATERIAL	0

layout(binding = MATERIAL) uniform material
{
	uvec2 Diffuse;
} Material;

in block
{
	vec2 Texcoord;
} In;

layout(location = FRAG_COLOR, index = 0) out vec4 Color;

void main()
{
	Color = texture(sampler2D(Material.Diffuse), In.Texcoord.st);
}
