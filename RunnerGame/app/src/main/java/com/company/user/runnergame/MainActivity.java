package com.company.user.runnergame;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.Point;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

import org.json.JSONException;

import java.util.HashMap;

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
            mGLSurfaceView = new MySurfaceView(this);
            //mGLSurfaceView = new GLSurfaceView(this);

            //mGLSurfaceView.setRenderer(new GLES20Renderer(this));
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

class MySurfaceView extends GLSurfaceView {
    private GLES20Renderer renderer;
    private Context context;
    int width, height;

    public MySurfaceView(Context context) {
        super(context);
        setEGLContextClientVersion(2);
        setPreserveEGLContextOnPause(true);
        setRenderer(renderer = new GLES20Renderer(context));
        this.context = context;

        WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //Log.i("X, Y: ", event.getX() + ", " + event.getY());
        //Log.i("action: ", Integer.toString(event.getAction()));
        float x = event.getX();
        float y = event.getY();
        float value = 0.01f;
        if (x > width / 2) {
            if (x < width / 2 + 200)
                renderer.camera.rotateLeft(value);
            else if (x > width - 200)
                renderer.camera.rotateRight(value);
            else if (y < 200)
                renderer.camera.rotateDown(value);
            else if (y > height - 200)
                renderer.camera.rotateUp(value);
            else
                renderer.camera.moveDown(value);
        } else {
            if (x < 200)
                renderer.camera.moveLeft(value);
            else if (x > width / 2 - 200)
                renderer.camera.moveRight(value);
            else if (y < 200)
                renderer.camera.moveForward(value);
            else if (y > height - 200)
                renderer.camera.moveBackward(value);
            else
                renderer.camera.moveUp(value);
        }
        return true;
    }
}

class GLES20Renderer implements GLSurfaceView.Renderer {
    private final Context context;
    private float[] mViewMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];

    private HashMap<String, Integer> handles = new HashMap<String, Integer>();
    public Camera camera;
    private Cube cube;
    private Mesh mesh;

    public GLES20Renderer(Context context) {
        this.context = context;
        camera = new Camera(new float[]{0.0f, 0.0f, 1.5f}, new float[]{0.0f, 0.0f, -5.0f}, new float[]{0.0f, 1.0f, 0.0f});
        cube = new Cube(1, 1, 1);
        try {
            mesh = JsonModelLoader.load(context, "autism_model.jm");
            mesh.loadBitmap(AssetsLoader.loadBitmap(context, "autism_texture.jpg"));
        } catch (JSONException e) {
            Log.i("Could not load JsonModel: ", e.getMessage());
        }
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 0.5f);
        //Matrix.setLookAtM(mViewMatrix, 0, 0.0f, 0.0f, 1.5f, 0.0f, 0.0f, -5.0f, 0.0f, 1.0f, 0.0f);

        //Matrix.setLookAtM(mViewMatrix, 0, 10, 10, 10, 0, 0, 0, 0, 1, 0);
        final String vertexShader = AssetsLoader.loadString(context, "simple_shader.vs");
        final String fragmentShader = AssetsLoader.loadString(context, "simple_shader.fs");

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
        GLES20.glBindAttribLocation(programHandle, 2, "a_TexCoordinate");
        GLES20.glLinkProgram(programHandle);
        final int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] == 0) {
            throw new RuntimeException("Failed to create shader program: " + GLES20.glGetShaderInfoLog(programHandle));
        }

        handles.put("u_MVPMatrix", GLES20.glGetUniformLocation(programHandle, "u_MVPMatrix"));
        handles.put("u_Texture", GLES20.glGetUniformLocation(programHandle, "u_Texture"));
        handles.put("a_Position", GLES20.glGetAttribLocation(programHandle, "a_Position"));
        handles.put("a_Color", GLES20.glGetAttribLocation(programHandle, "a_Color"));
        handles.put("a_TexCoordinate", GLES20.glGetAttribLocation(programHandle, "a_TexCoordinate"));
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
        Matrix.setLookAtM(mViewMatrix, 0,
                camera.position[0], camera.position[1], camera.position[2],
                camera.target[0], camera.target[1], camera.target[2],
                camera.up[0], camera.up[1], camera.up[2]);
        long time = SystemClock.uptimeMillis() % 10000L;
        float angleInDegrees = (360.0f / 10000.0f) * ((int) time);
        mesh.draw(handles, mViewMatrix, mProjectionMatrix);
        mesh.position = new float[]{0.0f, 0.0f, 0.0f};
        mesh.rotation[0] += angleInDegrees / 100;
        mesh.rotation[1] += angleInDegrees / 100;
        mesh.rotation[2] += angleInDegrees / 100;
        mesh.scaling[0] = mesh.scaling[1] = mesh.scaling[2] = 0.3f;
    }
}