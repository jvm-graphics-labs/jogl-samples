#ifdef VERTEX
varying vec2 xlv_TEXCOORD2;
varying vec3 xlv_TEXCOORD1;
varying vec2 xlv_TEXCOORD0;
uniform vec4 _MainTex_ST;
uniform vec4 unity_LightmapST;
uniform vec4 unity_Scale;
uniform mat4 _World2Object;
uniform mat4 _Object2World;

uniform vec3 _WorldSpaceCameraPos;
void main ()
{
  vec4 tmpvar_1;
  tmpvar_1.w = 1.0;
  tmpvar_1.xyz = _WorldSpaceCameraPos;
  vec3 tmpvar_2;
  tmpvar_2 = (gl_Vertex.xyz - ((_World2Object * tmpvar_1).xyz * unity_Scale.w));
  mat3 tmpvar_3;
  tmpvar_3[0] = _Object2World[0].xyz;
  tmpvar_3[1] = _Object2World[1].xyz;
  tmpvar_3[2] = _Object2World[2].xyz;
  gl_Position = (gl_ModelViewProjectionMatrix * gl_Vertex);
  xlv_TEXCOORD0 = ((gl_MultiTexCoord0.xy * _MainTex_ST.xy) + _MainTex_ST.zw);
  xlv_TEXCOORD1 = (tmpvar_3 * (tmpvar_2 - (2.0 * (dot (gl_Normal, tmpvar_2) * gl_Normal))));
  xlv_TEXCOORD2 = ((gl_MultiTexCoord1.xy * unity_LightmapST.xy) + unity_LightmapST.zw);
}


#endif
#ifdef FRAGMENT
varying vec2 xlv_TEXCOORD2;
varying vec3 xlv_TEXCOORD1;
varying vec2 xlv_TEXCOORD0;
uniform sampler2D unity_Lightmap;
uniform vec4 _ReflectColor;
uniform vec4 _Color;
uniform samplerCube _Cube;
uniform sampler2D _MainTex;
void main ()
{
  vec4 c_1;
  vec4 tmpvar_2;
  tmpvar_2 = texture2D (_MainTex, xlv_TEXCOORD0);
  vec4 tmpvar_3;
  tmpvar_3 = (tmpvar_2 * _Color);
  float tmpvar_4;
  tmpvar_4 = tmpvar_3.w;
  vec4 tmpvar_5;
  tmpvar_5 = texture2D (unity_Lightmap, xlv_TEXCOORD2);
  c_1.xyz = (tmpvar_3.xyz * ((8.0 * tmpvar_5.w) * tmpvar_5.xyz));
  c_1.w = tmpvar_4;
  c_1.xyz = (c_1.xyz + ((textureCube (_Cube, xlv_TEXCOORD1) * tmpvar_2.w).xyz * _ReflectColor.xyz));
  c_1.w = tmpvar_4;
  gl_FragData[0] = c_1;
}


#endif