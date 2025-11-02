package org.example;

import org.example.core.Camera;
import org.example.core.ShaderProgram;
import org.example.core.ShaderUtils;
import org.example.core.Sphere;
import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.joml.*;

import java.lang.Math;
import java.nio.FloatBuffer;
import java.util.*;
import java.util.Random;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * 黑洞模拟主程序
 * 使用光线步进技术模拟黑洞的引力透镜效应和吸积盘
 */
public class BlackHoleSimulation {
    private static final int WIDTH = 1200;
    private static final int HEIGHT = 800;

    // OpenGL对象
    private long window;
    private ShaderProgram shaderProgram;
    private Sphere sphere;
    private Camera camera;

    // 模拟数据
    private List<Vector3f> stars = new ArrayList<>();
    private int starVAO, starVBO;

    // 鼠标控制
    private double lastX = WIDTH / 2.0;
    private double lastY = HEIGHT / 2.0;
    private boolean firstMouse = true;

    /**
     * 程序主入口
     */
    public static void main(String[] args) {
        new BlackHoleSimulation().run();
    }

    /**
     * 主运行循环
     */
    public void run() {
        try {
            init();
            loop();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cleanup();
        }
    }

    /**
     * 初始化OpenGL上下文和模拟数据
     */
    private void init() throws Exception {
        // macOS特定修复
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            System.setProperty("org.lwjgl.glfw.checkThread0", "false");
        }

        // 初始化GLFW
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // 配置GLFW窗口
        configureWindow();

        // 创建窗口
        createWindow();

        // 初始化OpenGL
        initOpenGL();

        // 创建着色器程序
        initShaders();

        // 初始化相机
        camera = new Camera();

        // 创建几何体
        sphere = new Sphere(32);

        // 初始化模拟数据
        initializeSimulationData();

        // 设置回调函数
        setupCallbacks();

        // 打印调试信息
        printDebugInfo();
    }

    /**
     * 配置GLFW窗口参数
     */
    private void configureWindow() {
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        // 抗锯齿设置
        try {
            glfwWindowHint(GLFW_SAMPLES, 4);
        } catch (Exception e) {
            System.out.println("MSAA not supported, using fallback anti-aliasing");
        }
    }

    /**
     * 创建GLFW窗口
     */
    private void createWindow() {
        window = glfwCreateWindow(WIDTH, HEIGHT, "黑洞模拟 - 引力透镜效应", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create GLFW window");
        }

        // 设置窗口位置居中
        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(window, (vidmode.width() - WIDTH) / 2, (vidmode.height() - HEIGHT) / 2);

        // 设置OpenGL上下文
        glfwMakeContextCurrent(window);
        glfwSwapInterval(1); // 开启垂直同步
        glfwShowWindow(window);
    }

    /**
     * 初始化OpenGL状态
     */
    private void initOpenGL() {
        GL.createCapabilities();

        // 启用抗锯齿
        try {
            glEnable(GL_MULTISAMPLE);
            System.out.println("MSAA enabled successfully");
        } catch (Exception e) {
            System.out.println("MSAA not available, using alternative anti-aliasing");
            glEnable(GL_LINE_SMOOTH);
            glEnable(GL_POLYGON_SMOOTH);
            glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
            glHint(GL_POLYGON_SMOOTH_HINT, GL_NICEST);
        }

        // 设置基础OpenGL状态
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glEnable(GL_DEPTH_TEST);
    }

    /**
     * 初始化着色器程序
     */
    private void initShaders() throws Exception {
        shaderProgram = new ShaderProgram();

        // 从资源文件加载着色器源码
        String vertexShaderSource = ShaderUtils.loadShaderSource("/shaders/vertex_shader.glsl");
        String fragmentShaderSource = ShaderUtils.loadShaderSource("/shaders/fragment_shader.glsl");

        // 创建并链接着色器程序
        shaderProgram.createVertexShader(vertexShaderSource);
        shaderProgram.createFragmentShader(fragmentShaderSource);
        shaderProgram.link();

        System.out.println("Shader program created successfully");
    }

    /**
     * 初始化模拟数据（星空背景等）
     */
    private void initializeSimulationData() {
        Random random = new Random();

        // 生成随机星空
        for (int i = 0; i < 500; i++) {
            float x = (random.nextFloat() - 0.5f) * 50.0f;
            float y = (random.nextFloat() - 0.5f) * 50.0f;
            float z = (random.nextFloat() - 0.5f) * 50.0f;
            stars.add(new Vector3f(x, y, z));
        }

        // 初始化星星的顶点缓冲区
        initStarBuffers();
    }

    /**
     * 初始化星星的顶点缓冲区对象
     */
    private void initStarBuffers() {
        // 生成VAO和VBO
        starVAO = glGenVertexArrays();
        starVBO = glGenBuffers();

        glBindVertexArray(starVAO);
        glBindBuffer(GL_ARRAY_BUFFER, starVBO);

        // 创建星星顶点数据（位置 + 颜色）
        List<Float> starData = new ArrayList<>();
        for (Vector3f star : stars) {
            // 位置
            starData.add(star.x);
            starData.add(star.y);
            starData.add(star.z);
            // 颜色（白色）
            starData.add(1.0f);
            starData.add(1.0f);
            starData.add(1.0f);
        }

        // 上传数据到GPU
        FloatBuffer starBuffer = BufferUtils.createFloatBuffer(starData.size());
        for (Float f : starData) starBuffer.put(f);
        starBuffer.flip();
        glBufferData(GL_ARRAY_BUFFER, starBuffer, GL_STATIC_DRAW);

        // 设置顶点属性指针
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 6 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 6 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);

        // 解绑
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    /**
     * 设置GLFW回调函数
     */
    private void setupCallbacks() {
        glfwSetCursorPosCallback(window, this::cursorPosCallback);
        glfwSetScrollCallback(window, this::scrollCallback);
        glfwSetFramebufferSizeCallback(window, this::framebufferSizeCallback);
        glfwSetKeyCallback(window, this::keyCallback);

        // 隐藏光标并捕获
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
    }

    /**
     * 打印调试信息
     */
    private void printDebugInfo() {
        System.out.println("OpenGL version: " + glGetString(GL_VERSION));
        System.out.println("Stars count: " + stars.size());
    }

    /**
     * 主渲染循环
     */
    private void loop() {
        double lastTime = glfwGetTime();
        int frameCount = 0;

        while (!glfwWindowShouldClose(window)) {
            double currentTime = glfwGetTime();
            float deltaTime = (float) (currentTime - lastTime);
            lastTime = currentTime;

            // 计算并显示FPS
            frameCount++;
            if (frameCount % 60 == 0) {
                System.out.printf("FPS: %.1f, Camera: (%.1f, %.1f, %.1f)%n",
                        1.0 / deltaTime, camera.getPosition().x, camera.getPosition().y, camera.getPosition().z);
            }

            // 处理输入和渲染
            processInput(deltaTime);
            render();

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    /**
     * 渲染场景
     */
    private void render() {
        // 清除颜色和深度缓冲区
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        // 使用着色器程序
        shaderProgram.bind();

        // 设置视图和投影矩阵
        setupCameraMatrices();

        // 设置着色器uniform变量
        setupShaderUniforms();

        // 渲染黑洞（主要效果）
        renderBlackHole();

        // 渲染星空背景
        renderStars();

        // 解绑着色器
        shaderProgram.unbind();
    }

    /**
     * 设置相机矩阵
     */
    private void setupCameraMatrices() {
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = new Matrix4f().perspective(
                (float) Math.toRadians(camera.getZoom()),
                (float) WIDTH / HEIGHT,
                0.1f,
                1000.0f
        );

        shaderProgram.setMat4("view", viewMatrix);
        shaderProgram.setMat4("projection", projectionMatrix);
    }

    /**
     * 设置着色器uniform变量
     */
    private void setupShaderUniforms() {
        // 设置相机位置
        shaderProgram.setVec3("cameraPos", camera.getPosition());
        shaderProgram.setFloat("time", (float) glfwGetTime());

        // 设置黑洞物理参数
        shaderProgram.setVec3("blackHolePos", 0.0f, 0.0f, 0.0f);  // 黑洞位置在原点
        shaderProgram.setFloat("blackHoleMass", 4.0f);            // 黑洞质量
        shaderProgram.setFloat("eventHorizonRadius", 1.2f);       // 事件视界半径
        shaderProgram.setFloat("innerDiskRadius", 2.0f);          // 吸积盘内半径
        shaderProgram.setFloat("outerDiskRadius", 6.0f);          // 吸积盘外半径
    }

    /**
     * 渲染黑洞效果
     */
    private void renderBlackHole() {
        // 设置模型矩阵 - 放大球体以覆盖更多屏幕空间
        Matrix4f model = new Matrix4f().identity().scale(8.0f);
        shaderProgram.setMat4("model", model);

        // 渲染球体（黑洞效果在片段着色器中计算）
        sphere.render();
    }

    /**
     * 渲染星空背景
     */
    private void renderStars() {
        glBindVertexArray(starVAO);

        // 设置模型矩阵为单位矩阵
        Matrix4f model = new Matrix4f().identity();
        shaderProgram.setMat4("model", model);

        // 设置点大小并绘制
        glPointSize(2.0f);
        glDrawArrays(GL_POINTS, 0, stars.size());

        glBindVertexArray(0);
    }

    /**
     * 处理键盘输入
     */
    private void processInput(float deltaTime) {
        // ESC键退出
        if (glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS) {
            glfwSetWindowShouldClose(window, true);
        }

        // 相机移动控制
        handleCameraMovement(deltaTime);

        // R键重置相机
        if (glfwGetKey(window, GLFW_KEY_R) == GLFW_PRESS) {
            camera.reset();
        }
    }

    /**
     * 处理相机移动输入
     */
    private void handleCameraMovement(float deltaTime) {
        if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) {
            camera.processKeyboard(Camera.Movement.FORWARD, deltaTime);
        }
        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) {
            camera.processKeyboard(Camera.Movement.BACKWARD, deltaTime);
        }
        if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) {
            camera.processKeyboard(Camera.Movement.LEFT, deltaTime);
        }
        if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) {
            camera.processKeyboard(Camera.Movement.RIGHT, deltaTime);
        }
        if (glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS) {
            camera.processKeyboard(Camera.Movement.UP, deltaTime);
        }
        if (glfwGetKey(window, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS) {
            camera.processKeyboard(Camera.Movement.DOWN, deltaTime);
        }
    }

    /**
     * 鼠标位置回调函数
     */
    private void cursorPosCallback(long window, double xpos, double ypos) {
        if (firstMouse) {
            lastX = xpos;
            lastY = ypos;
            firstMouse = false;
        }

        float xOffset = (float) (xpos - lastX);
        float yOffset = (float) (lastY - ypos); // 反转Y坐标

        lastX = xpos;
        lastY = ypos;

        // 只有按下左键时才处理鼠标移动
        if (glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS) {
            camera.processMouseMovement(xOffset, yOffset);
        }
    }

    /**
     * 鼠标滚轮回调函数
     */
    private void scrollCallback(long window, double xoffset, double yoffset) {
        camera.processMouseScroll((float) yoffset);
    }

    /**
     * 窗口大小改变回调函数
     */
    private void framebufferSizeCallback(long window, int width, int height) {
        glViewport(0, 0, width, height);
    }

    /**
     * 键盘回调函数
     */
    private void keyCallback(long window, int key, int scancode, int action, int mods) {
        if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
            glfwSetWindowShouldClose(window, true);
        }
    }

    /**
     * 清理资源
     */
    private void cleanup() {
        // 释放窗口回调并销毁窗口
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // 删除OpenGL对象
        deleteOpenGLObjects();

        // 终止GLFW
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    /**
     * 删除OpenGL对象
     */
    private void deleteOpenGLObjects() {
        if (starVAO != 0) {
            glDeleteVertexArrays(starVAO);
        }
        if (starVBO != 0) {
            glDeleteBuffers(starVBO);
        }

        // 清理着色器程序
        if (shaderProgram != null) {
            shaderProgram.cleanup();
        }
    }
}
