# OpenGL 3.2 Program Highlights

### [gl-320-program](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/program/Gl_320_program.java) :

* [`GL_ARB_uniform_buffer_object`](https://www.opengl.org/registry/specs/ARB/uniform_buffer_object.txt) 
provides the capability to use several buffers for a uniform block in a way close to transform feedback. It's 
also provide several profiles to let the compiler optimize dead uniform, through elimination layout(packed) or 
to make sure the interface remains as defined in the code layout(std140).
* setting input attribute location with `glBindAttribLocation`
* setting output fragment location with `glBindFragDataLocation`
* getting block index with `glGetUniformBlockIndex`
* getting uniform block names with `glGetActiveUniformBlockName`
* getting uniform names with `glGetActiveUniformName`
* getting uniform block data size with `glGetActiveUniformBlockiv`

### [gl-320-program-uniform](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/program/Gl_320_program_uniform.java) :

* same, using uniform instead uniform buffer for color
