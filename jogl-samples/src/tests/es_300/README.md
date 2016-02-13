# OpenGL ES3 Highlights

### [es-300-draw-elements](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/es_300/Es_300_draw_elements.java):

* `glDrawElements`

### [es-300-fbo-srgb](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/es_300/Es_300_fbo_srgb.java):

* srgb encoding fbo
* [`GL_ARB_draw_instanced`](https://www.opengl.org/registry/specs/ARB/draw_instanced.txt) draw several 
instances with a single draw call
* `glDrawElementsInstanced`
* `glDrawArraysInstanced`
* [`GL_ARB_explicit_attrib_location`](https://www.opengl.org/registry/specs/ARB/explicit_attrib_location.txt)
This extension allows setting the location of the input variables inside the vertex shader and the output 
variables inside the fragment shader. This is actually a great and beautiful way to implement Cg 'semantics' 
in GLSL. 
* [`GL_ARB_texture_swizzle`](https://www.opengl.org/registry/specs/ARB/texture_swizzle.txt) Texture swizzle 
provides some new texture sampler states to swizzle the texture components which provides a great freedom to 
interpret texture format.
