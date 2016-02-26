# OpenGL 4.5 Highlights

### [gl-450-caps](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_450/Gl_450_caps.java):

* openGL 4.5 capabilities

### [gl-450-clip-control](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_450/Gl_450_clip_control.java):

*[`GL_ARB_clip_control`](https://www.opengl.org/registry/specs/ARB/clip_control.txt) provides additional clip control modes to configure how clip space is mapped to window space.  This extension's goal is to:
   * allow OpenGL to effectively match Direct3D's coordinate system conventions
   * potentially improve the numerical precision of the Z coordinate mapping.

Developers interested in this functionality may be porting content from Direct3D to OpenGL and/or interested in improving the numerical accuracy of depth testing, particularly with floating-point depth buffers.

OpenGL's initial and conventional clip control state is configured by:
```java
        gl4.glClipControl(GL_LOWER_LEFT, GL_NEGATIVE_ONE_TO_ONE);
```
where geometry with (x,y) normalized device coordinates of (-1,-1) correspond to the lower-left corner of the viewport and the near and far planes correspond to z normalized device coordinates of -1 and +1, respectively.

This extension can be used to render content used in a Direct3D application in OpenGL in a straightforward way without modifying vertex or matrix data.  When rendering into a window, the command
```java
        gl4.glClipControl(GL_LOWER_LEFT, GL_ZERO_TO_ONE);
```
configures the near clip plane to correspond to a z normalized device coordinate of 0 as in Direct3D.  Geometry with (x,y) normalized device coordinates of (-1,-1) correspond to the lower-left corner of the viewport in Direct3D, so no change relative to OpenGL conventions is needed there. Other state related to screen-space coordinates may need to be modified for the application to map from Direct3D to OpenGL window coordinate conventions.  For example, the viewport rectangle in Direct3D needs to be inverted within the window to work properly in OpenGL windowed rendering:
```java
       gl4.glViewport(d3d_viewport_x, window_height - (d3d_viewport_y + d3d_viewport_height), d3d_viewport_width, d3d_viewport_height);
```
When rendering Direct3D content into a framebuffer object in OpenGL, there is one complication -- how to get a correct image *out* of the related textures.  Direct3D applications would expect a texture coordinate of (0,0) to correspond to the upper-left corner of a rendered image, while OpenGL FBO conventions would map (0,0) to the lower-left corner of the rendered image.  For applications wishing to use Direct3D content with unmodified texture coordinates, the command
```java
        gl4.glClipControl(GL_UPPER_LEFT, GL_ZERO_TO_ONE);
```
configures the OpenGL to invert geometry vertically inside the viewport. Content at the top of the viewport for Direct3D will be rendered to the bottom of the viewport from the point of view of OpenGL, but will have a <t> texture coordinate of zero in both cases.  When operating in this mode, applications need not invert the programmed viewport rectangle as recommended for windowed rendering above.

Applications happy with OpenGL's origin conventions but seeking potentially improved depth precision can configure clip controls using:
```java
        gl4.glClipControl(GL_LOWER_LEFT, GL_ZERO_TO_ONE);
```
to avoid the loss of precision from the DepthRange transformation (which by default is `z_window = z_ndc * 0.5 + 0.5`).

### [gl-450-culling](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_450/Gl_450_culling.java):

* [GL_ARB_cull_distance](https://www.opengl.org/registry/specs/ARB/cull_distance.txt) adds a new GLSL `gl_CullDistance` shader output, similar to `gl_ClipDistance`, but used for whole primitive culling:
```glsl
out gl_PerVertex
{
    vec4 gl_Position;
    float gl_CullDistance[1];
};
```
This new stage in the pipeline is added as part of the primitive clipping stage.

### [gl-450-direct-state-access](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_450/Gl_450_direct_state_access.java): broken

* shit

### [gl-450-fbo-multisample-nv](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_450/Gl_450_fbo_multisample_explicit.java):

* loads a diffuse texture and creates two fbo, one multisampled and one normal. It renders the diffuse texture in the 
multisample buffer and then resolve to screen by picking only the first sample for the right half and an average of all
the sample for the left half.
 
### [gl-450-query-conditional](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_450/Gl_450_query_conditional.java):

* inverted conditional rendering without GPU stalling
* [`GL_ARB_conditional_render_inverted`](https://www.opengl.org/registry/specs/ARB/conditional_render_inverted.txt) adds 
new modes to BeginConditionalRender which invert the condition used to determine whether to draw or not.
 
### [gl-450-query-statistics-arb](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_450/Gl_450_query_statistic_arb.java):

* [`GL_ARB_pipeline_statistics_query`](https://www.opengl.org/registry/specs/ARB/pipeline_statistics_query.txt] introduces 
new query types that allow applications to get statistics information about different parts of the pipeline:

      * Number of vertices and primitives issued to the GL;

      * Number of times a vertex shader, tessellation evaluation shader, geometry shader, fragment shader, and compute shader was invoked;

      * Number of patches processed by the tessellation control shader stage;

      * Number of primitives emitted by a geometry shader;

      * Number of primitives that entered the primitive clipping stage;

      * Number of primitives that are output by the primitive clipping stage;

### [gl-450-texture-barrier](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_450/Gl_450_texture_barrier.java):

* loads a diffuse texture and creates 1000 random small viewports. Then, for each viewport, applies the viewport itself, 
renders the texture and set the texture barrier.
* [`GL_ARB_texture_barrier`](https://www.opengl.org/registry/specs/ARB/texture_barrier.txt) relaxes the restrictions on 
rendering to a currently bound texture and provides a mechanism to avoid read-after-write hazards.
* `glTextureBarrier()`

### [gl-450-texture-derivative](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_450/Gl_450_texture_derivative.java):

* [`GL_ARB_derivative_control`](https://www.opengl.org/registry/specs/ARB/derivative_control.txt)

### [gl-450-transform-feedback-arb](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_450/Gl_450_transform_feedback_arb.java):

* [`GL_ARB_transform_feedback_overflow_query`](https://www.opengl.org/registry/specs/ARB/transform_feedback_overflow_query.txt) 
adds new query types which can be used to detect overflow of transform feedback buffers. The new query types are also 
accepted by conditional rendering commands.
* `GL_TRANSFORM_FEEDBACK_STREAM_OVERFLOW_ARB`
* transforms positions and if there is no overflow it renders a quad.
