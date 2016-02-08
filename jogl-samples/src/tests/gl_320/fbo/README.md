# Highlights

### [gl-320-fbo](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/fbo/Gl_320_fbo.java) :

* set up a framebuffer object, render to it and then blit to the default framebuffer

### [gl-320-fbo_blend](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/fbo/Gl_320_fbo_blend.java) :

* same with blending

### [gl-320-fbo_blend_points](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/fbo/Gl_320_fbo_blend_points.java) :

* same with points blending

I remember I had to `glEnable(GL_POINT_SPRITE)` to get it working, but if I do it now I get 

```
GlDebugOutput.messageSent(): GLDebugEvent[ id 0x500
	type Error
	severity High: dangerous undefined behavior
	source GL API
	msg GL_INVALID_ENUM error generated. Cannot enable <cap> in the current profile.
	when 1454941896197
	source 4.5 (Core profile, arb, debug, compat[ES2, ES3, ES31, ES32], FBO, hardware) - 4.5.0 NVIDIA 361.43 - hash 0x1edbaa29]
```
So disabled

### [gl-320-fbo_blit](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/fbo/Gl_320_fbo_blit.java) :

* fbo blitting, `glBlitFramebuffer`
