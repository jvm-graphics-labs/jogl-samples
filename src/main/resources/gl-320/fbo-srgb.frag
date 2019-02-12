#version 150 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

in block
{
	flat vec4 Color;
} In;

out vec4 Color;
/*
/////////////
// convertRgbToSrgb

vec3 detail_rgbToSrgb(in vec3 ColorRGB, in float GammaCorrection)
{
	vec3 ClampedColorRGB = clamp(ColorRGB, 0.0, 1.0);

	return mix(
		pow(ClampedColorRGB, vec3(GammaCorrection)) * 1.055 - 0.055,
		ClampedColorRGB * 12.92,
		lessThan(ClampedColorRGB, vec3(0.0031308)));
}

vec3 convertRgbToSrgb(in vec3 ColorRGB, in float Gamma)
{
	return detail_rgbToSrgb(ColorRGB, 1.0 / Gamma);
}

vec3 convertRgbToSrgb(in vec3 ColorRGB)
{
	return detail_rgbToSrgb(ColorRGB, 0.41666);
}

vec4 convertRgbToSrgb(in vec4 ColorRGB, in float Gamma)
{
	return convertRgbToSrgb(vec4(vec3(ColorRGB), ColorRGB.a), Gamma);
}

vec4 convertRgbToSrgb(in vec4 ColorRGB)
{
	return convertRgbToSrgb(vec4(vec3(ColorRGB), ColorRGB.a), 0.41666);
}

/////////////
// convertSrgbToRgb

vec3 convertSrgbToRgb(in vec3 ColorSRGB, in float Gamma)
{
	return mix(
		pow((ColorSRGB + 0.055) * 0.94786729857819905213270142180095, vec3(Gamma)),
		ColorSRGB * 0.07739938080495356037151702786378,
		lessThanEqual(ColorSRGB, vec3(0.04045)));
}

vec3 convertSrgbToRgb(in vec3 ColorSRGB)
{
	return convertSrgbToRgb(ColorSRGB, 2.4);
}

vec4 convertSrgbToRgb(in vec4 ColorSRGB, in float Gamma)
{
	return vec4(convertSrgbToRgb(ColorSRGB.rgb, Gamma), ColorSRGB.a);
}

vec4 convertSrgbToRgb(in vec4 ColorSRGB)
{
	return vec4(convertSrgbToRgb(ColorSRGB.rgb, 2.4), ColorSRGB.a);
}

// For all settings: 1.0 = 100% 0.5=50% 1.5 = 150%
vec3 ContrastSaturationBrightness(vec3 color, float brt, float sat, float con)
{
	const vec3 LumCoeff = vec3(0.2125, 0.7154, 0.0721);

	vec3 brtColor = color * brt;
	vec3 intensity = vec3(dot(brtColor, LumCoeff));
	vec3 satColor = mix(intensity, brtColor, sat);
	vec3 conColor = mix(vec3(0.5), satColor, con);
	return conColor;
}
*/
// Main

void main()
{
	//vec3 ColorRGB = texture(Diffuse, In.Texcoord).rgb;

	//ColorRGB = convertRgbToSrgb(convertSrgbToRgb(convertRgbToSrgb(ColorRGB)));

	//ColorRGB = ContrastSaturationBrightness(ColorRGB, 1.0, 0.5, 1.0);

	Color = In.Color;
	//Color = vec4(convertRgbToSrgb(ColorRGB), 1.0);
}
