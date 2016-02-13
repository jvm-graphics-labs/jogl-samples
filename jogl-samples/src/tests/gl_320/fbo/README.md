# OpenGL 3.2 Fbo Highlights

### [gl-320-fbo](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/fbo/Gl_320_fbo.java) :

* set up a framebuffer object, render to it and then blit to the default framebuffer

### [gl-320-fbo-blend](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/fbo/Gl_320_fbo_blend.java) :

* same with blending

### [gl-320-fbo-blend-points](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/fbo/Gl_320_fbo_blend_points.java) :

* same with points blending

When I set compatibility profile I remember I had to `glEnable(GL_POINT_SPRITE)` to get it working, but now that I use core profile that call is not legal, because otherwise it will fire the following exception

```
GlDebugOutput.messageSent(): GLDebugEvent[ id 0x500
	type Error
	severity High: dangerous undefined behavior
	source GL API
	msg GL_INVALID_ENUM error generated. Cannot enable <cap> in the current profile.
	when 1454941896197
	source 4.5 (Core profile, arb, debug, compat[ES2, ES3, ES31, ES32], FBO, hardware) - 4.5.0 NVIDIA 361.43 - hash 0x1edbaa29]
```
So disable it in core, use it only in compatibility

### [gl-320-fbo-blit](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/fbo/Gl_320_fbo_blit.java) :

* fbo blitting, `glBlitFramebuffer`

### [gl-320-fbo-depth](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/fbo/Gl_320_fbo_depth.java) :

* fbo with only depth attachment

### [gl-320-fbo-depth_multisample](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/fbo/Gl_320_fbo_depth_multisample.java) :

* fbo with only multisample depth attachment

### [gl-320-fbo-depth_stencil](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/fbo/Gl_320_fbo_depth_stencil.java) :

* fbo with color and depth-stencil attachment

### [gl-320-fbo-integer](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/fbo/Gl_320_fbo_integer.java) :

* fbo with `GL_RGBA8UI`-`GL_RGBA_INTEGER` renderbuffer color attachment

### [gl-320-fbo-integer-blit](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/fbo/Gl_320_fbo_integer_blit.java) :

* blitting between integer fbos

### [gl-320-fbo-layered](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/fbo/Gl_320_fbo_layered.java) :

* [`GL_ARB_geometry_shader4`](https://www.opengl.org/registry/specs/ARB/geometry_shader4.txt) is now part of OpenGL 3.2 core
* Layered rendering to an fbo with a 3d texture based on 4 layers. The output layer is controlled by the geometry shader via `gl_Layer`

### [gl-320-fbo-multisample](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/fbo/Gl_320_fbo_multisample.java) :

* [`ARB_texture_multisample`](https://www.opengl.org/registry/specs/ARB/texture_multisample.txt) to access to sample values
* render to multisample fbo, blit to another fbo and render this content to screen

### [gl-320-fbo-multisample-explicit](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/fbo/Gl_320_fbo_multisample_explicit.java) :

* render to 4-multisample (color & depth) fbo and resolve to screen by using just the first sample for the right half part of the screen and the average of all 4 samples for the left half part.

### [gl-320-fbo-multisample-integer](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/fbo/Gl_320_fbo_multisample_integer.java) :

* render to a `GL_MAX_INTEGER_SAMPLES`-multisample integer color fbo, blit to an integer fbo and then resolve to screen

### [gl-320-fbo-rtt](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/fbo/Gl_320_fbo_rtt.java) :

* attach three textures to an fbo and render a different color component (r,g,b) to each of them. Then loop rendering each of them to a corner on screen

### [gl-320-fbo-srgb](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/fbo/Gl_320_fbo_srgb.java) :

* srgb fbo encoding for proper gamma correction

### [gl-320-fbo-srgb-blend](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/fbo/Gl_320_fbo_srgb_blend.java) :

* similar but with blending

### [gl-320-fbo-srgb-decode-ext](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/fbo/Gl_320_fbo_srgb_decode_ext.java) :

* the automatic conversion from sRGB to linear space color in texture has been disabled, `gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SRGB_DECODE_EXT, GL_SKIP_DECODE_EXT);`, therefore the shader will fetch and store sRGB values and we do not need any `GL_FRAMEBUFFER_SRGB` enabling or disabling

