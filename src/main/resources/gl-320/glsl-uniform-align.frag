#version 150

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform test0
{
	vec4 a;
	vec2 b; // starts a new vector
	vec2 c;
} Test0;

uniform test1
{
	vec2 a;
	vec4 b; // starts a new vector
	vec2 c; // starts a new vector
} Test1;

uniform test1b
{
	vec2 a;
	vec4 b; // starts a new vector
} Test1b;

uniform test2
{
	float a;
	float b;
	vec2 c;
} Test2;

uniform test3
{
	float a;
	vec2 b;
	float c;
} Test3;

uniform test4
{
	float a;
	float b;
	float c;
	vec2 d; // starts a new vector
} Test4;

uniform test5
{
	vec3 a;
	float b;
} Test5;

uniform test6
{
	float a;
	vec3 b;
} Test6;

uniform test7
{
	float a;
	float b;
	vec3 c; // starts a new vector
} Test7;

uniform test8
{
	float a;

	struct{
		vec4 b; // starts a new vector
		float c; // starts a new vector
	} d;
} Test8;

uniform test9
{
	float a;
	struct{
		float b; // starts a new vector
		vec4 c; // starts a new vector
	} d;
} Test9;

uniform test10
{
	struct{
		vec4 a;
		float b; // starts a new vector
	} c;

	float d; // starts a new vector
} Test10;

out vec4 Color;

void main()
{
	Color = 
		Test0.a + vec4(Test0.b, Test0.c) + 
		vec4(Test1.a.x, Test1.a.y, Test1.b.x, Test1.b.y) + vec4(Test1.b.z, Test1.b.w, Test1.c.x, Test1.c.y) + 
		vec4(Test2.a, Test2.b, Test2.c.x, Test2.c.y) + 
		vec4(Test3.a, Test3.b.x, Test3.b.y, Test3.c) + 
		vec4(Test4.a, Test4.b, Test4.c, Test4.d.x) + vec4(Test4.d.y);
}
