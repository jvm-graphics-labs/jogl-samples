#version 420 core
#extension GL_ARB_shader_image_size : require

in vec4 gl_FragCoord;

layout(binding = 0) uniform sampler2D diffuse;
layout(binding = 0, rgba8) uniform coherent image2D color;

void main()
{
    vec2 texCoord = gl_FragCoord.xy / vec2(imageSize(color));

    imageStore(color, ivec2(gl_FragCoord.xy), textureLod(diffuse, texCoord, 2));
}
