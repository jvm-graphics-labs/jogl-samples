#version 150 core

layout(std140) uniform;

uniform sampler2DMS diffuse;

in vec4 gl_FragCoord;
out vec4 color;

float linearizeDepth(float depth)
{
    float n = 0.1; // camera z near
    float f = 8.0; // camera z far
    return (2.0 * n) / (f + n - depth * (f - n));
}

void main()
{
    float depth = texelFetch(diffuse, ivec2(gl_FragCoord.xy), 0).r;
    for(int i = 1; i < 4; ++i)
        depth = min(depth, texelFetch(diffuse, ivec2(gl_FragCoord.xy), i).r);

    float linearDepth = linearizeDepth(depth);
		
    color = vec4(linearDepth, linearDepth, linearDepth, 1.0);
}



