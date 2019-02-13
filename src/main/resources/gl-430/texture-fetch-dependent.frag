#version 420 core

#define POSITION	0
#define COLOR		3
#define TEXCOORD	4
#define FRAG_COLOR	0

layout(binding = 0) uniform sampler2DArray Diffuse;
layout(binding = 1) uniform isampler2DArray Indirection;

layout(location = FRAG_COLOR, index = 0) out vec4 Color;

const ivec2 WINDOW_SIZE = ivec2(1280, 720);
const uint TextureFetchOffset = 104729;
const uint TextureFetchCount = 0;

#define GARANTEE_TEXTURE_CACHE_MISS
#ifdef GARANTEE_TEXTURE_CACHE_MISS
	const int TextureTexelWrap = 2048;
	const int TextureIndirectionWrap = WINDOW_SIZE.x * WINDOW_SIZE.y;
#else
	const int TextureTexelWrap = 4;
	const int TextureIndirectionWrap = 4;
#endif

/*
void main()
{
	vec2 FragCoord = gl_FragCoord.xy;
	vec2 Texcoord = FragCoord / WINDOW_SIZE;
	float Layer = FragCoord.x + FragCoord.y * WINDOW_SIZE.x;

	uint IndirectionIndex = uint(Layer);
	for(int i = 0; i < TextureFetchCount; ++i)
	{
		IndirectionIndex = IndirectionIndex % TableSize;
		IndirectionIndex = texture(Indirection, vec3(0, 0, IndirectionIndex)).x;
	}

	Color = texture(Diffuse, vec3(Texcoord.st, float(IndirectionIndex)));
}
*/
/*
void main()
{
	vec2 FragCoord = gl_FragCoord.xy;
	vec2 Texcoord = FragCoord / WINDOW_SIZE;
	float Layer = FragCoord.x + FragCoord.y * WINDOW_SIZE.x;

	uint IndirectionIndex = uint(Layer);
	uint IndirectionIndexTotal = IndirectionIndex;
	for(uint i = 0; i < TextureFetchCount; ++i)
	{
		IndirectionIndexTotal += texture(Indirection, vec3(0, 0, 
		uint FetchIndex = (IndirectionIndex + TextureFetchOffset * i) % TableSize;FetchIndex)).x;
	}

	Color = texture(Diffuse, vec3(Texcoord.st, mod(float(IndirectionIndexTotal), TableSize)));
}
*/


/*
void main()
{
	uvec2 FragCoord = uvec2(gl_FragCoord.xy);
	uint Layer = FragCoord.x + FragCoord.y * WINDOW_SIZE.x;

	uint IndirectionIndex = Layer;
	uint IndirectionIndexTotal = 0;
	for(uint i = 0; i < TextureFetchCount; ++i)
	{
		uint FetchIndex = IndirectionIndex + TextureFetchOffset * i;
		uvec2 TexelCoord = uvec2(FetchIndex % WINDOW_SIZE.x, FetchIndex / WINDOW_SIZE.y);
		IndirectionIndexTotal += texture(Indirection, vec3(TexelCoord, 0)).x;
	}

	Color = texture(Diffuse, vec3(0.0, 0.0, mod(float(IndirectionIndexTotal), 2047.0)));
}
*/

void main()
{
	ivec2 FragCoord = ivec2(gl_FragCoord.xy);
	int IndirectionIndex = FragCoord.x + FragCoord.y * WINDOW_SIZE.x;

	for(uint i = 0; i < TextureFetchCount; ++i)
	{
		ivec2 SampleCoord = ivec2(IndirectionIndex % WINDOW_SIZE.x, IndirectionIndex / WINDOW_SIZE.x);
		IndirectionIndex = texelFetch(Indirection, ivec3(SampleCoord, 0), 0).x;
		IndirectionIndex = IndirectionIndex % TextureIndirectionWrap;
	}

	Color = texture(Diffuse, vec3(0.0, 0.0, float(IndirectionIndex % TextureTexelWrap)));
}

/*
void main()
{
	ivec2 FragCoord = ivec2(gl_FragCoord.xy);
	int IndirectionIndex = FragCoord.x + FragCoord.y * WINDOW_SIZE.x;

	int IndirectionIndexTotal = IndirectionIndex;
	for(uint i = 0; i < TextureFetchCount; ++i)
	{
		ivec2 SampleCoord = ivec2(IndirectionIndex % WINDOW_SIZE.x, IndirectionIndex / WINDOW_SIZE.x);
		IndirectionIndexTotal += texelFetch(Indirection, ivec3(SampleCoord, 0), 0).x;
		IndirectionIndex = IndirectionIndex % TextureIndirectionWrap;
	}

	Color = texture(Diffuse, vec3(0.0, 0.0, float(IndirectionIndexTotal % TextureTexelWrap)));
}
*/
