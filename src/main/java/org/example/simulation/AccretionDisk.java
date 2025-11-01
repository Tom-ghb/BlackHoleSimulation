package org.example.simulation;

import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 吸积盘模拟类
 * 管理吸积盘的几何和物理特性
 */
public class AccretionDisk {
    private BlackHole blackHole;
    private List<Vector3f> particles;
    private List<Float> temperatures;
    private List<Float> velocities;

    private int particleCount;
    private float thickness;
    private float turbulenceStrength;

    private Random random;

    /**
     * 构造函数 - 初始化吸积盘
     */
    public AccretionDisk(BlackHole blackHole, int particleCount) {
        this.blackHole = blackHole;
        this.particleCount = particleCount;
        this.thickness = 0.1f;
        this.turbulenceStrength = 0.3f;
        this.random = new Random();

        this.particles = new ArrayList<>();
        this.temperatures = new ArrayList<>();
        this.velocities = new ArrayList();

        initializeDisk();
    }

    /**
     * 初始化吸积盘粒子
     */
    private void initializeDisk() {
        float innerRadius = blackHole.getAccretionDiskInnerRadius();
        float outerRadius = blackHole.getAccretionDiskOuterRadius();

        for (int i = 0; i < particleCount; i++) {
            // 随机角度和半径
            float angle = random.nextFloat() * (float) (2 * Math.PI);
            float radius = innerRadius + random.nextFloat() * (outerRadius - innerRadius);

            // 随机高度（在厚度范围内）
            float height = (random.nextFloat() - 0.5f) * thickness;

            // 计算位置
            float x = (float) (Math.cos(angle) * radius);
            float z = (float) (Math.sin(angle) * radius);
            Vector3f position = new Vector3f(x, height, z);

            particles.add(position);

            // 计算温度（内热外冷）
            float temperature = blackHole.calculateDiskTemperature(radius);
            temperatures.add(temperature);

            // 计算轨道速度（开普勒速度）
            float orbitalVelocity = calculateOrbitalVelocity(radius);
            velocities.add(orbitalVelocity);
        }
    }

    /**
     * 计算开普勒轨道速度
     * v = sqrt(GM/r)
     */
    private float calculateOrbitalVelocity(float radius) {
        return (float) Math.sqrt(BlackHole.GRAVITATIONAL_CONSTANT * blackHole.getMass() / radius);
    }

    /**
     * 更新吸积盘状态（随时间演化）
     */
    public void update(float deltaTime) {
        for (int i = 0; i < particles.size(); i++) {
            Vector3f particle = particles.get(i);
            float radius = (float) Math.sqrt(particle.x * particle.x + particle.z * particle.z);
            float orbitalVelocity = velocities.get(i);

            // 计算角度增量
            float angleIncrement = orbitalVelocity * deltaTime / radius;

            // 更新位置（绕Y轴旋转）
            float currentAngle = (float) Math.atan2(particle.z, particle.x);
            float newAngle = currentAngle + angleIncrement;

            particle.x = (float) (Math.cos(newAngle) * radius);
            particle.z = (float) (Math.sin(newAngle) * radius);

            // 添加湍流效果
            addTurbulence(particle, deltaTime);

            // 更新温度（简单的冷却模型）
            updateTemperature(i, deltaTime);
        }
    }

    /**
     * 添加湍流效果
     */
    private void addTurbulence(Vector3f particle, float deltaTime) {
        float turbulenceX = (random.nextFloat() - 0.5f) * turbulenceStrength * deltaTime;
        float turbulenceY = (random.nextFloat() - 0.5f) * turbulenceStrength * deltaTime * 0.1f; // 垂直方向湍流较弱
        float turbulenceZ = (random.nextFloat() - 0.5f) * turbulenceStrength * deltaTime;

        particle.add(turbulenceX, turbulenceY, turbulenceZ);
    }

    /**
     * 更新粒子温度
     */
    private void updateTemperature(int particleIndex, float deltaTime) {
        float currentTemp = temperatures.get(particleIndex);
        Vector3f particle = particles.get(particleIndex);
        float radius = (float) Math.sqrt(particle.x * particle.x + particle.z * particle.z);

        // 目标温度（基于距离）
        float targetTemp = blackHole.calculateDiskTemperature(radius);

        // 向目标温度渐变
        float newTemp = currentTemp + (targetTemp - currentTemp) * deltaTime * 0.5f;
        temperatures.set(particleIndex, newTemp);
    }

    /**
     * 根据温度获取颜色
     */
    public Vector3f getColorForTemperature(float temperature) {
        // 温度到颜色的映射
        if (temperature > 8000.0f) {
            // 高温：蓝白色
            return new Vector3f(0.9f, 0.9f, 1.0f);
        } else if (temperature > 6000.0f) {
            // 中高温：白色
            return new Vector3f(1.0f, 1.0f, 0.9f);
        } else if (temperature > 4000.0f) {
            // 中温：黄色
            return new Vector3f(1.0f, 0.8f, 0.3f);
        } else if (temperature > 3000.0f) {
            // 中低温：橙色
            return new Vector3f(1.0f, 0.6f, 0.2f);
        } else {
            // 低温：红色
            return new Vector3f(0.8f, 0.2f, 0.1f);
        }
    }

    /**
     * 获取吸积盘亮度（基于温度）
     */
    public float getBrightnessForTemperature(float temperature) {
        // 斯蒂芬-玻尔兹曼定律：亮度 ∝ T^4
        float normalizedTemp = temperature / 10000.0f;
        return (float) Math.pow(normalizedTemp, 4.0f);
    }

    // ========== Getter方法 ==========

    public List<Vector3f> getParticles() {
        return new ArrayList<>(particles);
    }

    public List<Float> getTemperatures() {
        return new ArrayList<>(temperatures);
    }

    public List<Vector3f> getParticleColors() {
        List<Vector3f> colors = new ArrayList<>();
        for (int i = 0; i < temperatures.size(); i++) {
            Vector3f color = getColorForTemperature(temperatures.get(i));
            float brightness = getBrightnessForTemperature(temperatures.get(i));
            colors.add(new Vector3f(color).mul(brightness));
        }
        return colors;
    }

    public int getParticleCount() {
        return particleCount;
    }

    public float getThickness() {
        return thickness;
    }

    public void setThickness(float thickness) {
        this.thickness = thickness;
    }

    public float getTurbulenceStrength() {
        return turbulenceStrength;
    }

    public void setTurbulenceStrength(float turbulenceStrength) {
        this.turbulenceStrength = turbulenceStrength;
    }

    /**
     * 获取吸积盘状态信息
     */
    public String getStatusInfo() {
        float avgTemp = (float) temperatures.stream().mapToDouble(Float::doubleValue).average().orElse(0.0);
        return String.format("Accretion Disk - Particles: %d, Avg Temp: %.0fK, Thickness: %.3f",
                particleCount, avgTemp, thickness);
    }

    /**
     * 重置吸积盘
     */
    public void reset() {
        particles.clear();
        temperatures.clear();
        velocities.clear();
        initializeDisk();
    }

    /**
     * 添加新粒子到吸积盘
     */
    public void addParticle(Vector3f position) {
        particles.add(new Vector3f(position));

        float radius = (float) Math.sqrt(position.x * position.x + position.z * position.z);
        float temperature = blackHole.calculateDiskTemperature(radius);
        temperatures.add(temperature);

        float orbitalVelocity = calculateOrbitalVelocity(radius);
        velocities.add(orbitalVelocity);
    }

    /**
     * 移除指定索引的粒子
     */
    public void removeParticle(int index) {
        if (index >= 0 && index < particles.size()) {
            particles.remove(index);
            temperatures.remove(index);
            velocities.remove(index);
        }
    }
}
