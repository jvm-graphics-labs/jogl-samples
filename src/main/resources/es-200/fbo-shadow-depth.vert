#version 100
precision highp float;

uniform mat4 LightProj;
uniform mat4 LightView;
uniform mat4 LightWorld;

attribute vec3 Position;

varying vec4 VertexPosition;

void main()
{
	VertexPosition = LightWorld * vec4(Position, 1.0);
	gl_Position = LightProj * LightView * VertexPosition;
}
