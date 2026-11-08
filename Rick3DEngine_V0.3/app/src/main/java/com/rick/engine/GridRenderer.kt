package com.rick.engine

import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.cos
import kotlin.math.sin

private data class SceneObject(val type: String, val x: Float, val y: Float, val z: Float)

class GridRenderer : GLSurfaceView.Renderer {
    private var program = 0
    private var mvpLocation = 0
    private var colorLocation = 0
    private lateinit var gridBuffer: FloatBuffer
    private lateinit var cubeBuffer: FloatBuffer
    private lateinit var sphereBuffer: FloatBuffer
    private val projection = FloatArray(16)
    private val view = FloatArray(16)
    private val model = FloatArray(16)
    private val mvp = FloatArray(16)
    private var yaw = 0.75f
    private var pitch = 0.72f
    private var distance = 13f
    private val objects = mutableListOf<SceneObject>()

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(0.075f, 0.09f, 0.12f, 1f)
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        val vs = compile(GLES30.GL_VERTEX_SHADER, """
            #version 300 es
            layout(location=0) in vec3 aPosition;
            uniform mat4 uMVP;
            void main(){ gl_Position = uMVP * vec4(aPosition,1.0); }
        """.trimIndent())
        val fs = compile(GLES30.GL_FRAGMENT_SHADER, """
            #version 300 es
            precision mediump float;
            uniform vec4 uColor;
            out vec4 fragColor;
            void main(){ fragColor = uColor; }
        """.trimIndent())
        program = GLES30.glCreateProgram()
        GLES30.glAttachShader(program, vs)
        GLES30.glAttachShader(program, fs)
        GLES30.glLinkProgram(program)
        mvpLocation = GLES30.glGetUniformLocation(program, "uMVP")
        colorLocation = GLES30.glGetUniformLocation(program, "uColor")
        gridBuffer = buffer(makeGrid())
        cubeBuffer = buffer(makeCube())
        sphereBuffer = buffer(makeSphere())
    }

    private fun compile(type: Int, source: String): Int {
        val shader = GLES30.glCreateShader(type)
        GLES30.glShaderSource(shader, source)
        GLES30.glCompileShader(shader)
        return shader
    }

    private fun buffer(data: FloatArray): FloatBuffer =
        ByteBuffer.allocateDirect(data.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().apply {
            put(data); position(0)
        }

    private fun makeGrid(): FloatArray {
        val out = ArrayList<Float>()
        for (i in -20..20) {
            out.addAll(listOf(-20f,0f,i.toFloat(), 20f,0f,i.toFloat()))
            out.addAll(listOf(i.toFloat(),0f,-20f, i.toFloat(),0f,20f))
        }
        return out.toFloatArray()
    }

    private fun makeCube(): FloatArray {
        val p = floatArrayOf(
            -1f,-1f,1f, 1f,-1f,1f, 1f,1f,1f, -1f,1f,1f,
            -1f,-1f,-1f, -1f,1f,-1f, 1f,1f,-1f, 1f,-1f,-1f,
            -1f,1f,-1f, -1f,1f,1f, 1f,1f,1f, 1f,1f,-1f,
            -1f,-1f,-1f, 1f,-1f,-1f, 1f,-1f,1f, -1f,-1f,1f,
            1f,-1f,-1f, 1f,1f,-1f, 1f,1f,1f, 1f,-1f,1f,
            -1f,-1f,-1f, -1f,-1f,1f, -1f,1f,1f, -1f,1f,-1f
        )
        val indices = intArrayOf(
            0,1,2,0,2,3, 4,5,6,4,6,7, 8,9,10,8,10,11,
            12,13,14,12,14,15, 16,17,18,16,18,19, 20,21,22,20,22,23
        )
        return indices.flatMap { i -> listOf(p[i*3],p[i*3+1],p[i*3+2]) }.toFloatArray()
    }

    private fun makeSphere(): FloatArray {
        val out = ArrayList<Float>()
        val stacks = 12
        val slices = 18
        for (i in 0 until stacks) {
            val lat0 = Math.PI * (-0.5 + i.toDouble() / stacks)
            val lat1 = Math.PI * (-0.5 + (i + 1).toDouble() / stacks)
            for (j in 0 until slices) {
                val lon0 = 2.0 * Math.PI * j / slices
                val lon1 = 2.0 * Math.PI * (j + 1) / slices
                val a = point(lat0, lon0); val b = point(lat1, lon0)
                val c = point(lat1, lon1); val d = point(lat0, lon1)
                listOf(a,b,c,a,c,d).forEach { out.addAll(it) }
            }
        }
        return out.toFloatArray()
    }

    private fun point(lat: Double, lon: Double) = floatArrayOf(
        (cos(lat) * cos(lon)).toFloat(),
        sin(lat).toFloat(),
        (cos(lat) * sin(lon)).toFloat()
    )

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        Matrix.perspectiveM(projection, 0, 55f, width.toFloat() / height.coerceAtLeast(1), 0.1f, 250f)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
        val cx = distance * cos(pitch) * sin(yaw)
        val cy = distance * sin(pitch)
        val cz = distance * cos(pitch) * cos(yaw)
        Matrix.setLookAtM(view, 0, cx, cy, cz, 0f, 0f, 0f, 0f, 1f, 0f)
        GLES30.glUseProgram(program)
        GLES30.glEnableVertexAttribArray(0)

        Matrix.setIdentityM(model, 0)
        draw(gridBuffer, 164, GLES30.GL_LINES, 0.30f, 0.38f, 0.49f)

        val snapshot = synchronized(objects) { objects.toList() }
        snapshot.forEach { obj ->
            Matrix.setIdentityM(model, 0)
            Matrix.translateM(model, 0, obj.x, obj.y, obj.z)
            Matrix.scaleM(model, 0, 0.8f, 0.8f, 0.8f)
            val data = if (obj.type == "cube") cubeBuffer else sphereBuffer
            val count = if (obj.type == "cube") 36 else sphereBuffer.limit() / 3
            draw(data, count, GLES30.GL_TRIANGLES,
                if (obj.type == "cube") 0.25f else 0.20f,
                if (obj.type == "cube") 0.65f else 0.48f,
                if (obj.type == "cube") 0.95f else 0.78f)
        }
        GLES30.glDisableVertexAttribArray(0)
    }

    private fun draw(data: FloatBuffer, count: Int, mode: Int, r: Float, g: Float, b: Float) {
        Matrix.multiplyMM(mvp, 0, view, 0, model, 0)
        val finalMvp = FloatArray(16)
        Matrix.multiplyMM(finalMvp, 0, projection, 0, mvp, 0)
        GLES30.glUniformMatrix4fv(mvpLocation, 1, false, finalMvp, 0)
        GLES30.glUniform4f(colorLocation, r, g, b, 1f)
        data.position(0)
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 0, data)
        GLES30.glDrawArrays(mode, 0, count)
    }

    companion object {
        @Volatile private var active: GridRenderer? = null

        fun addCube() { active?.let { synchronized(it.objects) {
            val n = it.objects.size
            it.objects.add(SceneObject("cube", ((n % 5) - 2) * 2.1f, 0.8f, (n / 5) * 2.1f))
        } } }

        fun addSphere() { active?.let { synchronized(it.objects) {
            val n = it.objects.size
            it.objects.add(SceneObject("sphere", ((n % 5) - 2) * 2.1f, 0.8f, (n / 5) * 2.1f))
        } } }

        fun clearObjects() { active?.let { synchronized(it.objects) { it.objects.clear() } } }

        fun orbit(dx: Float, dy: Float) {
            active?.let {
                it.yaw += dx * 0.006f
                it.pitch = (it.pitch - dy * 0.006f).coerceIn(0.12f, 1.48f)
            }
        }

        fun zoom(factor: Float) {
            active?.let { it.distance = (it.distance * factor).coerceIn(3.5f, 65f) }
        }
    }

    init { active = this }
}
