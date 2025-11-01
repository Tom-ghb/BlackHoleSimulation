package org.example.core;

import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL33.*;

/**
 * 球体几何体类
 * 使用三角形带生成球体网格
 */
public class Sphere {
    private int vao;
    private int vbo;
    private int vertexCount;

    /**
     * 构造函数 - 生成球体网格
     *
     * @param segments 球体分段数（越高越平滑）
     */
    public Sphere(int segments) {
        List<Float> vertices = generateSphereVertices(segments);
        setupBuffers(vertices);
    }

    /**
     * 生成球体顶点数据
     */
    private List<Float> generateSphereVertices(int segments) {
        List<Float> vertices = new ArrayList<>();

        for (int i = 0; i <= segments; i++) {
            double lat0 = Math.PI * (-0.5 + (double) (i - 1) / segments);
            double z0 = Math.sin(lat0);
            double zr0 = Math.cos(lat0);

            double lat1 = Math.PI * (-0.5 + (double) i / segments);
            double z1 = Math.sin(lat1);
            double zr1 = Math.cos(lat1);

            for (int j = 0; j <= segments; j++) {
                double lng = 2 * Math.PI * (double) (j - 1) / segments;
                double x = Math.cos(lng);
                double y = Math.sin(lng);

                // 第一个顶点
                vertices.add((float) (x * zr0));
                vertices.add((float) (y * zr0));
                vertices.add((float) z0);

                // 第二个顶点
                vertices.add((float) (x * zr1));
                vertices.add((float) (y * zr1));
                vertices.add((float) z1);
            }
        }

        vertexCount = vertices.size() / 3;
        return vertices;
    }

    /**
     * 设置OpenGL缓冲区
     */
    private void setupBuffers(List<Float> vertices) {
        // 生成VAO和VBO
        vao = glGenVertexArrays();
        vbo = glGenBuffers();

        // 绑定VAO
        glBindVertexArray(vao);

        // 绑定VBO并上传数据
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertices.size());
        for (Float f : vertices) vertexBuffer.put(f);
        vertexBuffer.flip();
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

        // 设置顶点属性指针
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        // 解绑
        glBindVertexArray(0);
    }

    /**
     * 渲染球体
     */
    public void render() {
        glBindVertexArray(vao);
        glDrawArrays(GL_TRIANGLE_STRIP, 0, vertexCount);
        glBindVertexArray(0);
    }

    /**
     * 清理资源
     */
    public void cleanup() {
        if (vao != 0) glDeleteVertexArrays(vao);
        if (vbo != 0) glDeleteBuffers(vbo);
    }
}
