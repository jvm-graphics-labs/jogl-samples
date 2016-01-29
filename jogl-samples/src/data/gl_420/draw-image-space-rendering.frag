#version 420 core

#include draw-image-space-rendering.glsl

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

layout(binding = DIFFUSE) uniform sampler2D diffuse;

in vec4 gl_FragCoord;
layout(location = FRAG_COLOR, index = 0) out vec4 color;

void main()
{
    color = texture(diffuse, vec2(gl_FragCoord.x, 1.0 - gl_FragCoord.y) / vec2(640 - 1, 480 - 1));
}
