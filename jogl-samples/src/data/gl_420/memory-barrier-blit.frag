#version 420 core

#define FRAG_COLOR	0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

layout(binding = 0) uniform sampler2D diffuse;

in vec4 gl_FragCoord;
layout(location = FRAG_COLOR, index = 0) out vec4 color;

void main()
{
    vec2 textSize = vec2(textureSize(diffuse, 0));
    vec2 texCoord = vec2(gl_FragCoord.x / textSize.x, gl_FragCoord.y / textSize.y);

    color = texture(diffuse, texCoord);
}
