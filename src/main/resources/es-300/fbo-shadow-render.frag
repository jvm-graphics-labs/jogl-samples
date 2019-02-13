#version 300 es

precision highp float;

uniform sampler2DShadow Shadow;

varying vec4 VertexColor;
varying vec4 ShadowCoord;

void main()
{
	vec3 Coord = ShadowCoord.xyz;
	Coord.z -= 0.005;

	float Visibility = mix(0.5, 1.0, texture(Shadow, Coord));
	gl_FragColor = Visibility * VertexColor;
}
