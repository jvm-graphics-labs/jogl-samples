#version 150 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform Transform
{
    mat4 mvp;
} transform;

in vec2 position;
in vec2 texCoord;

out Block
{
    vec4 color;
    vec2 texCoord;
    flat int instance;
} outBlock;

out gl_PerVertex
{
    vec4 gl_Position;
};

void main()
{
    outBlock.color = vec4(1.0, 0.5, 0.0, 1.0);
    outBlock.texCoord = texCoord;
    outBlock.instance = 0;
    gl_Position = transform.mvp * vec4(position, 0.0, 1.0);
}
