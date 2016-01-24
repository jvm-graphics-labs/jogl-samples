#version 420 core
#extension GL_ARB_shader_image_size : require

#define FRAG_COLOR		0
#define DIFFUSE			0

in vec4 gl_FragCoord;
layout(binding = 0, rgba8) uniform image2D diffuse;

layout(location = FRAG_COLOR, index = 0) out vec4 color;

const float border = 16;

void main()
{
    vec2 size = vec2(imageSize(diffuse));

    if(gl_FragCoord.x < border)
        color = vec4(1.0, 0.0, 0.0, 1.0);
    else if(gl_FragCoord.x > size.x - border * 2)
        color = vec4(0.0, 1.0, 0.0, 1.0);
    else if(gl_FragCoord.y < border)
        color = vec4(1.0, 1.0, 0.0, 1.0);
    else if(gl_FragCoord.y > size.y - border * 2)
        color = vec4(0.0, 0.0, 1.0, 1.0);
    else
        color = imageLoad(diffuse, ivec2(gl_FragCoord.xy));
}