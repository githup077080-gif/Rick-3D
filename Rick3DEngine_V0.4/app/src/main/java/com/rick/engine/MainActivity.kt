package com.rick.engine

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlin.math.hypot

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { RickEditor() }
    }
}

@Composable
private fun RickEditor() {
    var tool by remember { mutableStateOf(EditorTool.SELECT) }
    var count by remember { mutableIntStateOf(0) }
    var refresh by remember { mutableIntStateOf(0) }
    var glView by remember { mutableStateOf<RickGLSurfaceView?>(null) }

    MaterialTheme {
        Column(Modifier.fillMaxSize().background(Color(0xFF151923))) {
            Row(
                Modifier.fillMaxWidth().height(46.dp).background(Color(0xFF202838)).padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("RICK 3D ENGINE  •  0.4.0", color = Color.White)
                Text("ویرایشگر لمسی", color = Color(0xFF9FC5FF))
            }
            Row(Modifier.weight(1f).fillMaxWidth()) {
                Column(
                    Modifier.width(104.dp).fillMaxHeight().background(Color(0xFF202838)).padding(5.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text("ابزارها", color = Color.LightGray)
                    listOf(
                        EditorTool.SELECT to "انتخاب",
                        EditorTool.MOVE to "حرکت",
                        EditorTool.ROTATE to "چرخش",
                        EditorTool.SCALE to "اندازه"
                    ).forEach { (mode, label) ->
                        Button(
                            onClick = { tool = mode; glView?.setTool(mode) },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(2.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (tool == mode) Color(0xFF315A91) else Color(0xFF30394A)
                            )
                        ) { Text(label) }
                    }
                    HorizontalDivider(color = Color.DarkGray)
                    Button(onClick = { glView?.addObject("cube"); count = glView?.objectCount() ?: count + 1; refresh++ },
                        modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(2.dp)) { Text("+ مکعب") }
                    Button(onClick = { glView?.addObject("sphere"); count = glView?.objectCount() ?: count + 1; refresh++ },
                        modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(2.dp)) { Text("+ کره") }
                    Button(onClick = { glView?.deleteSelected(); count = glView?.objectCount() ?: 0; refresh++ },
                        modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(2.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B633D))) { Text("حذف انتخاب") }
                    Button(onClick = { glView?.clearAll(); count = 0; refresh++ },
                        modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(2.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B3D45))) { Text("پاک‌کردن") }
                }
                Box(Modifier.weight(1f).fillMaxHeight()) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { context -> RickGLSurfaceView(context).also { glView = it; it.setTool(tool) } },
                        update = { glView = it; it.setTool(tool) }
                    )
                    Text(
                        "ابزار: ${when(tool) { EditorTool.SELECT -> "انتخاب"; EditorTool.MOVE -> "حرکت"; EditorTool.ROTATE -> "چرخش"; EditorTool.SCALE -> "اندازه" }}  |  اشیا: $count\nSelect: لمس برای انتخاب و کشیدن برای چرخش دوربین • Move/Rotate/Scale: کشیدن شیء",
                        color = Color(0xFFCAD5E5),
                        modifier = Modifier.align(Alignment.BottomStart).padding(10.dp)
                    )
                }
                Column(Modifier.width(142.dp).fillMaxHeight().background(Color(0xFF202838)).padding(9.dp)) {
                    Text("بازرس", color = Color.White)
                    Spacer(Modifier.height(8.dp))
                    Text("نسخه 0.4.0", color = Color(0xFF9FC5FF))
                    Text("تعداد اشیا: $count", color = Color.LightGray)
                    Spacer(Modifier.height(18.dp))
                    Text("راهنما", color = Color.White)
                    Text("Select: انتخاب", color = Color.LightGray)
                    Text("Move: جابه‌جایی XZ", color = Color.LightGray)
                    Text("Rotate: محور Y", color = Color.LightGray)
                    Text("Scale: یکنواخت", color = Color.LightGray)
                    Text("دو انگشت: زوم", color = Color.LightGray)
                }
            }
            Row(
                Modifier.fillMaxWidth().height(30.dp).background(Color(0xFF202838)).padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) { Text("Rick 3D Engine • نسخهٔ آزمایشی 0.4.0", color = Color(0xFFB8C7DA)) }
        }
    }
}

private class RickGLSurfaceView(context: android.content.Context) : GLSurfaceView(context) {
    private val engineRenderer = GridRenderer()
    private var lastX = 0f
    private var lastY = 0f
    private var lastSpan = 0f
    private var downX = 0f
    private var downY = 0f
    private var dragging = false

    init {
        setEGLContextClientVersion(3)
        setRenderer(engineRenderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    fun setTool(tool: EditorTool) { engineRenderer.setTool(tool) }
    fun addObject(type: String) { queueEvent { engineRenderer.addObject(type) } }
    fun clearAll() { queueEvent { engineRenderer.clearAll() } }
    fun deleteSelected() { queueEvent { engineRenderer.deleteSelected() } }
    fun objectCount(): Int = engineRenderer.getObjectCount()

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.x; lastY = event.y
                downX = event.x; downY = event.y
                dragging = false
            }
            MotionEvent.ACTION_POINTER_DOWN -> lastSpan = span(event)
            MotionEvent.ACTION_MOVE -> {
                if (event.pointerCount >= 2) {
                    val newSpan = span(event)
                    if (lastSpan > 0f && newSpan > 0f) {
                        val factor = lastSpan / newSpan
                        queueEvent { engineRenderer.zoom(factor) }
                    }
                    lastSpan = newSpan
                } else {
                    val dx = event.x - lastX
                    val dy = event.y - lastY
                    if (hypot(event.x - downX, event.y - downY) > 10f) dragging = true
                    queueEvent {
                        when (engineRendererTool()) {
                            EditorTool.SELECT -> engineRenderer.orbit(dx, dy)
                            else -> engineRenderer.transformSelected(dx, dy)
                        }
                    }
                }
                lastX = event.x; lastY = event.y
            }
            MotionEvent.ACTION_UP -> {
                if (!dragging && event.pointerCount == 1) {
                    val x = event.x; val y = event.y
                    queueEvent { engineRenderer.selectAt(x, y, width, height) }
                }
                lastSpan = 0f
            }
            MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> lastSpan = 0f
        }
        return true
    }

    // The view mirrors the active tool so touch routing is deterministic.
    private var activeTool = EditorTool.SELECT
    fun setActiveTool(tool: EditorTool) { activeTool = tool; engineRenderer.setTool(tool) }
    private fun engineRendererTool() = activeTool

    private fun span(e: MotionEvent): Float {
        if (e.pointerCount < 2) return 0f
        return hypot(e.getX(0) - e.getX(1), e.getY(0) - e.getY(1))
    }
}
