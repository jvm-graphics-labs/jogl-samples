# OpenGL 4.1 Highlights ([g-truc review](http://www.g-truc.net/doc/OpenGL%204.1%20review.pdf))

### [gl-410-buffer-uniform-array](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_410/Gl_410_buffer_uniform_array.java) :

* uses two uniform buffer arrays, one for material one for transform
* implementation of the `gl_DrawID` in OpenGL, explained [here](http://www.g-truc.net/post-0518.html)
* `glVertexAttribIPointer`
* `glVertexAttribDivisor`
* `glDrawElementsInstancedBaseVertexBaseInstance`
* explicit varying locations `layout (location = EXPLICIT_POSITION_LOCATION) in vec4 position;`

### [gl-410-caps](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_410/Gl_410_caps.java) :

* OpenGL 4.1 capabilities

### [gl-410-fbo-layered](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_410/Gl_410_fbo_layered.java) :

* allocates a texture with depth equal 4 and attach it to the `GL_COLOR_ATTACHMENT0` of an fbo. It renders to each of them via geometry shader invocation `layout(triangles, invocations = 4) in` and set the destination layer by `gl_LayerID = gl_InvocationID`. Colors hardcoded in the fragment shader.
* `gl_LayerID` allows dispatching primitives to a layered framebuffer
* in the same way splash them to screen. Geometry shader this time will set also `gl_ViewportIndex = gl_InvocationID`
*  One difference from Direct3D and OpenGL is that OpenGL allows multiple rendertarget having different sizes. This is 
actually a contain relaxed from [`GL_EXT_framebuffer_object`](https://www.opengl.org/registry/specs/EXT/framebuffer_object.txt) 
when is has been promoted to [`GL_ARB_framebuffer_object`](https://www.opengl.org/registry/specs/ARB/framebuffer_object.txt) 
and OpenGL 3.0. Interestingly, this capability seems to me pretty useless without [`GL_ARB_viewport_array`](https://www.opengl.org/registry/specs/ARB/viewport_array.txt). 
This allows to render G-Buffers at various resolutions (and hence saving some memory bandwidth) in a single pass before 
the final G-Buffer compositing for example. I also imagine some interesting use for cascade shadows using layering 
rendering. It might look like a small feature at first look but for the rendering technique side, it's actually a key 
feature from which I expect a lot of performance benefits in lot of cases.

To use the extra viewports, GLSL includes a new variable called `gl_ViewportIndex` that can be written in the geometry 
shader. 

Setup 2 viewports for 2 render targets:
```java
    gl4.glViewportIndexedfv(0, new float[]{0.0f, 0.0f, size.x, size.y}, 0);
    gl4.glScissorIndexedv(0, new int[]{0, 0, size.x, size.y}, 0);
    gl4.glDepthRangeIndexed(0, 0.0f, 0.5f);

    gl4.glViewportIndexedfv(1, new float[]{0.0f, 0.0f, size.x * 0.5f, size.y * 0.5f}, 0);
    gl4.glScissorIndexedv(1, new int[]{0, 0, size.x * 0.5f, size.y * 0.5f}, 0);
    gl4.glDepthRangeIndexed(1, 0.5f, 1.0f); 
```

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
*  The goal is to be able to release a software without the GLSL source. A GLSL binary is platform dependant and loading a 
GLSL binary might fail which involves GLSL sources rebuild. We might see some standard binary formats in the future but so 
far there is nothing in the OpenGL world. However, binary formats has been present on the OpenGL ES world for a while and 
many propritary extensions have been released: [`GL_AMD_program_binary_Z400`](https://www.khronos.org/registry/gles/extensions/AMD/AMD_program_binary_Z400.txt), 
[`GL_IMG_program_binary`](https://www.khronos.org/registry/gles/extensions/IMG/IMG_program_binary.txt), 
[`GL_ARM_mali_shader_binary`](https://www.khronos.org/registry/gles/extensions/ARM/ARM_mali_shader_binary.txt).

Conscequently, the program binary is just a cache system for GLSL binaries... It is really going to make the loading 
significantly faster?

### [gl-410-program-separate](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_410/Gl_410_program_separate.java) :

*  As part of OpenGL 4.1 core, [`GL_ARB_separate_program_objects`](https://www.opengl.org/registry/specs/ARB/separate_shader_objects.txt) 
is another welcome feature expected from a long time and quite successful in it's approach.
 
It allows to independently use shader stages without changing others shader stages.  I see two mains reasons for it: 
Direct3D, Cg and even the old OpenGL ARB program does it but more importantly it brings some software design flexibilities 
allowing to see the graphics pipeline at a lower granularity. For example, my best enemy the VAO, is a container object 
that links buffer data, vertex layout data and GLSL program input data. Without a dedicated software design, this means 
that when I change the material of an object (a new fragment shader), I need different VAO... It's fortunately possible to 
keep the same VAO and only change the program by defining a convention on how to communicate between the C++ program and 
the GLSL program. It works well even if some drawbacks remains.

With the separate programs, the fragment shader and vertex shader stages can be independant so that we are free to change 
the fragment program without touching the VAO. Finally! It's incredible all the conscequences of an awfully designed API 
(VAOs)!

So just like Direct3D, OpenGL support separate programs but actually and we should get use to it, OpenGL outperform the 
Direct3D design. We have had a single program for ages for some reasons: the resource by name convention that requires a 
linking step across stages but also because it allows some effective compiler optimizations. From those, the one I rank 
number 1 discards all the unused varying variables... This consideration has an impact on the design decision of the 
extension.

`GL_ARB_separate_program_objects` is a superset of [`GL_EXT_separate_program_objects`](https://www.opengl.org/registry/specs/EXT/separate_shader_objects.txt) 
extension including just the right improvements to transform a badly design extension to a great extension. With the ARB 
version, a new object called pipeline program object is used to attach multiple programs. Also, it's possible to 
communicate between stages using user-defined variables instead of the deprecated varying variables... GLSL programs can 
contained multiple shader stages so that multiple stages can be linked and optimized all together. Chances are that vertex, 
control and evaluation shaders will be design to be use all together and conscenquently we can apply some extra 
optimizations by linking them. Finally, `GL_ARB_separate_program_objects` defines direct state access functions 
`glProgramUniform*` for all the `glUniform*` functions! 

Create and setup a pipeline program object:
```java
    glGenProgramPipelines(1, pipelineName);
    glBindProgramPipeline(pipelineName.get(0));
    glUseProgramStages(pipelineName.get(0), GL_VERTEX_SHADER_BIT, programName[Program.VERTEX]);
    glUseProgramStages(pipelineName.get(0), GL_FRAGMENT_SHADER_BIT, programName[Program.FRAGMENT]); 
```

