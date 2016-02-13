# OpenGL 3.2 Sync Highlights

### [gl-320-sync](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_320/sync/Gl_320_sync.java) :

* [`GL_ARB_sync`](https://www.opengl.org/registry/specs/ARB/sync.txt). This extension aimed to get more control 
than just glClear and glFinish during the commands list execusion, it provides a more accurate synchronization 
between the CPU and the GPU, between OpenGL contexts and between OpenGL and OpenCL. 
* `glFenceSync` insert a fence command into the GL command stream
* `glClientWaitSync(syncName, GL_SYNC_FLUSH_COMMANDS_BIT, maxTimeout)` waits for it
