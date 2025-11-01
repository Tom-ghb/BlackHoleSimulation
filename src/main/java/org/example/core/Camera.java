package org.example.core;

import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * 第一人称相机类
 * 支持自由移动和视角控制
 */
public class Camera {
    private Vector3f position;
    private Vector3f front;
    private Vector3f up;
    private Vector3f right;
    private Vector3f worldUp;

    private float yaw;
    private float pitch;
    private float movementSpeed;
    private float mouseSensitivity;
    private float zoom;

    /**
     * 相机移动方向枚举
     */
    public enum Movement {
        FORWARD,
        BACKWARD,
        LEFT,
        RIGHT,
        UP,
        DOWN
    }

    /**
     * 构造函数 - 初始化相机参数
     */
    public Camera() {
        position = new Vector3f(0.0f, 2.0f, 12.0f);  // 初始位置，提高视角
        worldUp = new Vector3f(0.0f, 1.0f, 0.0f);
        yaw = -90.0f;    // 朝向负Z轴
        pitch = -15.0f;  // 稍微向下看
        movementSpeed = 8.0f;
        mouseSensitivity = 0.1f;
        zoom = 45.0f;

        updateCameraVectors();
    }

    /**
     * 获取视图矩阵
     */
    public Matrix4f getViewMatrix() {
        Vector3f target = new Vector3f(position).add(front);
        return new Matrix4f().lookAt(position, target, up);
    }

    /**
     * 处理键盘输入移动相机
     */
    public void processKeyboard(Movement direction, float deltaTime) {
        float velocity = movementSpeed * deltaTime;
        Vector3f moveDir = new Vector3f();

        switch (direction) {
            case FORWARD:
                moveDir.set(front);
                break;
            case BACKWARD:
                moveDir.set(front).negate();
                break;
            case LEFT:
                moveDir.set(right).negate();
                break;
            case RIGHT:
                moveDir.set(right);
                break;
            case UP:
                moveDir.set(worldUp);
                break;
            case DOWN:
                moveDir.set(worldUp).negate();
                break;
        }

        // 移除Y分量（除了上下移动）
        if (direction != Movement.UP && direction != Movement.DOWN) {
            moveDir.y = 0;
        }

        // 标准化移动方向并应用速度
        if (moveDir.length() > 0) {
            moveDir.normalize();
        }

        position.add(moveDir.mul(velocity));
    }

    /**
     * 处理鼠标移动
     */
    public void processMouseMovement(float xoffset, float yoffset) {
        xoffset *= mouseSensitivity;
        yoffset *= mouseSensitivity;

        yaw += xoffset;
        pitch += yoffset;

        // 限制俯仰角范围
        if (pitch > 89.0f) pitch = 89.0f;
        if (pitch < -89.0f) pitch = -89.0f;

        updateCameraVectors();
    }

    /**
     * 处理鼠标滚轮缩放
     */
    public void processMouseScroll(float yoffset) {
        zoom -= yoffset;
        if (zoom < 1.0f) zoom = 1.0f;
        if (zoom > 45.0f) zoom = 45.0f;
    }

    /**
     * 更新相机向量（前向量、右向量、上向量）
     */
    private void updateCameraVectors() {
        // 计算新的前向量
        Vector3f newFront = new Vector3f();
        newFront.x = (float) (Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
        newFront.y = (float) (Math.sin(Math.toRadians(pitch)));
        newFront.z = (float) (Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
        front = newFront.normalize();

        // 重新计算右向量和上向量
        right = new Vector3f(front).cross(worldUp).normalize();
        up = new Vector3f(right).cross(front).normalize();
    }

    /**
     * 重置相机到初始状态
     */
    public void reset() {
        position.set(0.0f, 0.0f, 8.0f);
        yaw = -90.0f;
        pitch = 0.0f;
        updateCameraVectors();
    }

    // Getter方法
    public Vector3f getPosition() {
        return new Vector3f(position);
    }

    public Vector3f getFront() {
        return new Vector3f(front);
    }

    public float getZoom() {
        return zoom;
    }

    // Setter方法
    public void setPosition(Vector3f newPosition) {
        position.set(newPosition);
    }
}
