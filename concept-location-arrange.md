# Concept Location: Arrange (Send to Front / Send to Back) Feature

## Method

The Arrange feature was located using source-code search in VS Code and can be confirmed at runtime with the VS Code Java Debugger.  
The selected change request is:

**Arrange: Send to Front and Send to Back Actions**

The project was searched for the strings:

- `bringToFront`
- `sendToBack`
- `edit.bringToFront`

This revealed the controller classes, model interfaces, and concrete drawing/container implementations involved in the feature.

---

## Concept Location Steps

1. Searched for **"Send to Front"** in the project.
2. The initial hits were in `Labels.properties`, which only contain UI labels.
3. Then searched for:
   - `bringToFront`
   - `sendToBack`
4. This revealed the main feature-related classes:
   - `BringToFrontAction`
   - `SendToBackAction`
   - `Drawing`
   - `QuadTreeDrawing`
   - `AbstractCompositeFigure`
   - `QuadTreeCompositeFigure`
5. The search results showed that the action classes delegate the request to the drawing model, where the actual z-order change is performed.

---

## Observed Feature Structure

The feature starts from controller/action classes and then delegates into the drawing model:

- `BringToFrontAction` handles the UI action for **Send to Front**
- `SendToBackAction` handles the UI action for **Send to Back**
- These actions operate on the selected `Figure` objects in the current `DrawingView`
- The actual reordering logic is implemented in domain/model classes such as `Drawing` and composite figure classes

This means the feature begins in the UI layer, but the actual domain behavior is performed in the model layer.

---

## Initial Set of Classes

| # | Domain Class | Module | Responsibility |
|---|---|---|---|
| 1 | `Figure` | jhotdraw-core | Represents a drawable object in the drawing. It is the domain object whose z-order is changed when the user sends it to the front or back. |
| 2 | `Drawing` | jhotdraw-core | Core drawing model interface. Declares operations such as `bringToFront(Figure)` and `sendToBack(Figure)` which define the feature at the model level. |
| 3 | `QuadTreeDrawing` | jhotdraw-core | Concrete implementation of `Drawing`. Performs the actual reordering of figures in the drawing and updates the drawing state after the change. |
| 4 | `AbstractCompositeFigure` | jhotdraw-core | Base container for child figures. Supports reordering of child figures inside grouped/container figures by changing their internal order. |
| 5 | `QuadTreeCompositeFigure` | jhotdraw-core | Concrete composite/container implementation. Applies the same front/back ordering behavior for figures stored inside a composite structure. |

---

## Supporting Controller Classes

The following classes are part of the feature flow, but they act mainly as controllers rather than core domain classes:

| # | Class | Module | Responsibility |
|---|---|---|---|
| 1 | `BringToFrontAction` | jhotdraw-core | Controller action for the **Send to Front** command. Retrieves the selected figures and delegates the operation to the drawing model. |
| 2 | `SendToBackAction` | jhotdraw-core | Controller action for the **Send to Back** command. Retrieves the selected figures and delegates the operation to the drawing model. |

These classes are useful for tracing the feature, but the main domain logic belongs to the model classes listed in the previous table.

---

## Intended Runtime Flow

When the feature works, the execution flow is conceptually:

```text
User clicks Arrange > Send to Front
  -> BringToFrontAction
    -> gets selected figures from the current DrawingView
    -> delegates to the Drawing model
      -> Drawing.bringToFront(figure)
        -> concrete implementation reorders the figure
           (e.g. in QuadTreeDrawing or a composite figure container)
        -> drawing is refreshed