#version 420 core

vec4 hermite(in vec4 a, in vec4 b, in vec4 c, in vec4 d, in float s)
{
	mat4 hermite = mat4(
		vec4( 2,-3, 0, 1), 
		vec4(-2, 3, 0, 0),
		vec4( 1,-2, 1, 0),
		vec4( 1,-1, 0, 0));

	vec4 expo = vec4(s * s * s, s * s, s, 1);

	return expo * hermite * mat4(
		a[0], b[0], c[0], d[0],
		a[1], b[1], c[1], d[1],
		a[2], b[2], c[2], d[2],
		a[3], b[3], c[3], d[3]);
}

vec4 bezier(in vec4 a, in vec4 b, in vec4 c, in vec4 d, in float s)
{
	mat4 bezier = mat4(
		vec4(-1, 3,-3, 1), 
		vec4( 3,-6, 3, 0),
		vec4(-3, 3, 0, 0),
		vec4( 1, 0, 0, 0));

	vec4 expo = vec4(s * s * s, s * s, s, 1);

	return expo * bezier * mat4(
		a[0], b[0], c[0], d[0],
		a[1], b[1], c[1], d[1],
		a[2], b[2], c[2], d[2],
		a[3], b[3], c[3], d[3]);
}

vec4 catmullRom(in vec4 a, in vec4 b, in vec4 c, in vec4 d, in float s)
{
	mat4 catmullRom = mat4(
		vec4(-1, 2,-1, 0), 
		vec4( 3,-5, 0, 2),
		vec4(-3, 4, 1, 0),
		vec4( 1,-1, 0, 0));

	vec4 expo = vec4(s * s * s, s * s, s, 1);

	return 0.5 * expo * catmullRom * mat4(
		a[0], b[0], c[0], d[0],
		a[1], b[1], c[1], d[1],
		a[2], b[2], c[2], d[2],
		a[3], b[3], c[3], d[3]);
}

vec2 mirrorRepeat(in vec2 texCoord)
{
	vec2 clamp_ = vec2(ivec2(floor(texCoord)) % ivec2(2));
	vec2 floor_ = floor(texCoord);
	vec2 rest = texCoord - floor_;
	vec2 mirror = clamp_ + rest;
	
	return mix(vec2(1) - rest, rest, greaterThanEqual(mirror, vec2(1)));
}

float textureLevel(in sampler2D sampler, in vec2 texCoord)
{
	vec2 textureSize_ = vec2(textureSize(sampler, 0));

	float levelCount = max(log2(textureSize_.x), log2(textureSize_.y));

	vec2 dx = dFdx(texCoord * textureSize_);
	vec2 dy = dFdy(texCoord * textureSize_);
	float d = max(dot(dx, dx), dot(dy, dy));

	d = clamp(d, 1.0, pow(2, (levelCount - 1) * 2));

	return 0.5 * log2(d);
}

vec4 fetchBilinear(in sampler2D sampler, in vec2 interpolant, in ivec2 texelCoords[4], in int lod)
{
	vec4 texel00 = texelFetch(sampler, texelCoords[0], lod);
	vec4 texel10 = texelFetch(sampler, texelCoords[1], lod);
	vec4 texel11 = texelFetch(sampler, texelCoords[2], lod);
	vec4 texel01 = texelFetch(sampler, texelCoords[3], lod);
	
	vec4 texel0 = mix(texel00, texel01, interpolant.y);
	vec4 texel1 = mix(texel10, texel11, interpolant.y);
	return mix(texel0, texel1, interpolant.x);
}

///////////////
// Nearest 
vec4 textureNearest(in sampler2D sampler, in vec2 texCoord)
{
	int lodNearest = int(round(textureQueryLod(sampler, texCoord).x));

	ivec2 textureSize_ = textureSize(sampler, lodNearest);
	ivec2 texelCoord = ivec2(textureSize_ * texCoord);

	return texelFetch(sampler, texelCoord, lodNearest);
}

vec4 textureNearestLod(in sampler2D sampler, in vec2 texCoord, in float lod)
{
	int lodNearest = int(round(lod));

	ivec2 textureSize_ = textureSize(sampler, lodNearest);
	ivec2 texelCoord = ivec2(textureSize_ * texCoord);

	return texelFetch(sampler, texelCoord, lodNearest);
}

vec4 textureNearestLod(in sampler2D sampler, in vec2 texCoord, in int lod)
{
	ivec2 textureSize_ = textureSize(sampler, lod);
	ivec2 texelCoord = ivec2(textureSize_ * texCoord);

	return texelFetch(sampler, texelCoord, lod);
}

vec4 textureNearestLodRepeat(in sampler2D sampler, in vec2 texCoord, in float lod)
{
	int lodNearest = int(round(lod));
	ivec2 size = textureSize(sampler, lodNearest);

	ivec2 textureSize_ = textureSize(sampler, lodNearest);
	ivec2 texelCoord = ivec2(textureSize_ * texCoord) % size;

	return texelFetch(sampler, texelCoord, lodNearest);
}

///////////////
// Bilinear 
vec4 textureBilinear(in sampler2D sampler, in vec2 texCoord)
{
	int lod = int(round(textureQueryLod(sampler, texCoord).x));

	ivec2 size = textureSize(sampler, lod);
	vec2 texelCoord = texCoord * size - 0.5;
	ivec2 texelIndex = ivec2(texelCoord);
	
	ivec2 texelCoords[] = ivec2[4](
		texelIndex + ivec2(0, 0),
		texelIndex + ivec2(1, 0),
		texelIndex + ivec2(1, 1),
		texelIndex + ivec2(0, 1));

	return fetchBilinear(
		sampler, 
		fract(texelCoord), 
		texelCoords, 
		lod);
}

vec4 textureBilinearLod(in sampler2D sampler, in vec2 texCoord, in int lod)
{
	ivec2 size = textureSize(sampler, lod);
	vec2 texelCoord = texCoord * size - 0.5;
	ivec2 texelIndex = ivec2(texelCoord);
	
	ivec2 texelCoords[] = ivec2[4](
		texelIndex + ivec2(0, 0),
		texelIndex + ivec2(1, 0),
		texelIndex + ivec2(1, 1),
		texelIndex + ivec2(0, 1));

	return fetchBilinear(
		sampler, 
		fract(texelCoord), 
		texelCoords, 
		lod);
}

vec4 textureBilinearLodRepeat(in sampler2D sampler, in vec2 texCoord, in int lod)
{
	ivec2 size = textureSize(sampler, lod);
	vec2 texelCoord = texCoord * size - 0.5;
	ivec2 texelIndex = ivec2(texelCoord);
		
	ivec2 texelCoords[] = ivec2[4](
		(texelIndex + ivec2(0, 0)) % size,
		(texelIndex + ivec2(1, 0)) % size,
		(texelIndex + ivec2(1, 1)) % size,
		(texelIndex + ivec2(0, 1)) % size);
	
	return fetchBilinear(
		sampler, 
		fract(texelCoord), 
		texelCoords, 
		lod);
}

vec4 textureBilinearLodMirrorRepeat(in sampler2D sampler, in vec2 texCoord, in int lod)
{
	ivec2 size = textureSize(sampler, lod);
	vec2 texelCoord = mirrorRepeat(texCoord) * size - 0.5;
	ivec2 texelIndex = ivec2(texelCoord);
		
	ivec2 texelCoords[] = ivec2[4](
		(texelIndex + ivec2(0, 0)) % size,
		(texelIndex + ivec2(1, 0)) % size,
		(texelIndex + ivec2(1, 1)) % size,
		(texelIndex + ivec2(0, 1)) % size);
	
	return fetchBilinear(
		sampler, 
		fract(texelCoord), 
		texelCoords, 
		lod);
}

vec4 textureBilinearLodClampToBorder(in sampler2D sampler, in vec2 texCoord, in int lod)
{
	ivec2 size = textureSize(sampler, lod);
	vec2 texelCoord = clamp(texCoord, 0, 1) * size - 0.5;
	ivec2 texelIndex = ivec2(texelCoord);
	
	ivec2 texelCoords[] = ivec2[4](
		clamp(texelIndex + ivec2(0, 0), ivec2(0), size - 1),
		clamp(texelIndex + ivec2(1, 0), ivec2(0), size - 1),
		clamp(texelIndex + ivec2(1, 1), ivec2(0), size - 1),
		clamp(texelIndex + ivec2(0, 1), ivec2(0), size - 1));

	return fetchBilinear(
		sampler, 
		fract(texelCoord), 
		texelCoords, 
		lod);
}

///////////////
// Trilinear 
vec4 textureTrilinear(in sampler2D sampler, in vec2 texCoord)
{
	float lod = textureQueryLod(sampler, texCoord).x;

	int lodMin = int(floor(lod));
	int lodMax = int(ceil(lod));

	vec4 texelMin = textureBilinearLod(sampler, texCoord, lodMin);
	vec4 texelMax = textureBilinearLod(sampler, texCoord, lodMax);

	return mix(texelMin, texelMax, fract(lod));
}

vec4 textureTrilinearLod(in sampler2D sampler, in vec2 texCoord, in float lod)
{
	int lodMin = int(floor(lod));
	int lodMax = int(ceil(lod));

	vec4 texelMin = textureBilinearLod(sampler, texCoord, lodMin);
	vec4 texelMax = textureBilinearLod(sampler, texCoord, lodMax);

	return mix(texelMin, texelMax, fract(lod));
}

vec4 textureTrilinearLodRepeat(in sampler2D sampler, in vec2 texCoord, in float lod)
{
	int lodMin = int(floor(lod));
	int lodMax = int(ceil(lod));

	vec4 texelMin = textureBilinearLodRepeat(sampler, texCoord, lodMin);
	vec4 texelMax = textureBilinearLodRepeat(sampler, texCoord, lodMax);

	return mix(texelMin, texelMax, fract(lod));
}

vec4 textureTrilinearLodMirrorRepeat(in sampler2D sampler, in vec2 texCoord, in float lod)
{
	int lodMin = int(floor(lod));
	int lodMax = int(ceil(lod));

	vec4 texelMin = textureBilinearLodMirrorRepeat(sampler, texCoord, lodMin);
	vec4 texelMax = textureBilinearLodMirrorRepeat(sampler, texCoord, lodMax);

	return mix(texelMin, texelMax, fract(lod));
}

vec4 textureTrilinearLodClampToBorder(in sampler2D sampler, in vec2 texCoord, in float lod)
{
	int lodMin = int(floor(lod));
	int lodMax = int(ceil(lod));

	vec4 texelMin = textureBilinearLodClampToBorder(sampler, texCoord, lodMin);
	vec4 texelMax = textureBilinearLodClampToBorder(sampler, texCoord, lodMax);

	return mix(texelMin, texelMax, fract(lod));
}

///////////////
// Bicubic 
vec4 textureBicubicLod(in sampler2D sampler, in vec2 texCoord, in int lod)
{
	ivec2 size = textureSize(sampler, lod);
	vec2 texelCoord = texCoord * size - 0.5;
	ivec2 texelIndex = ivec2(texelCoord);

	vec4 texel00 = texelFetch(sampler, texelIndex + ivec2(-1,-1), lod);
	vec4 texel10 = texelFetch(sampler, texelIndex + ivec2( 0,-1), lod);
	vec4 texel20 = texelFetch(sampler, texelIndex + ivec2( 1,-1), lod);
	vec4 texel30 = texelFetch(sampler, texelIndex + ivec2( 2,-1), lod);

	vec4 texel01 = texelFetch(sampler, texelIndex + ivec2(-1, 0), lod);
	vec4 texel11 = texelFetch(sampler, texelIndex + ivec2( 0, 0), lod);
	vec4 texel21 = texelFetch(sampler, texelIndex + ivec2( 1, 0), lod);
	vec4 texel31 = texelFetch(sampler, texelIndex + ivec2( 2, 0), lod);
	
	vec4 texel02 = texelFetch(sampler, texelIndex + ivec2(-1, 1), lod);
	vec4 texel12 = texelFetch(sampler, texelIndex + ivec2( 0, 1), lod);
	vec4 texel22 = texelFetch(sampler, texelIndex + ivec2( 1, 1), lod);
	vec4 texel32 = texelFetch(sampler, texelIndex + ivec2( 2, 1), lod);

	vec4 texel03 = texelFetch(sampler, texelIndex + ivec2(-1, 2), lod);
	vec4 texel13 = texelFetch(sampler, texelIndex + ivec2( 0, 2), lod);
	vec4 texel23 = texelFetch(sampler, texelIndex + ivec2( 1, 2), lod);
	vec4 texel33 = texelFetch(sampler, texelIndex + ivec2( 2, 2), lod);

	vec2 splineCoord = fract(texelCoord);

	vec4 row0 = catmullRom(texel00, texel10, texel20, texel30, splineCoord.x);
	vec4 row1 = catmullRom(texel01, texel11, texel21, texel31, splineCoord.x);
	vec4 row2 = catmullRom(texel02, texel12, texel22, texel32, splineCoord.x);
	vec4 row3 = catmullRom(texel03, texel13, texel23, texel33, splineCoord.x);

	return catmullRom(row0, row1, row2, row3, splineCoord.y);
}

vec4 textureBicubicLodRepeat(in sampler2D sampler, in vec2 texCoord, in int lod)
{
	ivec2 size = textureSize(sampler, lod);
	vec2 texelCoord = texCoord * size - 0.5;
	ivec2 texelIndex = ivec2(texelCoord);

	vec4 texel00 = texelFetch(sampler, (texelIndex + ivec2(-1,-1)) % size, lod);
	vec4 texel10 = texelFetch(sampler, (texelIndex + ivec2( 0,-1)) % size, lod);
	vec4 texel20 = texelFetch(sampler, (texelIndex + ivec2( 1,-1)) % size, lod);
	vec4 texel30 = texelFetch(sampler, (texelIndex + ivec2( 2,-1)) % size, lod);

	vec4 texel01 = texelFetch(sampler, (texelIndex + ivec2(-1, 0)) % size, lod);
	vec4 texel11 = texelFetch(sampler, (texelIndex + ivec2( 0, 0)) % size, lod);
	vec4 texel21 = texelFetch(sampler, (texelIndex + ivec2( 1, 0)) % size, lod);
	vec4 texel31 = texelFetch(sampler, (texelIndex + ivec2( 2, 0)) % size, lod);

	vec4 texel02 = texelFetch(sampler, (texelIndex + ivec2(-1, 1)) % size, lod);
	vec4 texel12 = texelFetch(sampler, (texelIndex + ivec2( 0, 1)) % size, lod);
	vec4 texel22 = texelFetch(sampler, (texelIndex + ivec2( 1, 1)) % size, lod);
	vec4 texel32 = texelFetch(sampler, (texelIndex + ivec2( 2, 1)) % size, lod);

	vec4 texel03 = texelFetch(sampler, (texelIndex + ivec2(-1, 2)) % size, lod);
	vec4 texel13 = texelFetch(sampler, (texelIndex + ivec2( 0, 2)) % size, lod);
	vec4 texel23 = texelFetch(sampler, (texelIndex + ivec2( 1, 2)) % size, lod);
	vec4 texel33 = texelFetch(sampler, (texelIndex + ivec2( 2, 2)) % size, lod);

	vec2 splineCoord = fract(texelCoord);

	vec4 row0 = catmullRom(texel00, texel10, texel20, texel30, splineCoord.x);
	vec4 row1 = catmullRom(texel01, texel11, texel21, texel31, splineCoord.x);
	vec4 row2 = catmullRom(texel02, texel12, texel22, texel32, splineCoord.x);
	vec4 row3 = catmullRom(texel03, texel13, texel23, texel33, splineCoord.x);

	return catmullRom(row0, row1, row2, row3, splineCoord.y);
}

vec4 textureBicubicLodRepeat2(in sampler2D sampler, in vec2 texCoord, in int lod)
{
	ivec2 size = textureSize(sampler, lod);
	vec2 texelCoord = texCoord * size - 1.5;
	ivec2 texelIndex = ivec2(texelCoord);

	vec4 texel00 = texelFetch(sampler, (texelIndex + ivec2(0, 0)) % size, lod);
	vec4 texel10 = texelFetch(sampler, (texelIndex + ivec2(1, 0)) % size, lod);
	vec4 texel20 = texelFetch(sampler, (texelIndex + ivec2(2, 0)) % size, lod);
	vec4 texel30 = texelFetch(sampler, (texelIndex + ivec2(3, 0)) % size, lod);

	vec4 texel01 = texelFetch(sampler, (texelIndex + ivec2(0, 1)) % size, lod);
	vec4 texel11 = texelFetch(sampler, (texelIndex + ivec2(1, 1)) % size, lod);
	vec4 texel21 = texelFetch(sampler, (texelIndex + ivec2(2, 1)) % size, lod);
	vec4 texel31 = texelFetch(sampler, (texelIndex + ivec2(3, 1)) % size, lod);

	vec4 texel02 = texelFetch(sampler, (texelIndex + ivec2(0, 2)) % size, lod);
	vec4 texel12 = texelFetch(sampler, (texelIndex + ivec2(1, 2)) % size, lod);
	vec4 texel22 = texelFetch(sampler, (texelIndex + ivec2(2, 2)) % size, lod);
	vec4 texel32 = texelFetch(sampler, (texelIndex + ivec2(3, 2)) % size, lod);

	vec4 texel03 = texelFetch(sampler, (texelIndex + ivec2(0, 3)) % size, lod);
	vec4 texel13 = texelFetch(sampler, (texelIndex + ivec2(1, 3)) % size, lod);
	vec4 texel23 = texelFetch(sampler, (texelIndex + ivec2(2, 3)) % size, lod);
	vec4 texel33 = texelFetch(sampler, (texelIndex + ivec2(3, 3)) % size, lod);

	vec2 splineCoord = fract(texelCoord);

	vec4 row0 = catmullRom(texel00, texel10, texel20, texel30, splineCoord.x);
	vec4 row1 = catmullRom(texel01, texel11, texel21, texel31, splineCoord.x);
	vec4 row2 = catmullRom(texel02, texel12, texel22, texel32, splineCoord.x);
	vec4 row3 = catmullRom(texel03, texel13, texel23, texel33, splineCoord.x);

	return catmullRom(row0, row1, row2, row3, splineCoord.y);
}

vec4 textureBicubicLinearLodRepeat(in sampler2D sampler, in vec2 texCoord, in float lod)
{
	int lodMin = int(floor(lod));
	int lodMax = int(ceil(lod));

	vec4 texelMin = textureBicubicLodRepeat(sampler, texCoord, lodMin);
	vec4 texelMax = textureBicubicLodRepeat(sampler, texCoord, lodMax);

	return mix(texelMin, texelMax, fract(lod));
}

///////////////
// Tricubic 
vec4 textureTricubicLodRepeat(in sampler2D sampler, in vec2 texCoord, in float lod)
{
	int lodMin = int(floor(lod));
	int lodMax = int(ceil(lod));

	vec4 texelA = textureBicubicLodRepeat(sampler, texCoord, max(lodMin - 1, 0));
	vec4 texelB = textureBicubicLodRepeat(sampler, texCoord, lodMin);
	vec4 texelC = textureBicubicLodRepeat(sampler, texCoord, lodMax);
	vec4 texelD = textureBicubicLodRepeat(sampler, texCoord, lodMax + 1);

	return catmullRom(texelA, texelB, texelC, texelD, fract(lod));
}

/////////////////////
// Color procesing 
vec4 squeeze(in vec4 color, in float ratio)
{
	return color * (1.0 - ratio) + (ratio * 0.5);
}


