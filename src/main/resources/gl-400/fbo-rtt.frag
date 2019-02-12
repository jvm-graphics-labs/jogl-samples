#version 400 core

#define FRAG_COLOR		0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform sampler2D Diffuse;

in vec4 gl_FragCoord;
layout(location = FRAG_COLOR, index = 0) out vec4 Color;

void main()
{
	vec2 TextureSize = vec2(textureSize(Diffuse, 0));

	Color = texture(Diffuse, gl_FragCoord.xy / TextureSize);
	//Color = texelFetch(Diffuse, ivec2(gl_FragCoord.xy), 0);
}
