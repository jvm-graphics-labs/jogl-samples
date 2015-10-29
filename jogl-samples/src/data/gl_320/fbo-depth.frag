#version 150 core

layout(std140) uniform;

uniform sampler2D diffuse;

in vec4 gl_FragCoord;
out vec4 color;

float linearizeDepth(vec2 uv)
{
    float n = 0.1; // camera z near
    float f = 8.0; // camera z far
    float z = texture(diffuse, uv).x;
    return (2.0 * n) / (f + n - z * (f - n));
}

void main()
{
    vec2 texSize = vec2(textureSize(diffuse, 0));

    vec2 texCoord = gl_FragCoord.xy / texSize;
    float depth = texture(diffuse, texCoord).x;//LinearizeDepth(TexCoord);

    color = vec4(depth, depth, depth, 1.0);
}



