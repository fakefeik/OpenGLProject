package com.company.user.runnergame;

import android.opengl.Matrix;

import java.util.Vector;

/**
 * Created by user on 26.11.2014.
 */
public class Camera {
    public float[] position;
    public float[] target;
    public float[] up;

    float angleX = 0;
    float angleZ = 0;


    public Camera(float[] position, float[] target, float[] up) {
        this.position = position;
        this.target = target;
        this.up = up;
    }

    public void moveForward(float value) {
        float[] vector = {target[0] - position[0], target[1] - position[1], target[2] - position[2]};
        position[0] += vector[0] * value;
        position[2] += vector[2] * value;
        target[0] += vector[0] * value;
        target[2] += vector[2] * value;
    }

    public void moveBackward(float value) {
        float[] vector = {target[0] - position[0], target[1] - position[1], target[2] - position[2]};
        position[0] -= vector[0] * value;
        position[2] -= vector[2] * value;
        target[0] -= vector[0] * value;
        target[2] -= vector[2] * value;
    }

    public void moveLeft(float value) {
        float dx = target[0] - position[0];
        float dz = target[2] - position[2];
        float[] vector = {-dz, dx};
        position[0] -= vector[0] * value;
        position[2] -= vector[1] * value;
        target[0] -= vector[0] * value;
        target[2] -= vector[1] * value;
    }

    public void moveRight(float value) {
        float dx = target[0] - position[0];
        float dz = target[2] - position[2];
        float[] vector = {dz, -dx};
        position[0] -= vector[0] * value;
        position[2] -= vector[1] * value;
        target[0] -= vector[0] * value;
        target[2] -= vector[1] * value;
    }

    public void moveUp(float value) {
        position[1] += value;
        target[1] += value;
    }

    public void moveDown(float value) {
        position[1] -= value;
        target[1] -= value;
    }

    public void rotateLeft(float value) {
//        float[] mat4 = new float[16];
//        angleX += value;
//        Matrix.setRotateEulerM(mat4, 0, angleX, 0, angleZ);
//        float[] vec = {0, 0, 1, 1};
//        float[] vec2 = new float[4];
//        Matrix.multiplyMV(vec2, 0, mat4, 0, vec, 0);
//        target[0] = vec2[0] + position[0];
//        target[1] = vec2[1] + position[1];
//        target[2] = vec2[2] + position[2];
    }

    public void rotateRight(float value) {
//        float[] mat4 = new float[16];
//        angleX -= value;
//        Matrix.setRotateEulerM(mat4, 0, angleX, 0, angleZ);
//        float[] vec = {0, 0, 1, 1};
//        float[] vec2 = new float[4];
//        Matrix.multiplyMV(vec2, 0, mat4, 0, vec, 0);
//        target[0] = vec2[0] + position[0];
//        target[1] = vec2[1] + position[1];
//        target[2] = vec2[2] + position[2];
    }

    public void rotateUp(float value) {
//        float[] mat4 = new float[16];
//        angleZ -= value;
//        Matrix.setRotateEulerM(mat4, 0, angleX, 0, angleZ);
//        float[] vec = {0, 0, 1, 1};
//        float[] vec2 = new float[4];
//        Matrix.multiplyMV(vec2, 0, mat4, 0, vec, 0);
//        target[0] = vec2[0] + position[0];
//        target[1] = vec2[1] + position[1];
//        target[2] = vec2[2] + position[2];
    }

    public void rotateDown(float value) {
//        float[] mat4 = new float[16];
//        angleZ += value;
//        Matrix.setRotateEulerM(mat4, 0, angleX, 0, angleZ);
//        float[] vec = {0, 0, 1, 1};
//        float[] vec2 = new float[4];
//        Matrix.multiplyMV(vec2, 0, mat4, 0, vec, 0);
//        target[0] = vec2[0] + position[0];
//        target[1] = vec2[1] + position[1];
//        target[2] = vec2[2] + position[2];
    }
}
