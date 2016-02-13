# OpenGL 3.3 Highlights ([g-truc review](http://www.g-truc.net/doc/OpenGL%203.3%20review.pdf))

### [gl-330-blend-index](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_330/Gl_330_blend_index.java) :

* [dual source blending](https://www.opengl.org/wiki/Blending#Dual_Source_Blending)
* load a diffuse texture and enables blending in order to get the output of the fragment shader on the index 1

### [gl-330-blend-rtt](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_330/Gl_330_blend_rtt.java) :

* ?
* OpenGL texture objects is a conceptual non sens which blend data and operations on a single object. It 
results in a lot of limitations that various extensions try to remove. [`GL_ARB_sampler_objects`](https://www.opengl.org/registry/specs/ARB/sampler_objects.txt) 
allows sampling a single image with multiple different filters and sampling multiple images with the same 
filter. This could be a huge benefice both in texture memory (no data copy) and texture filtering processing 
thanks to an adaptive filtering per-fragment. Why using an amazing filtering method when the fragment is in 
a blurred part of the image? This extension still rely on the "texture unit" semantic.

### [gl-330-buffer-type](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_330/Gl_330_buffer_type.java) :

* render four different types of data, `RGB10A2`, `F32`, `I8` and `I32`

### [gl-330-caps](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_330/Gl_330_caps.java) :

* OpenGL 330 capabilities

### [gl-330-draw-instanced-array](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_330/Gl_330_draw_instanced_array.java) :

* renders 10 instance of the same geometry, changing the color attribute once every two instance
*  Instanced arrays provides an alternative to the current OpenGL instancing techniques. We can already use 
uniform buffer and texture buffer to store the per instance data, [`GL_ARB_instanced_arrays`](https://www.opengl.org/registry/specs/ARB/instanced_arrays.txt) 
proposed to use array buffers for per instance data. Using the function `glVertexAttribDivisor` for each 
per-instance array buffer, we specify that the draw call must use the first attribute for N vertices where N 
should be the count of instances vertices. This extension allows to draw multiples different objects as well, 
as far as they have the same number of vertices. Using attributes for instance data is likely to avoid the 
latency of a texture buffer fetch but might fill up the attribute data flow if the size of per instance data 
is quite large. Probably the fastest instancing method for small per instance data rendering and huge number 
of instance.
* `glVertexAttribDivisor(Semantic.Attr.POSITION, 0)` this means disabled (==0)
* `glVertexAttribDivisor(Semantic.Attr.COLOR, 2)` this means enabled (!=0)
* `glDrawArraysInstanced`

### [gl-330-fbo-multisample-explicit-nv](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_330/Gl_330_fbo_multisample_explicit_nv.java) :

* renders a texture to an fbo with multisampled (4) renderbuffer color and multisampled (4) renderbuffer depth and then resolve it on the screen. The left part of the screen renders averaging among samples, the right one fetch only the first sample.
* `GL_NV_explicit_multisample`
* `GL_TEXTURE_RENDERBUFFER_NV`
* `textureSizeRenderbuffer`
* `texelFetchRenderbuffer`

### [gl-330-query-conditional](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_330/Gl_330_query_conditional.java) :

* conditional rendering
* [`GL_ARB_occlusion_query2`](https://www.opengl.org/registry/specs/ARB/occlusion_query2.txt)
* `GL_ANY_SAMPLES_PASSED`
* `glBeginConditionalRender` - `glEndConditionalRender`

### [gl-330-query-counter](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_330/Gl_330_query_counter.java) :

* query the time at the begin and at the end of the rendering
* `glQueryCounter`
* `GL_TIMESTAMP`
* `GL_QUERY_RESULT_AVAILABLE`

### [gl-330-query-occlusion](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_330/Gl_330_query_occlusion.java) :

* queries if any samples has passed, expected `GL_FALSE` or `GL_TRUE`
* `GL_ANY_SAMPLES_PASSE`

### [gl-330-query-time](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_330/Gl_330_query_time.java) :

* [`GL_ARB_timer_query`](https://www.opengl.org/registry/specs/ARB/timer_query.txt) use the query object to 
request the time in nanosecond spend by OpenGL calls without stalling the graphics pipeline. A great 
extension for optimizations and maybe for dynamically adjusting the quality level to keep a good enough 
frame rate. 
* queries the elapsed time
* `GL_TIME_ELAPSED`

### [gl-330-sampler-anisotropy-ext](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_330/Gl_330_sampler_anysotropy_ext.java) :

* anisotropy filtering at 1, 2, 4 and 16
* `GL_TEXTURE_MAX_ANISOTROPY_EXT`

### [gl-330-sampler-filter](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_330/Gl_330_sampler_filter.java) :

* different sampler filters
* `GL_NEAREST`
* `GL_LINEAR`
* `GL_LINEAR_MIPMAP_NEAREST`
* `GL_LINEAR_MIPMAP_LINEAR`

### [gl-330-sampler-object](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_330/Gl_330_sampler_object.java) :

* renders the upper left corner of a texture with `GL_TEXTURE_MIN_FILTER`/`GL_TEXTURE_MAG_FILTER` to `GL_NEAREST_MIPMAP_NEAREST`/`GL_NEAREST` and the lower right with `GL_LINEAR_MIPMAP_LINEAR`/`GL_LINEAR`

### [gl-330-sampler-wrap](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_330/Gl_330_sampler_wrap.java) :

* renders a texture with four different wrap s/t
* `GL_MIRRORED_REPEAT`
* `GL_CLAMP_TO_BORDER`
* `GL_REPEAT`
* `GL_CLAMP_TO_EDGE`

### [gl-330-texture-integer-rgb10a2ui](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_330/Gl_330_texture_integer_rgb10a2ui.java) : not working

* Usually, for normals and tangents attributes we use floating point data. It is a lot of precision but a 
big cost in memory bandwidth. [`GL_ARB_vertex_type_2_10_10_10_rev`](https://www.opengl.org/registry/specs/ARB/vertex_type_2_10_10_10_rev.txt)
provide RGB10A2 format for vertex attribute. The bandwidth is reduced by 2 or 3 times and it keep a really 
good precision, actually higher than most normal maps. [OpenGL supports RGB10A2 textures](http://www.opengl.org/sdk/docs/man/xhtml/glTexImage2D.xml)
 since OpenGL 1.1 but [`GL_ARB_texture_rgb10_a2ui`](http://www.opengl.org/registry/specs/ARB/texture_rgb10_a2ui.txt) 
allows an unnormalized access to the texture data just like [`GL_EXT_texture_integer`](http://www.opengl.org/registry/specs/EXT/texture_integer.txt) 
allowed it for common interger textures in [OpenGL 3.0](http://www.opengl.org/registry/doc/glspec30.20080923.pdf)


### [gl-330-texture-rect](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_330/Gl_330_texture_rect.java) :

* [`GL_ARB_texture_rectangle`](https://www.opengl.org/registry/specs/ARB/texture_rectangle.txt)
* `GL_TEXTURE_RECTANGLE`
* pixel texture coordinates

### [gl-330-texture-swizzle](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_330/Gl_330_texture_swizzle.java) :

* texture swizzling
* `GL_TEXTURE_SWIZZLE_R`
* `GL_TEXTURE_SWIZZLE_G`
* `GL_TEXTURE_SWIZZLE_B`
* `GL_TEXTURE_SWIZZLE_A`


