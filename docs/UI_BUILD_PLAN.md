# UI / UX build plan — phased execution

This plan turns [UI_DESIGN_SYSTEM.md](UI_DESIGN_SYSTEM.md) into **ordered work**. Follow the **implementation safety** rules in that document on every phase (no `android:id` renames, no behavior changes in theme passes).

---

## Phase 1 — Foundation (COMPLETE — do not repeat)

**Status:** Done. **No further work** in this phase unless you discover a bug in an existing file.

| Deliverable | Location |
|-------------|----------|
| Design spec + safety rules | [docs/UI_DESIGN_SYSTEM.md](UI_DESIGN_SYSTEM.md) |
| Semantic color tokens (light) | [app/src/main/res/values/colors.xml](../app/src/main/res/values/colors.xml) |
| Semantic color tokens (night) | [app/src/main/res/values-night/colors.xml](../app/src/main/res/values-night/colors.xml) |
| Card / grid dimens | [app/src/main/res/values/dimens.xml](../app/src/main/res/values/dimens.xml) |
| Material 3 theme mapping | [app/src/main/res/values/themes.xml](../app/src/main/res/values/themes.xml), [values-night/themes.xml](../app/src/main/res/values-night/themes.xml) |

**Exit criteria (already met):** Tokens exist; theme references them; doc describes usage.

---

## Phase 2 — Student shell: hex to tokens (COMPLETE)

**Goal:** Replace inline `#RRGGBB` with `@color/campus_*` on the main student surfaces and the Explore badge adapter only. **Visual-only**; keep all view ids and Java logic except where colors are read from resources.

### 2.0 Theme hygiene (small fix)

- [x] In `values/themes.xml` and `values-night/themes.xml`, set `xmlns:tools` to `http://schemas.android.com/tools` (not `.../apk/res/android`).

### 2.1 Layouts (token swap)

| File | Notes |
|------|--------|
| [fragment_student_home.xml](../app/src/main/res/layout/fragment_student_home.xml) | Backgrounds, teal hero, stat chips, payments card, quick cards, section headers, empty state |
| [fragment_student_explore.xml](../app/src/main/res/layout/fragment_student_explore.xml) | App bar `@color/campus_teal_700`; canvas, text, dividers, footer links |
| [fragment_student_profile.xml](../app/src/main/res/layout/fragment_student_profile.xml) | Header, card, stats, rows, buttons |
| [item_home_upcoming_event.xml](../app/src/main/res/layout/item_home_upcoming_event.xml) | Date tile, text, category pill; card radius uses `@dimen/campus_radius_card_large` |
| [item_explore_event.xml](../app/src/main/res/layout/item_explore_event.xml) | Date block, title, venue; card radius uses `@dimen/campus_radius_card_medium` |
| [bg_explore_arrow_circle.xml](../app/src/main/res/drawable/bg_explore_arrow_circle.xml) | Arrow circle fill uses `campus_accent_blue` |

### 2.2 Java adapter

- [x] [ExploreEventAdapter.java](../app/src/main/java/view/ExploreEventAdapter.java) — Uses `ContextCompat.getColor(context, R.color.campus_badge_*)` for badge text/background.

**Exit criteria (met):** Listed layouts and adapter use tokens; Explore title bar unified to teal. Run **Phase 7** build/smoke after pulling these changes.

---

## Phase 3 — Student auxiliary screens

**Goal:** Same token rules for screens tied to the student shell (no navigation changes).

| File | Notes |
|------|--------|
| [activity_student_event_history.xml](../app/src/main/res/layout/activity_student_event_history.xml) | Header, subtitle, empty |
| [item_event_history_row.xml](../app/src/main/res/layout/item_event_history_row.xml) | Row text / backgrounds |
| [activity_student_followed_societies.xml](../app/src/main/res/layout/activity_student_followed_societies.xml) | Header, body |
| [item_followed_society_row.xml](../app/src/main/res/layout/item_followed_society_row.xml) | Card, text |
| [fragment_student_tickets.xml](../app/src/main/res/layout/fragment_student_tickets.xml) | If hex present |
| [item_student_ticket_row.xml](../app/src/main/res/layout/item_student_ticket_row.xml) | If hex present |

**Exit criteria:** No raw hex in these layouts; night mode still readable (tokens already dual-defined).

---

## Phase 4 — Societies, events, detail (high-traffic lists)

**Goal:** Align list/detail screens with the same `campus_*` vocabulary. Still **no** Firestore or navigation changes.

Suggested order (one PR or several small ones):

1. [item_society_row.xml](../app/src/main/res/layout/item_society_row.xml), [activity_societies_list.xml](../app/src/main/res/layout/activity_societies_list.xml)
2. [activity_society_events.xml](../app/src/main/res/layout/activity_society_events.xml), [item_event.xml](../app/src/main/res/layout/item_event.xml) (shared student list pattern)
3. [activity_event_detail.xml](../app/src/main/res/layout/activity_event_detail.xml) — large file; do in one focused pass
4. [activity_event_list.xml](../app/src/main/res/layout/activity_event_list.xml) if still used

**Exit criteria:** Society and event browsing use tokens; no accidental new blues unrelated to `campus_accent_blue` / primary teal unless documented in the design doc.

---

## Phase 5 — Organizer & staff (incremental)

**Goal:** Reduce drift on organizer-facing layouts without a single mega-diff.

| Area | Examples |
|------|----------|
| Organizer shell / home / my events | `fragment_organizer_*.xml`, `activity_organizer_main.xml` |
| Dashboard / create / edit | `activity_organizer_dashboard.xml`, `activity_create_event.xml`, `activity_edit_event.xml` |
| Organizer list rows | `item_organizer_event.xml` |
| Staff / misc | `activity_staff_approval.xml`, etc., as touched for other reasons |

**Exit criteria:** Each merged chunk has **zero** new raw hex in files it touches; optional: grep `layout/` for `#` and burn down over time.

---

## Phase 6 — Polish (optional, after color migration)

**Goal:** Typography and icon consistency; documentation discoverability.

- [ ] Introduce shared `styles.xml` entries (e.g. `TextAppearance.Campus.SectionTitle`) that wrap `TextAppearance.Material3.*` and reference `@color/campus_on_surface` — migrate **only** where you already edit a layout.
- [ ] Replace emoji used as **row** icons (e.g. profile quick rows, home stat chips) with **one** vector set + `app:tint` — only when editing those rows for another reason, or in a dedicated small PR.
- [ ] Add a one-line link to [UI_DESIGN_SYSTEM.md](UI_DESIGN_SYSTEM.md) from the root [README.md](../README.md) under a “Design” or “Contributing” subsection.

**Exit criteria:** No mandatory blocker for ship; this phase is quality-only.

---

## Phase 7 — Build and verify

**Goal:** Confirm the app compiles and core flows still work.

1. **Sync / compile:** Android Studio **Build > Make Project**, or `./gradlew assembleDebug` with `JAVA_HOME` set.
2. **Manual smoke (student):** Login as student → Home → Explore (filters, list) → Profile → Attendance history → My societies → Tickets.
3. **Night mode:** Toggle system dark theme; spot-check Home, Explore, Profile, one list screen from Phase 3 or 4.
4. **Regression:** No crash on tap targets; RSVP / navigation unchanged from pre-migration behavior.

**Exit criteria:** Green build; smoke checklist done; no new lint errors in edited files.

---

## Summary table

| Phase | Scope | Status |
|-------|--------|--------|
| **1** | Doc + `colors.xml` + `dimens.xml` + `themes.xml` (light/night) | **COMPLETE** — do not redo |
| **2** | Theme xmlns fix + student Home/Explore/Profile + home/explore item rows + `ExploreEventAdapter` + explore arrow drawable | **COMPLETE** |
| **3** | Student history / followed societies / tickets layouts | TODO |
| **4** | Societies list, society events, event rows, event detail, legacy event list | TODO |
| **5** | Organizer + staff layouts (incremental) | TODO |
| **6** | Shared text styles, vector icons for rows, README link | TODO (optional) |
| **7** | Build + manual smoke + dark check | **Run now** after Phase 2; repeat after 3–5 |

---

## How to “build it” after phases

- After **Phase 2** (minimum useful slice): run **Phase 7** — enough for a demo-worthy student shell on tokens.
- After **Phases 3–4**: run **Phase 7** again before a wider release or grading handoff.
- **Phases 5–6** can trail in follow-up milestones.

When executing work, update this file by checking off `[ ]` items or bumping phase status in the summary table.
