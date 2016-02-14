# OpenGL 3.2 Transform Feedback Highlights

### [gl-320-transform-feedback-interleaved](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/transform_feedback/Gl_320_transform_feedback_interleaved.java) :

* Transform feedback is the OpenGL name given to Direct3D output stream. It allows to capture processed 
vertices data before rasterization and to be more accurate, just before clipping.
* use the vertex shader to modify the data (position only -> position and colors interleaved) are going to be use the next step
* capture by `GL_TRANSFORM_FEEDBACK_PRIMITIVES_WRITTEN` query the number of primitive and use it in the following render call
* `glTransformFeedbackVaryings` is a program state that must to be called before GLSL program linking. The last 
parameter can be either `GL_SEPARATE_ATTRIBS` or `GL_INTERLEAVED_ATTRIBS`. `GL_SEPARATE_ATTRIBS` is used to 
save each transform feedback varying in different buffers and `GL_INTERLEAVED_ATTRIBS` is used to save all 
transform feedback varying in the same buffer.
* `glEnable(GL_RASTERIZER_DISCARD)`
* `glBeginQuery`, `glEndQuery`
* `glBeginTransformFeedback`, `glEndTransformFeedback`
* indices for `glBindBufferBase` are assigned starting from 0 based on the parameters order submitted to 
`glTransformFeedbackVaryings`
* `GL_TRANSFORM_FEEDBACK_PRIMITIVES_WRITTEN` is a query that can be used to get the number of primitives 
actually written in the transform feedback buffer(s). When data are captured from a vertex shader and the 
feedback primitive is the same as the drawn primitive, the number of primitives written in the buffer is 
likely to be the same as the number of primitives sent at draw call but transform feedback has such 
flexibility that transform feedback primitive can be different than draw call primitive.

Furthermore, transform feedback can capture geometry shader output. As geometry shader can generate or discard 
primitives, which output vertices count become unpredictable. Transform feedback buffers can be used as vertex 
data of further draw calls where the vertices polygon count might define the draw call primitive count. If you 
repeat a series of geometry shader and transform feedback, we might have a tessellator... but a really slow 
useless one! 

### [gl-320-transform-feedback-separated](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/transform_feedback/Gl_320_transform_feedback_separated.java) :

* same but with separated outputs (position input -> position and color separated)

Note: `glGetQueryObjectuiv` (to query the primitive number) may stall the graphics pipeline waiting for the 
OpenGL commands to be completed, use `glDrawTransformFeedback` instead, this does not require any number
