# OpenGL 3.2 Texture Highlights

### [gl-320-texture-2d](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/texture/Gl_320_texture_2d.java) :

* `glTexImage2D`
* `GL_TEXTURE_2D`
* `sampler2D`

### [gl-320-texture-3d](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/texture/Gl_320_texture_3d.java) :

* `glTexImage3D`
* `GL_TEXTURE_3D`
* `sampler3D`

### [gl-320-texture-buffer](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/texture/Gl_320_texture_buffer.java) :

* [`GL_ARB_texture_buffer_object`](https://www.opengl.org/registry/specs/ARB/texture_buffer_object.txt) an access of a whole large buffer like a 1D texture
* use textures as data containers
* `GL_TEXTURE_BUFFER`
* `glTexBuffer`
* `samplerBuffer`

### [gl-320-texture-compressed-ext](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/texture/Gl_320_texture_compressed_ext.java) :

* showcase of four different texture compressions, dxt1 srgb, dxt5 srgb, r ati1n unorm, rg ati2n unorm
* `glCompressedTexImage2D`

### [gl-320-texture-cube](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/texture/Gl_320_texture_cube.java) :

* cube texture, `GL_TEXTURE_CUBE_MAP`
* `samplerCube` 
* [`GL_ARB_seamless_cube_map`](https://www.opengl.org/registry/specs/ARB/seamless_cube_map.txt) allows the 
implementation to access adjacent cube map faces for the filtering, by standard indeed filtering across cube 
texture faces does not work. But you can enable it globally by `glEnable(GL_TEXTURE_CUBE_MAP_SEAMLESS)`

### [gl-320-texture-derivative](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/texture/Gl_320_texture_derivative.java) :

* [`GL_ARB_sample_shading`](https://www.opengl.org/registry/specs/ARB/sample_shading.txt) to force the processing of a minimum number of samples per fragment to reduce antialising
* `dFdx` - `dFdy`, _available only in the fragment shader_, these functions return the partial derivative of expression p with respect to the window x coordinate (for `dFdx\*`) and y coordinate (for `dFdy*`).
`dFdx` returns either `dFdxCoarse` or `dFdxFine`. `dFdy` returns either `dFdyCoarse` or `dFdyFine`. The implementation may choose which calculation to perform based upon factors such as performance or the value of the API `GL_FRAGMENT_SHADER_DERIVATIVE_HINT` hint. 

### [gl-320-texture-fetch](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/texture/Gl_320_texture_fetch.java) :

* `texelFetch`

### [gl-320-texture-float](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/texture/Gl_320_texture_float.java) :

* unsigned normalized integer on bytes that will resolve, in the shader, to a vector of floating-point values, [more](https://www.opengl.org/wiki/Image_Format#Color_formats)

### [gl-320-texture-format](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/texture/Gl_320_texture_formats.java) :

* loading the same texture in OpenGL in four different formats: `GL_RGBA8` (normalized), `GL_RGBA8UI` (integer), `GL_RGBA16F` (normalized) and `GL_RGBA8_SNORM` (normalized)
* `usampler2D` for the integer format

### [gl-320-texture-integer](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/texture/Gl_320_texture_integer.java) :

* unsigned normalized integer texture
* `GL_RGBA8UI` 
* `GL_RGB_INTEGER`
* `GL_UNSIGNED_BYTE`
* `usampler2D` 

### [gl-320-texture-lod](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/texture/Gl_320_texture_lod.java) :

* load the same texture playing with LOD parameters
* `GL_TEXTURE_MIN_LOD`
* `GL_TEXTURE_MAX_LOD`
* `GL_TEXTURE_LOD_BIA`

### [gl-320-texture-offset](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/texture/Gl_320_texture_offset.java) :

* `GL_CLAMP_TO_EDGE` shader implementation

### [gl-320-texture-pixel-store](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/texture/Gl_320_texture_pixel_store.java) :

* setup the pixel storage to load only a rectangle in the middle of the source texture
* `glPixelStorei`
* `GL_UNPACK_ROW_LENGTH`
* `GL_UNPACK_SKIP_PIXELS`
* `GL_UNPACK_SKIP_ROWS`

### [gl-320-texture-streaming](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/texture/Gl_320_texture_pixel_store.java) :

* how to stream textures with `glMapBufferRange`
* `GL_PIXEL_UNPACK_BUFFER`
