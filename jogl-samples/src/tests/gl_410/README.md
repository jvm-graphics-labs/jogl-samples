# GL 410 - Highlights

### [gl-410-buffer-uniform-array](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_410/Gl_410_buffer_uniform_array.java) :

* uses two uniform buffer arrays, one for material one for transform
* implementation of the `gl_DrawID` in OpenGL, explained [here](http://www.g-truc.net/post-0518.html)

### [gl-410-buffer-uniform-array](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_410/Gl_410_buffer_uniform_array.java) :

* uses two uniform buffer arrays, one for material one for transform
* implementation of the `gl_DrawID` in OpenGL, explained [here](http://www.g-truc.net/post-0518.html)

### [gl-410-caps](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_410/Gl_410_caps.java) :

* OpenGL 4.1 capabilities

### [gl-410-fbo-layered](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_410/Gl_410_fbo_layered.java) :

* allocates a texture with depth equal 4 and attach it to the `GL_COLOR_ATTACHMENT0` of an fbo. It renders to each of them via geometry shader invocation `layout(triangles, invocations = 4) in` and set the destination layer by `gl_Layer = gl_InvocationID`. Colors hardcoded in the fragment shader.
* in the same way splash them to screen. Geometry shader this time will set also `gl_ViewportIndex = gl_InvocationID`, viewports set previously through `glViewportIndexedfv`
