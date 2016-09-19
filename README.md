# jogl-samples

The Java OpenGL Samples Pack (called unsurprisingly jogl-samples  :scream: ) is the java port of the [OpenGL Samples Pack](http://www.g-truc.net/project-0026.html), a collection of [OpenGL](http://www.opengl.org/) samples based on the OpenGL "core profile" specifications.

The project aims to promote the new OpenGL features making easier version transitions for OpenGL programmers with a complementary documentation for the OpenGL specification. Despite the fact that the OpenGL Samples Pack provides as simple (and dumb) as possible samples, it's not a tutorial for beginner but a project for programmers already familiar with OpenGL. The OpenGL Samples Pack is also a good OpenGL drivers feature test.

These samples use [NEWT](http://jogamp.org/jogl/doc/NEWT-Overview.html) to create window and [jogl](http://jogamp.org/jogl/www/) of [Jogamp](http://jogamp.org/) as OpenGL wrapper, [glm](https://github.com/elect86/Jglm) as math library and to replace OpenGL fixed pipeline functions and [gli](https://github.com/elect86/jgli) to load images. 

The over 230 samples illustrate almost all OpenGL features ranging from ES 2.0 up to the last GL extenstions, same of them usually also called AZDO (Almost Zero Driver Overhead). They are divided per profiles and I also wrote a little wiki for each profile quoting the most relevant part of g-truc's reviews, really interesting (I have learnt a lot myself reading them):

* [es-200](https://github.com/elect86/jogl-samples/tree/master/jogl-samples/src/tests/es_200)
* [es-300](https://github.com/elect86/jogl-samples/tree/master/jogl-samples/src/tests/es_300)
* [gl-300](https://github.com/elect86/jogl-samples/tree/master/jogl-samples/src/tests/gl_300)
* [gl-320](https://github.com/elect86/jogl-samples/tree/master/jogl-samples/src/tests/gl_320)
* [gl-330](https://github.com/elect86/jogl-samples/tree/master/jogl-samples/src/tests/gl_330)
* [gl-400](https://github.com/elect86/jogl-samples/tree/master/jogl-samples/src/tests/gl_400)
* [gl-410](https://github.com/elect86/jogl-samples/tree/master/jogl-samples/src/tests/gl_410)
* [gl-420](https://github.com/elect86/jogl-samples/tree/master/jogl-samples/src/tests/gl_420)
* [gl-430](https://github.com/elect86/jogl-samples/tree/master/jogl-samples/src/tests/gl_430)
* [gl-440](https://github.com/elect86/jogl-samples/tree/master/jogl-samples/src/tests/gl_440)
* [gl-450](https://github.com/elect86/jogl-samples/tree/master/jogl-samples/src/tests/gl_450)
* [gl-500](https://github.com/elect86/jogl-samples/tree/master/jogl-samples/src/tests/gl_500)
* [micro]

## Quick setup:

- add "-ea" in VM options in run config for the `assert`s to work
- add all the jars you find under [`dependencies`](https://github.com/elect86/jogl-samples/tree/master/jogl-samples/dependencies)

You can find the results inside the [`templates` directory](https://github.com/elect86/jogl-samples/tree/master/jogl-samples/src/templates).

Ps: Hello Triangle and Hello Texture have been moved into a stand-alone [project](https://github.com/elect86/helloTriangle)

Pps: I'd need some guinea pigs (aka: volunteers, but don't worry, it won't hurt :smirk:) to test the amd-only and intel-only extensions :octocat:

## Changelog:
- 03-03-16, [`gl-430-program-compute-variable-group-size`](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_430/Gl_430_program_compute_variable_group_size.java) added
