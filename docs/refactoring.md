# Refactoring: Undo/Redo Feature (Lab 4)

## 1. Introduction

This document describes the refactoring work performed on the Undo/Redo feature area of JHotDraw as part of Lab 4 (Prefactoring/Postfactoring - Phases 4 and 6 of the Software Change model). The refactorings are based on code smells identified in the classes discovered during Concept Location (Lab 2) and Impact Analysis (Lab 3).

The change request being addressed is:

> *"As a user, I want to undo and redo my drawing actions so that I can correct mistakes easily."*

The 5 CHANGED classes from the Impact Analysis (Lab 3) are the primary targets of this refactoring work:

- `UndoAction` (jhotdraw-actions)
- `RedoAction` (jhotdraw-actions)
- `UndoRedoManager` (jhotdraw-utils)
- `DrawView` (jhotdraw-samples-misc)
- `DefaultApplicationModel` (jhotdraw-app)

---

## 2. Code Smells Identified

Code smells are surface-level symptoms in the source code that indicate deeper structural or design problems (Fowler, Chapter 3; Kerievsky [Ker05], Chapter 4). They are not bugs - the code may still function - but they make the code harder to understand, modify, and maintain, which directly increases the cost and risk of future changes.

The following code smells were identified in the Undo/Redo feature classes:

### Smell 1: Duplicated Code - UndoAction and RedoAction

**Where:** `UndoAction.java` and `RedoAction.java` in `org.jhotdraw.action.edit`

**Description:** These two classes are nearly identical - 115 lines each with the same structure. Both follow the exact same delegation pattern: they look up a "real" action from the active view's ActionMap and delegate `actionPerformed()`, `updateEnabledState()`, `updateView()`, `installViewListeners()`, and `uninstallViewListeners()` to it. The only difference between the two is the action ID string: `"edit.undo"` vs `"edit.redo"`, and the variable names (`getRealUndoAction` vs `getRealRedoAction`).

**Why this is a problem:** Duplicated Code (Fowler) is one of the most fundamental code smells. If a bug is found in the delegation logic, it must be fixed in two places. If a new feature is added (e.g., a listener change), both files must be updated identically. This violates the DRY (Don't Repeat Yourself) principle and increases the risk of inconsistent behavior when one copy is updated but the other is forgotten.

### Smell 2: Long Method - DrawView Constructor

**Where:** `DrawView()` constructor in `org.jhotdraw.samples.draw.DrawView`

**Description:** The constructor was 30 lines long and performed at least four distinct responsibilities: (1) configuring the scroll pane layout and border, (2) creating and wiring the UndoRedoManager with listeners and the ActionMap, (3) creating and styling placard buttons (zoom and grid toggle), and (4) adding the placard panel to the scroll pane.

**Why this is a problem:** Long Method (Fowler) makes the code harder to understand at a glance. A reader must parse through all 30 lines to understand what the constructor does, rather than seeing a high-level outline. It also makes the constructor harder to maintain - changes to button styling require wading through undo manager setup code, and vice versa. According to Clean Code (Martin), functions should be small and do one thing.

### Smell 3: Duplicated Code - Placard Button Styling

**Where:** Inside the `DrawView()` constructor (before refactoring)

**Description:** Two consecutive blocks of code applied identical styling to two different buttons:

```java
pButton.putClientProperty("Quaqua.Button.style", "placard");
pButton.putClientProperty("Quaqua.Component.visualMargin", new Insets(0, 0, 0, 0));
pButton.setFont(UIManager.getFont("SmallSystemFont"));
```

This three-line block appeared twice - once for the zoom button, once for the grid toggle button.

**Why this is a problem:** This is Duplicated Code within a single method. If the placard styling needs to change (e.g., a new client property or a different font), it must be changed in two places. This is the kind of subtle duplication that leads to inconsistencies when one copy is updated but the other is missed.

### Smell 4: Inconsistent Error Handling - UndoRedoManager Inner Actions

**Where:** Inner classes `UndoAction` and `RedoAction` inside `UndoRedoManager.java`

**Description:** The inner `UndoAction` caught `CannotUndoException` and reported it using `System.err.println("Cannot undo: " + e)` followed by `e.printStackTrace()`. The inner `RedoAction` caught `CannotRedoException` and reported it using `System.out.println("Cannot redo: " + e)` with *no* stack trace. Two nearly identical error paths used different output streams (stderr vs stdout) and different levels of detail.

**Why this is a problem:** Inconsistent error handling is a form of Duplicated Code that has drifted apart over time. It makes debugging harder - a developer looking for error output might check only stderr and miss the redo errors going to stdout. It also violates Clean Code's principle that error handling should be consistent and not scattered across ad hoc print statements.

### Smell 5: Duplicated Code - undo()/redo()/undoOrRedo() Boilerplate

**Where:** `UndoRedoManager.java`, methods `undo()`, `redo()`, and `undoOrRedo()`

**Description:** All three methods followed the exact same pattern:

```java
undoOrRedoInProgress = true;
try {
    super.xxx();  // only this line differs
} finally {
    undoOrRedoInProgress = false;
    updateActions();
}
```

The four boilerplate lines (set flag, try-finally, reset flag, update actions) were copy-pasted three times, with only the `super.xxx()` call changing.

**Why this is a problem:** Tripled Duplicated Code. The progress-flag management is a cross-cutting concern that should be expressed once. If the behavior needs to change (e.g., adding logging or a new post-operation step), all three methods must be updated identically.

### Smell 6: Duplicated Code - updateActions() Pattern

**Where:** `UndoRedoManager.updateActions()` method

**Description:** The method updated the undo action and redo action with identical logic - check `canUndo()`/`canRedo()`, set enabled state, choose a label, set NAME and SHORT_DESCRIPTION. The same if/else/putValue pattern appeared twice.

**Why this is a problem:** Duplicated Code within a single method. The pattern for updating an action is the same for undo and redo - it should be expressed once with parameters.

### Smell 7: Primitive Obsession - Magic String for Property Name

**Where:** `UndoRedoManager.setHasSignificantEdits()` method

**Description:** The property name `"hasSignificantEdits"` was used as a raw string literal in `firePropertyChange("hasSignificantEdits", ...)`. Any listener subscribing to this property must use the same exact string, with no compiler assistance.

**Why this is a problem:** Primitive Obsession (using a primitive string where a constant would be more appropriate). A typo in the string - e.g., `"hasSignificantEdit"` - would silently fail at runtime with no compiler error. This is exactly the kind of bug that takes hours to find.

### Smell 8: Comments (as Smell) - Redundant Comments

**Where:** Throughout `UndoRedoManager.java`

**Description:** Several comments simply restated what the code already said, e.g.:

- `/** The undo action instance. */` above `private UndoAction undoAction;`
- `/** Invoked when an action occurs. */` above `actionPerformed()`
- `/** Creates new UndoRedoManager */` above the constructor

**Why this is a problem:** Comments as Smell (Fowler). Redundant comments add visual clutter without adding information. They also drift out of sync with the code over time, becoming actively misleading. As Robert C. Martin states: "Don't comment bad code - rewrite it." If a comment merely restates the code, the code is either already clear (making the comment unnecessary) or needs to be refactored (making the comment a band-aid).

---

## 3. Refactoring Plan and Strategy

The overall strategy follows the course's Phased Model of Software Change:

- **Prefactoring (Phase 4):** Restructure the code to make the undo/redo bug fix easier and safer to implement.
- **Bug Fix (Phase 5 - Actualization):** Fix the ActionMap overwrite that prevents undo/redo from functioning.
- **Postfactoring (Phase 6):** Clean up remaining code smells to leave the codebase better than we found it.

Each refactoring preserves external behavior - tests are run after every change to verify this.

---

## 4. Refactorings Applied

### Refactoring 1: Extract Superclass (for Duplicated Code - UndoAction/RedoAction)

**Code Smell:** Duplicated Code (Smell 1)

**Refactoring Pattern:** *Extract Superclass* combined with *Template Method* (Kerievsky [Ker05]; Fowler - "Extract Superclass", "Form Template Method")

**Strategy:** Since `UndoAction` and `RedoAction` share identical logic with only the action ID differing, the common behavior is pulled into an abstract superclass. The varying part (the action ID string) becomes an abstract method that each subclass overrides - this is the Template Method pattern.

**What changed:**

- **NEW FILE:** `AbstractUndoRedoAction.java` - Contains all shared logic: the `PropertyChangeListener`, `updateEnabledState()`, `updateView()`, `installViewListeners()`, `uninstallViewListeners()`, `actionPerformed()`, and `getRealAction()`. Declares abstract method `getActionId()`.
- **MODIFIED:** `UndoAction.java` - Reduced from 115 lines to 35 lines. Now extends `AbstractUndoRedoAction` and only provides `ID = "edit.undo"` and overrides `getActionId()`.
- **MODIFIED:** `RedoAction.java` - Same reduction. Extends `AbstractUndoRedoAction` with `ID = "edit.redo"`.

**Reasoning:** This is the textbook application of *Extract Superclass* when two sibling classes share nearly identical code. The Template Method pattern is the natural fit because the algorithm (delegate to a view-specific action) is the same - only the "which action ID" step varies. This means future bugs or enhancements to the delegation logic need to be fixed in exactly one place.

**Before (UndoAction.java - 115 lines, showing key duplication):**
```java
public class UndoAction extends AbstractViewAction {
    public static final String ID = "edit.undo";
    private PropertyChangeListener redoActionPropertyListener = new PropertyChangeListener() { ... };
    protected void updateEnabledState() { ... }
    protected void updateView(View oldValue, View newValue) { ... }
    protected void installViewListeners(View p) { ... }
    protected void uninstallViewListeners(View p) { ... }
    public void actionPerformed(ActionEvent e) { ... }
    private Action getRealUndoAction() { ... }
}
```

**After (UndoAction.java - 35 lines):**
```java
public class UndoAction extends AbstractUndoRedoAction {
    public static final String ID = "edit.undo";
    public UndoAction(Application app, View view) { super(app, view); }
    @Override protected String getActionId() { return ID; }
}
```

---

### Refactoring 2: Extract Method (for Long Method - DrawView Constructor)

**Code Smell:** Long Method (Smell 2)

**Refactoring Pattern:** *Extract Method* (Fowler - "Extract Method"; Kerievsky [Ker05])

**Strategy:** Identify the distinct responsibilities within the 30-line constructor and extract each into a well-named private method. The constructor becomes a high-level outline that reads like a narrative.

**What changed:**

- **MODIFIED:** `DrawView.java`
  - Constructor reduced from 30 lines to 5 lines
  - Extracted `initScrollPane()` - scroll pane layout and border configuration
  - Extracted `initUndoManager()` - UndoRedoManager creation, listener wiring, ActionMap registration
  - Extracted `initPlacardPanel()` - placard panel creation with zoom and grid buttons

**Reasoning:** The Stepdown Rule (Clean Code, Martin) says code should read like a top-down narrative: the constructor tells you *what* happens (init scroll pane, set editor, init undo manager, init placard panel), and each extracted method tells you *how*. This makes the code much easier to comprehend and modify. A developer looking for undo-related initialization goes straight to `initUndoManager()` without wading through button styling code.

**Before:**
```java
public DrawView() {
    initComponents();
    scrollPane.setLayout(new PlacardScrollPaneLayout());
    scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
    setEditor(new DefaultDrawingEditor());
    undo = new UndoRedoManager();
    view.setDrawing(createDrawing());
    view.getDrawing().addUndoableEditListener(undo);
    initActions();
    undo.addPropertyChangeListener(...);
    // ... 15 more lines of button creation ...
}
```

**After:**
```java
public DrawView() {
    initComponents();
    initScrollPane();
    setEditor(new DefaultDrawingEditor());
    initUndoManager();
    initPlacardPanel();
}
```

---

### Refactoring 3: Extract Method (for Duplicated Code - Placard Button Styling)

**Code Smell:** Duplicated Code (Smell 3)

**Refactoring Pattern:** *Extract Method* (Fowler)

**Strategy:** Extract the three repeated styling lines into a single method `configurePlacardButton(AbstractButton)` that returns the styled button.

**What changed:**

- **MODIFIED:** `DrawView.java` - New private method `configurePlacardButton()` replaces two identical inline styling blocks.

**Reasoning:** DRY - styling logic expressed once. If the placard button style changes, there is exactly one place to modify.

**Before:**
```java
pButton = ButtonFactory.createZoomButton(view);
pButton.putClientProperty("Quaqua.Button.style", "placard");
pButton.putClientProperty("Quaqua.Component.visualMargin", new Insets(0, 0, 0, 0));
pButton.setFont(UIManager.getFont("SmallSystemFont"));
// ... same 3 lines repeated for grid button ...
```

**After:**
```java
AbstractButton zoomButton = configurePlacardButton(ButtonFactory.createZoomButton(view));
AbstractButton gridButton = configurePlacardButton(ButtonFactory.createToggleGridButton(view));
```

---

### Refactoring 4: Replace Ad Hoc Logging with java.util.logging (for Inconsistent Error Handling)

**Code Smell:** Inconsistent Error Handling (Smell 4)

**Refactoring Pattern:** *Introduce Consistent Error Handling* (Clean Code principle: use exceptions/logging consistently)

**Strategy:** Replace the mismatched `System.err.println` / `System.out.println` calls in UndoRedoManager's inner actions with `java.util.logging.Logger`, using the same severity level (`Level.WARNING`) and including the exception for both undo and redo failures.

**What changed:**

- **MODIFIED:** `UndoRedoManager.java`
  - Added `private static final Logger LOG = Logger.getLogger(UndoRedoManager.class.getName());`
  - Inner `UndoAction.actionPerformed()`: `System.err.println` + `printStackTrace()` → `LOG.log(Level.WARNING, "Cannot undo", e)`
  - Inner `RedoAction.actionPerformed()`: `System.out.println` → `LOG.log(Level.WARNING, "Cannot redo", e)`
  - DEBUG statements also converted from `System.out.println` to `LOG.log(Level.FINE, ...)`

**Reasoning:** Using `java.util.logging` provides consistent, configurable error reporting. Both inner actions now handle errors identically - same stream, same severity, same stack trace inclusion. This also follows the Clean Code principle of preferring structured logging over raw console output.

---

### Refactoring 5: Extract Method (for Duplicated Code - undo/redo/undoOrRedo Boilerplate)

**Code Smell:** Duplicated Code (Smell 5)

**Refactoring Pattern:** *Extract Method* (Fowler)

**Strategy:** Extract the four repeated boilerplate lines (set flag, try, finally reset flag + update actions) into a private method `executeWithProgressFlag(Runnable)`. Each of the three methods becomes a one-liner using a Java 8 method reference.

**What changed:**

- **MODIFIED:** `UndoRedoManager.java`
  - New private method `executeWithProgressFlag(Runnable operation)`
  - `undo()` → `executeWithProgressFlag(super::undo)`
  - `redo()` → `executeWithProgressFlag(super::redo)`
  - `undoOrRedo()` → `executeWithProgressFlag(super::undoOrRedo)`

**Reasoning:** The progress-flag management is a cross-cutting concern unrelated to the specific operation (undo vs redo). Extracting it makes each method's intent crystal clear and ensures the flag management logic is defined exactly once. If new behavior is needed (e.g., logging, event firing), it only needs to be added in one place.

**Before:**
```java
public void undo() throws CannotUndoException {
    undoOrRedoInProgress = true;
    try {
        super.undo();
    } finally {
        undoOrRedoInProgress = false;
        updateActions();
    }
}
// same pattern repeated for redo() and undoOrRedo()
```

**After:**
```java
public void undo() throws CannotUndoException {
    executeWithProgressFlag(super::undo);
}
```

---

### Refactoring 6: Extract Method (for Duplicated Code - updateActions Pattern)

**Code Smell:** Duplicated Code (Smell 6)

**Refactoring Pattern:** *Extract Method* (Fowler)

**Strategy:** Extract the repeated if/else/putValue pattern for updating an action into `updateAction(AbstractAction, boolean, String, String)`.

**What changed:**

- **MODIFIED:** `UndoRedoManager.java` - `updateActions()` now calls `updateAction()` twice instead of containing two duplicated if/else blocks.

**Before:**
```java
if (canUndo()) {
    undoAction.setEnabled(true);
    label = getUndoPresentationName();
} else {
    undoAction.setEnabled(false);
    label = labels.getString("edit.undo.text");
}
undoAction.putValue(Action.NAME, label);
undoAction.putValue(Action.SHORT_DESCRIPTION, label);
// same pattern for redoAction
```

**After:**
```java
updateAction(undoAction, canUndo(), getUndoPresentationName(), "edit.undo.text");
updateAction(redoAction, canRedo(), getRedoPresentationName(), "edit.redo.text");
```

---

### Refactoring 7: Replace Magic String with Constant

**Code Smell:** Primitive Obsession / Magic String (Smell 7)

**Refactoring Pattern:** *Replace Magic Number/String with Symbolic Constant* (Fowler - "Replace Magic Number with Symbolic Constant", adapted for strings)

**Strategy:** Introduce a `public static final String` constant for the property name.

**What changed:**

- **MODIFIED:** `UndoRedoManager.java`
  - Added: `public static final String HAS_SIGNIFICANT_EDITS_PROPERTY = "hasSignificantEdits";`
  - `firePropertyChange("hasSignificantEdits", ...)` → `firePropertyChange(HAS_SIGNIFICANT_EDITS_PROPERTY, ...)`

**Reasoning:** The constant provides compile-time checking (IDE autocomplete, find-usages, safe rename) and a single source of truth for the property name. Any listener registering for this property can reference the constant instead of duplicating the string.

---

### Refactoring 8: Remove Redundant Comments

**Code Smell:** Comments (as Smell) (Smell 8)

**Refactoring Pattern:** *Remove Dead Code / Redundant Comments* (Clean Code principle: "Don't comment bad code - rewrite it")

**Strategy:** Delete comments that merely restate the code. Keep comments that explain *why* something is done or document the public API with meaningful Javadoc.

**What changed:**

- **MODIFIED:** `UndoRedoManager.java`
  - Removed: `/** The undo action instance. */` (above `private UndoAction undoAction;` - the field name says this already)
  - Removed: `/** The redo action instance. */` (same reason)
  - Removed: `/** Invoked when an action occurs. */` (above `actionPerformed()` - obvious from the method name and the `@Override` annotation)
  - Removed: `/** Creates new UndoRedoManager */` (above the constructor - redundant)
  - Kept: Javadoc for public methods that explains behavior, parameters, or contracts
  - Kept: Comment on `undoOrRedoInProgress` flag explaining *why* it exists

**Reasoning:** Comments that restate the code are noise. They increase the maintenance burden (must be updated when code changes) and can become misleading when they drift out of sync. Self-documenting code with meaningful names is always preferable.

---

## 5. Bug Fix: ActionMap Overwrite (Phase 5 - Actualization)

**File:** `DefaultApplicationModel.java` (`org.jhotdraw.app`)

**Root cause (identified in Concept Location, Lab 2):** `DefaultApplicationModel.createActionMap()` unconditionally created new `UndoAction` and `RedoAction` wrapper instances, which overwrote the real `UndoRedoManager` inner actions that `DrawView.initActions()` had already registered in the view's ActionMap.

**Consequence:** When the wrapper `UndoAction.actionPerformed()` called `getRealUndoAction()`, it looked up the ActionMap and found *itself* (the wrapper) instead of the manager's inner action. The guard condition `realUndoAction != this` prevented execution, silently skipping the undo operation.

**Fix:** Added a conditional check - only create new wrapper actions if the view doesn't already have undo/redo actions registered in its ActionMap:

```java
if (v == null || v.getActionMap().get(UndoAction.ID) == null) {
    m.put(UndoAction.ID, new UndoAction(a, v));
}
if (v == null || v.getActionMap().get(RedoAction.ID) == null) {
    m.put(RedoAction.ID, new RedoAction(a, v));
}
```

This preserves the original behavior for views that do *not* register their own undo/redo actions (the `v == null` or missing-action case), while correctly preserving the `UndoRedoManager`'s inner actions for views like `DrawView` that do.

---

## 6. Files Changed Summary

| File | Module | Change Type |
|------|--------|------------|
| `AbstractUndoRedoAction.java` | jhotdraw-actions | **NEW** - Extracted superclass for UndoAction/RedoAction |
| `UndoAction.java` | jhotdraw-actions | **CHANGED** - Simplified to extend AbstractUndoRedoAction |
| `RedoAction.java` | jhotdraw-actions | **CHANGED** - Simplified to extend AbstractUndoRedoAction |
| `UndoRedoManager.java` | jhotdraw-utils | **CHANGED** - Consistent logging, extracted methods, constant, cleaned comments |
| `DrawView.java` | jhotdraw-samples-misc | **CHANGED** - Constructor broken into focused methods |
| `DefaultApplicationModel.java` | jhotdraw-app | **CHANGED** - Bug fix: conditional undo/redo action creation |

---

## 7. Verification

All existing tests pass after each refactoring step:

```
Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

No new test failures introduced. External behavior is preserved - this is the fundamental contract of refactoring.
