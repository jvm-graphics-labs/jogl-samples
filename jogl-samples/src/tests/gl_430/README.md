# OpenGL 4.3 Highlights ([g-truc review](http://www.g-truc.net/doc/OpenGL%204.3%20review.pdf))

### [gl-430-atomic-counter](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_430/Gl_430_atomic_counter.java):

* [`GL_ARB_clear_buffer_object`](https://www.opengl.org/registry/specs/ARB/clear_buffer_object.txt) "OpenGL contains mechanisms to copy sections of buffers from one to another, but it has no mechanism to initialize the content of a buffer to a known value. In effect, it has memcpy, but not memset. This extension adds such a mechanism and has several use cases. Examples include clearing a pixel unpack buffer before transferring data to a texture or resetting buffer data to a known value before sparse updates through shader image stores or transform feedback."
* each fragment performs an `atomicCounterIncrement`
* the atomic buffer gets reset by a simple `glClearBufferSubData`

### [gl-430-buffer-uniform](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_430/Gl_430_buffer_uniform.java):

* [`GL_ARB_program_interface_query`](https://www.opengl.org/registry/specs/ARB/clear_buffer_object.txt) "This extension provides a single unified set of query commands that can be used by applications to determine properties of various interfaces and resources used by program objects to communicate with application code, fixed-function OpenGL pipeline stages, and other programs."
* `glGetProgramInterfaceiv`
* `glGetProgramResourceName`
* `glGetProgramResourceiv`

### [gl-430-caps](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_430/Gl_430_caps.java):

* OpenGL 4.3 capabilities

### [gl-430-debug](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_430/Gl_430_debug.java):

* [`GL_KHR_debug`](https://www.opengl.org/registry/specs/KHR/debug.txt)
* `glObjectLabel` you can name object and make debug even easier -> cool!

### [gl-430-direct-state-access](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_430/Gl_430_direct_state_access.java):

* this sample wasn't easy to get it working, let me explain how I got all the shits working..
* this is the same thing of Debug Output, since DSA got into core, jogl skipped `GL_EXT_direct_state_access` and jumped directly on the [`GL_EXT_direct_state_access`](GL_ARB_direct_state_access), this means for the 90% of EXT-DSA calls just removing the `EXT` suffix at the end. For the rest it's another story..
* remember when in the opengl school they taught you about `glGen*`? Well, forget it bitch! :open_mouth: Now what exists is only `glCreate*`, this because when you call `glGen*` you create the object but the real allocation happens at the first `glBind`, so if now you don't bind anything you will get errors everywhere because none of your objects are allocated... so `glCreateProgramPipelines`, `glCreateBuffers`, `glCreateSamplers`, `glCreateFramebuffers`, `glCreateVertexArrays`. Wait where are the textures? Exactly... `glCreateTextures` has the "nice" feature to request the target, so if previously you were creating all the different textures you had in one shot, now you have to run it for each different target... :expressionless:
* no more `glVertexArrayVertexAttribOffsetEXT` or `glEnableVertexArrayAttribEXT`, you have to go VAB (vertex array buffer), format and binding separated, `glVertexArrayAttribFormat`, `glVertexArrayVertexBuffer`, `glVertexArrayAttribBinding` and `glVertexArrayElementBuffer`, all of them connected by a common `int bindingIndex`, in my case `Semantic.Buffer.STATIC`
* [`GL_ARB_gpu_shader5`](https://www.opengl.org/registry/specs/ARB/gpu_shader5.txt) extends this functionality 
so that we get a fully programmable texture filtering. The sampler object is no longer useful. It allows to 
gather any component of a 2x2 footprint. It allows to use an arbitrary offset to select the footprint and even 
extend this functionality to a per-component offset. Finally, it allows to perform a per-sample depth comparison.
