#version 150 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform sampler2D Diffuse;

in block
{
	vec2 Texcoord;
} In;

out vec4 Color;

vec4 blur5(sampler2D Texture, vec2 Texcoord, vec2 Resolution, vec2 Direction)
{
	vec2 Off1 = vec2(1.3333333333333333) * Direction;
	vec4 Color = vec4(0.0);
	Color += texture(Texture, Texcoord) * 0.29411764705882354;
	Color += texture(Texture, Texcoord + (Off1 / Resolution)) * 0.35294117647058826;
	Color += texture(Texture, Texcoord - (Off1 / Resolution)) * 0.35294117647058826;
	return Color; 
}

vec4 blur9(sampler2D Texture, vec2 Texcoord, vec2 Resolution, vec2 Direction)
{
	vec4 Color = vec4(0.0);
	vec2 Offset1 = vec2(1.3846153846) * Direction;
	vec2 Offset2 = vec2(3.2307692308) * Direction;
	Color += texture(Texture, Texcoord) * 0.2270270270;
	Color += texture(Texture, Texcoord + (Offset1 / Resolution)) * 0.3162162162;
	Color += texture(Texture, Texcoord - (Offset1 / Resolution)) * 0.3162162162;
	Color += texture(Texture, Texcoord + (Offset2 / Resolution)) * 0.0702702703;
	Color += texture(Texture, Texcoord - (Offset2 / Resolution)) * 0.0702702703;
	return Color;
}

vec4 blur13(sampler2D Texture, vec2 Texcoord, vec2 Resolution, vec2 Direction)
{
	vec4 Color = vec4(0.0);
	vec2 Offset1 = vec2(1.411764705882353) * Direction;
	vec2 Offset2 = vec2(3.2941176470588234) * Direction;
	vec2 Offset3 = vec2(5.176470588235294) * Direction;
	Color += texture(Texture, Texcoord) * 0.1964825501511404;
	Color += texture(Texture, Texcoord + (Offset1 / Resolution)) * 0.2969069646728344;
	Color += texture(Texture, Texcoord - (Offset1 / Resolution)) * 0.2969069646728344;
	Color += texture(Texture, Texcoord + (Offset2 / Resolution)) * 0.09447039785044732;
	Color += texture(Texture, Texcoord - (Offset2 / Resolution)) * 0.09447039785044732;
	Color += texture(Texture, Texcoord + (Offset3 / Resolution)) * 0.010381362401148057;
	Color += texture(Texture, Texcoord - (Offset3 / Resolution)) * 0.010381362401148057;
	return Color;
}

void main()
{
	//Color = blur13(Diffuse, In.Texcoord, vec2(640, 480), vec2(1.5));
	Color = texture(Diffuse, In.Texcoord);
}
