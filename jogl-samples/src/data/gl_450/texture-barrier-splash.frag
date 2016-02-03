#version 420 core

#include texture-barrier-splash.glsl

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

layout(binding = DIFFUSE) uniform sampler2D diffuse;

in vec4 gl_FragCoord;
layout(location = FRAG_COLOR, index = 0) out vec4 color;

void main()
{
    vec2 textSize = vec2(textureSize(diffuse, 0));

    color = texture(diffuse, gl_FragCoord.xy / textSize);
    //Color = texelFetch(Diffuse, ivec2(gl_FragCoord.xy), 0);
}
