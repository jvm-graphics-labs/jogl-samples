# Highlights

### [gl-330-blend-index](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_330/Gl_330_blend_index.java) :

* indexed blending

### [gl-330-blend-rtt](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_330/Gl_330_blend_rtt.java) :

* ?

### [gl-330-buffer-type](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_330/Gl_330_buffer_type.java) :

* render four different types of data, `RGB10A2`, `F32`, `I8` and `I32`

### [gl-330-caps](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_330/Gl_330_caps.java) :

* OpenGL 330 capabilities

### [gl-330-draw-instanced-array](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_330/Gl_330_draw_instanced_arrray.java) :

* renders 10 instance of the same geometry, changing the color attribute once every two instance
* `glVertexAttribDivisor(Semantic.Attr.POSITION, 0)` this means disabled (==0)
* `glVertexAttribDivisor(Semantic.Attr.COLOR, 2)` this means enabled (!=0)
* `glDrawArraysInstanced`

