precision highp float;

uniform mat4 P;
uniform mat4 V;
uniform mat4 W;

attribute vec3 Position;
attribute vec4 Color;

varying vec3 VertexColor;
varying vec3 VertexPosition;

void main()
{
	VertexPosition = (W * vec4(Position, 1.0)).xyz;
	VertexColor = Color.rgb;

	gl_Position = P * V * vec4(Position, 1.0);
}
