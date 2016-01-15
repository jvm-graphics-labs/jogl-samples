#version 430 core

layout(std140) uniform;

struct Material
{
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
    float shininess;
} material;

struct Light
{
    vec3 position; // Camera space
};

uniform PerScene
{
    Material material;
} perScene;

uniform PerPass
{
    Light light;
} perPass;

in Block
{
    vec3 normal;
    vec3 view;
    vec3 color;
} inBlock;

out vec4 color;

void main()
{
    vec3 n = normalize(inBlock.normal);
    vec3 l = normalize(perPass.light.position + inBlock.view);
    vec3 v = normalize(inBlock.view);

    vec3 diffuse = max(dot(n, l), 0.0) * perScene.material.diffuse;
    vec3 r = reflect(-l, n);
    vec3 specular = pow(max(dot(r, v), 0.0), perScene.material.shininess) * perScene.material.specular;

    color = vec4(perScene.material.ambient + diffuse + specular, 1.0);
}
