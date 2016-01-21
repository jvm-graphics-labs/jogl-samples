#version 420 core
#extension GL_ARB_shader_image_size : require

#define POSITION	0
#define COLOR		3
#define TEXCOORD	4
#define FRAG_COLOR	0

layout(binding = 0, rgba8) uniform image2D diffuse[3];

in Block
{
    vec2 texCoord;
    flat int instance;
} inBlock;

layout(location = FRAG_COLOR, index = 0) out vec4 color;

vec4 fetchBilinear(layout(rgba8) in image2D image, in vec2 interpolant, in ivec2 texelcoords[4])
{
	vec4 texel00 = imageLoad(image, texelcoords[0]);
	vec4 texel10 = imageLoad(image, texelcoords[1]);
	vec4 texel11 = imageLoad(image, texelcoords[2]);
	vec4 texel01 = imageLoad(image, texelcoords[3]);
	
	vec4 texel0 = mix(texel00, texel01, interpolant.y);
	vec4 texel1 = mix(texel10, texel11, interpolant.y);
	return mix(texel0, texel1, interpolant.x);
}

vec4 imageBilinear(layout(rgba8) in image2D image, in vec2 texCoord)
{
	//const ivec2 SizeArray[3] = ivec2[3](ivec2(256), ivec2(128), ivec2(64));
	//ivec2 Size = SizeArray[Instance];

	ivec2 size = imageSize(image);
	vec2 texelCoord = texCoord * size - 0.5;
	ivec2 texelIndex = ivec2(texelCoord);
	
	ivec2 texelCoords[] = ivec2[4](
		texelIndex + ivec2(0, 0),
		texelIndex + ivec2(1, 0),
		texelIndex + ivec2(1, 1),
		texelIndex + ivec2(0, 1));

	return fetchBilinear(
		image, 
		fract(texelCoord), 
		texelCoords);
}

void main()
{
    color = imageBilinear(diffuse[inBlock.instance], inBlock.texCoord);
}
