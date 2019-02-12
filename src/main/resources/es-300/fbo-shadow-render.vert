#version 300 es

precision highp float;

uniform mat4 MVP;
uniform mat4 DepthBiasMVP;

attribute vec3 Position;
attribute vec4 Color;

varying vec4 VertexColor;
varying vec4 ShadowCoord;

void main()
{
	gl_Position = MVP * vec4(Position, 1.0);
	ShadowCoord = DepthBiasMVP * vec4(Position, 1.0);
	VertexColor = Color;
}
