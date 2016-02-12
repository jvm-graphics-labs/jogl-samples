# OpenGL 4.1 Highlights ([g-truc review](http://www.g-truc.net/doc/OpenGL%204.1%20review.pdf))

### [gl-410-buffer-uniform-array](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_410/Gl_410_buffer_uniform_array.java) :

* uses two uniform buffer arrays, one for material one for transform
* implementation of the `gl_DrawID` in OpenGL, explained [here](http://www.g-truc.net/post-0518.html)
* `glVertexAttribIPointer`
* `glVertexAttribDivisor`
* `glDrawElementsInstancedBaseVertexBaseInstance`

### [gl-410-buffer-uniform-array](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_410/Gl_410_buffer_uniform_array.java) :

* uses two uniform buffer arrays, one for material one for transform
* implementation of the `gl_DrawID` in OpenGL, explained [here](http://www.g-truc.net/post-0518.html)

### [gl-410-caps](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_410/Gl_410_caps.java) :

* OpenGL 4.1 capabilities

### [gl-410-fbo-layered](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_410/Gl_410_fbo_layered.java) :

* allocates a texture with depth equal 4 and attach it to the `GL_COLOR_ATTACHMENT0` of an fbo. It renders to each of them via geometry shader invocation `layout(triangles, invocations = 4) in` and set the destination layer by `gl_LayerID = gl_InvocationID`. Colors hardcoded in the fragment shader.
* `gl_LayerID` allows dispatching primitives to a layered framebuffer
* in the same way splash them to screen. Geometry shader this time will set also `gl_ViewportIndex = gl_InvocationID`, viewports set previously through `glViewportIndexedfv`

### [gl-410-glsl-block](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_410/Gl_410_glsl_block.java) :

* shows how you can include additional shaders (in this case a second fragment shader) holding functions and having also the same block input

### [gl-410-primitive-instanced](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_410/Gl_410_primitive_instanced.java) :

* primitive instancing through geometry shader 

### [gl-410-primitive-tessellation2](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_410/Gl_410_primitive_tessellation2.java) :

* primitive tessellation, two program pipelines

### [gl-410-primitive-tessellation5](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_410/Gl_410_primitive_tessellation5.java) :

* primitive tessellation, five program pipelines

### [gl-410-program-64](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_410/Gl_410_primitive_tessellation5.java) :

* `Mat4d` - `dmat4`
* `glProgramUniformMatrix4dv`
* `dvec3` - `dvec4`
* `glProgramUniform4dv`

### [gl-410-program-binary](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_410/Gl_410_program_binary.java) :

* compiles the program pipelines (vert, geom and frag), saves them binary, loads them binary and uses them
* `GL_PROGRAM_BINARY_RETRIEVABLE_HINT`
* `glGetProgramBinary`
* `glProgramBinary`

### [gl-410-program-separate](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_410/Gl_410_program_separate.java) :

* renders a texture on the left side with a separate program and the right one with an unified one
* `glGenProgramPipelines`
* `glUseProgramStages`
