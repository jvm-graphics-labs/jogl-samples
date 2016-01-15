#version 430 core

layout(std140) uniform;

struct Transform
{
    mat4 p;
    mat4 mv;
    mat3 normal;
};

uniform PerDraw
{
    Transform transform;
} perDraw;

in vec3 position;
in vec3 normal;
in vec4 color;

out Block
{
    vec3 normal;
    vec3 view;
    vec3 color;
} outBlock;

void main()
{
    vec4 p = perDraw.transform.mv * vec4(position, 1.0);

    outBlock.normal = mat3(perDraw.transform.mv) * normal;
    outBlock.view = -p.xyz;
    outBlock.color = color.rgb;

    gl_Position = perDraw.transform.p * p;
}
