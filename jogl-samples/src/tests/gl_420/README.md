# GL 420 - Highlights

### [gl-420-atomic-counter](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_420/Gl_420_atomic_counter.java) :

* atomic counter usage, can only be int or uint
* `GL_ATOMIC_COUNTER_BUFFER`
* `layout(binding = 0, offset = 0) uniform atomic_uint atomic`
* `atomicCounterIncrement(atomic)`

### [gl-420-buffer-uniform](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_420/Gl_420_buffer_uniform.java) :

* binds directly in the shader the uniform buffer to the wanted index, `layout(binding = TRANSFORM0) uniform Transform`

### [gl-420-caps](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_420/Gl_420_caps.java) :

* OpenGL 4.2 capabilities

### [gl-420-clipping](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_420/Gl_420_clipping.java) :

* sets the `gl_ClipDistance[0]` field inside `gl_PerVertex`

### [gl-420-debug-output](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_420/Gl_420_debug-output.java) :

* a failing program that show the debug output utility. In this case a `glClearBufferfv` is called by passing `GL_UNIFORM_BUFFER` instead of a valid buffer type.
* output
```
GlDebugOutput.messageSent(): GLDebugEvent[ id 0x500
	type Error
	severity High: dangerous undefined behavior
	source GL API
	msg GL_INVALID_ENUM error generated. ClearBuffer: <buffer> enum is invalid; expected GL_COLOR, GL_DEPTH, GL_STENCIL, or GL_DEPTH_STENCIL.
	when 1455201727472
	source 4.5 (Core profile, arb, debug, compat[ES2, ES3, ES31, ES32], FBO, hardware) - 4.5.0 NVIDIA 361.43 - hash 0x3d23e4d5]
```

### [gl-420-draw-base-instance](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_420/Gl_420_draw_base_instance.java) :

* `gl4.glDrawElementsInstancedBaseVertexBaseInstance(GL_TRIANGLES, elementCount, GL_UNSIGNED_INT, 1 * Integer.BYTES, 5, 1, 5);` requires primitive type, indices count, indices type, indices offset, instance count, base vertex (constant added to each element of indices when chosing elements from the enabled vertex arrays), base instance (in fetching instanced vertex attributes). The indices offset is the offset in bytes to add before fetching any index from `elementData`. Instance count is the number of instances we want. Base vertex indicates the index offset before using the vertex in `positionData`. Base instance is the number the `layout(location = COLOR) in vec4 color` will start choosing colors from.
* `glVertexAttribDivisor` to 1 set to increment the attribute every instance. 

### [gl-420-draw-image-space](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_420/Gl_420_draw_image_space.java) :

* renders a texture directly in image space (pixels coordinates)
* `#include draw-image-space-rendering.glsl`
 
### [gl-420-fbo](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_420/Gl_420_fbo.java) :

* loads a diffuse texture and creates an fbo with a color and depth texture and renders in it the diffuse texture twice by instantiation. Then it splashes the content of the fbo texture on screen without using any vertex attribute, just `gl_VertexID`

### [gl-420-image-load](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_420/Gl_420_image_load.java) :

* `glTexStorage2D` to allocate the image/texture
* `glTexSubImage2D` to initialize it
* `glBindImageTexture`
* `layout(binding = DIFFUSE, rgba16f) coherent uniform image2D diffuse;` in the fragment shader
* `imageLoad`

### [gl-420-image-store](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_420/Gl_420_image_store.java) :

* disables any draw buffer and writes/stores an hardcoded color to an image with `imageStore`
* then at the second step it offset the splashing a little in order to see the background color and read from the previous image with `imageLoad`

### [gl-420-image-unpack](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_420/Gl_420_image_unpack.java) :

* binds the diffuse texture, gets a 32-bit unsigned int with `imageLoad` and then unpacks it into four 8-bit unsigned integers with `unpackUnorm4x8`, then, each component is converted to a normalized floating-point value to generate the returned four-component vector as `f / 255.0`. [More](https://www.opengl.org/sdk/docs/man/html/unpackUnorm.xhtml)

### [gl-420-interface-matching](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_420/Gl_420_interface_matching.java) :

* creates a program pipeline made of one stage with vert, tesc, tese, eval and geom and another one with frag. It defines the vertex attribute input `vec2 position[2]` by offsetting only the index `glVertexAttribPointer(Semantic.Attr.POSITION + 0` all the rest remains identical. During the rendering, the program will return, indeed, only a single attribute but with size 2 and type `GL_FLOAT_VEC2`.
* the program passes a struct and a block through, this one will be modified in the tessellation control shader

### [gl-420-memory-barrier](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_420/Gl_420_memory_barrier.java) :

* Loads a diffuse texture and creates an fbo with another black texture same as big on `GL_COLOR_ATTACHMENT0`
* for 255 consecutive frames binds the black texture, set the barrier `glMemoryBarrier(GL_TEXTURE_UPDATE_BARRIER_BIT | GL_TEXTURE_FETCH_BARRIER_BIT)`, while the 256th time it chooses instead the diffuse texture, and renders it to the fbo.
* then it binds the default framebuffer, the texture on `GL_COLOR_ATTACHMENT0`, sets the `glMemoryBarrier(GL_TEXTURE_UPDATE_BARRIER_BIT)` and renders it

### [gl-420-picking](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_420/Gl_420_picking.java) :

* allocates a `Float.BYTES` `GL_TEXTURE_BUFFER` buffer for picking (depth value). 
* `layout(binding = 1, r32f) writeonly uniform imageBuffer depth`
* in the fragment shader, at hardcoded coords, it saves the depth `imageStore(depth, 0, vec4(gl_FragCoord.z, 0, 0, 0))`
* reads it back with `glMapBufferRange`
* Explaned [here](http://stackoverflow.com/a/34764441/1047713)

### [gl-420-primitive-line-aa](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_420/Gl_420_primitive_line_aa.java) :

* generates a circle made of lines, a multisample fbo with a 8-sample texture and another basic fbo with a texture same as big.
* renders three instances of this circle in the multisampled fbo, blits to the other fbo and then resolve it on screen by fetching with a factor of 8 to show easier the multisampling
* `glHint(GL_LINE_SMOOTH_HINT, GL_NICEST)`

### [gl-420-sampler-fetch](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_420/Gl_420_primitive_sampler_fetch.java) :

* shows how to fetch from a texture with the builtin `texture(sampler, vec2)` function and how instead implement it completely in shader code by itself
* custom `textureBicubicLod`
* custom` textureTrilinear`

### [gl-420-sampler-gather](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_420/Gl_420_primitive_sampler_gather.java) :

* shows a nice effect through the usage of `textureGatherOffset` that gathers four texels from a texture with offset

### [gl-420-test-depth-conservative](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_420/Gl_420_test_depth_conservative.java) :

* "For forward renders, a typical practice is to start by rendering a fast depth buffer pass only and then render the colorbuffer so that only the visible fragments are processed by the fragment shader and write to the framebuffer, saving both compute and bandwaith"
* `layout (depth_greater) out float gl_FragDepth;`
* [`GL_ARB_conservative_depth`](https://www.opengl.org/registry/specs/ARB/conservative_depth.txt)

### [gl-420-texture-array](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_420/Gl_420_texture_array.java) :

* allocates a 2d texture array with `glTexStorage3D` 
* initializes it with  `glTexSubImage3D`
* `sampler2DArray`

### [gl-420-texture-compressed](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_420/Gl_420_texture_compressed.java) :

* renders four different types of textures
* two compressed, dxt5 srgb and unorm
* two plain, rgba8 and rgb9e5 ufloat
* `glTexStorage2D`
* `glCompressedTexSubImage2D`
* `glTexSubImage2D`
* `sampler2D`

### [gl-420-texture-conversion](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_420/Gl_420_texture_conversion.java) :

* loads the same diffuse texture in four different formats
* `GL_COMPRESSED_RGBA_S3TC_DXT5_EXT`
* `GL_RGBA8UI`
* `GL_COMPRESSED_RGBA_BPTC_UNORM_ARB`
* `GL_RGBA8_SNORM`

