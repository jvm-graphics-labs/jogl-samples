# Highlights

### [gl-320-buffer-uniform](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/buffer/Gl_320_buffer_uniform.java) :

* how to set up three different uniform buffers for transform (perDraw), light (perPass) and material (perScene)

### [gl-320-buffer-shared](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/buffer/Gl_320_buffer_uniform.java) :

* one unique uniform buffer holding transform and material
* `GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT` and `glBindBufferRange`

### [gl-320-buffer-update](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/buffer/Gl_320_buffer_update.java) :

* how to copy data from an array buffer to another one, `glBindBuffer` and `glCopyBufferSubData`
* how to get uniform block data size, `glGetActiveUniformBlockiv`

