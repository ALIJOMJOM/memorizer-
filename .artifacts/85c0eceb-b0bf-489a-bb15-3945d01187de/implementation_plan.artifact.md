# Implementation Plan - Stability, Stats & Persistent Local Storage

This plan addresses the current crashes, revamps the statistics UI, and refactors the data storage to use visible local files on the device storage.

## User Review Required

> [!IMPORTANT]
> **External Database Storage**: I will move the Room database from the private app directory to a public folder (e.g., `/Documents/Memorizer/`). This requires the `MANAGE_EXTERNAL_STORAGE` or `READ_EXTERNAL_STORAGE/WRITE_EXTERNAL_STORAGE` permissions. This ensures your data is accessible as a file and persists even if the app is uninstalled.

> [!NOTE]
> **Stats List Revamp**: I am replacing the text-based stage summary with a professional list. This will remove the "+16d" notation and provide a clearer view of your academic progress using a scrollable list of memory stages.

## Proposed Changes

### 1. Stability & Threading Fixes
- **[MODIFY] `LibraryFragment.java`**:
    - Wrap all post-operation UI refreshes in `refreshUI()` using `runOnUiThread`.
    - Ensure breadcrumbs and folder study buttons are only updated on the Main Thread.

### 2. Stats Dashboard Revamp
- **[NEW] `MemoryStage.java`**: UI model for the stats list.
- **[NEW] `MemoryStageAdapter.java`**: Adapter to display 6 retention levels (1h, 4h, 24h, 3d, 7d, 16d).
- **[MODIFY] `fragment_stats.xml`**: Replace the static stage text with a `RecyclerView`.
- **[MODIFY] `StatsFragment.java`**: Populate the list with real data from the hierarchical stats engine.

### 3. Public File Storage (All Data)
- **[MODIFY] `AppDatabase.java`**:
    - Refactor `getDatabase` to use a custom file path pointing to the device's `Documents/Memorizer` directory.
    - Implement a helper to ensure the directory exists before building the database.
- **[MODIFY] `AndroidManifest.xml`**: Add storage permissions.

### 4. Backup & Portability
- **[MODIFY] `MainActivity.java`**:
    - Add an "Export All to JSON" option in the FAB menu.
    - This allows you to manually save "All Data" as a readable text file anywhere on your device.

## Verification Plan

### Automated Tests
- None.

### Manual Verification
- **Move Test**: Verify flashcards move between folders without crashing.
- **File Explorer**: Use the Android Files app to verify `memorizer_database` exists in the `Documents/Memorizer` folder.
- **Stats View**: Verify the new memory stages list is accurate and scrollable.
