# Frontend Changes: Add Words Feature

## 1. set-detail.html — Conditional Button Display
**File**: `src/main/resources/templates/fragments/set-detail.html`

- **Owner** (`isOwner == true`): Shows "Add Word" button linking to `/cards/new?setId=${set.id}`. Subscribe/Unsubscribe/Fork buttons are hidden.
- **Non-owner** (`isOwner == false`): Shows Subscribe or Unsubscribe (based on `isSubscribed`) + Fork. "Add Word" button is hidden.
- Used `th:if="${isOwner}"` and `th:unless="${isOwner}"` for conditional rendering.

## 2. card-form.html — Multi-Set Badge Selection
**File**: `src/main/resources/templates/fragments/card-form.html`

Added to the "Create new word" form:
- **Set selector dropdown** (`<select id="setSelector">`) populated from `userSets` model attribute
- **Badge container** (`<div id="selectedSetsContainer">`) showing colored pill badges for selected sets
- **Hidden inputs** (`<div id="hiddenSetInputs">`) with `<input type="hidden" name="setIds">` for form submission
- **JavaScript logic**:
  - `addSetBadge(setId, setName)` — adds badge + hidden input, prevents duplicates
  - `removeSet(setId)` — removes badge + hidden input on × click
  - Select change handler adds badge and resets dropdown
  - Pre-selection: if `preSelectedSetId` is set in model, auto-selects that set on page load and expands the form
- Made `<script>` tag `th:inline="javascript"` for Thymeleaf expression support

## 3. main.css — Badge Styles
**File**: `src/main/resources/static/css/main.css`

Added styles at end of file:
- `.set-badges-container` — flex container with wrap and gap
- `.set-badge` — inline-flex pill with white text, rounded corners
- Color cycling via `nth-child(4n+X)`: accent (indigo), success (green), warning (amber), danger (red)
- `.set-badge-remove` — × button with hover opacity effect
