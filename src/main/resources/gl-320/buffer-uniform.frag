#version 150 core

layout(std140) uniform;

struct material
{
	vec3 Ambient;
	vec3 Diffuse;
	vec3 Specular;
	float Shininess;
} Material;

struct light
{
	vec3 Position; // Camera space
};

uniform per_scene
{
	material Material;
} PerScene;

uniform per_pass
{
	light Light;
} PerPass;

in block
{
	vec3 Normal;
	vec3 View;
	vec3 Color;
} In;

out vec4 Color;

void main()
{
	vec3 N = normalize(In.Normal);
	vec3 L = normalize(PerPass.Light.Position + In.View);
	vec3 V = normalize(In.View);

	vec3 Diffuse = max(dot(N, L), 0.0) * PerScene.Material.Diffuse;
	vec3 R = reflect(-L, N);
	vec3 Specular = pow(max(dot(R, V), 0.0), PerScene.Material.Shininess) * PerScene.Material.Specular;

	Color = vec4(PerScene.Material.Ambient + Diffuse + Specular, 1.0);
}
