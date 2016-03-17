/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_430;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import static com.jogamp.opengl.GL2.GL_PERFMON_RESULT_AMD;
import static com.jogamp.opengl.GL2.GL_PERFMON_RESULT_SIZE_AMD;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLES2;
import com.jogamp.opengl.util.GLBuffers;
import framework.BufferUtils;
import framework.Profile;
import framework.Test;
import glf.Vertex_v2fv2f;
import glm.vec._2.Vec2;
import glm.vec._2.i.Vec2i;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;

/**
 *
 * @author elect
 */
public class Gl_430_perf_monitor_amd extends Test {

    public static void main(String[] args) {
        Gl_430_perf_monitor_amd gl_430_perf_monitor_amd = new Gl_430_perf_monitor_amd();
    }

    public Gl_430_perf_monitor_amd() {
        super("gl-430-perf-monitor-amd", Profile.CORE, 4, 3);
    }

    private final String SHADERS_SOURCE_TEXTURE = "fbo-texture-2d";
    private final String SHADERS_SOURCE_SPLASH = "fbo-splash";
    private final String SHADERS_ROOT = "src/data/gl_430";
    private final String TEXTURE_DIFFUSE = "kueken7_rgba_dxt1_unorm.dds";

    private int vertexCount = 4;
    private int vertexSize = vertexCount * Vertex_v2fv2f.SIZE;
    private float[] vertexData = {
        -1.0f, -1.0f,/**/ 0.0f, 1.0f,
        +1.0f, -1.0f,/**/ 1.0f, 1.0f,
        +1.0f, +1.0f,/**/ 1.0f, 0.0f,
        -1.0f, +1.0f,/**/ 0.0f, 0.0f};

    private int elementCount = 6;
    private int elementSize = elementCount * Short.BYTES;
    private short[] elementData = {
        0, 1, 2,
        2, 3, 0};

    private class Buffer {

        public static final int VERTEX = 0;
        public static final int ELEMENT = 1;
        public static final int TRANSFORM = 2;
        public static final int MAX = 3;
    }

    private class Texture {

        public static final int DIFFUSE = 0;
        public static final int COLORBUFFER = 1;
        public static final int RENDERBUFFER = 2;
        public static final int MAX = 3;
    }

    private class Pipeline {

        public static final int TEXTURE = 0;
        public static final int SPLASH = 1;
        public static final int MAX = 2;
    }

    private class Monitor {

        public IntBuffer name;
        private HashMap<String, Integer> stringToGroup;
        private HashMap<Integer, Group> groups;

        public Monitor(GL2 gl2, Gl_430_perf_monitor_amd app) {

            name = GLBuffers.newDirectIntBuffer(1);

            int GROUP_NAME_SIZE = 256;

            IntBuffer groupSize = GLBuffers.newDirectIntBuffer(1);
            gl2.glGetPerfMonitorGroupsAMD(groupSize, 0, null);

            IntBuffer groups_ = GLBuffers.newDirectIntBuffer(groupSize.get(0));
            gl2.glGetPerfMonitorGroupsAMD(null, groupSize.get(0), groups_);

            for (int groupIndex = 0; groupIndex < groups_.capacity(); groupIndex++) {

                ByteBuffer groupName = GLBuffers.newDirectByteBuffer(GROUP_NAME_SIZE);
                gl2.glGetPerfMonitorGroupStringAMD(groups_.get(groupIndex), GROUP_NAME_SIZE, null, groupName);

                IntBuffer numCounters = GLBuffers.newDirectIntBuffer(1);
                IntBuffer maxActiveCounters = GLBuffers.newDirectIntBuffer(1);
                gl2.glGetPerfMonitorCountersAMD(groups_.get(groupIndex), numCounters, maxActiveCounters, 0, null);

                IntBuffer counters = GLBuffers.newDirectIntBuffer(numCounters.get(0));
                gl2.glGetPerfMonitorCountersAMD(groups_.get(groupIndex), null, null, numCounters.get(0), counters);

                for (int counterIndex = 0; counterIndex < counters.capacity(); counterIndex++) {

                    ByteBuffer counterName = GLBuffers.newDirectByteBuffer(GROUP_NAME_SIZE);

                    gl2.glGetPerfMonitorCounterStringAMD(groups_.get(groupIndex), counters.get(counterIndex),
                            GROUP_NAME_SIZE, null, counterName);

                    BufferUtils.destroyDirectBuffer(counterName);
                }

                String groupString = "";
                for (int i = 0; i < groupName.capacity(); ++i) {
                    if ((char) (groupName.get(i) & 0xff) == '\0') {
                        break;
                    } else {
                        groupString += (char) (groupName.get(i) & 0xff);
                    }
                }

                groups.put(groups_.get(groupIndex), new Group(groupString, counters));
                stringToGroup.put(groupString, groups_.get(groupIndex));

                BufferUtils.destroyDirectBuffer(groupName);
                BufferUtils.destroyDirectBuffer(numCounters);
                BufferUtils.destroyDirectBuffer(maxActiveCounters);
                BufferUtils.destroyDirectBuffer(counters);
            }

            gl2.glGenPerfMonitorsAMD(1, name);
        }

        public void dispose(GL2 gl2) {
            gl2.glDeletePerfMonitorsAMD(1, name);
        }

        public void begin(GL2 gl2) {
            gl2.glBeginPerfMonitorAMD(name.get(0));
        }

        public void end(GL2 gl2) {
            gl2.glEndPerfMonitorAMD(name.get(0));
        }

        public void record(GL2 gl2, String groupString, int counterCount) {

            assert (stringToGroup.containsKey(groupString));
            int name_ = stringToGroup.get(groupString);

            assert (groups.containsKey(name_));
            Group group = groups.get(name_);

            gl2.glSelectPerfMonitorCountersAMD(name.get(0), true, name_,
                    Math.min(counterCount, group.counter.capacity()), group.counter);
        }
        
        public void log(GL2 gl2) {
            // read the counters
            IntBuffer resultSize=GLBuffers.newDirectIntBuffer(1);
            gl2.glGetPerfMonitorCounterDataAMD(name.get(0), GL_PERFMON_RESULT_SIZE_AMD, Integer.BYTES, resultSize, null);

            IntBuffer result=GLBuffers.newDirectIntBuffer(resultSize.get(0));

            IntBuffer resultWritten=GLBuffers.newDirectIntBuffer(1);
            gl2.glGetPerfMonitorCounterDataAMD(name.get(0), GL_PERFMON_RESULT_AMD, resultSize.get(0), result, 
                    resultWritten);

//            GLsizei wordCount = 0;
//
//            while ((4 * wordCount) < resultWritten)
//            {
//                    GLuint GroupId = result[wordCount];
//                    GLuint CounterId = result[wordCount + 1];
//
//                    std::map<GLuint, group>::iterator GroupIt = this->Groups.find(GroupId);
//                    assert(GroupIt != this->Groups.end());
//
//                    // Determine the counter type
//                    GLuint CounterType;
//                    glGetPerfMonitorCounterInfoAMD(GroupId, CounterId, GL_COUNTER_TYPE_AMD, &CounterType);
//
//                    switch(CounterType)
//                    {
//                            case GL_UNSIGNED_INT64_AMD:
//                            {
//                                    glm::uint64 * counterResult = reinterpret_cast<glm::uint64*>(&result[wordCount + 2]);
//                                    printf("%s(%d): %ld\n", GroupIt->second.Name.c_str(), CounterId, *counterResult);
//                                    wordCount += 4;
//                                    break;
//                            }
//                            case GL_FLOAT:
//                            {
//                                    float * counterResult = reinterpret_cast<float*>(&result[wordCount + 2]);
//                                    printf("%s(%d): %f\n", GroupIt->second.Name.c_str(), CounterId, *counterResult);
//                                    wordCount += 3;
//                                    break;
//                            }
//                            case GL_UNSIGNED_INT:
//                            {
//                                    glm::uint32 * counterResult = reinterpret_cast<glm::uint32*>(&result[wordCount + 2]);
//                                    printf("%s(%d): %d\n", GroupIt->second.Name.c_str(), CounterId, *counterResult);
//                                    wordCount += 3;
//                                    break;
//                            }
//                            case GL_PERCENTAGE_AMD:
//                            {
//                                    float * counterResult = reinterpret_cast<float*>(&result[wordCount + 2]);
//                                    printf("%s(%d): %f\n", GroupIt->second.Name.c_str(), CounterId, *counterResult);
//                                    wordCount += 3;
//                                    break;
//                            }
//                            default:
//                                    assert(0);
//                    }
//            }
        }
    }

    private class Group {

        public String name;
        public IntBuffer counter;

        public Group(String name, IntBuffer counter) {
            this.name = name;
            this.counter = counter;
        }
    }

//
//    private int[] framebufferName = {0}, pipelineName = new int[Pipeline.MAX], programName = new int[Pipeline.MAX],
//            vertexArrayName = new int[Pipeline.MAX], bufferName = new int[Buffer.MAX], texturename = new int[Texture.MAX];
//
//    @Override
//    protected boolean begin(GL gl) {
//
//        GL4 gl4 = (GL4) gl;
//
//        boolean validated = true;
//        validated = validated && gl4.isExtensionAvailable("GL_AMD_performance_monitor");
//        GL2 gles2 = (GL2) gl;
////        gles2.glgeet
////		if(validated)
////		{
////			this->Monitor.reset(new monitor());
////			this->Monitor->record("CP", 1);
////		}
////
////		if(validated)
////			validated = initProgram();
////		if(validated)
////			validated = initBuffer();
////		if(validated)
////			validated = initVertexArray();
////		if(validated)
////			validated = initTexture();
////		if(validated)
////			validated = initFramebuffer();
//        return validated;
//    }
}
