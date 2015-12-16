#version 150 core

layout(std140) uniform;

uniform sampler2D diffuse;

in vec4 gl_FragCoord;
out vec4 color;

void main()
{
    color = texture(diffuse, vec2(gl_FragCoord.x, 1.0 - gl_FragCoord.y) / vec2(640, 480));
}
