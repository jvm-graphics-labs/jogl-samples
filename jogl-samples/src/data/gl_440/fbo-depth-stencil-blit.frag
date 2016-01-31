#version 150 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform sampler2D diffuse;

in vec4 gl_FragCoord;
out vec4 color;

void main()
{
    vec2 textSize = vec2(textureSize(diffuse, 0));

    color = texture(diffuse, gl_FragCoord.xy * 2.0 / textSize);
}