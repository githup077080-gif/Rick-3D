# Rick 3D Engine V6 — Technical Architecture

## 1. Non-negotiable product rules

- Android-first and touch-first. Keyboard and mouse are optional, never required.
- The initial scene is empty. No automatic terrain, skybox, buildings, or sample objects. Grid and axes are optional overlays.
- Device capability must select rendering quality; do not promise desktop-class performance on ordinary phones.
- Never label a feature complete before code, build, and device/simulator validation.

## 2. Scene data model

Each scene object should have:
- stable UUID (not list index);
- type and asset reference;
- position, quaternion rotation, and 3D scale;
- visibility and lock flags;
- optional material and component references.

Scene serialization must include a schema version. Loading an unsupported version must show a clear error and preserve the original file.

## 3. Input and picking

Convert touch coordinates to normalized device coordinates, multiply by inverse view-projection to create a world-space ray, then test the ray against object bounds. Select the nearest positive hit. Keep selection logic independent of Compose UI.

Gizmo hit targets must be enlarged for touch. A drag begins only after a handle is acquired; otherwise the gesture belongs to camera navigation. Provide a cancel path and restore the pre-drag transform if the gesture is cancelled.

## 4. Transform tools

- Move: world/local X, Y, Z and optional plane handles.
- Rotate: X, Y, Z rings and optional snapping.
- Scale: axis and uniform handles; prevent zero/negative scale in the first release.
- Numeric Inspector remains available as an accessible alternative to tiny handles.

## 5. History

Use command objects with execute/undo. Group continuous pointer movement into one history item when the gesture ends. Set a configurable history memory limit and discard oldest entries safely.

## 6. Rendering and performance

Start with OpenGL ES 3.0 because it is broadly available on Android devices. Keep renderer interfaces backend-neutral so Vulkan can be considered later. Use frustum culling and object batching before expensive effects. Expose low/medium/high quality presets and an FPS/frame-time overlay.

Terrain must use chunking and LOD; never allocate a huge full-resolution mesh at once. Streaming and memory budgets are required.

## 7. Project files and assets

Use Android Storage Access Framework for user-selected project locations. Keep scene JSON separate from imported assets. Copy or reference assets with explicit user consent and detect missing files on load. Initial supported model format target: glTF 2.0 / GLB.

## 8. Build and validation gates

Gate A: project syncs and compiles.
Gate B: empty scene and touch camera work.
Gate C: create/select/transform/delete work.
Gate D: save/reopen round trip preserves scene.
Gate E: undo/redo restores exact state.
Gate F: import test GLB and handle invalid files.
Gate G: APK installs and launches on at least one physical Android device.

No gate may be marked passed without recorded evidence.
