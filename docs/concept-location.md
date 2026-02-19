# Concept Location: Undo/Redo Feature

## Method

The Undo/Redo feature was traced at runtime using the VS Code Java Debugger. The application was launched via the `org.jhotdraw.samples.draw.Main` entry point. Breakpoints were placed on key methods, and stack traces were captured using `java.util.Arrays.toString(Thread.currentThread().getStackTrace())` in the Debug Console.

## Debugging Steps

1. A breakpoint was set on `UndoAction.actionPerformed()` (line 106) in the `jhotdraw-actions` module.
2. The application was started in debug mode.
3. A shape was drawn on the canvas, then Edit > Undo was clicked.
4. The debugger paused at the breakpoint. The stack trace was captured.
5. Additional breakpoints were placed on `UndoRedoManager.undo()` (line 256) and on the `undo()` methods of various edit classes (`SetBoundsEdit`, `TransformEdit`, etc.). These breakpoints were never hit.
6. Evaluating `getRealUndoAction()` in the Debug Console revealed that the returned object was an `UndoAction` instance rather than the expected `UndoRedoManager` inner action.

## Captured Stack Trace

When Edit > Undo is clicked, the following call chain executes:

```
java.util.Arrays.toString(Thread.currentThread().getStackTrace())

Thread.getStackTrace()
UndoAction.actionPerformed(UndoAction.java:106)       <-- JHotDraw entry point
AbstractButton.fireActionPerformed(AbstractButton.java:1972)
AbstractButton$Handler.actionPerformed(AbstractButton.java:2313)
DefaultButtonModel.fireActionPerformed(DefaultButtonModel.java:405)
DefaultButtonModel.setPressed(DefaultButtonModel.java:262)
AbstractButton.doClick(AbstractButton.java:374)
BasicMenuItemUI.doClick(BasicMenuItemUI.java:985)
BasicMenuItemUI$Handler.mouseReleased(BasicMenuItemUI.java:1029)
... (Swing event dispatch)
EventDispatchThread.run(EventDispatchThread.java:90)
```

Only `UndoAction` is a JHotDraw class in this trace. Everything else is Swing event dispatch plumbing.

## ActionMap Overwrite Problem

During debugging, `UndoRedoManager.undo()` was never reached. Inspecting `getRealUndoAction()` in the Debug Console showed it returned an `UndoAction` controller instance instead of the `UndoRedoManager`'s private inner undo action. This means the guard condition at line 107 (`realUndoAction != this`) prevents execution, and the undo is silently skipped.

The root cause is an ActionMap overwrite during initialization:

1. `DrawView()` constructor calls `initActions()`, which registers the `UndoRedoManager`'s inner undo action in the view's ActionMap:
   ```java
   // DrawView.java:131
   getActionMap().put(UndoAction.ID, undo.getUndoAction());
   ```

2. After the constructor returns, `AbstractApplication.createView()` replaces the entire ActionMap:
   ```java
   // AbstractApplication.java:176
   v.setActionMap(createViewActionMap(v));
   ```

3. `SDIApplication.createViewActionMap()` delegates to `DefaultApplicationModel.createActionMap()`, which registers a new `UndoAction` controller under the same key:
   ```java
   // DefaultApplicationModel.java:87
   m.put(UndoAction.ID, new UndoAction(a, v));
   ```

This overwrites the `UndoRedoManager`'s inner action with a plain `UndoAction` controller, breaking the delegation chain.

## Initial Set of Classes

| # | Domain Class | Module | Responsibility |
|---|---|---|---|
| 1 | `UndoAction` | jhotdraw-actions | Controller action bound to `Edit > Undo`. Looks up the real undo action from the active view's ActionMap and delegates to it. Located in `org.jhotdraw.action.edit`. |
| 2 | `RedoAction` | jhotdraw-actions | Controller action bound to `Edit > Redo`. Same delegation pattern as `UndoAction`. Located in `org.jhotdraw.action.edit`. |
| 3 | `UndoRedoManager` | jhotdraw-utils | Extends `javax.swing.undo.UndoManager`. Maintains the undo/redo edit stack and exposes private inner `UndoAction`/`RedoAction` classes via `getUndoAction()`/`getRedoAction()`. Located in `org.jhotdraw.undo`. |
| 4 | `DrawView` | jhotdraw-samples-misc | The view for the Draw sample application. Creates an `UndoRedoManager` instance, registers it as an `UndoableEditListener` on the `Drawing`, and wires the undo/redo actions into the ActionMap via `initActions()`. Located in `org.jhotdraw.samples.draw`. |
| 5 | `AbstractApplication` | jhotdraw-app | After constructing the view, calls `v.setActionMap(createViewActionMap(v))` which overwrites the ActionMap that `DrawView.initActions()` had populated. Located in `org.jhotdraw.app`. |
| 6 | `SDIApplication` | jhotdraw-app | Implements `createViewActionMap()`. Builds the view's ActionMap using `DefaultApplicationModel.createActionMap()` and chains it with an intermediate map and the application-level map. Located in `org.jhotdraw.app`. |
| 7 | `DefaultApplicationModel` | jhotdraw-app | Creates per-view action maps via `createActionMap()`. Puts a new `UndoAction(a, v)` controller under `"edit.undo"`, which replaces the `UndoRedoManager`'s inner action that `DrawView` had registered. Located in `org.jhotdraw.app`. |
| 8 | `AbstractDrawing` | jhotdraw-core | Implements the `Drawing` interface. Manages `UndoableEditListener` registration and fires `UndoableEditEvent` when figures are modified. This is how edits reach the `UndoRedoManager`. Located in `org.jhotdraw.draw`. |
| 9 | `Drawing` | jhotdraw-core | Interface that defines `addUndoableEditListener()` and `fireUndoableEditHappened()`. Acts as the mediator between figure modifications and the undo stack. Located in `org.jhotdraw.draw`. |
| 10 | `CompositeEdit` | jhotdraw-utils | Groups multiple edits into a single undo/redo step. Located in `org.jhotdraw.undo`. |
| 11 | `AttributeChangeEdit` | jhotdraw-core | Records old/new values of figure attributes and restores them on undo. Located in `org.jhotdraw.draw.event`. |
| 12 | `TransformEdit` | jhotdraw-core | Records geometric transformations and applies the inverse on undo. Located in `org.jhotdraw.draw.event`. |
| 13 | `SetBoundsEdit` | jhotdraw-core | Records figure bounds changes and restores them on undo. Located in `org.jhotdraw.draw.event`. |
| 14 | `TransformRestoreEdit` | jhotdraw-core | Restores full figure state for lossy transformations (rotation, scaling). Located in `org.jhotdraw.draw.event`. |
| 15 | `BezierNodeEdit` | jhotdraw-core | Records changes to Bezier curve control points. Located in `org.jhotdraw.draw.event`. |
| 16 | `CompositeFigureEdit` | jhotdraw-core | Compound edit that notifies figures via `willChange()`/`changed()` during undo/redo. Located in `org.jhotdraw.draw.event`. |

## Intended Runtime Flow (from source code analysis)

When working correctly, the undo chain should be:

```
User clicks Edit > Undo
  -> UndoAction.actionPerformed()                 [jhotdraw-actions]
    -> getRealUndoAction()                         looks up ActionMap
    -> realUndoAction.actionPerformed()            should be UndoRedoManager's inner action
      -> UndoRedoManager.undo()                    [jhotdraw-utils]
        -> super.undo()                            pops edit from stack
          -> edit.undo()                           e.g. TransformEdit, SetBoundsEdit
            -> restores figure state
```

When a figure is modified, edits enter the system through:

```
Tool/Handle modifies a Figure
  -> AbstractDrawing.fireUndoableEditHappened(edit)  [jhotdraw-core]
    -> UndoRedoManager.addEdit(edit)                 adds to undo stack
```
