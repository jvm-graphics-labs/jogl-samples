#version 420 core

layout(binding = 0) uniform sampler2D diffuse;

in vec4 gl_FragCoord;

layout(location = 0, index = 0) out vec4 color;

void main()
{
    vec2 size = vec2(textureSize(diffuse, 0));

    color = texture(diffuse, gl_FragCoord.xy * 2.0 / size);
}