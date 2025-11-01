# 3D 黑洞模拟

一个基于物理的实时3D黑洞模拟程序，使用OpenGL和光线步进技术实现引力透镜效应和吸积盘渲染。

## 特性

### 核心物理效果
- 引力透镜效应 - 模拟黑洞对周围光线的弯曲
- 事件视界 - 真实的史瓦西半径计算
- 吸积盘渲染 - 基于温度梯度的动态吸积盘
- 多普勒效应 - 考虑相对论效应的颜色变化
- 湍流模拟 - 使用FBM噪声的吸积盘湍流

### 交互功能
- 自由相机控制 - WASD移动，鼠标视角控制
- 实时参数调整 - 可调节黑洞质量和吸积盘参数
- 抗锯齿渲染 - 支持MSAA多重采样抗锯齿
- 性能监控 - 实时FPS和相机位置显示

## 快速开始

### 系统要求
- Java 8
- OpenGL 3.3 兼容显卡
- 至少 2GB 可用内存
- 支持 macOS (ARM64/x64), Windows, Linux

### 构建和运行

1. 克隆项目
   git clone https://github.com/pann-code/BlackHoleSimulation.git
   cd BlackHoleSimulation

2. 使用Maven构建
   mvn clean install package

3. 运行程序
   java -XstartOnFirstThread -jar ./target/BlackHoleSimulation-1.0-SNAPSHOT.jar
   注意：在 macOS 上运行时必须添加 -XstartOnFirstThread 参数

## 操作指南

### 相机控制
- WASD - 前后左右移动
- 空格/Shift - 上下移动
- 鼠标左键拖拽 - 旋转视角
- 鼠标滚轮 - 缩放视野
- R键 - 重置相机位置

### 视角建议
- 从侧面观察吸积盘的最佳效果
- 靠近黑洞观察引力透镜效应
- 从上方俯瞰完整的吸积盘结构

## 项目结构

BlackHoleSimulation/
├── src/main/java/org/example/
│   ├── BlackHoleSimulation.java      # 主程序入口
│   ├── core/
│   │   ├── Camera.java               # 相机控制系统
│   │   ├── ShaderProgram.java        # 着色器管理
│   │   ├── Sphere.java               # 球体几何体
│   │   └── ShaderUtils.java          # 着色器工具
│   └── simulation/
│       ├── BlackHole.java            # 黑洞物理模拟
│       └── AccretionDisk.java        # 吸积盘模拟
├── src/main/resources/
│   └── shaders/
│       ├── vertex_shader.glsl        # 顶点着色器
│       └── fragment_shader.glsl      # 片段着色器
├── pom.xml                           # Maven配置
└── README.md                         # 项目说明

## 技术细节

### 渲染技术
- 光线步进 (Ray Marching) - 在片段着色器中实现
- 分形布朗运动 (FBM) - 用于吸积盘湍流纹理
- 多重采样抗锯齿 (MSAA) - 提升视觉质量
- 色调映射 - 优化颜色输出

### 物理模型
- 牛顿引力 - 用于光线弯曲计算
- 史瓦西度规 - 简化的事件视界模型
- 开普勒运动 - 吸积盘粒子轨道
- 黑体辐射 - 温度-颜色映射

### 着色器Uniforms
uniform vec3 cameraPos;           // 相机位置
uniform float time;               // 运行时间
uniform vec3 blackHolePos;        // 黑洞位置
uniform float blackHoleMass;      // 黑洞质量
uniform float eventHorizonRadius; // 事件视界半径
uniform float innerDiskRadius;    // 吸积盘内半径
uniform float outerDiskRadius;    // 吸积盘外半径

## 配置参数

### 黑洞参数（可在代码中调整）
在 BlackHoleSimulation.java 中修改
shaderProgram.setFloat("blackHoleMass", 4.0f);           // 黑洞质量
shaderProgram.setFloat("eventHorizonRadius", 1.2f);      // 事件视界半径
shaderProgram.setFloat("innerDiskRadius", 2.5f);         // 吸积盘内半径
shaderProgram.setFloat("outerDiskRadius", 8.0f);         // 吸积盘外半径