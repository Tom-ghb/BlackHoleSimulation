package org.example.core;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL30.*;

/**
 * 着色器程序管理类
 * 负责着色器的编译、链接和uniform变量设置
 */
public class ShaderProgram {
    private final int programId;
    private int vertexShaderId;
    private int fragmentShaderId;
    private final Map<String, Integer> uniforms;

    /**
     * 构造函数 - 创建着色器程序
     */
    public ShaderProgram() throws Exception {
        programId = glCreateProgram();
        if (programId == 0) {
            throw new Exception("Could not create Shader Program");
        }
        uniforms = new HashMap<>();
    }

    /**
     * 创建顶点着色器
     */
    public void createVertexShader(String shaderCode) throws Exception {
        vertexShaderId = createShader(shaderCode, GL_VERTEX_SHADER);
    }

    /**
     * 创建片段着色器
     */
    public void createFragmentShader(String shaderCode) throws Exception {
        fragmentShaderId = createShader(shaderCode, GL_FRAGMENT_SHADER);
    }

    /**
     * 创建着色器对象
     */
    private int createShader(String shaderCode, int shaderType) throws Exception {
        int shaderId = glCreateShader(shaderType);
        if (shaderId == 0) {
            throw new Exception("Error creating shader. Type: " + shaderType);
        }

        glShaderSource(shaderId, shaderCode);
        glCompileShader(shaderId);

        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == 0) {
            throw new Exception("Error compiling Shader: " + glGetShaderInfoLog(shaderId, 1024));
        }

        glAttachShader(programId, shaderId);
        return shaderId;
    }

    /**
     * 链接着色器程序
     */
    public void link() throws Exception {
        glLinkProgram(programId);
        if (glGetProgrami(programId, GL_LINK_STATUS) == 0) {
            throw new Exception("Error linking Shader program: " + glGetProgramInfoLog(programId, 1024));
        }

        // 分离着色器（链接后不再需要）
        if (vertexShaderId != 0) {
            glDetachShader(programId, vertexShaderId);
        }
        if (fragmentShaderId != 0) {
            glDetachShader(programId, fragmentShaderId);
        }

        // 验证程序
        glValidateProgram(programId);
        if (glGetProgrami(programId, GL_VALIDATE_STATUS) == 0) {
            System.err.println("Warning validating Shader code: " + glGetProgramInfoLog(programId, 1024));
        }
    }

    /**
     * 绑定着色器程序
     */
    public void bind() {
        glUseProgram(programId);
    }

    /**
     * 解绑着色器程序
     */
    public void unbind() {
        glUseProgram(0);
    }

    /**
     * 清理资源
     */
    public void cleanup() {
        unbind();
        if (programId != 0) {
            glDeleteProgram(programId);
        }
    }

    /**
     * 获取uniform位置（带缓存）
     */
    private int getUniformLocation(String uniformName) {
        return uniforms.computeIfAbsent(uniformName,
                name -> glGetUniformLocation(programId, name));
    }

    // ========== Uniform设置方法 ==========

    /**
     * 设置4x4矩阵uniform
     */
    public void setMat4(String uniformName, Matrix4f value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer fb = stack.mallocFloat(16);
            value.get(fb);
            glUniformMatrix4fv(getUniformLocation(uniformName), false, fb);
        }
    }

    /**
     * 设置3x3矩阵uniform
     */
    public void setMat3(String uniformName, Matrix3f value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer fb = stack.mallocFloat(9);
            value.get(fb);
            glUniformMatrix3fv(getUniformLocation(uniformName), false, fb);
        }
    }

    /**
     * 设置3维向量uniform
     */
    public void setVec3(String uniformName, Vector3f value) {
        glUniform3f(getUniformLocation(uniformName), value.x, value.y, value.z);
    }

    /**
     * 设置3维向量uniform（分量形式）
     */
    public void setVec3(String uniformName, float x, float y, float z) {
        glUniform3f(getUniformLocation(uniformName), x, y, z);
    }

    /**
     * 设置4维向量uniform
     */
    public void setVec4(String uniformName, Vector4f value) {
        glUniform4f(getUniformLocation(uniformName), value.x, value.y, value.z, value.w);
    }

    /**
     * 设置浮点数uniform
     */
    public void setFloat(String uniformName, float value) {
        glUniform1f(getUniformLocation(uniformName), value);
    }

    /**
     * 设置整数uniform
     */
    public void setInt(String uniformName, int value) {
        glUniform1i(getUniformLocation(uniformName), value);
    }

    /**
     * 设置布尔值uniform
     */
    public void setBool(String uniformName, boolean value) {
        glUniform1i(getUniformLocation(uniformName), value ? 1 : 0);
    }
}
