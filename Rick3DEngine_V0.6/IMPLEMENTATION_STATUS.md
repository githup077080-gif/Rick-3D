# V6.1 Implementation Status

## Implemented in source
- Android application project configuration (AGP 8.7.3, Kotlin 2.0.21, Compose).
- OpenGL ES 3.0 viewport with an initially empty scene and visible grid.
- Touch orbit and two-finger zoom.
- Add cube and sphere.
- Select object by approximate screen-space center hit test.
- Move selected object on the XZ plane.
- Rotate selected object around Y.
- Uniform scale selected object.
- Delete selected object and clear scene.
- Save scene to a JSON file using Android's document picker.
- Open and validate a Rick 3D JSON scene using Android's document picker.
- Scene JSON stores object IDs, types, transforms, and camera settings.
- Undo/Redo history for object creation, deletion, clear, scene import, and transform gestures (up to 50 history entries).
- Basic tool and inspector panels.

## Not yet implemented
- True ray/triangle picking and axis gizmos.
- Three-axis move/rotate/scale.
- Real scene tree and numeric inspector.
- GLB/glTF import, terrain, materials, physics, scripting.
- Release APK signing and physical-device validation.

## Save format
- JSON format identifier: `rick3d-scene`
- Current schema version: `1`
- Supported object types: `cube`, `sphere`
- Import validates format, version, IDs, object types, numeric values, scale range, and object-count limit.
- The project file is portable JSON; it does not embed external 3D assets.

## Build and validation
A GitHub Actions workflow exists at `.github/workflows/rick-v6-android.yml` and is configured to build a debug APK when relevant files change. The source commits are not proof that the workflow passed. No successful build or physical-device test has been confirmed yet.

## Known prototype limitations
- Selection is based on projected object centers, not geometry ray casting.
- In Select mode, one-finger drag orbits the camera; transform modes drag the selected object.
- Move is XZ-plane only; rotate is Y-only; scale is uniform.
- Rendering is a simple unlit prototype, not PBR.
- Undo/Redo history is in-memory and is cleared when the app process restarts; save files preserve the scene, not the history.
