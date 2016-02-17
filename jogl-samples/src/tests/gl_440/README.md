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
* [`GL_ARB_vertex_type_10f_11f_11f_rev`](https://www.opengl.org/registry/specs/ARB/vertex_type_10f_11f_11f_rev.txt) 

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
* "Clearing a texture with OpenGL 4.3 is the most cumbersome thing to do. We need to create an FBO with this texture
attached and clear it as a framebuffer attachment. Ah! Not only it costs a lot of CPU overhead to create that FBO but this
affects the rendering pipeline as we need to bind this FBO to clear it.

Fortunately OpenGL 4.4 [`GL_ARB_clear_texture`](https://www.opengl.org/registry/specs/ARB/clear_texture.txt) introduces 
`glClearTexImage` to clear a texture image and `glClearTexSubimage` to clear a part of this texture image. This feature 
provides a nice interaction with sparse textures and image load and store."

### [gl-440-interface-matching](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_440/Gl_440_interface_matching.java):

* interface matching
* "I have discussed many times about the issues with the GLSL shader interface and even ended up writing an article for
_OpenGL Insight_ about this topic. Since then, many things have changed and GLSL 4.40 shader interface are a lot more 
robust than what they used to be in GLSL 1.50. Unfortunately, these improvements come at the cost of complexity. In the 
following section we will cover the features provided by [`ARB_enhanced_layouts`](https://www.opengl.org/registry/specs/ARB/enhanced_layouts.txt)
to OpenGL 4.4 core specifications."
You should now match interfaces by location rather than by name to avoid any trouble for the compiler if we don't link.
Example of a shader output interface matching by name:
```glsl
layout out vec4 A;
layout out vec3 B;
layout out float C;
Example of a shader input interface matching by name:
```glsl
layout out vec4 A;
layout out float C;
```
Example of a shader output interface matching by location:
```glsl
layout (location = 0) out vec4 A;
layout (location = 1) out vec3 B;
layout (location = 2) out float C;
```
Example of a shader input interface matching by location:
```glsl
layout (location = 0) in vec4 M;
layout (location = 2) in float N;
```
* Same for blocks, example of a shader output interface matching by block name:
```glsl
out blockA // This is called the block name
{
   vec4 A;
   vec3 B;
} outA; // This is called the instance name

out blockB
{
   vec2 A;
   vec2 B;
} outB;
```
Example of a shader input interface matching by block name:
```glsl
in blockB
{
   vec2 A;
   vec2 B;
} inB;
```
If we don't link the interface won't match.
Example of a shader output interface matching by location:
```glsl
layout (location = 0) out blockA
{
   vec4 A;
   vec3 B;
} outA; 

layout (location = 2) out blockB
{
   vec2 A;
   vec2 B;
} outB;
```
Example of a shader input interface matching by location:
```glsl

layout (location = 2) in blockB
{
   vec2 A;
   vec2 B;
} inB;
```
" I used 0 and 2 as the locations fro the two blocks. Why not using 0 and 1? In that case outB.A and OutA.B would use the 
same location for these two variables.

A location is not an index but an abstract representation of the memory layout. Basically, all scalar and vector types
consume 1 location except `dvec3` and `dvec4` that may consume 2 locations. However, it doesn't mean that the OpenGL 
implementations necessarily consume 4 components when we use a float variable for example. Locations provide a way to 
locate where variables are stored but giving enough freedom to the compiler to pack the inputs and the outputs the way it
wants."

* Moreover, struct member can't have location:
```glsl
layout(location = 3) in struct S {
    vec3 a;                       // gets location 3
    mat2 b;                       // gets locations 4 and 5
    vec4 c[2];                    // gets locations 6 and 7
    layout (location = 8) vec2 A; // ERROR, can't use on struct member
} s;
```
* you need to take care if you explicitely want to assign a specific location to a block member:
```glsl
layout(location = 4) in block {
    vec4 d;                       // gets location 4
    vec4 e;                       // gets location 5
    layout(location = 7) vec4 f;  // gets location 7
    vec4 g;                       // gets location 8
    layout (location = 1) vec4 h; // gets location 1
    vec4 i;                       // gets location 2
    vec4 j;                       // gets location 3
    vec4 k;                       // ERROR, location 4 already used
};
```

* `ARB_enhanced_layouts` gives a finer granularity control of how the components are consumed by the locations. Along with 
the `location` qualifier we have a new `component` layout qualifier to assign for each component of a single location
multiple variables. "
Think about `component` as a kind of _component offset_ where the variable starts from, components count is 4, you cannot
exceed that value.
The specifications give a good code sample to understand the feature:
```glsl
// a consumes components 2 and 3 of location 4
layout(location = 4, component = 2) in vec2 a;
  
// b consumes component 1 of location 4
layout(location = 4, component = 1) in float b; 

// ERROR: c overflows components 2 and 3
layout(location = 3, component = 2) in vec3 c;
```
* If the variable is an array, each element of the array, in order, is assigned to consecutive locations, but all 
at the same specified component within each location.  For example:
```glsl
// component 3 in 6 locations are consumed
layout(location = 2, component = 3) in float d[6]; 
```
That is, location 2 component 3 will hold `d[0]`, location 3 component 3 will hold `d[1]`, ..., up through location 7 
component 3 holding `d[5]`.

* This allows packing of two arrays into the same set of locations:
```glsl
// e consumes beginning (components 0, 1 and 2) of each of 6 slots
layout(location = 0, component = 0) in vec3 e[6];  

// f consumes last component of the same 6 slots            
layout(location = 0, component = 3) in float f[6]; 
```
* Two new qualifiers have been added to uniform and storage blocks: `offset`and `align`. These qualifiers give a control 
per variable on how they map the memory:
```glsl
uniform block
{
   layout (offset = 0) vec4 kueken;
   layout (offset = 64) vec4 ovtsa;
};
```
The value passed to the `offset` qualifier is expressed in bytes and points to the memory used to store the variable in
memory. If the compiler removes a variable between `kueken` and `ovtsa` because it is not an active variable, thanks to the
`offset` qualifier on `ovtsa` we can guarantee that the variable will back the right memory.

The `align` qualifier provides guarantees that the variables will use the correct padding of the memory:
```glsl
uniform block
{
   vec4 kueken;
   layout (offset = 44, align = 8) vec4 ovtsa; // Effectively store at offset 48
};
```

* Constant expressions. Let's say that we want to do a partial interface matching on the `outB` block:
```glsl
out block
{
   A a; // A is a structure
   B b; // B is a structure
   C c; // C is a structure
} outB;
```
If we remove `B` in the corresponding input block, the shader interface won't match when using separate programs so we need
to use the layout `location` qualifier:
```glsl
out block
{
   layout (lofation = 0) A a; // A is a structure
   layout (lofation = LOCATION_OF_A) B b; // B is a structure
   layout (lofation = LOCATION_OF_A + LOCATION_OF_B) C c; // C is a structure
} outB;

// In the subsequent shader stage:
in block
{
   layout (location = 0) A a; 
   layout (location = LOCATION_OF_A + LOCATION_OF_B) C c;
} inB;
```

* More in the [GLSL 4.4 specifications](https://www.opengl.org/registry/doc/GLSLangSpec.4.40.pdf).

### [gl-440-multi-draw-indirect-id](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_440/Gl_440_multi_draw_indirect_id.java): broken

* "A common misconception in rendering is that draw calls are expensive. They are not. What's expensive is switching 
resources between draw calls as that introduces a massive CPU overhead while the draw call itself is just the GPU command
processor launching a job. Hence, we need to avoid switching resources relying on batching and on shader based dynamically
uniform resource indexing. I have largely cover this topic in my GPU Pro 4 chapter titled ["Introducing the programmable
pulling rendering pipeline"](http://nedrilad.com/Tutorial/topic-58/GPU-Pro-Advanced-Rendering-Techniques-35.html). For this
purpose, OpenGL introduces two new extentions: [`ARB_shader_draw_parameters`](https://www.opengl.org/registry/specs/ARB/shader_draw_parameters.txt)
and [`ARB_indirect_parameters`](https://www.opengl.org/registry/specs/ARB/indirect_parameters.txt).

### [gl-440-query-occlusion](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_440/Gl_440_query_occlusion.java): broken (bug)

* http://stackoverflow.com/questions/35451405/glgetqueryobjectuiv-bound-query-buffer-is-not-large-enough-to-store-result
* https://jogamp.org/bugzilla/show_bug.cgi?id=1291
* "Looking at my OpenGL Pipeline Map (in the review), we see that GPUs do a lot of things using fixed function hardware.
Having both programmable and fixed functions functionalities invites us at considering the interoperability between both.
This is what [`ARB_query_buffer_object`](https://www.opengl.org/registry/specs/ARB/query_buffer_object.txt) does by 
capturing results into a buffer object that we can directly access within shaders without CPU round trip.
Usage sample of query buffers:
```java
//    
-­‐-­‐-­‐
Application    side    
-­‐-­‐-­‐
// Create a buffer object for the query result
gl4.glGenBuffers(1, queryBuffer);
gl4.glBindBuffer(GL_QUERY_BUFFER_AMD, queryBuffer.get(0));
gl4.glBufferData(GL_QUERY_BUFFER_AMD, Integer.BYTES, null, GL_DYNAMIC_COPY);
// Perform occlusion query
gl4.glBeginQuery(GL_SAMPLES_PASSED, queryId.get(0))
...
gl4.glEndQuery(GL_SAMPLES_PASSED);
// Get query results to buffer object
gl4.glBindBuffer(GL_QUERY_BUFFER_AMD, queryBuffer.get(0));
gl4.glGetQueryObjectuiv(queryId, GL_QUERY_RESULT, 0);
// Bind query result buffer as uniform buffer
gl4.glBindBufferBase(GL_UNIFORM_BUFFER, 0, queryBuffer.get(0));
...
//    
-­‐-­‐-­‐
Shader    
-­‐-­‐-­‐
...
uniform queryResult
{
uint samplesPassed;
}
void main()
{
   ...
   if(samplesPassed > threshold)    {
      //    Complex    processing
      ...
   }    
   else
   {
      //    Simplified    processing
      ...
   }
}
```
Furthermore, with query buffers, we can request the result of many queries at the same time by mapping the buffer instead 
of submitting one OpenGL command per query to get each result.


### [gl-440-sampler-wrap](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_440/Gl_440_sampler_wrap.java)

* "Some features seem to live forgotten by all until someone realized: "Hey, we all support it now, let's put it in core".
This is what happens to [`ARB_texture_mirror_clamp_to_edge`](https://www.opengl.org/registry/specs/ARB/texture_mirror_clamp_to_edge.txt)
that has been promoted from [`EXT_texture_mirror_clamp`](https://www.opengl.org/registry/specs/EXT/texture_mirror_clamp.txt),
itself promoted from [`ATI_texture_mirror_once`](https://www.opengl.org/registry/specs/ATI/texture_mirror_once.txt) and
stripped down to one feature, a new wrap mode called `GL_MIRROR_CLAMP_TO_EDGE`. It looks like that `GL_MIRROR_CLAMP_TO_BORDER`
has been removed because every vendor doesn't support it. A quick look at [glCapsViewer database](http://delphigl.de/glcapsviewer/gl_listreports.php)
shows that AMD and NVIDIA support `EXT_texture_mirror_clamp` but not Intel.

Ultimately, the GPU texture units will remain fixed function at least for the 10 years to come. This is because a texture 
unit gathers many texels but only outputs a single filtered sample. Sending all these texels to the shader units would cost
too much transistors just to convoy them and it consumes a lot more local registers. Using the `fetchTexel` instruction it
is possible to program even an anisotropic sampling but this is magnitude slower according to my experience. "
* "As mentioned, what's expensive is not submitting draw calls but switching resources. With [`ARB_multi_bind`](https://www.opengl.org/registry/specs/ARB/multi_bind.txt)
we can bind all the textures, all the buffers, all the texture images, all the samplers in one call, reducing the CPU 
overhead. I like the API because we no longer need the target parameter or selectors to bind a texture:
```java
// Binding textures with OpenGL 4.3
gl4.glActiveTexture(GL_TEXTURE0);
gl4.glBindTexture(GL_TEXTURE_2D, textureNames.get(0));
gl4.glActiveTexture(GL_TEXTURE1);
gl4.glBindTexture(GL_TEXTURE_2D, textureNames.get(1));
// Binding textures with OpenGL 4.4
gl4.glBindTextures(0, 2, textureNames);
```
Furthermore, derived texture states are also use to bind texture images. Maybe one limitation of this extension is that we
must bind consecutive units.
* [`GL_ARB_texture_mirror_clamp_to_edge`](https://www.opengl.org/registry/specs/ARB/texture_mirror_clamp_to_edge.txt)
* `glBindSamplers(int first, int count, samplers)`
* `glBindTextures(int first, int count, textures)`

### [gl-440-transform-feedback](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_440/Gl_440_transform_feedback.java)

* * With OpenGL 4.3 every single shader interface could reference its resource unit in the shader... every interface but 
one! The one used for transform feedback. This is done using new ugly namespaced qualifier. Transform feedback setup in a 
geometry shader:
```glsl
layout (xfb_buffer = 0, xfb_offset = 0) out vec3 var1;
layout (xfb_buffer = 0, xfb_offset = 24) out vec3 var1;
layout (xfb_buffer = 1, xfb_offset = 0) out vec3 var1;
layout (xfb_buffer = 1, xfb_offset = 32) out vec3 var1;
```
The process is actually simpler that the old API approach. We just need to bind our buffers to transform feedbacks unit and
then set at which offset we will store the variable data. The stride qualifier is used to specify at which offset to output
the next vertex.
* transforms a `vec4 position` into `vec4 position` and `vec4 color`
* set the interface in the following way:
```glsl
layout(xfb_buffer = 0, xfb_stride = 32) out;

out Block
{
    layout(xfb_buffer = 0, xfb_offset = 16) vec4 color;
} outBlock;

out gl_PerVertex
{
    layout(xfb_buffer = 0, xfb_offset = 0) vec4 gl_Position;
};
```