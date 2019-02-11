package oglSamples.framework

import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import gln.cap.Caps.Profile
import kool.IntBuffer
import uno.glfw.glfw
import uno.glfw.windowHint.Api
import oglSamples.invoke
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.system.Platform
import uno.glfw.GlfwWindow
import uno.glfw.MouseButton
import kotlin.math.min
import uno.glfw.windowHint.Profile as GlfwProfile

open class Framework(

        val title: String,
        val profile: Profile,
        val major: Int,
        val minor: Int,
        windowSize: Vec2i = Vec2i(640, 480),
        orientation: Vec2 = Vec2(),
        position: Vec2 = Vec2(0, 4),
        val frameCount: Int = 2,
        val success: Success = Success.MATCH_TEMPLATE,
        val heuristic: Heuristic = Heuristic.ALL) {

    //    GLFWwindow* Window;
    val timerQueryName = IntBuffer(1)

    var timeSum = 0.0
    var timeMin = Double.MAX_VALUE
    var timeMax = 0.0

    val mouseOrigin = windowSize ushr 1
    val mouseCurrent = windowSize ushr 1
    val tranlationOrigin = position
    val tranlationCurrent = Vec2(position)
    val rotationOrigin = orientation
    val rotationCurrent = Vec2(orientation)
    var mouseButtonFlags = 0
    //    std::array<bool, 512> KeyPressed;
    var error = false
    val viewSetupFlags = ViewSetupFlag.TRANSLATE.i or ViewSetupFlag.ROTATE_X.i or ViewSetupFlag.ROTATE_Y.i

    init {
        assert(windowSize allGreaterThan 0)

        glfw {
            init()
            windowHint {
                resizable = false
                visible = true
                srgb = false
                decorated = true
                api = if (this@Framework.profile == Profile.ES) Api.glEs else Api.gl

                if (version(major, minor) >= version(3, 2) || this@Framework.profile == Profile.ES) {

                    context.major = major
                    context.minor = minor
                    if (Platform.get() == Platform.MACOSX) {
                        profile = GlfwProfile.core
                        forwardComp = true
                    } else {
                        if (this@Framework.profile != Profile.ES) {
                            profile = if (this@Framework == Profile.CORE) GlfwProfile.core else GlfwProfile.compat
                            forwardComp = this@Framework.profile == Profile.CORE
                        }
                        debug = DEBUG
                    }
                }
            }
        }
    }

    val window = run {
        val dpi = if (Platform.get() == Platform.MACOSX) 2 else 1
        GlfwWindow(windowSize / dpi, title)
    }.apply {
        pos = Vec2i(64)
        mouseButtonCallback = { button: Int, action: Int, mods: Int ->
            when (action) {
                GLFW_PRESS -> {
                    mouseOrigin put mouseCurrent
                    when (button) {
                        GLFW_MOUSE_BUTTON_LEFT -> {
                            mouseButtonFlags = mouseButtonFlags or MouseButton.LEFT.i
                            tranlationOrigin put tranlationCurrent
                        }
                        GLFW_MOUSE_BUTTON_MIDDLE -> mouseButtonFlags = mouseButtonFlags or MouseButton.MIDDLE.i
                        GLFW_MOUSE_BUTTON_RIGHT -> {
                            mouseButtonFlags = mouseButtonFlags or MouseButton.RIGHT.i
                            rotationOrigin put rotationCurrent
                        }
                    }
                }
                GLFW_RELEASE -> when (button) {
                    GLFW_MOUSE_BUTTON_LEFT -> {
                        tranlationOrigin += (mouseCurrent-mouseOrigin) / 10f
                        mouseButtonFlags = mouseButtonFlags wo MouseButton.LEFT
                    }
                        break
                        case GLFW_MOUSE_BUTTON_MIDDLE :
                    { Test ->
                        MouseButtonFlags & = ~framework::MOUSE_BUTTON_MIDDLE
                    }
                        break
                        case GLFW_MOUSE_BUTTON_RIGHT :
                    { Test ->
                        RotationOrigin += glm::radians(Test->MouseCurrent-Test->MouseOrigin)
                        Test->MouseButtonFlags & = ~framework::MOUSE_BUTTON_RIGHT
                    }
                        break
                }
            }
        }
    }

    framework(
    int argc, char* argv[], char const * Title,
    profile Profile, int Major, int Minor,
    std::size_t FrameCount,
    success Success,
    glm::uvec2 const & WindowSize)
    framework(
    int argc, char* argv[], char const * Title,
    profile Profile, int Major, int Minor,
    std::size_t FrameCount,
    glm::uvec2 const & WindowSize = glm::uvec2(640, 480),
    glm::vec2 const & Orientation = glm::vec2(0, 0),
    glm::vec2 const & Position = glm::vec2(0, 4))
    framework(
    int argc, char* argv[], char const * Title,
    profile Profile, int Major, int Minor,
    glm::vec2 const & Orientation,
    success Success = MATCH_TEMPLATE)
    framework(
    int argc, char* argv[], char const * Title,
    profile Profile, int Major, int Minor,
    heuristic Heuristic)
    virtual ~framework()

    virtual bool begin() = 0
    virtual bool end() = 0
    virtual bool render() = 0

    void swap()
    void sync(sync_mode const & Sync)
    void stop()

    bool isExtensionSupported(char const * String)
    glm::uvec2 getWindowSize() const
    bool isKeyPressed(int Key) const
    glm::mat4 view() const
    float cameraDistance()
    const { return this->TranlationCurrent.y; }
    glm::vec3 cameraPosition() const
    bool checkTemplate(GLFWwindow* pWindow, char const * Title)

    protected :
    void beginTimer()
    void endTimer()

    std::string loadFile(std::string const & Filename) const
    void logImplementationDependentLimit(GLenum Value, std::string const & String) const
    bool validate(GLuint VertexArrayName, std::vector<vertexattrib> const & Expected) const
    bool checkError(const char* Title) const
    bool checkFramebuffer(GLuint FramebufferName) const
    bool checkExtension(char const * ExtensionName)
    const

    fun version(major: Int, minor: Int) = major * 100 + min(* 10)

    const { return Major * 100 + Minor * 10; }
    bool checkGLVersion(GLint MajorVersionRequire, GLint MinorVersionRequire) const

    static void cursorPositionCallback(GLFWwindow* Window, double x, double y)
    static void mouseButtonCallback(GLFWwindow* Window, int Button, int Action, int mods)
    static void keyCallback(GLFWwindow* Window, int Key, int Scancode, int Action, int Mods)

    public :
    static void APIENTRY debugOutput(GLenum source, GLenum type, GLuint id, GLenum severity, GLsizei length, const GLchar* message, const GLvoid* userParam)


}