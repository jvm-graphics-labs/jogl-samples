/*
 * Fragment shader.
 */

#version 400

in vec3 interpolatedColor;

out vec4 outputColor;

void main() {
    outputColor = vec4(interpolatedColor, 1);
    /*if(interpolatedColor.z == 0)
        outputColor = vec4(1, 0, 0, 1);
    else
        outputColor = vec4(0, 1, 0, 1);*/
}
