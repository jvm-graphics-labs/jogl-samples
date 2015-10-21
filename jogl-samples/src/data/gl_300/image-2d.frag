#version 130

uniform sampler2D diffuse;

in vec2 vertTexCoord;

out vec4 color;

void main()
{
    color = texture(diffuse, vertTexCoord);

    if(length(vertTexCoord - 0.5) > 0.5)
        color.a = 0.0;
}