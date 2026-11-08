# V6.0 Implementation Status

This directory now contains the first Android application source for Rick 3D Engine V6, migrated from the V0.4 prototype into an independent Gradle application.

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
- Basic tool and inspector panels.

## Not yet implemented
- True ray/triangle picking and axis gizmos.
- Three-axis move/rotate/scale.
- Real scene tree and numeric inspector.
- Save/open project and Undo/Redo.
- GLB/glTF import, terrain, materials, physics, scripting.
- Release APK signing and device validation.

## Build
A GitHub Actions workflow was added at `.github/workflows/rick-v6-android.yml`. It attempts a debug build and publishes the APK as a workflow artifact. A successful source upload is not proof of a successful build; check the Actions run before installing.

## Known prototype limitations
- Selection is based on projected object centers, not geometry ray casting.
- The editor currently routes a one-finger drag to camera orbit in Select mode; transform modes drag the selected object.
- Move is XZ-plane only; rotate is Y-only; scale is uniform.
- Rendering is a simple unlit prototype, not PBR.
