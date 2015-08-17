/*
 * Vertex shader.
 */
#version 400

in vec2 position;
in vec3 color;

uniform mat4 modelToClipMatrix;

out vec3 interpolatedColor;

void main() {

    gl_Position = modelToClipMatrix * vec4(position, 0, 1);

    interpolatedColor = color;
}
