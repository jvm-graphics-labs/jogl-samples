#version 130

uniform sampler2D diffuse;

in vec2 vertTexcoord;

out vec4 color;

void main()
{
    color = texture(diffuse, vertTexcoord);

    if(length(vertTexcoord - 0.5) > 0.5)
        color.a = 0.0;
}