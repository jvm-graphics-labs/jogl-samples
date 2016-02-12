# OpenGL 3.2 Transform Feedback Highlights

### [gl-320-transform-feedback-interleaved](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/transform_feedback/Gl_320_transform_feedback_interleaved.java) :

* use the vertex shader to modify the data (position only -> position and colors interleaved) are going to be use the next step
* capture by `GL_TRANSFORM_FEEDBACK_PRIMITIVES_WRITTEN` query the number of primitive and use it in the following render call
* `glTransformFeedbackVaryings(programName[Program.TRANSFORM], 2, strings, GL_INTERLEAVED_ATTRIBS);`
* `glEnable(GL_RASTERIZER_DISCARD)`
* `glBeginQuery`, `glEndQuery`
* `glBeginTransformFeedback`, `glEndTransformFeedback`
* indices for `glBindBufferBase` are assigned starting from 0 based on the parameters order submitted to `glTransformFeedbackVaryings`

### [gl-320-transform-feedback-separated](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/transform_feedback/Gl_320_transform_feedback_separated.java) :

* same but with separated outputs (position input -> position and color separated)

Note: `glGetQueryObjectuiv` (to query the primitive number) may stall the graphics pipeline waiting for the OpenGL commands to be completed, use `glDrawTransformFeedback` instead, this does not require any number
