/*
 * Fragment shader.
 */

#version 400

in vec3 interpolatedColor;

out vec4 outputColor;

void main() {

    outputColor = vec4(interpolatedColor, 1);
}
