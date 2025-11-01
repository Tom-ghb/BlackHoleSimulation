#version 330 core
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aColor;

out vec3 FragPos;
out vec3 Color;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

/**
 * 顶点着色器主函数
 * 将顶点位置变换到裁剪空间，并传递位置和颜色信息
 */
void main()
{
    // 计算世界空间中的片段位置
    FragPos = vec3(model * vec4(aPos, 1.0));

    // 变换到裁剪空间
    gl_Position = projection * view * vec4(FragPos, 1.0);

    // 传递颜色信息到片段着色器
    Color = aColor;
}
