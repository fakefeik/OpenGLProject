package com.company.user.runnergame;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.content.res.AssetManager;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class MainActivity extends Activity {
    private GLSurfaceView mGLSurfaceView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (hasGlEs20()) {
            mGLSurfaceView = new GLSurfaceView(this);
            mGLSurfaceView.setEGLContextClientVersion(2);
            mGLSurfaceView.setPreserveEGLContextOnPause(true);
            mGLSurfaceView.setRenderer(new GLES20Renderer(this));
        } else return;
        setContentView(mGLSurfaceView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLSurfaceView.onPause();
    }

    private boolean hasGlEs20() {
        ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo info = am.getDeviceConfigurationInfo();
        return info.reqGlEsVersion >= 0x20000;
    }
}

class GLES20Renderer implements GLSurfaceView.Renderer {
    private final Context context;
    private final FloatBuffer mVerticesBuffer;
    private float[] mViewMatrix = new float[16];
    private float[] mModelMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    private int mMVPMatrixHandle;
    private int mPositionHandle;
    private int mColorHandle;
    private Cube cube;
    private Mesh mesh;


    /** How many bytes per float. */
    private final int mBytesPerFloat = 4;

    /** How many elements per vertex. */
    private final int mStrideBytes = 7 * mBytesPerFloat;

    /** Offset of the position data. */
    private final int mPositionOffset = 0;

    /** Size of the position data in elements. */
    private final int mPositionDataSize = 3;

    /** Offset of the color data. */
    private final int mColorOffset = 3;

    /** Size of the color data in elements. */
    private final int mColorDataSize = 4;

    public GLES20Renderer(Context context) {
        this.context = context;
        final float verticesData[] = {
                -0.5f, -0.25f, 0.0f,
                1.0f, 0.0f, 0.0f, 1.0f
        };
        mVerticesBuffer = ByteBuffer.allocateDirect(verticesData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mVerticesBuffer.put(verticesData).position(0);
        cube = new Cube(0.1f, 0.1f, 0.1f);
        try {
            mesh = JsonModelLoader.load(context, "model.jm");
        } catch (JSONException e) {
            Log.i("Could not load JsonModel: ", e.getMessage());
        }
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 0.5f);
        Matrix.setLookAtM(mViewMatrix, 0, 0.0f, 0.0f, 1.5f, 0.0f, 0.0f, -5.0f, 0.0f, 1.0f, 0.0f);

        //Matrix.setLookAtM(mViewMatrix, 0, 10, 10, 10, 0, 0, 0, 0, 1, 0);
        final String vertexShader = AssetsLoader.loadAsset(context, "simple_shader.vs");
        final String fragmentShader = AssetsLoader.loadAsset(context, "simple_shader.fs");

        // Loading vertex shader
        int vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        if (vertexShaderHandle == 0)
            throw new RuntimeException("Failed to create vertex shader outside of OpenGL thread.");
        GLES20.glShaderSource(vertexShaderHandle, vertexShader);
        GLES20.glCompileShader(vertexShaderHandle);
        final int[] vertexStatus = new int[1];
        GLES20.glGetShaderiv(vertexShaderHandle, GLES20.GL_COMPILE_STATUS, vertexStatus, 0);
        if (vertexStatus[0] == 0) {
            throw new RuntimeException("Failed to create vertex shader: " + GLES20.glGetShaderInfoLog(vertexShaderHandle));
        }

        // Loading fragment shader
        int fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        if (fragmentShaderHandle == 0)
            throw new RuntimeException("Failed to create fragment shader outside of OpenGL thread.");
        GLES20.glShaderSource(fragmentShaderHandle, fragmentShader);
        GLES20.glCompileShader(fragmentShaderHandle);
        final int[] fragmentStatus = new int[1];
        GLES20.glGetShaderiv(fragmentShaderHandle, GLES20.GL_COMPILE_STATUS, fragmentStatus, 0);
        if (fragmentStatus[0] == 0) {
            throw new RuntimeException("Failed to create fragment shader: " + GLES20.glGetShaderInfoLog(fragmentShaderHandle));
        }

        // Creating program
        int programHandle = GLES20.glCreateProgram();
        if (programHandle == 0)
            throw new RuntimeException("Failed to create shader program outside of OpenGL thread.");
        GLES20.glAttachShader(programHandle, vertexShaderHandle);
        GLES20.glAttachShader(programHandle, fragmentShaderHandle);
        GLES20.glBindAttribLocation(programHandle, 0, "a_Position");
        GLES20.glBindAttribLocation(programHandle, 1, "a_Color");
        GLES20.glLinkProgram(programHandle);
        final int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] == 0) {
            throw new RuntimeException("Failed to create shader program: " + GLES20.glGetShaderInfoLog(programHandle));
        }

        mMVPMatrixHandle = GLES20.glGetUniformLocation(programHandle, "u_MVPMatrix");
        mPositionHandle = GLES20.glGetAttribLocation(programHandle, "a_Position");
        mColorHandle = GLES20.glGetAttribLocation(programHandle, "a_Color");
        GLES20.glUseProgram(programHandle);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        final float ratio = (float) width / height;
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -1.0f;
        final float top = 1.0f;
        final float near = 1.0f;
        final float far = 10.0f;
        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        long time = SystemClock.uptimeMillis() % 10000L;
        float angleInDegrees = (360.0f / 10000.0f) * ((int) time);
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 1.0f, 1.0f, 1.0f);
        //drawTriangle(mVerticesBuffer);
        //cube.draw(mPositionHandle, mMVPMatrixHandle, mMVPMatrix, mViewMatrix, mModelMatrix, mProjectionMatrix);
        mesh.draw(mPositionHandle, mMVPMatrixHandle, mMVPMatrix, mViewMatrix, mModelMatrix, mProjectionMatrix);
        //GLES20.glDrawElements(GLES20.GL_TRIANGLES, 7, GLES20.GL_FLOAT, mVerticesBuffer);
    }

    private void drawTriangle(final FloatBuffer aTriangleBuffer)
    {
        // Pass in the position information
        aTriangleBuffer.position(mPositionOffset);
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
                mStrideBytes, aTriangleBuffer);

        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Pass in the color information
        aTriangleBuffer.position(mColorOffset);
        GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize, GLES20.GL_FLOAT, false,
                mStrideBytes, aTriangleBuffer);

        GLES20.glEnableVertexAttribArray(mColorHandle);

        // This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
        // (which currently contains model * view).
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);

        // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
    }
}