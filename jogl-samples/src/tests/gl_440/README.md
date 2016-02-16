# OpenGL 4.4 Highlights ([g-truc review](http://www.g-truc.net/doc/OpenGL%204.4%20review.pdf))

### [gl-440-atomic-counter](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_440/Gl_440_atomic_counter.java):

* creates a `GL_ATOMIC_COUNTER_BUFFER` buffer and in the fragment shader increments it and uses it to shade
* "Searching performance, we might mislead ourselves very easily. Good performance for a real time software is not about having
the highest average frame rate but having the highest minimum framerate by avoiding latency spikes. Sadly, most benchmarks 
don't really show the performance of hardware but how bad they are to do whatever they do and they will typically report 
average FPS ignoring the real user experience.

The OpenGL specifications provide buffer objects since OpenGL 1.5 and that includes the famous usage hints. Why is it called
_hints_? Because an implementation can do whatever it wants with a buffer regardless of the hint set at the buffer creation.
In average the drivers do a good job and increase the average frame rate however the behavior of the drivers is nearly
unpredictable and it could decide to move a buffer at the worse time in our program leading to latency spikes.

The purpose of [`ARB_buffer_storage`](https://www.opengl.org/registry/specs/ARB/buffer_storage.txt) is to resolve this issue and
make performance more reliable by replacing the usage hints by usage flags and creating immutable buffers.

Immutable buffers are buffers that we can't change the size or orphans. To create an immutable buffer we use `glBufferStorage`
command and because it is immutable we can't possible call `glBufferData` on that buffer.

If we want to modify a buffer we need to use the flag `GL_MAP_WRITE_BIT`. If we want to read its content, we need to use the
flag `GL_MAP_READ_BIT`. Only using these flags implies that we can't use `glBufferSubData` to modify an immutable buffer.

`GL_MAP_PERSISTENT_BIT` simply ensures that the mapped pointer remains valid even if rendering commands are executed during the
mapping. `GL_MAP_COHERENT_BIT` opens opportunities to ensure that whenever the client of server side performs charges on a 
buffer, these changes will be visible on this other side.

So, no more hints on buffers? Unfortunately, these are not dead as two hints have been included: `GL_DYNAMIC_STORAGE_BIT` and 
`GL_CLIENT_STORAGE_BIT`. I guess if we want to be serious about OpenGL programming and performance level, we should never use
`GL_DYNAMIC_STORAGE_BIT` and `GL_CLIENT_STORAGE_BIT`.

### [gl-440-buffer-storage](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_440/Gl_440_buffer_storage.java):

* loads a diffuse texture and creates a `COPY` immutable storage buffer with `glBufferStorage` and ´GL_MAP_WRITE_BIT` and 
writes in it the vertex positions and indices. Then it creates other two immutable buffers, `VERTEX` and `ELEMENT`, and copies 
in them the corresponding data from `COPY`. Then it renders the diffuse texture.

### [gl-440-sampler-wrap](https://github.com/elect86/jogl-samples/blob/master/jogl-samples/src/tests/gl_440/Gl_440_sampler_wrap.java)

* `glBindBufferBase(int target, int first, int count, int[] buffers, int buffers_offset)`
* `glBindSamplers(int first, int count, int[] samplers, int samplers_offset)`
* `glBindTextures(int first, int count, int[] textures, int textures_offset)`
