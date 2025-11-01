package org.example.simulation;

import org.joml.Vector3f;

/**
 * 黑洞物理模拟类
 * 管理黑洞的物理属性和状态
 */
public class BlackHole {
    private Vector3f position;
    private float mass;
    private float eventHorizonRadius;
    private float accretionDiskInnerRadius;
    private float accretionDiskOuterRadius;

    // 物理常数
    public static final float GRAVITATIONAL_CONSTANT = 6.67430e-11f;
    public static final float SPEED_OF_LIGHT = 299792458.0f;

    /**
     * 构造函数 - 初始化黑洞参数
     */
    public BlackHole() {
        this.position = new Vector3f(0.0f, 0.0f, 0.0f);
        this.mass = 4.0f; // 默认质量
        updateDerivedProperties();
    }

    /**
     * 构造函数 - 自定义参数
     */
    public BlackHole(Vector3f position, float mass) {
        this.position = new Vector3f(position);
        this.mass = mass;
        updateDerivedProperties();
    }

    /**
     * 更新派生属性（基于质量计算）
     */
    private void updateDerivedProperties() {
        // 计算史瓦西半径（事件视界）
        this.eventHorizonRadius = calculateSchwarzschildRadius(mass);

        // 计算吸积盘范围（基于ISCO - Innermost Stable Circular Orbit）
        this.accretionDiskInnerRadius = eventHorizonRadius * 3.0f; // 3倍史瓦西半径
        this.accretionDiskOuterRadius = eventHorizonRadius * 8.0f; // 8倍史瓦西半径
    }

    /**
     * 计算史瓦西半径
     * R_s = 2GM/c^2
     */
    private float calculateSchwarzschildRadius(float mass) {
        return (2.0f * GRAVITATIONAL_CONSTANT * mass) / (SPEED_OF_LIGHT * SPEED_OF_LIGHT);
    }

    /**
     * 计算引力强度在给定位置
     */
    public float calculateGravityStrength(Vector3f point) {
        Vector3f toBlackHole = new Vector3f(position).sub(point);
        float distance = toBlackHole.length();

        // 避免除零
        if (distance < 0.001f) {
            return Float.MAX_VALUE;
        }

        // 牛顿引力公式：F = G * M / r^2
        return (GRAVITATIONAL_CONSTANT * mass) / (distance * distance);
    }

    /**
     * 计算引力方向
     */
    public Vector3f calculateGravityDirection(Vector3f point) {
        Vector3f direction = new Vector3f(position).sub(point);
        return direction.normalize();
    }

    /**
     * 检查点是否在事件视界内
     */
    public boolean isInsideEventHorizon(Vector3f point) {
        float distance = new Vector3f(position).sub(point).length();
        return distance < eventHorizonRadius;
    }

    /**
     * 检查点是否在吸积盘内
     */
    public boolean isInsideAccretionDisk(Vector3f point) {
        // 转换为黑洞为中心的坐标
        Vector3f relativePos = new Vector3f(point).sub(position);

        // 计算径向距离（忽略Y轴）
        float radialDistance = (float) Math.sqrt(relativePos.x * relativePos.x + relativePos.z * relativePos.z);

        // 检查高度（吸积盘很薄）
        float height = Math.abs(relativePos.y);
        float diskThickness = 0.1f * accretionDiskInnerRadius;

        return radialDistance >= accretionDiskInnerRadius &&
                radialDistance <= accretionDiskOuterRadius &&
                height <= diskThickness;
    }

    /**
     * 计算吸积盘温度（基于距离）
     */
    public float calculateDiskTemperature(float radialDistance) {
        // 温度随距离增加而降低
        float normalizedDistance = (radialDistance - accretionDiskInnerRadius) /
                (accretionDiskOuterRadius - accretionDiskInnerRadius);

        // 温度从内到外：10000K -> 3000K
        return 10000.0f - normalizedDistance * 7000.0f;
    }

    /**
     * 计算引力红移因子
     */
    public float calculateRedshiftFactor(Vector3f observerPos, Vector3f sourcePos) {
        float r_observer = new Vector3f(observerPos).sub(position).length();
        float r_source = new Vector3f(sourcePos).sub(position).length();

        // 简化的红移计算
        return (float) Math.sqrt((1 - eventHorizonRadius / r_observer) /
                (1 - eventHorizonRadius / r_source));
    }

    public Vector3f getPosition() {
        return new Vector3f(position);
    }

    public void setPosition(Vector3f position) {
        this.position.set(position);
    }

    public float getMass() {
        return mass;
    }

    public void setMass(float mass) {
        this.mass = mass;
        updateDerivedProperties();
    }

    public float getEventHorizonRadius() {
        return eventHorizonRadius;
    }

    public float getAccretionDiskInnerRadius() {
        return accretionDiskInnerRadius;
    }

    public float getAccretionDiskOuterRadius() {
        return accretionDiskOuterRadius;
    }

    /**
     * 获取黑洞状态信息
     */
    public String getStatusInfo() {
        return String.format("Black Hole - Mass: %.2f, Event Horizon: %.2f, Disk: [%.2f - %.2f]",
                mass, eventHorizonRadius, accretionDiskInnerRadius, accretionDiskOuterRadius);
    }
}
