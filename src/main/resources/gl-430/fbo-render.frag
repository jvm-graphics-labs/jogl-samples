#version 420 core
#extension GL_ARB_shader_image_size : require

in vec4 gl_FragCoord;

layout(binding = 0) uniform sampler2D Diffuse;
layout(binding = 0, rgba8) uniform coherent image2D Color;

void main()
{
	vec2 Texcoord = gl_FragCoord.xy / vec2(imageSize(Color));

	imageStore(Color, ivec2(gl_FragCoord.xy), textureLod(Diffuse, Texcoord, 2));
}
