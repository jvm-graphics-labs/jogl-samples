# OpenGL 4.2 Highlights ([g-truc review](http://www.g-truc.net/doc/OpenGL%204.2%20review.pdf))

### [gl-420-atomic-counter](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_420/Gl_420_atomic_counter.java) :

* The atomic counter is a new opaque type which can be declare in any stage with up to 8 instances of them. The atomic 
counter must be backed by a buffer object which allows accessing on the application side to the values of an atomic 
counter.
 
An atomic counter is represented by a 32 bits unsigned int and only three operations can be performed on it: 

we can read the current value (`atomicCounter`)

get the counter value then increment it (`atomicCounterIncrement`) 

decrement the counter value then get the counter value (`atomicCounterDecrement`)
 
It is safe to increment or decrement in any shader stage and for any execution of this stage. However, incrementing and 
decrementing within an execution should be considered as if the operations on the atomic counter were unordered. The 
OpenGL specification doesn’t define any mechanism (memory barrier) to ensure operations ordering. 
```glsl
// binding: unit where the atomic buffer is bound
// offset: buffer data offset, where the atomic counter value is stored.
layout(binding = 0, offset = 0) uniform atomic_uint a;
```
This extension is extremely simple to use. An atomic counter is an opaque type declared as a uniform variable just like 
the sampler object. A buffer object is bound to an atomic counter binding point but unlike sampler, we can't set the index 
of the bounding point with `glUniform1i`. It requires to be set within the GLSL program using the layout qualifier 
_binding_. This is also possible for samplers thanks to [`GL_ARB_shading_language_420pack`](https://www.opengl.org/registry/specs/ARB/shading_language_420pack.txt).
* OpenGL 4.2 provides two new opaque objects for both image types and atomic counter types. To associate variables declared 
with these types, GLSL provides the layout qualifier _binding_. The extra goodness is that this qualifier has been extended 
to sampler and uniform buffer objects, bye bye `glUniform1i` and `glUniformBlockBinding`, I won’t miss you!
* `GL_ATOMIC_COUNTER_BUFFER`
* `layout(binding = 0, offset = 0) uniform atomic_uint atomic`
* `layout(binding = TRANSFORM0) uniform Transform`
* `atomicCounterIncrement(atomic)`

### [gl-420-buffer-uniform](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_420/Gl_420_buffer_uniform.java) :

* binds directly in the shader the uniform buffer to the wanted index, `layout(binding = TRANSFORM0) uniform Transform`

### [gl-420-caps](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_420/Gl_420_caps.java) :

* OpenGL 4.2 capabilities

### [gl-420-clipping](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_420/Gl_420_clipping.java) :

* sets the `gl_ClipDistance[0]` field inside `gl_PerVertex`

### [gl-420-debug-output](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_420/Gl_420_debug-output.java) :

* in theory it should illustrates `GL_ARB_debug_output` but since the extension got into core, jogl skipped it and supports directly `GL_KHR_debug`, so this is basically a copy of gl-430-debug-output..
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

* OpenGL 3.2 brought a very interesting feature on the regard of buffer management through the extension 
[`ARB_draw_elements_base_vertex`](https://www.opengl.org/registry/specs/ARB/draw_elements_base_vertex.txt). It allows to 
run a draw call on a subset of the array buffers attached to the VAO for what could be understood as sparse rendering of 
large buffers, multiple rendering of different meshes with the same VAO.

Effectively, this extension provides a parameter call _basevertex_ which is an offset from the beginning of the array 
buffer. This offset is really interesting but the interaction with instanced arrays is not so good. Instanced arrays can 
be small, building larger instanced arrays by packing multiple of then could have benefits for performance. This wasn’t 
possible until the release of OpenGL 4.2 and [`ARB_base_instance`](https://www.opengl.org/registry/specs/ARB/base_instance.txt).

To remove these restrictions, the new command: 
`glDrawElementsInstancedBaseVertexBaseInstance` (*ouf*!) 

It adds a new parameter call _baseinstance_ which adds an offset to the element index using the following equation:

_element_ = floor(`gl_InstanceID` / _divisor_) + _baseinstance_

The _baseinstance_ parameter has no effect if the value of the divisor is 0. 

* `gl4.glDrawElementsInstancedBaseVertexBaseInstance(GL_TRIANGLES, elementCount, GL_UNSIGNED_INT, 1 * Integer.BYTES, 5, 1, 5);` requires primitive type, indices count, indices type, indices offset, instance count, base vertex (constant added to each element of indices when chosing elements from the enabled vertex arrays), base instance (in fetching instanced vertex attributes). The indices offset is the offset in bytes to add before fetching any index from `elementData`. Instance count is the number of instances we want. Base vertex indicates the index offset before using the vertex in `positionData`. Base instance is the number the `layout(location = COLOR) in vec4 color` will start choosing colors from.
* `glVertexAttribDivisor` to 1 set to increment the attribute every instance. 

### [gl-420-draw-image-space](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_420/Gl_420_draw_image_space.java) :

* renders a texture directly in image space (pixels coordinates)
* [`GL_ARB_shading_language_include`](https://www.opengl.org/registry/specs/ARB/shading_language_include.txt) 
is an extension that provides an `#include` directive to GLSL. The goal is to reuse the same shader text in 
multiple shader across multiple contexts. The way `GL_ARB_shading_language_include` allows this, it 
introduces _named strings_ to create some kind of paths in the GLSL compiler space that contains the shader 
texts. These name strings are created and deleted by `glNamedStringARB` and `glDeleteNamedStringARB`. From 
these name strings, the shader text can be compiled with the function `glCompileShaderIncludeARB` of 
`glCompileShader`. 
* `#include draw-image-space-rendering.glsl`
 
### [gl-420-fbo](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_420/Gl_420_fbo.java) :

* loads a diffuse texture and creates an fbo with a color and depth texture and renders in it the diffuse texture twice by instantiation. Then it splashes the content of the fbo texture on screen without using any vertex attribute, just `gl_VertexID`

### [gl-420-image-load](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_420/Gl_420_image_load.java) :

* [`GL_ARB_shader_image_load_store`](https://www.opengl.org/registry/specs/ARB/shader_image_load_store.txt) is wonderful 
new extension but also the promoted extension to core of [`EXT_shader_image_load_store`](https://www.opengl.org/registry/specs/EXT/shader_image_load_store.txt) 
thanks to few changes between the two extensions. To introduce this extension, I think that there is nothing like the first 
sentence of the extension overview:

_This extension provides GLSL built-in functions allowing shaders to load from, store to, and perform atomic read-modify-write 
operations to a single level of a texture object from any shader stage."

Incredible? This is exactly the feeling I had when I first discovered it.

OpenGL 4.2 clarifies and maybe actually specify what is an _image_ in OpenGL. An image could be a single mipmap level or a 
single texture 2d array layer which composes a texture. Images are bound to _image units_ using the command 
`glBindImageTexture` for a maximum minimum of 8 units in the fragment shader stage only according to the OpenGL 4.2 
specifications. In practice, I believe that both AMD and NVIDIA hardware allow binding these units to the vertex shader 
stage as well and maybe any shader stage. 
```java
void glBindImageTexture(
    int unit, 
    int texture, 
    int level, 
    boolean layered, 
    int layer, 
    int access, 
    int format);
```
Most of the rest of this extension happens in GLSL and take the following shape for example:
```glsl
layout(binding = 0, rgba8ui) uniform readonly image2D Image;
```
Two main GLSL functions are available to access any image of any format: `imageLoad` and `imageStore`. However, the 
following extended set of atomic functions is only available for images using the formats `r32i` and `r32ui`.

Atomic operations on integer types:

imageAtomicAdd

imageAtomicMin

imageAtomicMax

imageAtomicAnd

imageAtomicOr

imageAtomicXor

imageAtomicExchange

imageAtomicCompSwap

* `imageLoad`
* `glTexStorage2D` to allocate the image/texture
* `glTexSubImage2D` to initialize it
* `glBindImageTexture`
* `layout(binding = DIFFUSE, rgba16f) coherent uniform image2D diffuse;` in the fragment shader

### [gl-420-image-store](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_420/Gl_420_image_store.java) :

* disables any draw buffer and writes/stores an hardcoded color to an image with `imageStore`
* then at the second step it offset the splashing a little in order to see the background color and read from the previous image with `imageLoad`

### [gl-420-image-unpack](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_420/Gl_420_image_unpack.java) :

* [`GL_ARB_shading_language_packing`](https://www.opengl.org/registry/specs/ARB/shading_language_packing.txt) provides a 
set of _pack_ and _unpack_ functions which allow loading and storing 16 bits floating values as an unsigned integer in a 
similar fashion than `packDouble2x32` and `unpackDouble2x32` using the new GLSL functions `packHalf2x16` and `unpackHalf2x16`.

This extension also gathers some packing functions from ARB_gpu_shader5 so that the packing functions can be exposed by 
OpenGL 3 hardware. This includes `[un]packUnorm4x8`, `[un]packSnorm4x8`, `[un]packUnorm2x16` and the previously missing 
`[un]packSnorm2x16`. These functions are essential as they expose the normalization mechanisms of the hardware.

The feature set exposed by this extension is taking a new light with the release of OpenGL 4.2 and [`ARB_shader_image_load_store`](https://www.opengl.org/registry/specs/ARB/shader_image_load_store.txt) 
because atomic operations on image data can only been perform on signed and unsigned 32 bits integers.

* binds the diffuse texture, gets a 32-bit unsigned int with `imageLoad` and then unpacks it into four 8-bit unsigned integers with `unpackUnorm4x8`, then, each component is converted to a normalized floating-point value to generate the returned four-component vector as `f / 255.0`. [More](https://www.opengl.org/sdk/docs/man/html/unpackUnorm.xhtml)

### [gl-420-interface-matching](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_420/Gl_420_interface_matching.java) :

* creates a program pipeline made of one stage with vert, tesc, tese, eval and geom and another one with frag. It defines the vertex attribute input `vec2 position[2]` by offsetting only the index `glVertexAttribPointer(Semantic.Attr.POSITION + 0` all the rest remains identical. During the rendering, the program will return, indeed, only a single attribute but with size 2 and type `GL_FLOAT_VEC2`.
* the program passes a struct and a block through, this one will be modified in the tessellation control shader

### [gl-420-memory-barrier](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_420/Gl_420_memory_barrier.java) :

* These dynamic load and store involve some pretty complex memory management issues: when we are reading a data, are we 
sure the result of a previous operation is stored already? For this purpose, this extension provides a GLSL function and 
an OpenGL command which will make sure that the previous operations are executed: `glMemoryBarrier` and `memoryBarrier. 

`glMemoryBarrier` orders that the memory transactions of certain types are issued prior the commands after this barrier. 
This barrier apply only to selected types of data by the OpenGL programmer.  

Flags that can be passed to glMemoryBarrier:
`GL_VERTEX_ATTRIB_ARRAY_BARRIER_BIT`
`GL_ELEMENT_ARRAY_BARRIER_BIT`
`GL_UNIFORM_BARRIER_BIT`
`GL_TEXTURE_FETCH_BARRIER_BIT`
`GL_SHADER_IMAGE_ACCESS_BARRIER_BIT`
`GL_COMMAND_BARRIER_BIT`
`GL_PIXEL_BUFFER_BARRIER_BIT`
`GL_TEXTURE_UPDATE_BARRIER_BIT`
`GL_BUFFER_UPDATE_BARRIER_BIT`
`GL_FRAMEBUFFER_BARRIER_BIT`
`GL_TRANSFORM_FEEDBACK_BARRIER_BIT`
`GL_ATOMIC_COUNTER_BARRIER_BIT`
`GL_ALL_BARRIER_BITS`

`memoryBarrier` (unfortunately not yet present in jogl) behaves the same way than `glMemoryBarrier` except that it doesn’t 
expose a fine grain memory barrier and ensure instead that all memory accesses are performed prior to the synchronization 
point. 

This set of feature is inherited from [`NV_texture_barrier`](https://www.opengl.org/registry/specs/NV/texture_barrier.txt) 
which has demonstrated effective use cases outside the scope of OpenGL images, for example for ping-pong rendering and it 
provides significative performance benefices. I believe that this extension should have been exposed in a separated 
ARB extension to benefit OpenGL 3 hardware.
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

* It shows a nice effect 
through the usage of `textureGatherOffset` that gathers four texels from a texture with a given offset

### [gl-420-test-depth-conservative](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_420/Gl_420_test_depth_conservative.java) :

* [`AMD_conservative_depth`](https://www.opengl.org/registry/specs/AMD/conservative_depth.txt) has been promoted to 
[`ARB_conservative_depth`](https://www.opengl.org/registry/specs/ARB/conservative_depth.txt) and OpenGL 4.2 core 
specification. It enables some hardware optimisations according criteria defined by the OpenGL users. 

For forward renderers, a typical practice is to start by rendering a fast depth buffer pass only and then render the 
colorbuffer so that only the visible fragments are processed by the fragment shader and write to the framebuffer, saving 
both compute and bandwidth.

This extension provides a level of programmability for at least some of the optimisations involved at this level using some 
layout qualifiers to specify how to expect the value of the `gl_FragDepth`.
```glsl
// assume it may be modified in any way
layout (depth_any) out float gl_FragDepth;

// assume it may be modified such that its value will only increase
layout (depth_greater) out float gl_FragDepth;

// assume it may be modified such that its value will only decrease
layout (depth_less) out float gl_FragDepth;

// assume it will not be modified
layout (depth_unchanged) out float gl_FragDepth;
```

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
* `GL_COMPRESSED_RGBA_BPTC_UNORM_ARB` (BC7)
* `GL_RGBA8_SNORM`
* Released alongside with OpenGL 4.0, [`ARB_texture_compression_bptc`](https://www.opengl.org/registry/specs/ARB/texture_compression_bptc.txt) 
extension finally reach OpenGL core specification. It provides Direct3D 11 compressed formats known as BC6H and BC7 and 
called respectively `GL_BPTC_FLOAT` and `GL_BPTC` with OpenGL. They aim high dynamic range, low dynamic range texture 
compression and high quality compression of sharp edges. The compression ratio for `GL_BPTC_FLOAT` and `GL_BPTC` are 6:1 
and 3:1.
[image comparison, read the review]
The result given by BPTC is absolutely stunning. This picture is actually challenging which explains the ugly result given 
by DXT1: it's blocky, smooth parts become stairs and noisy parts become fat pixels. BPTC remains very close to the 
uncompressed texture. BPTC even manage to provide more details than RGTC1 in many cases.

BPTC is a great texture format for visual quality so that for this property I expect to see it used instead of DXT1 and 
DXT5 in many cases in the future. However, the BPTC format has a main drawback: it's slow, very slow to generate! Chances 
are that all the real-time compression use cases of DXT5 will stick to this format for a while. I even think that this is 
such a limitation that it will prevent a wide adoption in real application for a while.


### [gl-420-texture-cube](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_420/Gl_420_texture_cube.java) :

* array cube map showcase
* `glTexStorage3D`
* [`GL_ARB_texture_cube_map_array`}(https://www.opengl.org/registry/specs/ARB/texture_cube_map_array.txt)
provides D3D10.1 cube map arrays
* `GL_TEXTURE_CUBE_MAP_ARRAY`
* `glTexSubImage3D`
* `samplerCubeArray`

### [gl-420-texture-pixel-store](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_420/Gl_420_texture_pixel_store.java) :

* OpenGL provides a functionality which allows uploading a subset of a texture to graphics memory without creating 
temporary buffers. This is accomplished using `glPixelStorei` with the arguments `GL_UNPACK_ROW_LENGTH`, 
`GL_UNPACK_SKIP_PIXELS` and `GL_UNPACK_SKIP_ROWS`. It also allows downloading to global memory a subset of a texture store 
in graphics memory using the arguments `GL_PACK_ROW_LENGTH`, `GL_PACK_SKIP_PIXELS` and `GL_PACK_SKIP_ROWS`. 

The following code uploads a subset of the image _TEXTURE_DIFFUSE_RGB8_ which is half the size and the centre of original 
picture.

jgli.Texture2d image = new Texture2d(jgli.Load.load(TEXTURE_DIFFUSE_RGB8));

gl4.glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
gl4.glPixelStorei(GL_UNPACK_ROW_LENGTH, image.dimensions(0).x);
gl4.glPixelStorei(GL_UNPACK_SKIP_PIXELS, image.dimensions(0).x) / 4);
gl4.glPixelStorei(GL_UNPACK_SKIP_ROWS, image.dimensions(0).y) / 4);

gl4.glTexImage2D(
    GL_TEXTURE_2D, 
    0, 
    GL_RGBA8, 
    image.dimensions(0).x / 2, 
    image.dimensions(0).y / 2, 
    0,  
    GL_BGR, 
    GL_UNSIGNED_BYTE, 
    image.data(0));

However, this is designed to work on pixels. It is not good for compressed texture formats which are typically packed 
blocks of N by M pixels. Thus, this isn’t available for compressed texture format in OpenGL 4.1.

OpenGL 4.2 and [`ARB_compressed_texture_pixel_storage`](https://www.opengl.org/registry/specs/ARB/compressed_texture_pixel_storage.txt) 
remove this limitation of compressed textures allowing `glPixelStorei` to control the way compressed texture are uploaded 
and downloaded from the GPU memory. To make this possible, arguments for `glPixelStorei` has been added:

New tokens for partial compressed texture data copy:

`GL_UNPACK_COMPRESSED_BLOCK_WIDTH` 
`GL_UNPACK_COMPRESSED_BLOCK_HEIGHT` 
`GL_UNPACK_COMPRESSED_BLOCK_DEPTH` 
`GL_UNPACK_COMPRESSED_BLOCK_SIZE` 
`GL_PACK_COMPRESSED_BLOCK_WIDTH`
`GL_PACK_COMPRESSED_BLOCK_HEIGHT`
`GL_PACK_COMPRESSED_BLOCK_DEPTH`
`GL_PACK_COMPRESSED_BLOCK_SIZE`

These arguments are used to specify the size of a block in pixels and in bytes giving to OpenGL implementations enough 
information to read and write subset of compressed images without splitting compression blocks.

The following code uploads a subset of the compressed texture _TEXTURE_DIFFUSE_DXT1_ which is half the size and the centre 
of original picture.

jgli.Texture2d image = jgli.Load.load(TEXTURE_DIFFUSE_DXT1);

gl4.glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
gl4.glPixelStorei(GL_UNPACK_COMPRESSED_BLOCK_WIDTH, 4);
gl4.glPixelStorei(GL_UNPACK_COMPRESSED_BLOCK_HEIGHT, 4);
gl4.glPixelStorei(GL_UNPACK_COMPRESSED_BLOCK_DEPTH, 1);
gl4.glPixelStorei(GL_UNPACK_COMPRESSED_BLOCK_SIZE, 8);
gl4.glPixelStorei(GL_UNPACK_SKIP_PIXELS, image.dimensions(0).x / 4);
gl4.glPixelStorei(GL_UNPACK_SKIP_ROWS, image.dimensions(0).y / 4);
gl4.glPixelStorei(GL_UNPACK_ROW_LENGTH, image.dimensions().x);

gl4.glCompressedTexImage2D(
    GL_TEXTURE_2D, 
    0, 
    GL_COMPRESSED_RGB_S3TC_DXT1_EXT, 
    image.dimensions(0).x) / 2, 
    image.dimensions(0).y) / 2, 
    0,  
    image.capacity(0) / 4, 
    image.data(0));

* loads only a portion of a texture by setting pixel store parameters
* `glPixelStorei`
* `GL_UNPACK_ROW_LENGTH`
* `GL_UNPACK_SKIP_PIXELS`
* `GL_UNPACK_SKIP_ROWS`
* `glCompressedTexSubImage2D`

### [gl-420-texture-storage](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_420/Gl_420_texture_storage.java) :

* "the new [`GL_ARB_texture_storage`](https://www.opengl.org/registry/specs/ARB/texture_storage.txt) extension decouples 
the allocation and the initialisation of a texture object to provide immutable textures. After calling the new commands 
`glTexStorage*D` the three commands `glTexImage*D`, `glCopyTexImage*D` and `glCompressedTexImage*D` can't be called 
anymore on this texture object without generating an invalid operation error. Immutable texture objects are initialized 
with the commands `glTexSubImage*d`, `glCompressedTexSubImage*D` and `glCopyTexSubImage*D` and actually any command or set 
of commands that won't reallocate the memory for this object.

The purpose of immutable texture objects is mainly on the drivers side to avoid continuous complex completeness checking. 
Hence, it should provide some performance improvement without really affecting the user programmability. If a user really 
needs to change the format, the target or the size of a texture, he can always delete and create a new texture object 
which is pretty much what is happening anyway on mutable texture object. 

Bonus of this extension: it provides some good interactions with [`EXT_direct_state_access`](https://www.opengl.org/registry/specs/EXT/direct_state_access.txt). 
However, this extension doesn't provide any interaction with multisample textures so that it's not possible to create 
immutable multisample texture objects. This type of texture doesn't really have completeness checking as it doesn't hold 
mipmaps so the functionality itself isn't needed and we could only enjoy such command for consistency.
```java
// OpenGL 4.1, mutable texture creation
gl4.glGenTextures(1, texture);
gl4.glActiveTexture(GL_TEXTURE0);
gl4.glBindTexture(GL_TEXTURE_2D, texture.get(0);
gl4.glTexParameteri(GL_TEXTURE_2D, GL_*, …);
   for(int level = 0; level < levels; ++level)
gl4.glTexImage2D(GL_TEXTURE_2D, …); // Allocation and initialisation

// OpenGL 4.2, immutable texture creation
gl4.glGenTextures(1, texture);
gl4.glActiveTexture(GL_TEXTURE0);
gl4.glBindTexture(GL_TEXTURE_2D, texture.get(0));
gl4.glTexParameteri(GL_TEXTURE_2D, GL_*, …);
gl4.glTexStorage2D(GL_TEXTURE_2D, ...); // Allocation
   for(int level = 0; level < levels; ++level)
glTexSubImage*D(GL_TEXTURE_2D, ...); // Initialisation

// OpenGL 4.2 + EXT_direct_state_access, immutable texture creation
gl4.glGenTextures(1, texture);
gl4.glTextureParameteriEXT(GL_TEXTURE_2D, GL_*, ... );
gl4.glTextureStorage2DEXT(GL_TEXTURE_2D, ...); // Allocation
   for(int level = 0; level < levels; ++level)
gl4.glTextureSubImage*DEXT(GL_TEXTURE_2D, ...); // Initialisation
```
* `glTexStorage3D` to allocate
* `glTexSubImage3D` to initialize

### [gl-420-transform-instanced](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_420/Gl_420_transform_instanced.java) :

* transforms `vec4 position` in `vec4 position` and `vec4 color` and performs an instanced rendering on the results
* `glDrawTransformFeedbackStreamInstanced`
* `GL_TRIANGLE_STRIP`
* OpenGL 4.0 brought a lot of improvement for transform feedback including a transform feedback object, transform feedback 
streams, the capability to pause the transform feedback or to draw directly without querying the number of primitives 
recorded in the buffer. 

However an interaction with an essential extension has been forgotten on the way: [`ARB_draw_instanced`](https://www.opengl.org/registry/specs/ARB/draw_instanced.txt) 
which is part of OpenGL 3.2 core specification. It implies that with OpenGL 4.1, it isn’t possible to draw instances from transform feedback buffers without querying the number of primitives written in these buffers. A penalty because of the CPU - GPU synchronisation.

Fortunately, OpenGL 4.2 and [`ARB_transform_feedback_instanced`](https://www.opengl.org/registry/specs/ARB/transform_feedback_instanced.txt) 
fixed this issue by providing the following new commands:

`glDrawTransformFeedbackInstanced`

`glDrawTransformFeedbackStreamInstanced`

Including these new commands, I don’t think it’s enough features for transform feedback but it might be all OpenGL 4 
hardware got.

