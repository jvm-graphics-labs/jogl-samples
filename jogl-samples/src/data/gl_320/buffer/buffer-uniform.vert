#version 150 core

layout(std140) uniform;

struct Transform
{
    mat4 p;
    mat4 mV;
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
} blockOut;

void main()
{
    vec4 p = perDraw.transform.mV * vec4(position, 1.0);

    blockOut.normal = mat3(perDraw.transform.mV) * normal;
    blockOut.view = -p.xyz;
    blockOut.color = color.rgb;

    gl_Position = perDraw.transform.p * p;
}