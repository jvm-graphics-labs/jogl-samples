#version 420 core

layout(binding = 0) uniform sampler2D Diffuse;

in vec4 gl_FragCoord;

layout(location = 0, index = 0) out vec4 Color;

void main()
{
	vec2 Size = vec2(textureSize(Diffuse, 0));

	Color = texture(Diffuse, gl_FragCoord.xy * 2.0 / Size);
}
