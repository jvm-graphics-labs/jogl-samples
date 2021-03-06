#version 150

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform samplerBuffer displacement;
uniform mat4 mvp;

in vec2 position;

out Block
{
    flat int instance;
} outBlock;

void main()
{	
    outBlock.instance = gl_InstanceID;
    gl_Position = mvp * (vec4(position, 0.0, 0.0) + texelFetch(displacement, gl_InstanceID));
}
