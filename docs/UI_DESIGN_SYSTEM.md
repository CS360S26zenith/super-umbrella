# UI / UX design system — Campus Events (Android)

This document is the **single source of truth** for how the app should look: colors, type scale, shape, icons, and list patterns. The goal is a **modern, minimal, slightly playful** product that is still **cheap to build and maintain** on Material 3.

**Active palette (Option B):** Slate canvas `#F8FAFC`, headers `#0F172A`, text `#0F172A` / `#64748B`, **single accent** indigo `#4F46E5` (no second blue). Cards white with `#E2E8F0` stroke and light elevation. See [values/colors.xml](../app/src/main/res/values/colors.xml) for token names.

## Implementation safety (read first)

These rules protect **functionality** and keep changes **reversible**:

1. **Token and style refactors only** — Prefer replacing raw hex (`#RRGGBB`) with `@color/...` / `?attr/...` and consolidating duplicate sizes into `@dimen/...`. Do **not** rename `android:id` values, change adapter logic, or restructure navigation as part of a “theme” pass.
2. **Small, reviewable diffs** — Touch one **surface** at a time (e.g. student Home + its list rows) when practical; avoid mixing large refactors with theme work in the same change set.
3. **No feature changes in theme PRs** — A theme commit should not alter Firestore calls, click targets, or business rules. If a layout must move for visual balance, keep **view ids** and **listener wiring** identical.
4. **Easy rollback** — Keep compatibility: if a color is renamed, grep the codebase and update all references in the same commit. Do not leave half the app on old hex and half on tokens.
5. **Reversible experiments** — “Funky” touches (e.g. hero emphasis) should use **one** token or drawable so one revert restores the old look.

When in doubt, **ship tokens + one screen** first, then expand.

## Principles

- **One unified palette** for students and organizers: same primary, surfaces, and neutrals. Role is expressed by **content and nav**, not a different primary color per area.
- **Content first** — Lists stay calm: white / surface cards, one accent (primary) for key actions and app bars.
- **Minimal chrome** — Prefer spacing and type hierarchy over extra borders; use **one** card radius family (see below).
- **Funky, but contained** — Playfulness is allowed in **one** place per screen (e.g. hero block, or a single accent icon tint), not rainbow cards per row.

## Color tokens (semantic)

All new UI should reference these **names**, not raw hex in XML.

| Token | Role |
|-------|------|
| `campus_teal_700` | Primary brand / app bars (light mode) |
| `campus_teal_800` | Darker teal panels (e.g. stat chips on hero) |
| `campus_teal_900` | Deepest teal (e.g. emphasis card, strong headings on light) |
| `campus_teal_muted_on_teal` | Muted text **on** teal backgrounds |
| `campus_teal_container_light` | Light teal tags / pills on white cards |
| `campus_background` | Page background (slight tint) |
| `campus_surface` | Cards on top of background |
| `campus_surface_variant` | Scroll pages with subtle contrast (e.g. profile) |
| `campus_on_surface` | Primary text on light surfaces |
| `campus_on_surface_secondary` | Secondary text |
| `campus_on_surface_tertiary` | Hints, chevrons |
| `campus_on_surface_body` | Body / meta (venue lines) |
| `campus_section_label` | Small caps / section labels |
| `campus_link` | Text links on light background |
| `campus_accent_blue` | Single secondary accent (icons, optional links) |
| `campus_divider` | Hairline separators |
| `campus_error` | Destructive / error |
| Semantic badge colors | `campus_badge_*` for availability pills only |

Dark theme overrides live in `values-night/colors.xml` with the **same resource names**.

## Typography

- Default: **Material 3 / Roboto** via theme (`Theme.Material3.DayNight.NoActionBar`).
- Avoid one-off `textSize` where possible; prefer `TextAppearance.Material3.*` or shared styles when we introduce `styles.xml` entries.
- **Titles**: bold, primary on-surface color.
- **Meta**: smaller, `campus_on_surface_secondary`.

## Shape

- **Cards / dialogs**: `@dimen/campus_radius_card_large` (14dp) or `@dimen/campus_radius_card_medium` (12dp).
- **Chips**: follow Material chip defaults unless we define a shared dimen.

## Icons

- Use **vectors** in `res/drawable` with **`app:tint`** from `@color/campus_*` or `?attr/colorPrimary`.
- **Emoji** — Allowed only in low-risk decorative spots (e.g. hero greeting); **not** as the only affordance on transactional rows. Prefer replacing row emoji with a tinted vector when touching that layout.

## Imagery (societies, avatars)

- **Consistent avatar slot**: initials in a circle or a **generic** society glyph — avoid unrelated decorative logos unless they are real society branding assets stored in data.
- Events and societies lists share **padding, avatar column width, and meta text style** whenever possible.

## Screen mapping (reference)

| Area | App bar / hero | Main surface | Notes |
|------|----------------|-------------|--------|
| Student Home | `campus_teal_700` | `campus_background` + white cards | Hero extends primary |
| Explore | `campus_teal_700` (unified) | `campus_explore_canvas` | Search + filters on surface |
| Profile | `campus_teal_700` (unified) | `campus_surface_variant` | Stat card on surface |
| List rows | — | `campus_surface` | Date tile / badge tokens |

## PR checklist

- [ ] No new raw `#` hex in XML without a token (or documented exception).
- [ ] No navigation / id renames for “visual” reasons.
- [ ] Icons: vector + tint, not ad-hoc multi-color assets for list rows.
- [ ] Night: if you add a `values/` color, add or confirm `values-night/` override.

## Maintenance

- **Files of record**: [app/src/main/res/values/colors.xml](app/src/main/res/values/colors.xml), [app/src/main/res/values/themes.xml](app/src/main/res/values/themes.xml), [app/src/main/res/values/dimens.xml](app/src/main/res/values/dimens.xml), [app/src/main/res/values-night/colors.xml](app/src/main/res/values-night/colors.xml).
- When adding a color, **name it by role** (`campus_on_surface_secondary`), not by hex.

## Phased implementation

Execution order, checklists, and “what’s done vs next” live in **[UI_BUILD_PLAN.md](UI_BUILD_PLAN.md)**. Phase 1 (foundation) is complete there; later phases migrate layouts and adapters without changing behavior.
