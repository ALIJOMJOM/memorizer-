# Tasks - Stability, Stats & Persistent Local Storage

- [x] Stability & Threading Fixes
    - [x] Fix `LibraryFragment` to ensure UI updates are on Main Thread
- [x] Stats Dashboard Revamp
    - [x] Create `MemoryStage.java` model
    - [x] Create `item_memory_stage.xml` layout
    - [x] Create `MemoryStageAdapter.java`
    - [x] Update `fragment_stats.xml` (RecyclerView for stages)
    - [x] Update `StatsFragment.java` (Logic for list population)
- [x] Public Local File Storage
    - [x] Add Storage Permissions to `AndroidManifest.xml`
    - [x] Update `AppDatabase.java` to use custom public file path
- [x] Backup & Export
    - [x] Add "Export All to JSON" in `MainActivity`
