# Highlights

### [gl-320-fbo](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/fbo/Gl_320_fbo.java) :

* set up a framebuffer object, render to it and then blit to the default framebuffer

### [gl-320-fbo_blend](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/fbo/Gl_320_fbo_blend.java) :

* same with blending

### [gl-320-fbo_blend_points](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/fbo/Gl_320_fbo_blend_points.java) :

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

### [gl-320-fbo_blit](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/fbo/Gl_320_fbo_blit.java) :

* fbo blitting, `glBlitFramebuffer`

### [gl-320-fbo_depth](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/fbo/Gl_320_fbo_depth.java) :

* fbo with only depth attachment

### [gl-320-fbo_depth_multisample](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/fbo/Gl_320_fbo_depth_multisample.java) :

* fbo with only multisample depth attachment

### [gl-320-fbo_depth_stencil](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/fbo/Gl_320_fbo_depth_stencil.java) :

* fbo with color and depth-stencil attachment

### [gl-320-fbo_integer](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/fbo/Gl_320_fbo_integer.java) :

* fbo with `GL_RGBA8UI`-`GL_RGBA_INTEGER` renderbuffer color attachment

### [gl-320-fbo_integer_blit](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/fbo/Gl_320_fbo_integer_blit.java) :

* blitting between integer fbos

### [gl-320-fbo_layered](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/fbo/Gl_320_fbo_layered.java) :

* Layered rendering to an fbo with a 3d texture based on 4 layers. The output layer is controlled by the geometry shader via `gl_Layer`

### [gl-320-fbo_multisample](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/fbo/Gl_320_fbo_multisample.java) :

* render to multisample fbo, blit to another fbo and render this content to screen

### [gl-320-fbo_multisample_explicit](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/fbo/Gl_320_fbo_multisample_explicit.java) :

* render to 4-multisample (color & depth) fbo and resolve to screen by using just the first sample for the right half part of the screen and the average of all 4 samples for the left half part.

### [gl-320-fbo_multisample_integer](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/fbo/Gl_320_fbo_multisample_integer.java) :

* render to a `GL_MAX_INTEGER_SAMPLES`-multisample integer color fbo, blit to an integer fbo and then resolve to screen

### [gl-320-fbo_rtt](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/fbo/Gl_320_fbo_rtt.java) :

* attach three textures to an fbo and render a different color component (r,g,b) to each of them. Then loop rendering each of them to a corner on screen

