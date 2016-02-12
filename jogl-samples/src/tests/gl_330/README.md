# OpenGL 3.3 Highlights ([g-truc review](http://www.g-truc.net/doc/OpenGL%203.3%20review.pdf))

### [gl-330-blend-index](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_330/Gl_330_blend_index.java) :

* [dual source blending](https://www.opengl.org/wiki/Blending#Dual_Source_Blending)
* load a diffuse texture and enables blending in order to get the output of the fragment shader on the index 1

### [gl-330-blend-rtt](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_330/Gl_330_blend_rtt.java) :

* ?

### [gl-330-buffer-type](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_330/Gl_330_buffer_type.java) :

* render four different types of data, `RGB10A2`, `F32`, `I8` and `I32`

### [gl-330-caps](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_330/Gl_330_caps.java) :

* OpenGL 330 capabilities

### [gl-330-draw-instanced-array](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_330/Gl_330_draw_instanced_array.java) :

* renders 10 instance of the same geometry, changing the color attribute once every two instance
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

### [gl-330-texture-rect](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_330/Gl_330_texture_rect.java) :

* `GL_TEXTURE_RECTANGLE`
* pixel texture coordinates

### [gl-330-texture-swizzle](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_330/Gl_330_texture_swizzle.java) :

* texture swizzling
* `GL_TEXTURE_SWIZZLE_R`
* `GL_TEXTURE_SWIZZLE_G`
* `GL_TEXTURE_SWIZZLE_B`
* `GL_TEXTURE_SWIZZLE_A`


