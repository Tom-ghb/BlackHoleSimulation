#version 330 core
out vec4 FragColor;

in vec3 FragPos;
in vec3 Color;

uniform vec3 cameraPos;
uniform float time;
uniform vec3 blackHolePos;
uniform float blackHoleMass;
uniform float eventHorizonRadius;
uniform float innerDiskRadius;
uniform float outerDiskRadius;

/**
 * 哈希函数 - 用于生成伪随机数
 */
float hash(vec2 p) {
    return fract(1e4 * sin(17.0 * p.x + p.y * 0.1) * (0.1 + abs(sin(p.y * 13.0 + p.x))));
}

/**
 * 2D噪声函数 - 基于哈希生成连续噪声
 */
float noise(vec2 x) {
    vec2 i = floor(x);
    vec2 f = fract(x);
    f = f * f * (3.0 - 2.0 * f);

    float a = hash(i);
    float b = hash(i + vec2(1.0, 0.0));
    float c = hash(i + vec2(0.0, 1.0));
    float d = hash(i + vec2(1.0, 1.0));

    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

/**
 * 分形布朗运动 - 用于生成湍流纹理
 */
float fbm(vec2 p) {
    float value = 0.0;
    float amplitude = 0.5;
    float frequency = 1.0;

    for (int i = 0; i < 4; i++) {
        value += amplitude * noise(frequency * p);
        amplitude *= 0.5;
        frequency *= 2.0;
    }
    return value;
}

void main() {
    // 如果是星星或基础几何体，直接使用传入的颜色
    if (length(Color) > 0.1) {
        FragColor = vec4(Color, 1.0);
        return;
    }

    vec3 rayOrigin = cameraPos;
    vec3 rayDir = normalize(FragPos - cameraPos);

    vec3 currentPos = rayOrigin;
    vec3 finalColor = vec3(0); // 更暗的背景

    bool hitSomething = false;

    // 光线步进循环 - 模拟光线在黑洞引力场中的传播
    for (int i = 0; i < 200; i++) {
        vec3 toBlackHole = blackHolePos - currentPos;
        float distToBlackHole = length(toBlackHole);

        // 事件视界检测 - 如果光线进入事件视界，显示纯黑
        if (distToBlackHole < eventHorizonRadius) {
            finalColor = vec3(0.0);
            hitSomething = true;
            break;
        }

        // 吸积盘检测 - 更薄的吸积盘
        float diskRadius = length(currentPos.xz);
        if (diskRadius > innerDiskRadius && diskRadius < outerDiskRadius) {
            float height = abs(currentPos.y);

            // 吸积盘厚度参数
            float baseThickness = 0.08; // 基础厚度
            float dynamicThickness = 0.03 * sin(diskRadius * 2.0 - time * 1.5);
            float diskThickness = baseThickness + dynamicThickness * dynamicThickness;

            // 检查是否在吸积盘内部
            if (height < diskThickness) {
                // 计算吸积盘颜色 - 基于温度梯度
                float t = (diskRadius - innerDiskRadius) / (outerDiskRadius - innerDiskRadius);
                t = smoothstep(0.0, 1.0, t);

                // 温度梯度颜色
                vec3 innerColor = vec3(1.0, 1.0, 0.9); // 高温：偏白/蓝白
                vec3 middleColor = vec3(1.0, 0.7, 0.3); // 中温：橙黄
                vec3 outerColor = vec3(0.8, 0.2, 0.1);  // 低温：深红

                vec3 diskColor;
                if (t < 0.6) {
                    diskColor = mix(innerColor, middleColor, t / 0.3);
                } else {
                    diskColor = mix(middleColor, outerColor, (t - 0.3) / 0.7);
                }

                // 湍流效果 - 使用FBM生成动态纹理
                vec2 uv = vec2(atan(currentPos.z, currentPos.x) / (2.0 * 3.14159), diskRadius * 0.5);
                float turbulence = fbm(uv * 6.0 + time * 0.5) * 0.3 +
                                  fbm(uv * 12.0 - time * 0.8) * 0.15;

                turbulence = clamp(turbulence, -0.2, 0.2);
                diskColor *= 3.9 + 3.2 * turbulence;

                // 高度衰减 - 边缘羽化
                float heightFactor = 1.0 - smoothstep(0.0, diskThickness * 0.8, height);
                diskColor *= heightFactor;

                // 径向亮度衰减
                float radialAttenuation = 1.0 - t * 0.3;
                diskColor *= radialAttenuation;

                finalColor = diskColor;
                hitSomething = true;
                break;
            }
        }

        // 引力透镜效应 - 模拟黑洞对光线的弯曲
        float gravityStrength = blackHoleMass / (distToBlackHole * distToBlackHole + 0.1);
        vec3 gravityDir = normalize(toBlackHole);

        float bendFactor = min(gravityStrength * 0.12, 0.25);
        rayDir = normalize(rayDir + gravityDir * bendFactor);

        // 自适应步长 - 离黑洞越近，步长越小
        float stepSize = mix(0.05, 0.3, smoothstep(0.0, 3.0, distToBlackHole));
        currentPos += rayDir * stepSize;

        // 距离限制 - 防止无限循环
        if (length(currentPos - rayOrigin) > 200.0) {
            // 星空背景 - 检查是否在黑洞方向
            vec3 toBlackHoleDir = normalize(blackHolePos - rayOrigin);
            float dotProduct = dot(rayDir, toBlackHoleDir);

            // 如果射线方向指向黑洞区域，显示纯黑，否则显示星星
            if (dotProduct > 0.98) {
                finalColor = vec3(0.0);
            } else {
                vec2 uv = gl_FragCoord.xy / vec2(1200.0, 800.0);
                float starValue = hash(uv * 200.0 + time * 0.05);
                if (starValue > 0.998) {
                    float starBrightness = hash(uv + 1.0);
                    finalColor = vec3(0.9 + 0.1 * starBrightness);
                }
            }
            break;
        }
    }

    // 色调映射 - 调整最终颜色输出
    finalColor = pow(finalColor, vec3(0.85));
    FragColor = vec4(finalColor, 1.0);
}
