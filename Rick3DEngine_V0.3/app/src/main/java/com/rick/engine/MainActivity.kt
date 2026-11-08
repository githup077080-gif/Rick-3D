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
        setContent { RickEditorV2() }
    }
}

@Composable
private fun RickEditorV2() {
    var tool by remember { mutableStateOf("انتخاب") }
    var objectCount by remember { mutableIntStateOf(0) }
    MaterialTheme {
        Column(Modifier.fillMaxSize().background(Color(0xFF151923))) {
            Row(
                Modifier.fillMaxWidth().height(48.dp).background(Color(0xFF202838)).padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("RICK 3D ENGINE  •  0.2.0", color = Color.White)
                Text("صحنه آزمایشی", color = Color(0xFF9FC5FF))
            }
            Row(Modifier.weight(1f).fillMaxWidth()) {
                Column(
                    Modifier.width(94.dp).fillMaxHeight().background(Color(0xFF202838)).padding(5.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("ابزار", color = Color.LightGray)
                    listOf("انتخاب", "حرکت", "چرخش", "اندازه").forEach { item ->
                        Button(
                            onClick = { tool = item },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(2.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (tool == item) Color(0xFF315A91) else Color(0xFF30394A)
                            )
                        ) { Text(item) }
                    }
                    Divider(color = Color.DarkGray)
                    Button(onClick = { GridRenderer.addCube(); objectCount++ }, modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(2.dp)) { Text("+ مکعب") }
                    Button(onClick = { GridRenderer.addSphere(); objectCount++ }, modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(2.dp)) { Text("+ کره") }
                    Button(onClick = { GridRenderer.clearObjects(); objectCount = 0 }, modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(2.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B3D45))) {
                        Text("پاک‌کردن")
                    }
                }
                Box(Modifier.weight(1f).fillMaxHeight()) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { context -> RickGLSurfaceView(context) }
                    )
                    Text(
                        "ابزار: $tool  |  اشیا: $objectCount\nیک انگشت: چرخش دوربین • دو انگشت: زوم",
                        color = Color(0xFFCAD5E5),
                        modifier = Modifier.align(Alignment.BottomStart).padding(12.dp)
                    )
                }
                Column(Modifier.width(155.dp).fillMaxHeight().background(Color(0xFF202838)).padding(10.dp)) {
                    Text("بازرس", color = Color.White)
                    Spacer(Modifier.height(8.dp))
                    Text("نسخه 0.2.0", color = Color(0xFF9FC5FF))
                    Text("تعداد اشیا: $objectCount", color = Color.LightGray)
                    Spacer(Modifier.height(18.dp))
                    Text("صحنه", color = Color.White)
                    Text("• World", color = Color.LightGray)
                    Text("• Grid", color = Color.LightGray)
                    repeat(objectCount) { index ->
                        Text("• Object ${index + 1}", color = Color(0xFFB9C9DB))
                    }
                }
            }
            Row(
                Modifier.fillMaxWidth().height(32.dp).background(Color(0xFF202838)).padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) { Text("آماده | مکعب و کره را از نوار ابزار اضافه کن", color = Color(0xFFB8C7DA)) }
        }
    }
}

private class RickGLSurfaceView(context: android.content.Context) : GLSurfaceView(context) {
    private var lastX = 0f
    private var lastY = 0f
    private var lastSpan = 0f

    init {
        setEGLContextClientVersion(3)
        setRenderer(GridRenderer())
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.x
                lastY = event.y
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                lastSpan = span(event)
            }
            MotionEvent.ACTION_MOVE -> {
                if (event.pointerCount >= 2) {
                    val newSpan = span(event)
                    if (lastSpan > 0f && newSpan > 0f) {
                        val factor = lastSpan / newSpan
                        queueEvent { GridRenderer.zoom(factor) }
                    }
                    lastSpan = newSpan
                } else {
                    val dx = event.x - lastX
                    val dy = event.y - lastY
                    queueEvent { GridRenderer.orbit(dx, dy) }
                }
                lastX = event.x
                lastY = event.y
            }
            MotionEvent.ACTION_POINTER_UP -> lastSpan = 0f
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> lastSpan = 0f
        }
        return true
    }

    private fun span(e: MotionEvent): Float {
        if (e.pointerCount < 2) return 0f
        return hypot(e.getX(0) - e.getX(1), e.getY(0) - e.getY(1))
    }
}
