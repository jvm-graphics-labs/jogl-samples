# Highlights

### [gl-320-draw-base-vertex](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/draw/Gl_320_draw_base_vertex.java) :

* `glDrawElementsInstancedBaseVertex`

### [gl-320-draw-image-space](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/draw/Gl_320_draw_image_space.java) :

* draw in image space

Model -> World -> Camera/View -> Clip -> NDC -> Window/Image space (i.e. coordinates in pixels)

### [gl-320-draw-instanced](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/draw/Gl_320_draw_instanced.java) :

* `glDrawArraysInstanced` and `gl_InstanceID`

### [gl-320-draw-multiple](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/draw/Gl_320_draw_multiple.java) :

* `glMultiDrawElementsBaseVertex`, one call equivalent to draw multiple elements with `glDrawElementsBaseVertex`

### [gl-320-draw-range-array](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/draw/Gl_320_draw_range_array.java) :

* draw only a specific range of vertices within an array.
* `glDrawArraysInstanced`

### [gl-320-draw-range-elements](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/draw/Gl_320_draw_range_elements.java) :

* draw only a specific range of elements within an array.
* `glDrawElementsInstancedBaseVertex`

### [gl-320-draw-without-vertex_attrib](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/draw/Gl_320_draw_without_vertex_attrib.java) :

* draw without any vertex attribute. The vertex positions will are hardcoded inside the vertex shader and accesses via `gl_VertexID`
