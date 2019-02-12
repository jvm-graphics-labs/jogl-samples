#version 500 core

uniform constants Constants;
uniform perFrame PerFrame;
uniform perPass PerPass;

uniform atomic_uint Atomic;

buffer static Static;
buffer dynamic Dynamic;

subroutine vec4 filtering(in sampler2D Sampler, in vec2 Texcoord);
subroutine uniform filtering Specular;

out gl_PerVertex
{
	vec4 gl_Position;
	float gl_PointSize;
	float gl_ClipDistance[];
};

out varying Out; 

subroutine(diffuse) vec4 diffuseLQ()
{
	return texture(DiffuseDXT1, In.Texcoord);
}

subroutine(diffuse) vec4 diffuseHQ()
{
	return texture(DiffuseRGB8, In.Texcoord);
}

void funcA()
{
	for(uniform int i = 0; i < count; ++i)
	{
	
	}
}

void main()
{	
	gl_Position = Transform.MVP * vec4((Position[0] + Position[1]) * 0.5, 0.0, 1.0);
	st_Out[0].Color = vec4(Color) * 0.25;
	st_Out[1].Color = vec4(Color) * 0.50;
	bl_Out.Color = vec4(Color) * 0.25;

	for(int i = 0; i < 2; ++i)
		bl_Out.Lumimance[i] = 1.0 / 2.0;
}
