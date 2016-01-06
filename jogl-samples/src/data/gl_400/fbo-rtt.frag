#version 400 core

#define FRAG_COLOR		0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform sampler2D diffuse;

in vec4 gl_FragCoord;
layout(location = FRAG_COLOR, index = 0) out vec4 color;

void main()
{
    vec2 textSize = vec2(textureSize(diffuse, 0));

    color = texture(diffuse, gl_FragCoord.xy / textSize);
    //Color = texelFetch(Diffuse, ivec2(gl_FragCoord.xy), 0);
}
