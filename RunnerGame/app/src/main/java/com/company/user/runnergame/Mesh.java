package com.company.user.runnergame;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Admin on 23.11.2014.
 */
public class Mesh {
    private String name;
    public float[] position = {0.0f, 0.0f, 0.0f};
    public float[] rotation = {0.0f, 0.0f, 0.0f};
    public float[] scaling = {1.0f, 1.0f, 1.0f};

    public FloatBuffer verticesBuffer = null;
    public FloatBuffer normalsBuffer = null;
    public ShortBuffer indicesBuffer = null;
    public FloatBuffer textureBuffer = null;
    public ShortBuffer linesBuffer = null;

    private float[] mMVPMatrix = new float[16];
    private float[] mModelMatrix = new float[16];

    public int lineIndicesCount = -1;
    public int indicesCount = -1;
    private int textureId = -1;
    private Bitmap bitmap;
    private boolean loadTexture = false;
    private float[] rgba = new float[]{1.0f, 1.0f, 1.0f, 1.0f};
    private FloatBuffer colorBuffer = null;


    public Mesh(String name, float[] position, float[] rotation, float[] scaling, float[] vertices, float[] normals, float[] textures, int[] indices) {
        this.name = name;
        this.position = position;
        this.rotation = rotation;
        this.scaling = scaling;
        setVertices(vertices);
        setNormals(normals);
        setTextureCoordinates(textures);
        setIndices(indices);
    }

    public Mesh() {

    }

    public void loadBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        loadTexture = true;
    }

    private void loadTexture() {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        textureId = textures[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

        //gl.glTexParameterf(GL10.GL_TEXTURE_2D, gl.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        //gl.glTexParameterf(GL10.GL_TEXTURE_2D, gl.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT);

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

    }

    protected void setVertices(float[] vertices) {
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        verticesBuffer = vbb.asFloatBuffer();
        verticesBuffer.put(vertices);
        verticesBuffer.position(0);
    }

    protected void setNormals(float[] normals) {
        ByteBuffer vbb = ByteBuffer.allocateDirect(normals.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        normalsBuffer = vbb.asFloatBuffer();
        normalsBuffer.put(normals);
        normalsBuffer.position(0);
    }

    protected void setIndices(int[] indices) {
        ByteBuffer ibb = ByteBuffer.allocateDirect(indices.length * 4);

        short[] newind = new short[indices.length];
        for (int i = 0; i < indices.length; i++)
            newind[i] = (short)indices[i];

        //for (int i = 0; i < indices.length; i++) {
        //    Log.i("Index " + i + ": ", Integer.toString(indices[i]));
        //    Log.i("New Index " + i + ": ", Short.toString(newind[i]));
        //}

        ibb.order(ByteOrder.nativeOrder());
        indicesBuffer = ibb.asShortBuffer();
        indicesBuffer.put(newind);
        indicesBuffer.position(0);
        indicesCount = indices.length;
        setLineIndices(newind);
    }

    protected void setLineIndices(short[] indices) {
        short[] newIndices = new short[indices.length * 2];
        int j = 0;
        for (int i = 0; i < newIndices.length; i += 6) {
            int a = indices[j];
            int b = indices[j + 1];
            int c = indices[j + 2];
            j += 3;
            newIndices[i] = (short)a;
            newIndices[i + 1] = (short)b;
            newIndices[i + 2] = (short)b;
            newIndices[i + 3] = (short)c;
            newIndices[i + 4] = (short)c;
            newIndices[i + 5] = (short)a;
        }
        ByteBuffer ibb = ByteBuffer.allocateDirect(newIndices.length * 4);
        ibb.order(ByteOrder.nativeOrder());
        linesBuffer = ibb.asShortBuffer();
        linesBuffer.put(newIndices);
        linesBuffer.position(0);
        lineIndicesCount = newIndices.length;
    }

    protected void setTextureCoordinates(float[] textureCoordinates) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(textureCoordinates.length * 4);
        buffer.order(ByteOrder.nativeOrder());
        textureBuffer = buffer.asFloatBuffer();
        textureBuffer.put(textureCoordinates);
        textureBuffer.position(0);
    }

    protected void setColor(float red, float green, float blue, float alpha) {
        rgba[0] = red;
        rgba[1] = green;
        rgba[2] = blue;
        rgba[3] = alpha;
    }

    protected void setColors(float[] colors) {
        ByteBuffer cbb = ByteBuffer.allocateDirect(colors.length * 4);
        cbb.order(ByteOrder.nativeOrder());
        colorBuffer = cbb.asFloatBuffer();
        colorBuffer.put(colors);
        colorBuffer.position(0);
    }

//    public void draw() {
//        gl.glFrontFace(GL10.GL_CCW);
//        gl.glEnable(GL10.GL_CULL_FACE);
//        gl.glCullFace(culling_option);
//        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
//        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, verticesBuffer);
//        gl.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);
//        if (colorBuffer != null) {
//            gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
//            gl.glColorPointer(4, GL10.GL_FLOAT, 0, colorBuffer);
//        }
//
//        if (loadTexture) {
//            loadTexture(gl);
//            loadTexture = false;
//        }
//
//        if (textureId != -1 && textureBuffer != null) {
//            gl.glEnable(GL10.GL_TEXTURE_2D);
//            gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
//
//            gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);
//            gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId);
//        }
//
//        gl.glDrawElements(GL10.GL_TRIANGLES, indicesCount, GL10.GL_UNSIGNED_SHORT, indicesBuffer);
//        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
//        if (textureId != -1 && textureBuffer != null) {
//            gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
//            gl.glDisable(GL10.GL_TEXTURE_2D);
//        }
//        gl.glDisable(GL10.GL_CULL_FACE);
//    }

    public void draw(HashMap<String, Integer> handles, float[] mViewMatrix, float[] mProjectionMatrix) {
        GLES20.glFrontFace(GLES20.GL_CCW);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_FRONT);

        GLES20.glEnableVertexAttribArray(handles.get("a_Position"));
        GLES20.glVertexAttribPointer(handles.get("a_Position"), 3, GLES20.GL_FLOAT, false,
                12, verticesBuffer);
        if (loadTexture) {
            loadTexture();
            loadTexture = false;
        }

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glUniform1i(handles.get("u_Texture"), 0);
        GLES20.glEnable(GLES20.GL_TEXTURE_2D);
        GLES20.glEnableVertexAttribArray(handles.get("a_TexCoordinate"));
        GLES20.glVertexAttribPointer(handles.get("a_TexCoordinate"), 2, GLES20.GL_FLOAT, false, 8, textureBuffer);


        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, position[0], position[1], position[2]);
        Matrix.rotateM(mModelMatrix, 0, rotation[0], 1, 0, 0);
        Matrix.rotateM(mModelMatrix, 0, rotation[1], 0, 1, 0);
        Matrix.rotateM(mModelMatrix, 0, rotation[2], 0, 0, 1);
        Matrix.scaleM(mModelMatrix, 0, scaling[0], scaling[1], scaling[2]);

        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(handles.get("u_MVPMatrix"), 1, false, mMVPMatrix, 0);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indicesCount, GLES20.GL_UNSIGNED_SHORT, indicesBuffer);

        //GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

    public void drawWireframe(int mPositionHandle, int mMVPMatrixHandle, float[] mViewMatrix, float[] mProjectionMatrix) {
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false,
                12, verticesBuffer);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, position[0], position[1], position[2]);
        Matrix.rotateM(mModelMatrix, 0, rotation[0], 1, 0, 0);
        Matrix.rotateM(mModelMatrix, 0, rotation[1], 0, 1, 0);
        Matrix.rotateM(mModelMatrix, 0, rotation[2], 0, 0, 1);
        Matrix.scaleM(mModelMatrix, 0, scaling[0], scaling[1], scaling[2]);

        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glDrawElements(GLES20.GL_LINES, lineIndicesCount, GLES20.GL_UNSIGNED_SHORT, linesBuffer);
    }

//    public void drawWireframe(GL10 gl) {
//        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
//        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, verticesBuffer);
//        gl.glTranslatef(x, y, z);
//        gl.glRotatef(rx, 1, 0, 0);
//        gl.glRotatef(ry, 0, 1, 0);
//        gl.glRotatef(rz, 0, 0, 1);
//        gl.glScalef(sx, sy, sz);
//        gl.glDrawElements(GL10.GL_LINES, lineIndicesCount, GL10.GL_UNSIGNED_SHORT, linesBuffer);
//        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
//    }
}
