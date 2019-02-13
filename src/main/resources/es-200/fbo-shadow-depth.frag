#version 100
precision mediump float;

uniform vec3 PointLightPosition;
uniform vec2 ShadowClipNearFar;

varying vec4 VertexPosition;

void main()
{
	vec3 FromLightToFrag = (VertexPosition.xyz - PointLightPosition);

	float LightFragDist = (length(FromLightToFrag) - ShadowClipNearFar.x) / (ShadowClipNearFar.y - ShadowClipNearFar.x);

	gl_FragColor = vec4(LightFragDist, LightFragDist, LightFragDist, 1.0);
}
