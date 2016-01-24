#version 130

uniform mat4 mvp;

in vec2 position;
in vec2 texCoord;

out vec2 vertTexCoord;

void main()
{	
    vertTexCoord = texCoord;
    gl_Position = mvp * vec4(position, 0.0, 1.0);
}