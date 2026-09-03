# Implementation Plan - Hierarchical Study & Unified Library

This plan addresses UI gaps in the library hierarchy, fixes the "Add Flashcard" workflow, and refines the "Play Now" science-based study modes.

## User Review Required

> [!IMPORTANT]
> **Library Unified View**: I will merge Folders (Categories) and Files (Flashcards) into a single list. When you open a Chapter or Topic, you will see both sub-folders and the flashcards within that folder.

> [!TIP]
> **Context-Aware Adding**: When you click the `+` button, the app will now know exactly which folder you are currently viewing. New flashcards and sub-folders will be automatically placed into that directory.

## Proposed Changes

### 1. Unified Library Hierarchy
- **[NEW] `LibraryItem.java`**: A wrapper class/interface to handle both `Category` and `Flashcard` objects in the same list.
- **[MODIFY] `LibraryFragment.java`**:
    - Update to observe both Sub-Categories and Flashcards for the current `categoryId`.
    - Pass the current `parentId` to the `MainActivity` so the FAB knows where to add new items.
- **[MODIFY] `CategoryAdapter.java` ➔ `LibraryAdapter.java`**:
    - Support two view types: `FOLDER` and `FILE`.
    - Display card counts and mastery for folders.
    - Display the question/answer and a "Due" badge for flashcards.

### 2. Context-Aware Add & Fixes
- **[MODIFY] `MainActivity.java`**:
    - Maintain a `currentPathId` state based on fragment navigation.
    - Update the "Add Flashcard" and "Add Category" actions to use this ID.
- **[MODIFY] `AddFlashcardActivity.java`**:
    - Remove the manual `Category` text input.
    - Fix the validation error by ensuring `categoryId` is correctly passed and handled.
- **[MODIFY] `CategoryDao.java`**: Add a query to count cards within a specific topic for numbering.

### 3. "Play Now" Study Modes Upgrade
- **[MODIFY] `StudyActivity.java`**:
    - Refine the mode selection dialog to show two clear science-based options:
        1. **Practice Mode**: Study all cards (including those not due) without altering their next review schedule.
        2. **Study Ahead**: Study early and progress the SRS stages immediately.
    - Add card numbering (e.g., "Card 5 of 20") to the study UI.

### 4. UI Polish & Academic Mastery
- Add mastery ratings to all levels of sub-categories.
- Improve typography and spacing for a "Modern" look.

## Verification Plan

### Automated Tests
- Test hierarchical folder creation: Verify a new category added inside "Math" has the correct `parentId`.
- Test SRS track jumps in "Study Ahead" vs "Practice" mode.

### Manual Verification
- **Drill-down**: Navigate to a Topic and verify both sub-topics and flashcards are visible.
- **Adding**: Add a card from within "Topic A" and verify it appears immediately in that topic.
- **Play Now**: Start a session before cards are due, pick "Practice", and verify the Home screen countdown doesn't change.
