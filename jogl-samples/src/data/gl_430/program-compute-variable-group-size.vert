#version 420 core
#extension GL_ARB_shader_storage_buffer_object : require

// Attributes
#define POSITION    0
#define TEXCOORD    4
// Uniforms
#define TRANSFORM0      1

precision highp float;
precision highp int;
layout(std140, column_major) uniform;
layout(std430, column_major) buffer;


layout(location = POSITION) in vec2 position;
layout(location = TEXCOORD) in vec2 texCoord;

layout(binding = TRANSFORM0) uniform Transform
{
    mat4 mvp;
} transform;

out gl_PerVertex
{
    vec4 gl_Position;
};

out Block
{
    vec2 texCoord;
} outBlock;

void main()
{	
    gl_Position = transform.mvp * vec4(position, 0, 1);
    outBlock.texCoord = texCoord;    
}