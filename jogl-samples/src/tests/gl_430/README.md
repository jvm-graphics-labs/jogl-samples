# OpenGL 4.3 Highlights ([g-truc review](http://www.g-truc.net/doc/OpenGL%204.3%20review.pdf))

### [gl-430-atomic-counter](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_430/Gl_430_atomic_counter.java):

* [`GL_ARB_clear_buffer_object`](https://www.opengl.org/registry/specs/ARB/clear_buffer_object.txt) provides in my opinion 
one of the oddest features designed for OpenGL (this way better than the original [`GL_ARB_vertex_array_object`](https://www.opengl.org/registry/specs/ARB/vertex_array_object.txt).
It provides an useful functionality design to initialize a buffer but the buffer API is designed with the texture API conventions,
using both a format and an internalFormat.

Reading this extension we understand that this API is actually designed to initialize a texture buffer, not generic buffer, even
if it can work on any buffer... Consedering that OpenGL 4.3 is released with the storage buffer from [`GL_ARB_shader_storage_buffer_object`](https://www.opengl.org/registry/specs/ARB/shader_storage_buffer_object.txt),
one could consider the texture buffer simply deprecated. However, `ARB_clear_buffer_object` can't be used to initialize a
storage buffer..

I picture one really useful use case of this extension with atomic buffer. In some scenarios, only the shaders are writing into
the atomic buffer but for example for every frame, draw call, etc., the atomic counter needs to be reinitialized because it stores
a count, an offset which is only useful for these specific ranges. Howver really for these cases atomics don't even need to be
backed by a buffer and it seems to me that it only forces the implementation to take a slower path than what the hardware
could do.
* each fragment performs an `atomicCounterIncrement`
* the atomic buffer gets reset by `glClearBufferSubData`

### [gl-430-buffer-uniform](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_430/Gl_430_buffer_uniform.java):

* Opengl provides a long list of states queries and it is typically recommended to avoid using them for performance. Yes, the
query API is not efficient but for debug builds they can provide valuable information to avoid looking for hours why a silent 
error is happening. (OpenGL doesn't generate an error but the rendering is "obviously" wrong). As an example, the query API
could be used to validate whether the vertex input interface matches the bound vertex array object. Unfortunately, the query
API is not complete: we can't query the fragment shader output or we can't query the varying input and output variables either.
[`GL_ARB_program_interface_query`](https://www.opengl.org/registry/specs/ARB/program_interface_query.txt) resolves these issues
and actually provides a unified new query API so that with only seven functions we can do all the possible queries, including
transform feedbacks, uniform blocks, samplers, etc.

I also think it is important to notice that the language defining the name strings to use for the queries has been clarify which
should lead to less bugs on that side.
* `glGetProgramInterfaceiv`
* `glGetProgramResourceName`
* `glGetProgramResourceiv`

### [gl-430-caps](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_430/Gl_430_caps.java):

* OpenGL 4.3 capabilities

### [gl-430-debug](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_430/Gl_430_debug.java):

* I keep saying that [`GL_ARB_debug_output`](https://www.opengl.org/registry/specs/ARB/debug_output.txt) is one of the greatest
evolutions that OpenGL has known for the past years. There is really no reason on platforms supporting it to keep using 
`glGetError`. Using an API is only viable when it's practical to use it. `ARB_debug_output`  brings this level of viability to
OpenGL. We can instantly figure out where an error happens and have a descriptive comment for the nature of the error. 
OpenGL Insights includes a chapter called [ARB_debug_output: An Helping Hand for Desperate Developer](http://www.seas.upenn.edu/~pcozzi/OpenGLInsights/OpenGLInsights-ARB_debug_output.pdf]
by Antonio Remises Fernandes and Bruno Oliveira describing in details this extension and what we can expect from implementations.

This extension didn't reach OpenGL core as it is. Instead it got promoted to [`KHR_debug`](https://www.opengl.org/registry/specs/KHR/debug.txt),
which is a superset of `ARB_debug_output` not only designed for OpenGL but also for OpenGL ES! `KHR_debug` is a superset 
becazse it includes ARB_debug_output but add some features like debug groups, debug markers and debug labels, features 
inspired by the OpenGL ES extensions [`EXT_debug_marker`](https://www.opengl.org/registry/specs/EXT/EXT_debug_marker.txt) and
[`EXT_debug_label`](https://www.opengl.org/registry/specs/EXT/EXT_debug_label.txt).

The debug label allows attaching a descriptive string for any OpenGL object. Then this label can be reused to generate the 
debug output messages generated from this object. With debug marker, the OpenGL programmer can annotate the debug output
stream to notify specific events. Debug group allows encapsulating a section of the code so that a specific debug output
volume setup can be used for this group. Both entering and leaving a debug group can generate messages so that debug group 
can just be used as debug group markers.

Basic debug output setup asking the implementation to generate all the feedback it can:
```java
gl4.glEnable(GL_DEBUG_OUTPUT_SYNCHRONOUS);
gl4.glPushDebugGroup(GL_DEBUG_SOURCE_APPLICATION, 76, -1, “My debug group”);
gl4.glDebugMessageControl(GL_DONT_CARE, GL_DONT_CARE, GL_DONT_CARE, 0, NULL, true);
```

In jogl, to enable Debug Output you have to pass it at the context creation, this is the only way contry to standard OpenGL. 
`gl4.glEnable(GL_DEBUG_OUTPUT_SYNCHRONOUS)` won't enabled it if you didn't set up properly the context.

### [gl-430-direct-state-access-ext](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_430/Gl_430_direct_state_access_ext.java):

* this sample wasn't easy to get it working, let me explain how I got all the shits working..
* this is the same thing of Debug Output, since DSA got into core, jogl skipped `GL_EXT_direct_state_access` and jumped directly on the [`GL_EXT_direct_state_access`](GL_ARB_direct_state_access), this means for the 90% of EXT-DSA calls just removing the `EXT` suffix at the end. For the rest it's another story..
* remember when in the opengl school they taught you about `glGen*`? Well, forget it bitch! :scream: Now what exists is only `glCreate*`, this because when you call `glGen*` you create the object but the real allocation happens at the first `glBind`, so if now you don't bind anything you will get errors everywhere because none of your objects are allocated... so `glCreateProgramPipelines`, `glCreateBuffers`, `glCreateSamplers`, `glCreateFramebuffers`, `glCreateVertexArrays`. Wait where are the textures? Exactly... `glCreateTextures` has the "nice" feature to request the target, so if previously you were creating all the different textures you had in one shot, now you have to run it for each different target... :expressionless:
* no more `glVertexArrayVertexAttribOffsetEXT` or `glEnableVertexArrayAttribEXT`, you have to go VAB (vertex array buffer), format and binding separated, `glVertexArrayAttribFormat`, `glVertexArrayVertexBuffer`, `glVertexArrayAttribBinding` and `glVertexArrayElementBuffer`, all of them connected by a common `int bindingIndex`, in my case `Semantic.Buffer.STATIC`
* [`GL_ARB_gpu_shader5`](https://www.opengl.org/registry/specs/ARB/gpu_shader5.txt) extends this functionality 
so that we get a fully programmable texture filtering. The sampler object is no longer useful. It allows to 
gather any component of a 2x2 footprint. It allows to use an arbitrary offset to select the footprint and even 
extend this functionality to a per-component offset. Finally, it allows to perform a per-sample depth comparison.

### [gl-430-vertex-attrib-binding](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_430/Gl_430_vertex_attrib_binding.java):

* "If you have been reading G-Truc Creation, you probably know that I hate so much the Vertex Array Object and I believe it 
is the biggest mistake ever made in OpenGL. [`ARB_vertex_attrib_binding`](https://www.opengl.org/registry/specs/ARB/vertex_attrib_binding.txt)
is kind of the vertex array object done right: the vertex format and the vertex array buffer are now separated. It should be
easy to update an existing application to take advantage of `ARB_vertex_attrib_binding`.

It's pretty unfortunate to see this extension being released now when not only the battle is finished but a new war has
began with programmable vertex pulling where ultimately we won't even need vertex array objects, array buffers and maybe
element array buffers.
* `glVertexAttribFormat`
* `glVertexAttribBinding`
* `glBindVertexBuffer`

### [gl-430-draw-without-vertex-attrib](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_430/Gl_430_draw_without_vertex_attrib.java):

* An usage of the shader storage buffer, containing vertex positions
* `GL_SHADER_STORAGE_BUFFER`
* `layout(std430, binding = VERTEX) buffer Mesh`

### [gl-430-fbo-invalidate](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_430/Gl_430_fbo_invalidate.java):

* [`ARB_invalidate_subdata`](https://www.opengl.org/registry/specs/ARB/invalidate_subdata.txt) is an extension designed to
avoid unnecessary memory transfers. By invalidating buffers or images either entirely or by ranges implementations no longer
need to maintain these data allowing them to avoid memory transfer from a memory space to another. In practice it sounds like
whenever we don't need the content of a resource we should use new functions. We can expect that the behaviours of this set
of new functions will depend from implementation to implementation. We can also imagine that to be effective a minimum range
of the data will need to be invalidated per call.

### [gl-430-fbo-srgb-decode](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_430/Gl_430_fbo_srgb_decode.java):

* loads a diffuse srgb texture and create an fbo with an srgb texture
* renders the diffuse texture to the fbo and then splash it on screen

### [gl-430-fbo-without-attachment](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_430/Gl_430_fbo_without_attachment.java):

* Thanks to OpenGL 4.2 and [`ARB_shader_image_load_store`](https://www.opengl.org/registry/specs/ARB/shader_image_load_store.txt)
we can write a shader that will effectively never need to write to the framebuffer attachment. However, to setup the 
rasterizer, an application needs to create a framebuffer and an attachment. It is particularly embarrassing when we want to
setup the rasterize for layered cube map rendering or HDR multisample rendering. The memory will be reserved by the drivers
but never used! [`ARB_framebuffer_no_attachments`](https://www.opengl.org/registry/specs/ARB/framebuffer_no_attachments.txt)
resolves this issue. `ARB_shader_storage_buffer_object` can be used in place of `ARB_shader_image_load_store` in this 
context.

Initialization of a framebuffer object without attachment:
```java
gl4.glGenFramebuffers(1, name);
gl4.glBindFramebuffer(GL_FRAMEBUFFER, name.get(0));
gl4.glFramebufferParameteri(GL_FRAMEBUFFER, GL_FRAMEBUFFER_DEFAULT_WIDTH, width);
gl4.glFramebufferParameteri(GL_FRAMEBUFFER, GL_FRAMEBUFFER_DEFAULT_HEIGHT, height);
gl4.glFramebufferParameteri(GL_FRAMEBUFFER, GL_FRAMEBUFFER_DEFAULT_LAYERS, layers);
gl4.glFramebufferParameteri(GL_FRAMEBUFFER, GL_FRAMEBUFFER_DEFAULT_SAMPLES, samples);
gl4.glFramebufferParameteri(GL_FRAMEBUFFER, GL_FRAMEBUFFER_DEFAULT_FIXED_SAMPLE_LOCATIONS, fixed);
gl4.glBindFramebuffer(GL_FRAMEBUFFER, 0);
```

Combined with a compute shader stage we can arguably say that this extension enables a form of programmable blending. For a
more effective form, we could image that allows using the shadred blocks in a fragment shader stage could give us an extra
edge.
* loads a diffuse texture, bind an attachmentless fbo and renders it on another texture through `glBindImageTexture` and
`imageStore` and then splashes it on screen.

### [gl-430-fbo-without-attachment](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_430/Gl_430_fbo_without_attachment.java):

*