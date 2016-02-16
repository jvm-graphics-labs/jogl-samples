# OpenGL 4.4 Highlights ([g-truc review](http://www.g-truc.net/doc/OpenGL%204.4%20review.pdf))

### [gl-440-atomic-counter](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_440/Gl_440_atomic_counter.java):

* creates a `GL_ATOMIC_COUNTER_BUFFER` buffer and in the fragment shader increments it and uses it to shade
* "Searching performance, we might mislead ourselves very easily. Good performance for a real time software is not about having
the highest average frame rate but having the highest minimum framerate by avoiding latency spikes. Sadly, most benchmarks 
don't really show the performance of hardware but how bad they are to do whatever they do and they will typically report 
average FPS ignoring the real user experience.

The OpenGL specifications provide buffer objects since OpenGL 1.5 and that includes the famous usage hints. Why is it called
_hints_? Because an implementation can do whatever it wants with a buffer regardless of the hint set at the buffer creation.
In average the drivers do a good job and increase the average frame rate however the behavior of the drivers is nearly
unpredictable and it could decide to move a buffer at the worse time in our program leading to latency spikes.

The purpose of [`ARB_buffer_storage`](https://www.opengl.org/registry/specs/ARB/buffer_storage.txt) is to resolve this issue and
make performance more reliable by replacing the usage hints by usage flags and creating immutable buffers.

Immutable buffers are buffers that we can't change the size or orphans. To create an immutable buffer we use `glBufferStorage`
command and because it is immutable we can't possible call `glBufferData` on that buffer.

If we want to modify a buffer we need to use the flag `GL_MAP_WRITE_BIT`. If we want to read its content, we need to use the
flag `GL_MAP_READ_BIT`. Only using these flags implies that we can't use `glBufferSubData` to modify an immutable buffer.

`GL_MAP_PERSISTENT_BIT` simply ensures that the mapped pointer remains valid even if rendering commands are executed during the
mapping. `GL_MAP_COHERENT_BIT` opens opportunities to ensure that whenever the client of server side performs charges on a 
buffer, these changes will be visible on this other side.

So, no more hints on buffers? Unfortunately, these are not dead as two hints have been included: `GL_DYNAMIC_STORAGE_BIT` and 
`GL_CLIENT_STORAGE_BIT`. I guess if we want to be serious about OpenGL programming and performance level, we should never use
`GL_DYNAMIC_STORAGE_BIT` and `GL_CLIENT_STORAGE_BIT`.

### [gl-440-buffer-storage](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_440/Gl_440_buffer_storage.java):

* loads a diffuse texture and creates a `COPY` immutable storage buffer with `glBufferStorage` and ´GL_MAP_WRITE_BIT` and 
writes in it the vertex positions and indices. Then it creates other two immutable buffers, `VERTEX` and `ELEMENT`, and copies 
in them the corresponding data from `COPY`. Then it renders the diffuse texture.

### [gl-440-buffer-type](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_440/Gl_440_buffer_type.java):

* 6 different types of data buffer: `F32`, `I8`, `I32`, `RGB10A2`, `F16`, `RG11B10F`

### [gl-440-caps](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_440/Gl_440_caps.java):

* OpenGL 4.4 capabilities

### [gl-440-fbo-depth-stencil](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_440/Gl_440_fbo_depth_stencil.java):

* "[`ARB_texture_stencil8`](https://www.opengl.org/registry/specs/ARB/texture_stencil8.txt) is a good sense extension. The 
only case where we would want to use renderbuffer with OpenGL 4.3 would be to create a stencil only framebuffer attachment. 
With this new extension, we can create a stencil texture, which bury renderbuffers. They could have been useful for tile based
GPUs but it seems that nobody cared to keep them special enough to legitimate their existence. Hence, they are deprecated in
my mind and if I were to use them, it would be only to write cross API code between OpenGL ES 3.0 and OpenGL desktop."
* loads a diffuse texture and creates an fbo with a color texture, a depth texture and a stencil texture as big as the diffuse.
Then it splashes on screen.

### [gl-440-fbo-without-attachment](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_440/Gl_440_fbo_without_attachment.java):

* `GL_ARB_framebuffer_no_attachments`
* `GL_ARB_clear_texture`
* `GL_ARB_shader_storage_buffer_object`
* loads a diffuse texture and renders it to another texture via an attachmentless fbo and then splashes it to screen

### [gl-440-interface-matching](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_440/Gl_440_interface_matching.java):

* interface matching

### [gl-440-multi-draw-indirect-id](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_440/Gl_440_multi_draw_indirect_id.java): broken

* "A common misconception in rendering is that draw calls are expensive. They are not. What's expensive is switching 
resources between draw calls as that introduces a massive CPU overhead while the draw call itself is just the GPU command
processor launching a job. Hence, we need to avoid switching resources relying on batching and on shader based dynamically
uniform resource indexing. I have largely cover this topic in my GPU Pro 4 chapter titled ["Introducing the programmable
pulling rendering pipeline"](http://nedrilad.com/Tutorial/topic-58/GPU-Pro-Advanced-Rendering-Techniques-35.html). For this
purpose, OpenGL introduces two new extentions: [`ARB_shader_draw_parameters`](https://www.opengl.org/registry/specs/ARB/shader_draw_parameters.txt)
and [`ARB_indirect_parameters`](https://www.opengl.org/registry/specs/ARB/indirect_parameters.txt).

### [gl-440-sampler-wrap](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_440/Gl_440_sampler_wrap.java)

* `glBindBufferBase(int target, int first, int count, int[] buffers, int buffers_offset)`
* `glBindSamplers(int first, int count, int[] samplers, int samplers_offset)`
* `glBindTextures(int first, int count, int[] textures, int textures_offset)`
