#version 450 core

#define POSITION	0
#define COLOR		3
#define TEXCOORD	4
#define FRAG_COLOR	0

#define TRANSFORM0	1

#define DIFFUSE		0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

layout(binding = DIFFUSE) uniform sampler2DArray Diffuse;

in vec4 gl_FragCoord;
layout(location = FRAG_COLOR, index = 0) out vec4 Color;

void main()
{
	vec2 TextureSize = vec2(textureSize(Diffuse, 0));

	Color = texture(Diffuse, vec3(gl_FragCoord.xy / 16.0 / TextureSize, 0.0));
	//Color = texelFetch(Diffuse, ivec2(gl_FragCoord.xy), 0);
}
