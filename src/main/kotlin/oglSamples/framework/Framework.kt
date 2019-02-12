package oglSamples.framework

import gli_.has
import glm_.bool
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.max
import glm_.min
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import gln.cap.Caps.Profile
import kool.IntBuffer
import oglSamples.glBeginQuery
import oglSamples.glGetQueryObjectui
import oglSamples.invoke
import oglSamples.wo
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.ARBDebugOutput.*
import org.lwjgl.opengl.GL30C.GL_NUM_EXTENSIONS
import org.lwjgl.opengl.GL30C.glGetStringi
import org.lwjgl.opengl.GL43C.*
import org.lwjgl.opengl.GLDebugMessageCallback
import org.lwjgl.opengl.GLDebugMessageCallbackI
import org.lwjgl.system.MemoryUtil.NULL
import org.lwjgl.system.Platform
import uno.glfw.*
import uno.glfw.windowHint.Api
import uno.glfw.windowHint.Profile as GlfwProfile

abstract class Framework(

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

    val mouseOrigin = Vec2(windowSize ushr 1)
    val mouseCurrent = Vec2(windowSize ushr 1)
    val tranlationOrigin = position
    val tranlationCurrent = Vec2(position)
    val rotationOrigin = orientation
    val rotationCurrent = Vec2(orientation)
    var mouseButtonFlags = 0
    //    std::array<bool, 512> KeyPressed;
    var error = false
    var viewSetupFlags = ViewSetupFlag.TRANSLATE.i or ViewSetupFlag.ROTATE_X.i or ViewSetupFlag.ROTATE_Y.i

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
    }

    val cursorPositionCallback: CursorPosCallbackT = { pos: Vec2 ->

        mouseCurrent put pos
        tranlationCurrent put when {
            mouseButtonFlags has MouseButton.LEFT.i -> tranlationOrigin + (mouseCurrent - mouseOrigin) / 10f
            else -> tranlationOrigin
        }
        rotationCurrent put when {
            mouseButtonFlags has MouseButton.RIGHT.i -> rotationOrigin + glm.radians(mouseCurrent - mouseOrigin)
            else -> rotationOrigin
        }
    }

    val mouseButtonCallback: MouseButtonCallbackT = { button: Int, action: Int, mods: Int ->
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
                    tranlationOrigin += (mouseCurrent - mouseOrigin) / 10f
                    mouseButtonFlags = mouseButtonFlags wo MouseButton.LEFT.i
                }
                GLFW_MOUSE_BUTTON_MIDDLE -> mouseButtonFlags = mouseButtonFlags wo MouseButton.MIDDLE.i
                GLFW_MOUSE_BUTTON_RIGHT -> {
                    rotationOrigin += glm.radians(mouseCurrent - mouseOrigin)
                    mouseButtonFlags = mouseButtonFlags wo MouseButton.RIGHT.i
                }
            }
        }
    }

    val keyCallback: KeyCallbackT = { key: Int, _: Int, _: Int, _: Int ->
        if (key == Key.ESCAPE.i)
            stop()
    }

    val debugOutput: GLDebugMessageCallbackI = GLDebugMessageCallback.create { source, type, id, severity, length, message, userParam ->

        val debSource = when (source) {
            GL_DEBUG_SOURCE_API_ARB -> "OpenGL"
            GL_DEBUG_SOURCE_WINDOW_SYSTEM_ARB -> "Windows"
            GL_DEBUG_SOURCE_SHADER_COMPILER_ARB -> "Shader Compiler"
            GL_DEBUG_SOURCE_THIRD_PARTY_ARB -> "Third Party"
            GL_DEBUG_SOURCE_APPLICATION_ARB -> "Application"
            GL_DEBUG_SOURCE_OTHER_ARB -> "Other"
            else -> throw Exception("[Debug] invalid source")
        }

        val debType = when (type) {
            GL_DEBUG_TYPE_ERROR -> "error"
            GL_DEBUG_TYPE_DEPRECATED_BEHAVIOR -> "deprecated behavior"
            GL_DEBUG_TYPE_UNDEFINED_BEHAVIOR -> "undefined behavior"
            GL_DEBUG_TYPE_PORTABILITY -> "portability"
            GL_DEBUG_TYPE_PERFORMANCE -> "performance"
            GL_DEBUG_TYPE_OTHER -> "message"
            GL_DEBUG_TYPE_MARKER -> "marker"
            GL_DEBUG_TYPE_PUSH_GROUP -> "push group"
            GL_DEBUG_TYPE_POP_GROUP -> "pop group"
            else -> throw Exception("[Debug] invalid type")
        }

        val debSev = when (severity) {
            GL_DEBUG_SEVERITY_HIGH_ARB -> {
                if (success == Success.GENERATE_ERROR || source != GL_DEBUG_SOURCE_SHADER_COMPILER_ARB)
                    error = true
                "high"
            }
            GL_DEBUG_SEVERITY_MEDIUM_ARB -> "medium"
            GL_DEBUG_SEVERITY_LOW_ARB -> "low"
            GL_DEBUG_SEVERITY_NOTIFICATION -> "notification"
            else -> throw Exception("[Debug] invalid severity")
        }

        System.err.println("$debSource: $debType($debSev) $id: ${GLDebugMessageCallback.getMessage(length, message)}")

        if (success != Success.GENERATE_ERROR && source != GL_DEBUG_SOURCE_SHADER_COMPILER_ARB)
            assert(!error)
    }

    init {
        window.pos = Vec2i(64)
        window.mouseButtonCallback = mouseButtonCallback
        window.cursorPosCallback = cursorPositionCallback
        window.keyCallback = keyCallback
        window.makeContextCurrent()

        window.createCapabilities(profile)

        if (DEBUG && window.caps.caps.GL_KHR_debug)
            if (isExtensionSupported("GL_KHR_debug")) {
                glEnable(GL_DEBUG_OUTPUT)
                glEnable(GL_DEBUG_OUTPUT_SYNCHRONOUS)
                glDebugMessageControl(GL_DONT_CARE, GL_DONT_CARE, GL_DONT_CARE, 0, true)
                glDebugMessageCallback(debugOutput, NULL)
            }
    }

    constructor(title: String, profile: Profile,
                major: Int, minor: Int,
                frameCount: Int, success: Success, windowSize: Vec2i) :
            this(title, profile, major, minor, windowSize, Vec2(), Vec2(), frameCount, success)

    constructor(title: String, profile: Profile,
                major: Int, minor: Int,
                orientation: Vec2, success: Success) :
            this(title, profile, major, minor, Vec2i(640, 480), orientation, Vec2(0, 4), 2, success)

    constructor(title: String, profile: Profile,
                major: Int, minor: Int,
                frameCount: Int, windowSize: Vec2i,
                orientation: Vec2, position: Vec2) :
            this(title, profile, major, minor, windowSize, orientation, position, frameCount, Success.RUN_ONLY)

    constructor(title: String, profile: Profile,
                major: Int, minor: Int, heuristic: Heuristic) :
            this(title, profile, major, minor, Vec2i(640, 480), Vec2(), Vec2(0, 4), 2, Success.MATCH_TEMPLATE, heuristic)

    open fun destroy() {

        if (timerQueryName[0].bool)
            glDeleteQueries(timerQueryName)

        window.destroy()

        glfw.terminate()
    }

    operator fun invoke(): Exit {

        var result = Exit.SUCCESS

        if (result == Exit.SUCCESS)
            if (version(major, minor) >= version(3, 0))
                result = if (checkGLVersion(major, minor)) Exit.SUCCESS else Exit.FAILURE

        if (result == Exit.SUCCESS)
            result = if (begin()) Exit.SUCCESS else Exit.FAILURE

        var frameNum = frameCount
        val automated = AUTOMATED_TESTS

        while (result == Exit.SUCCESS && !error) {
            result = if (render()) Exit.SUCCESS else Exit.FAILURE
            result = when {
                result == Exit.FAILURE && checkError("render") -> Exit.FAILURE
                else -> Exit.SUCCESS
            }

            glfw.pollEvents()
            if (window.shouldClose || (automated && frameNum == 0)) {
                if (success == Success.MATCH_TEMPLATE) {
                    TODO()
//                    if(!checkTemplate(this->Window, this->Title.c_str()))
//                    result = EXIT_FAILURE
//                    this->checkError("checkTemplate")
                }
                break
            }

            window.swapBuffers()

            if (automated)
                --frameNum
        }

        if (result == Exit.SUCCESS)
            result = when {
                end() && result == Exit.SUCCESS -> Exit.SUCCESS
                else -> Exit.FAILURE
            }

        return when (success) {
            Success.GENERATE_ERROR -> when {
                result != Exit.SUCCESS || error -> Exit.SUCCESS
                else -> Exit.FAILURE
            }
            else -> when {
                result == Exit.SUCCESS && !error -> Exit.SUCCESS
                else -> Exit.FAILURE
            }
        }
    }

    //    void log(csv & CSV, char const * String)
    fun setupView(translate: Boolean, rotateX: Boolean, rotateY: Boolean) {
        viewSetupFlags =
                (if (translate) ViewSetupFlag.TRANSLATE.i else 0) or
                        (if (rotateX) ViewSetupFlag.ROTATE_X.i else 0) or
                        (if (rotateY) ViewSetupFlag.ROTATE_Y.i else 0)
    }

    abstract fun begin(): Boolean
    abstract fun end(): Boolean
    abstract fun render(): Boolean

    fun stop() {
        window.shouldClose = true
    }

    fun isExtensionSupported(string: String): Boolean {
        val extensionCount = glGetInteger(GL_NUM_EXTENSIONS)
        for (i in 0 until extensionCount)
            if (glGetStringi(GL_EXTENSIONS, i) == string)
                return true
        println("Failed to find Extension: \"$string\"")
        return false
    }

    val windowSize get() = window.framebufferSize
    //    bool isKeyPressed(int Key) const
    val view: Mat4
        get() {
            var viewTranslate = Mat4(1f)
            if (viewSetupFlags has ViewSetupFlag.TRANSLATE.i)
                viewTranslate = Mat4(1f).translate(0f, 0f, -tranlationCurrent.y)

            var viewRotateX = viewTranslate
            if (viewSetupFlags has ViewSetupFlag.ROTATE_X.i)
                viewRotateX = viewTranslate.rotate(rotationCurrent.y, 1f, 0f, 0f)

            var view = viewRotateX
            if (viewSetupFlags has ViewSetupFlag.ROTATE_Y.i)
                view = viewRotateX.rotate(rotationCurrent.x, 0f, 1f, 0f)
            return view
        }

    val cameraDistance get() = tranlationCurrent.y
    val cameraPosition get() = Vec3(0f, 0f, -tranlationCurrent.y)
//    bool checkTemplate(GLFWwindow* pWindow, char const * Title)

    fun beginTimer() = glBeginQuery(GL_TIME_ELAPSED, timerQueryName)
    fun endTimer() {

        glEndQuery(GL_TIME_ELAPSED)

        val queryTime = glGetQueryObjectui(timerQueryName, GL_QUERY_RESULT)

        val instantTime = queryTime / 1000.0

        timeSum += instantTime
        timeMax = timeMax max instantTime
        timeMin = timeMin min instantTime

        println("\rTime: %2.4f ms    ".format(instantTime / 1000.0))
    }

//    std::string loadFile(std::string const & Filename) const

    fun logImplementationDependentLimit(value: Int, string: String) {

        val result = glGetInteger(value)
        val message = "$string: $result"
        if(Platform.get() != Platform.MACOSX && window.caps.caps.GL_ARB_debug_output)
            glDebugMessageInsertARB(GL_DEBUG_SOURCE_APPLICATION_ARB, GL_DEBUG_TYPE_OTHER_ARB, 1, GL_DEBUG_SEVERITY_LOW_ARB, message)
    }


//    bool validate(GLuint VertexArrayName, std::vector<vertexattrib> const & Expected)

    fun checkError(title: String): Boolean {
        val error = glGetError()
        if (error != GL_NO_ERROR) {
            val errorString = when (error) {
                GL_INVALID_ENUM -> "GL_INVALID_ENUM"
                GL_INVALID_VALUE -> "GL_INVALID_VALUE"
                GL_INVALID_OPERATION -> "GL_INVALID_OPERATION"
                GL_INVALID_FRAMEBUFFER_OPERATION -> "GL_INVALID_FRAMEBUFFER_OPERATION"
                GL_OUT_OF_MEMORY -> "GL_OUT_OF_MEMORY"
                else -> "UNKNOWN"
            }
            print("OpenGL Error($errorString): $title")
            assert(false)
        }
        return error == GL_NO_ERROR
    }

    fun checkFramebuffer(framebufferName: Int): Boolean {
        val status = glCheckFramebufferStatus(GL_FRAMEBUFFER)
        val error = when (status) {
            GL_FRAMEBUFFER_UNDEFINED -> "GL_FRAMEBUFFER_UNDEFINED"
            GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT -> "GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT"
            GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT -> "GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT"
            GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER -> "GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER"
            GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER -> "GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER"
            GL_FRAMEBUFFER_UNSUPPORTED -> "GL_FRAMEBUFFER_UNSUPPORTED"
            GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE -> "GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE"
            GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS -> "GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS"
            else -> return true
        }

        println("OpenGL Error($error)")

        return status == GL_FRAMEBUFFER_COMPLETE
    }

    fun checkExtension(extensionName: String): Boolean {
        val extensionCount = glGetInteger(GL_NUM_EXTENSIONS)
        for(i in 0 until extensionCount)
            if(glGetStringi(GL_EXTENSIONS, i) == extensionName)
                return true
        println("Failed to find Extension: \"$extensionName\"")
        return false
    }

    fun version(major: Int, minor: Int) = major * 100 + minor * 10

    fun checkGLVersion(majorVersionRequire: Int, minorVersionRequire: Int): Boolean {
        val majorVersionContext = glGetInteger(GL_MAJOR_VERSION)
        val minorVersionContext = glGetInteger(GL_MINOR_VERSION)
        println("OpenGL Version Needed $majorVersionRequire.$minorVersionRequire ( $majorVersionContext.$minorVersionContext Found )")
        return version(majorVersionContext, minorVersionContext) >= version(majorVersionRequire, minorVersionRequire)
    }
}