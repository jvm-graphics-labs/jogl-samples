# GL-400 Highlights

### [gl-400-blend-rtt](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_400/Gl_400_blend_rtt.java) :

* ? Probabily magic

### [gl-400-buffer-uniform-array](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_400/Gl_400_buffer_uniform_array.java) :

* two buffer uniform arrays

### [gl-400-caps](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_400/Gl_400_caps.java) :

* OpenGL 400 capabilities

### [gl-400-draw-indirect](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_400/Gl_400_draw_indirect.java) :

* `glDrawElementsIndirect` similar to `glDrawElementsInstancedBaseVertexBaseInstance` but take parameters from a bound `GL_DRAW_INDIRECT_BUFFER` buffer containing a `DrawElementsIndirectCommand` command.
* you can switch between the two draw calls in order to see how they are equivalent

### [gl-400-fbo-layered](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_400/Gl_400_fbo_layered.java) :

* attaches a 4-layer texture to the `GL_COLOR_ATTACHMENT0` of an fbo and render in each of them by instances in the geometry shader. Then it binds the texture and splash each of them to screen by selecting the i-th layer via uniform.

### [gl-400-fbo-multisample](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_400/Gl_400_fbo_multisample.java) :

* generates one multisample fbo with a 4-samples texture and one simple fbo with a texture of the same dimension. Before rendering, it enables shading for each sample with `glEnable(GL_MULTISAMPLE)`, `glEnable(GL_SAMPLE_SHADING)` and `glMinSampleShading(1.0f)`. It renders the diffuse texture to the multisample fbo, blits the content to the other fbo and then render the final result to screen.
* given a `interpolateAtSample` bug in the nvidia [glsl compiler](https://devtalk.nvidia.com/default/topic/914874/opengl/glsl-compiler-bug-on-interpolateatsample-/) I had to find a trick and I was told to use `sample` identifier instead in the fragment shader to get the sample coordinates.
* [issue 7](https://github.com/elect86/jogl-samples/issues/7)

### [gl-400-fbo-rtt](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_400/Gl_400_fbo_rtt.java) :

* allocates three empty textures and attaches each of them to the first three color attachments of an fbo. Then it clears them with a different color and render them to the screen each in a different corner

### [gl-400-fbo-rtt-texture-array](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_400/Gl_400_fbo_rtt_texture_array.java) :

* same but using texture array
* `sampler2DArray`

### [gl-400-fbo-shadow](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_400/Gl_400_fbo_shadow.java) :

* render to a shadow map (depth texture) and use it to render the shadow in the next step

### [gl-400-primitive-instanced](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_400/Gl_400_primitive_instanced.java) :

* primitive instancing with geometry shader, `layout(triangles, invocations = 6) in`

### [gl-400-primitive-smooth-shading](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_400/Gl_400_primitive_smooth_shading.java) :

* primitive smooth shading in comparison, tessellation on left vs interpolated values on right

### [gl-400-primitive-tessellation](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_400/Gl_400_primitive_tessellation.java) :

* primitive tessellation (`glPatchParameterfv` calls seem to be useless)

### [gl-400-program-64](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_400/Gl_400_program_64.java) :

* `double` matrices and uniforms
* `dmat4`
* `dvec4`

### [gl-400-program-64](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_400/Gl_400_program_64.java) :

* `double` matrices and uniforms
* `dmat4`
* `dvec4`

### [gl-400-program-subroutine](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_400/Gl_400_program_subroutine.java) :

* program subroutine example

### [gl-400-program-varying-blocks](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_400/Gl_400_program_varying_blocks.java) :

* varying color with blocks

### [gl-400-program-varying-structs](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_400/Gl_400_program_varying_blocks.java) :

* varying color with blocks

### [gl-400-program-varying-structs](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_400/Gl_400_program_varying_blocks.java) :

* varying color with structs

### [gl-400-sampler-array](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_400/Gl_400_sampler_array.java) :

* loads a diffuse texture twice with right (rgba) and inverted (brga) swizzle and set the layer with an uniform `uniformDiffuseIndex` variable

### [gl-400-sampler-array-nv](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_400/Gl_400_sampler_array_nv.java) :

* `GL_NV_gpu_shader5`
* similar but different declaration `uniform sampler2D diffuse[2]` instead `uniform sampler2DArray diffuse[2]`
* and access `texture(diffuse[index], inVert.texCoord)` instead `texture(diffuse[index], vec3(texCoord, layer))`

### [gl-400-sampler-fetch](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_400/Gl_400_sampler_fetch.java) :

* `texelFetch(sampler*, ivec3 coord, int level)`
* trinilinearLod (`GL_LINEAR_MIPMAPS_LINEAR`) shader implementation

### [gl-400-texture-buffer](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_400/Gl_400_texture_buffer.java) :

* `samplerBuffer`
* loads position offsets and diffuse color in texture buffers

### [gl-400-texture-cube](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_400/Gl_400_texture_cube.java) :

* loads a texture cube as a 1-layer `GL_TEXTURE_CUBE_MAP_ARRAY`
* `samplerCubeArray`

### [gl-400-texture-derivative](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_400/Gl_400_texture_derivative.java) :

* `interpolateAtOffset` bug in the nvidia [glsl compiler](https://devtalk.nvidia.com/default/topic/914874/opengl/glsl-compiler-bug-on-interpolateatsample-/)
* [issue 7](https://github.com/elect86/jogl-samples/issues/7)

### [gl-400-transform-feedback-object](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_400/Gl_400_transform_feedback_object.java) :

* use the transform feedback to transform a `vec4 position` into a `vec4 position` and `vec4 color`.
* `glEnable(GL_RASTERIZER_DISCARD)`
* `glBindTransformFeedback(GL_TRANSFORM_FEEDBACK, feedbackName[0])`
* `glBeginTransformFeedback(GL_TRIANGLES)`
* `glDrawTransformFeedback`, no more primitive number! No more stalling queries! Cool
* `GL_INTERLEAVED_ATTRIBS`

### [gl-400-transform-feedback-stream](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_400/Gl_400_transform_feedback_stream.java) :

* same but using explicit stream instead
* `glDrawTransformFeedbackStream(GL_TRIANGLES, feedbackName[0], 0)` is equivalent to `glDrawTransformFeedback` where stream 0 is implicit
