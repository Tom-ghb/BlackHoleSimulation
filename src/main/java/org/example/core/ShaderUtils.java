package org.example.core;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 着色器工具类
 * 提供着色器源码加载功能
 */
public class ShaderUtils {

    /**
     * 从资源文件加载着色器源码
     *
     * @param filePath 资源文件路径
     * @return 着色器源码字符串
     */
    public static String loadShaderSource(String filePath) {
        StringBuilder shaderSource = new StringBuilder();

        try (InputStream inputStream = ShaderUtils.class.getResourceAsStream(filePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            String line;
            while ((line = reader.readLine()) != null) {
                shaderSource.append(line).append("\n");
            }
        } catch (Exception e) {
            System.err.println("Error loading shader: " + filePath);
            e.printStackTrace();
        }

        return shaderSource.toString();
    }
}
