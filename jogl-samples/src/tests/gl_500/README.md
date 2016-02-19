# OpenGL 5 Highlights

### [gl-500-blend-op-amd](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_500/Gl_500_blend_op_amd.java): todo I don't have ext

* [`GL_AMD_blend_minmax_factor`](https://www.opengl.org/registry/specs/AMD/blend_minmax_factor.txt)

### [gl-500-buffer-pinned-amd](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_500/Gl_500_buffer_pinned_amd.java): todo I don't have ext

* [`GL_AMD_pinned_memory`](https://www.opengl.org/registry/specs/AMD/pinned_memory.txt)

### [gl-500-buffer-sparse-arb](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_500/Gl_500_buffer_sparse_arb.java): todo I don't have ext

* The ARB_sparse_texture extension adds to GL a mechanism to decouple the virtual and physical storage requirements of 
textures and allows an application to create partially populated textures that would over-subscribe available graphics memory 
if made fully resident. [`GL_ARB_sparse_buffer`](https://www.opengl.org/registry/specs/ARB/sparse_buffer.txt) extension provides 
like functionality for buffer objects, allowing applications to manage buffer object storage in a similar manner.
* It creates a `COPY` SSBO and loads in it vertex and index data. Then it creates two sparse SSBOs, one for vertices and 
another one for indices and transfer the content of `COPY` accordingly. Finally it renders mapping the sparse buffer.

### [gl-500-conservative-raster-nv](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_500/Gl_500_conservative_raster_nv.java): check, I don't have ext

* [`NV_conservative_raster`](https://www.opengl.org/registry/specs/NV/conservative_raster.txt) adds a "conservative" 
rasterization mode where any pixel that is partially covered, even if no sample location is covered, is treated as fully 
covered and a corresponding fragment will be shaded.    

A new control is also added to modify window coordinate snapping precision.

These controls can be used to implement "binning" to a low-resolution render target, for example to determine which tiles of a 
sparse texture need to be populated. An app can construct a framebuffer where there is one pixel per tile in the sparse 
texture, and adjust the number of subpixel bits such that snapping occurs to the same effective grid as when rendering to the 
sparse texture. Then triangles should cover (at least) the same pixels in the low-res framebuffer as they do tiles in the 
sparse texture.

* `SubpixelPrecisionBiasNV`
* `SUBPIXEL_PRECISION_BIAS_X_BITS_NV`
* `SUBPIXEL_PRECISION_BIAS_Y_BITS_NV`
* `MAX_SUBPIXEL_PRECISION_BIAS_BITS_NV`

### [gl-500-direct-state-access-gtc](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_500/Gl_500_direct_state_access_gtc.java):

* Direct State Access

### [gl-500-fbo-layered-amd](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_500/Gl_500_fbo_layered_amd.java): todo I don't have ext

* `GL_AMD_vertex_shader_viewport_index`
* `GL_AMD_vertex_shader_layer`

### [gl-500-fbo-layered-nv](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_500/Gl_500_fbo_layered_nv.java): todo I don't have ext

* `GL_NV_viewport_array2`

### [gl-500-fbo-multisample-position-amd](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_500/Gl_500_multisample_position_amd.java): todo I don't have ext

* `GL_AMD_sample_positions`

### [gl-500-fill-rectangle-nv](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_500/Gl_500_fill_rectangle_nv.java): todo, I don't have ext

* `GL_NV_fill_rectangle`

### [gl-500-glsl-vote-arb](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_500/Gl_500_glsl_vote_arb.java): 

* [`ARB_shader_group_vote`](https://www.opengl.org/registry/specs/ARB/shader_group_vote.txt) provides new built-in 
functions to compute the composite of a set of boolean conditions across a group of shader invocations. These composite 
results may be used to execute shaders more efficiently on a single-instruction multiple-data (SIMD) processor. The set of 
shader invocations across which boolean conditions are evaluated is implementation-dependent, and this extension provides 
no guarantee over how individual shader invocations are assigned to such sets. In particular, the set of shader invocations 
has no necessary relationship with the compute shader local work group -- a pair of shader invocations in a single compute 
shader work group may end up in different sets used by these built-ins.

Compute shaders operate on an explicitly specified group of threads (a local work group), but many implementations of 
OpenGL 4.3 will even group non-compute shader invocations and execute them in a SIMD fashion. When executing code like:
```glsl
      if (condition) {
        result = do_fast_path();
      } else {
        result = do_general_path();
      }
```
where `condition` diverges between invocations, a SIMD implementation might first call `do_fast_path()` for the invocations 
where `condition` is true and leave the other invocations dormant. Once `do_fast_path()` returns, it might call 
`do_general_path()` for invocations where `condition` is false and leave the other invocations dormant. In this case, the 
shader executes *both* the fast and the general path and might be better off just using the general path for all 
invocations.

This extension provides the ability to avoid divergent execution by evaluting a condition across an entire SIMD invocation 
group using code like:
```glsl
      if (allInvocationsARB(condition)) {
        result = do_fast_path();
      } else {
        result = do_general_path();
      }
```
The built-in function `allInvocationsARB()` will return the same value for all invocations in the group, so the group will 
either execute `do_fast_path()` or `do_general_path()`, but never both. For example, shader code might want to evaluate a 
complex function iteratively by starting with an approximation of the result and then refining the approximation. Some 
input values may require a small number of iterations to generate an accurate result (`do_fast_path`) while others require 
a larger number (`do_general_path`). In another example, shader code might want to evaluate a complex function 
(`do_general_path`) that can be greatly simplified when assuming a specific value for one of its inputs (`do_fast_path`).
* loads a diffuse texture and if all the fragment shader invocations of the same group satisfies this condition:
```glsl
gl_FragCoord.y / gl_FragCoord.x < 3.0 / 4.0
```
then the level 5 (mipmap), instead of the base one (0), will be choosen.

### [gl-500-multi-draw-indirect-arb](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_500/Gl_500_multi_draw_indirect_arb.java): toFix

* 

### [gl-500-multi-draw-indirect-count-arb](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_500/Gl_500_multi_draw_indirect_count_arb.java): jogl bug

* [jogl bug 1288](https://jogamp.org/bugzilla/show_bug.cgi?id=1288)

### [gl-500-primitive-blindless-nv](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_500/Gl_500_primitive_blindless_nv.java): jogl issue

* `GL_NV_shader_buffer_load`
* `GL_NV_vertex_buffer_unified_memory`
* [heavy performance issues on jogl](https://jogamp.org/bugzilla/show_bug.cgi?id=1167)

### [gl-500-primitive-shading-nv](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_500/Gl_500_primitive_shading_nv.java): toFinish, I dont have ext

* `GL_NV_geometry_shader_passthrough`

### [gl-500-sample-location-grid-nv](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_500/Gl_500_sample_location_grid_nv.java): toFinish, jogl has to implement it yet

* `GL_NV_sample_locations`
* [jogl bug 1292](https://jogamp.org/bugzilla/show_bug.cgi?id=1292)

### [gl-500-sample-location-nv](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_500/Gl_500_sample_location_nv.java): toFinish, jogl has to implement it yet

* same
* `GL_NV_sample_locations`
* `GL_NV_internalformat_sample_query`

### [gl-500-shader-blend-intel](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_500/Gl_500_shader_blend_intel.java): todo, I dont have ext

* `GL_INTEL_fragment_shader_ordering`

### [gl-500-shader-blend-nv](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_500/Gl_500_shader_blend_nv.java): todo, I dont have ext

* `GL_NV_fragment_shader_interlock`

### [gl-500-shader-group-nv](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_500/Gl_500_shader_group_nv.java):

* ? surely magic
* [`NV_shader_thread_group`](https://www.opengl.org/registry/specs/NV/shader_thread_group.txt)
* `in bool gl_HelperInvocation`

### [gl-500-shader-invocation-nv](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_500/Gl_500_shader_invocation_nv.java):

* [`NV_shader_thread_group`](https://www.opengl.org/registry/specs/NV/shader_thread_group.txt)
* renders an imaged computing the fragment color based on the id of the warp and on the SM (Streaming Multiprocessor)
* `gl_WarpIDNV` holds the warp id of the executing thread. This  variable is in the range [0, `gl_WarpsPerSMNV - 1`], 
where `gl_WarpsPerSMNV` is the maximum number of warp executing on a SM.
* `gl_SMIDNV` holds the SM id of the executing thread. This variable is in the range [0, `gl_SMCountNV - 1`], where 
`gl_SMCountNV` is the number of SM on the GPU.

### [gl-500-test-depth-clamp-separate-amd](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_500/Gl_500_test_depth_clamp_separate_amd.java): todo, I don't have ext

*

### [gl-500-texture-bindless-arb](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_500/Gl_500_texture_bindless_arb.java): broken

*

### [gl-500-texture-bindless-nv](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_500/Gl_500_texture_bindless_nv.java): broken

*

### [gl-500-texture-cube-arb](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_500/Gl_500_texture_cube_arb.java): 

* In unextended OpenGL, cube maps are treated as sets of six, independent texture images. Once a face is selected from the 
set, it is treated exactly as any other two-dimensional texture would be. When sampling linearly from the texture, all of 
the individual texels that would be used to to create the final, bilinear sample values are taken from the same cube face. 
The normal, two-dimensional texture coordinate wrapping modes are honored. This sometimes causes seams to appear in cube maps.

`ARB_seamless_cube_map` (and subsequently, OpenGL 3.2) addresses this issue by providing a mechanism whereby an 
implementation could take each of the taps of a bilinear sample from a different face, spanning face boundaries and 
providing seamless filtering from cube map textures. However, in `ARB_seamless_cube_map`, this feature was exposed as a 
global state, affecting all bound cube map textures. It was not possible to mix seamless and per-face cube map sampling 
modes during sampling. Furthermore, if an application included cube maps that were meant to be sampled seamlessly and 
non-seamlessly, it would have to track this state and enable or disable seamless cube map sampling as needed.

[`ARB_seamless_cubemap_per_texture`](https://www.opengl.org/registry/specs/ARB/seamless_cubemap_per_texture.txt) addresses 
this issue and provides an orthogonal method for allowing an implementation to provide a per-texture setting for enabling 
seamless sampling from cube maps.
* `GL_ARB_shader_storage_buffer_object`
* `GL_ARB_buffer_storage`
* `GL_ARB_multi_bind`
* generates a cube texture and two sampler, one seamless one non-seamless. Then it renders the seamless on the left half
side of the screen and the non-seamless one on the left side.

### [gl-500-texture-spars-arb](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_500/Gl_500_texture_spars_arb.java): broken

* 