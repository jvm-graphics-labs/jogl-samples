#version 150 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

#define COUNT 4

uniform Transform
{
    mat4 mvp;
} transform;

in vec2 position;
in vec2 texCoord;

out Block
{
    vec2 texCoord;
    vec4 lumimance[COUNT];
} outBlock;

void main()
{
    lowp int count = lowp int(COUNT); // This shader is invalid, lowp is not part of the type hence of the cast

    for(lowp int i = 0; i < count; ++i)
        outBlock.lumimance[i] = vec4(1.0) / vec4(COUNT);

    outBlock.texCoord = texCoord;
    gl_Position = transform.mvp * vec4(position, 0.0, 1.0);
}
