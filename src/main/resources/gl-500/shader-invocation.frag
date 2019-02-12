#version 450 core
#extension GL_NV_shader_thread_group : enable

#define FRAG_COLOR	0
#define CONSTANT	0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;
layout(std430, column_major) buffer;

in perVertex
{
	flat uint ExecutionUnit;
} PerVertex;

layout(location = FRAG_COLOR, index = 0) out vec4 Color;

layout(binding = CONSTANT) uniform constant
{
	int WrapSize;
	int WrapsPerSM;
	int SMCount;
} Constant;

vec3 rgbColor(vec3 hsvColor)
{
	vec3 hsv = hsvColor;
	vec3 rgbColor;

	if(hsv.y == 0)
		// achromatic (grey)
		rgbColor = vec3(hsv.z);
	else
	{
		float sector = floor(hsv.x / float(60));
		float frac = (hsv.x / float(60)) - sector;
		// factorial part of h
		float o = hsv.z * (float(1) - hsv.y);
		float p = hsv.z * (float(1) - hsv.y * frac);
		float q = hsv.z * (float(1) - hsv.y * (float(1) - frac));

		switch(int(sector))
		{
		default:
		case 0:
			rgbColor.r = hsv.z;
			rgbColor.g = q;
			rgbColor.b = o;
			break;
		case 1:
			rgbColor.r = p;
			rgbColor.g = hsv.z;
			rgbColor.b = o;
			break;
		case 2:
			rgbColor.r = o;
			rgbColor.g = hsv.z;
			rgbColor.b = q;
			break;
		case 3:
			rgbColor.r = o;
			rgbColor.g = p;
			rgbColor.b = hsv.z;
			break;
		case 4:
			rgbColor.r = q; 
			rgbColor.g = o; 
			rgbColor.b = hsv.z;
			break;
		case 5:
			rgbColor.r = hsv.z; 
			rgbColor.g = o; 
			rgbColor.b = p;
			break;
		}
	}

	return rgbColor;
}

void main()
{
	//vec3 hsv = vec3(float(gl_SMIDNV) / 8.0f * 60.f, 1.0f, 1.0f); // gl_SMIDNV Red - Orange - Yellow
	//vec3 hsv = vec3(90.f, 1.0f, float(gl_SMIDNV) / float(Constant.SMCount)); // gl_SMIDNV - NVIDIA green

	//Color = vec4(float(gl_WarpIDNV) / (float(Constant.WrapsPerSM)), float(gl_SMIDNV) / float(Constant.SMCount - 1), 0.0, 1.0);

	Color = vec4(float(0.0) / (float(0.0)), float(PerVertex.ExecutionUnit) / float(Constant.SMCount - 1), 0.0, 1.0);

/*
	vec3 hsv = vec3(90.f, 1.0f, float(gl_WarpIDNV) / (float(Constant.WrapsPerSM))); // NVIDIA green
	if (gl_WarpIDNV == 0 && gl_SMIDNV == 0)
		Color = vec4(1.0, 0.0, 1.0, 1.0);
	else if (gl_WarpIDNV == 0)
		Color = vec4(1.0, 0.0, 0.0, 1.0);
	else if (gl_SMIDNV == 0)
		Color = vec4(0.0, 0.0, 1.0, 1.0);
	else
		Color = vec4(rgbColor(hsv), 1.0f);
*/
}
