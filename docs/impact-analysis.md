# Impact Analysis: Undo/Redo Drawing Actions

## Table 1

| Package name | # of classes | Comments |
|---|---|---|
| org.jhotdraw.action.edit | 2 | UndoAction and RedoAction — both CHANGED. Wrapper actions that delegate via getRealUndoAction()/getRealRedoAction(). The ActionMap overwrite causes them to find themselves instead of UndoRedoManager's inner actions, breaking the delegation chain. |
| org.jhotdraw.action | 1 | AbstractViewAction — PROPAGATES. Base class for UndoAction/RedoAction, provides view-aware listener infrastructure. Does not cause the bug itself but propagates impact through inheritance. |
| org.jhotdraw.undo | 2 | UndoRedoManager — CHANGED: creates the correct inner undo/redo actions that should remain in the ActionMap. CompositeEdit — UNCHANGED: groups edits, not involved in ActionMap registration. |
| org.jhotdraw.app | 4 | DefaultApplicationModel — CHANGED (root cause): createActionMap() lines 87–88 create new UndoAction/RedoAction that overwrite the view's correct actions. AbstractApplicationModel — PROPAGATES. AbstractApplication — PROPAGATES. SDIApplication — PROPAGATES. These three form the initialization chain that triggers the overwrite. |
| org.jhotdraw.samples.draw | 1 | DrawView — CHANGED. initActions() correctly registers UndoRedoManager's inner actions into the ActionMap, but this is subsequently overwritten by DefaultApplicationModel. |
| org.jhotdraw.draw | 2 | AbstractDrawing and Drawing (interface) — both UNCHANGED. Handle UndoableEditListener registration and event firing at the model level, independent of the ActionMap bug. |
| org.jhotdraw.draw.event | 6 | AttributeChangeEdit, TransformEdit, SetBoundsEdit, TransformRestoreEdit, BezierNodeEdit, CompositeFigureEdit — all UNCHANGED. These are UndoableEdit implementations that record/restore figure state. They operate at the model layer and are unaffected by the ActionMap overwrite. |
| **Total** | **18** | **5 CHANGED, 4 PROPAGATES, 9 UNCHANGED** |

### Trace of the algorithm execution

| Step | Class | Mark | New classes added to EIS |
|------|-------|------|------------------------|
| 1 | UndoAction | CHANGED | AbstractViewAction |
| 2 | RedoAction | CHANGED | — (AbstractViewAction already in EIS) |
| 3 | UndoRedoManager | CHANGED | — |
| 4 | DrawView | CHANGED | AbstractView → inspected → PROPAGATES, no new |
| 5 | AbstractApplication | PROPAGATES | — |
| 6 | SDIApplication | PROPAGATES | — |
| 7 | DefaultApplicationModel | CHANGED | AbstractApplicationModel → inspected → PROPAGATES, no new |
| 8 | AbstractDrawing | UNCHANGED | — |
| 9 | Drawing | UNCHANGED | — |
| 10 | CompositeEdit | UNCHANGED | — |
| 11 | AttributeChangeEdit | UNCHANGED | — |
| 12 | TransformEdit | UNCHANGED | — |
| 13 | SetBoundsEdit | UNCHANGED | — |
| 14 | TransformRestoreEdit | UNCHANGED | — |
| 15 | BezierNodeEdit | UNCHANGED | — |
| 16 | CompositeFigureEdit | UNCHANGED | — |
| 17 | AbstractViewAction | PROPAGATES | — |
| 18 | AbstractView | PROPAGATES | — |
| — | **EIS exhausted** | | |
