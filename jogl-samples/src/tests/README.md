# tests

The Java OpenGL Samples Pack is a port of the [OpenGL Samples Pack](http://www.g-truc.net/project-0026.html), a collection of [OpenGL](http://www.opengl.org/) samples based on the OpenGL "core profile" specifications.

The project aims to promote the new OpenGL features making easier version transitions for OpenGL programmers with a complementary documentation for the OpenGL specification. Despite the fact that the OpenGL Samples Pack provides as simple (and dumb) as possible samples, it's not a tutorial for beginner but a project for programmers already familiar with OpenGL. The OpenGL Samples Pack is also a good OpenGL drivers feature test.

These samples use [NEWT](http://jogamp.org/jogl/doc/NEWT-Overview.html) to create window and [jogl](http://jogamp.org/jogl/www/) of [Jogamp](http://jogamp.org/) as OpenGL wrapper, GLM as math library and to replace OpenGL fixed pipeline functions and GLI to load images. 


Features:

### [es-200-draw-elements](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/es200/Es_200_draw_elements.java)

- draw elements
- `glBindAttribLocation`

### [es-300-draw-elements](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/es300/Es_300_draw_elements.java)

- draw elements
- `glBindAttribLocation`

### [gl-300-fbo-multisample](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl300/Gl_300_fbo_multisample.java)

- dx11 texture
- renderBuffer
- `glRenderbufferStorageMultisample`

### [gl-320-buffer-uniform-shared](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl320/Gl_320_buffer_uniform_shared.java)

- uniform buffer shared
- `glMapBufferRange`
- `GL_UNIFORM_BLOCK_DATA_SIZE`

### [gl-320-buffer-uniform](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl320/Gl_320_buffer_uniform.java)

- uniform buffer
- `glMapBufferRange`
- uniform buffer containing `struct`s

### [gl-320-buffer-update](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl320/Gl_320_buffer_update.java)

- `glCopyBufferSubData`
 
### [gl-320-draw-base-vertex](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl320/Gl_320_draw_base_vertex.java)

- `glDrawElementsInstancedBaseVertex`
 
### [gl-320-draw-instanced](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl320/Gl_320_draw_instanced.java)

- `glDrawArraysInstanced`

### [gl-320-draw-multiple](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl320/Gl_320_draw_multiple.java)

- `glMultiDrawElementsBaseVertex`

### [gl-320-draw-range-arrays](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl320/Gl_320_draw_range_arrays.java)

- `glDrawArraysInstanced`
