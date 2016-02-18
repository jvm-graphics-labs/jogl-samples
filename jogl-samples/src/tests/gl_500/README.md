# OpenGL 5 Highlights

### [gl-500-blend-op-amd](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_500/Gl_500_blend_op_amd.java): todo

* [`GL_AMD_blend_minmax_factor`](https://www.opengl.org/registry/specs/AMD/blend_minmax_factor.txt)

### [gl-500-buffer-pinned-amd](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_500/Gl_500_buffer_pinned_amd.java): todo

* [`GL_AMD_pinned_memory`](https://www.opengl.org/registry/specs/AMD/pinned_memory.txt)

### [gl-500-buffer-sparse-arb](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_500/Gl_500_buffer_sparse_arb.java): todo

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

### [gl-500-fbo-layered-amd](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_500/Gl_500_fbo_layered_amd.java): todo

* `GL_AMD_vertex_shader_viewport_index`
* `GL_AMD_vertex_shader_layer`

### [gl-500-fbo-layered-nv](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_500/Gl_500_fbo_layered_nv.java): todo

* `GL_NV_viewport_array2`

### [gl-500-fbo-multisample-position-amd](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_500/Gl_500_multisample_position_amd.java): todo

* `GL_AMD_sample_positions`

### [gl-500-fill-rectangle-nv](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_500/Gl_500_fill_rectangle_nv.java): todo, missing ext

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

### [gl-500-multi-draw-indirect-arb](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_500/Gl_500_multi_draw_indirect_arb.java): toFix

* 

### [gl-500-multi-draw-indirect-count-arb](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_500/Gl_500_multi_draw_indirect_count_arb.java): jogl bug

* [jogl bug 1288](https://jogamp.org/bugzilla/show_bug.cgi?id=1288)

### [gl-500-primitive-blindless-nv](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_500/Gl_500_primitive_blindless_nv.java): jogl bug

* `GL_NV_shader_buffer_load`
* `GL_NV_vertex_buffer_unified_memory`