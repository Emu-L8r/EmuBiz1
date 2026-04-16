# Master Prompt — Improve Matrix Digital Rain and Open PR

## Goal
Create a focused pull request that improves the Matrix digital rain in `app/src/main/java/com/emul8r/bizap/ui/gui3/components/MatrixBackground.kt`.

The result should make the rain feel more like a comet stream:
- brighter, more visible heads
- fast-fading tails
- smoother cubic/exponential fade curve
- rare, hero-only glitch/flicker accents
- slightly fewer characters per column so the heads pop more

## Context
Bizap is an Android invoicing app with GUI3 as the Matrix/cyberpunk Compose experience. The rain effect already exists and is part of the immersive background system.

Relevant files:
- `app/src/main/java/com/emul8r/bizap/ui/gui3/components/MatrixBackground.kt`
- `app/src/main/java/com/emul8r/bizap/ui/gui3/components/MatrixBackgroundWrapper.kt`
- `app/src/main/java/com/emul8r/bizap/ui/gui3/util/MatrixAnimationStandard.kt`
- `app/src/main/java/com/emul8r/bizap/ui/gui3/theme/MatrixTheme.kt`

## What to change
Please improve the existing rain tuning only. Prefer small, surgical edits.

### Visual requirements
1. **Brighter head**
   - Make the first glyph in each column noticeably brighter than the tail.
   - Keep the head visually dominant.
   - Increase the minimum visible alpha for the head.

2. **Fading tail**
   - Trail glyphs should fade quickly toward near-invisible.
   - The tail should feel like a comet tail, not a uniform string.

3. **Smoother alpha curve**
   - Use a cubic or exponential-shaped falloff, not linear.
   - The head should stay bright longer.
   - The back half of each column should disappear faster.

4. **Rare hero-only glitch/flicker**
   - Keep glitch accents subtle and uncommon.
   - Prefer hero columns only.
   - Avoid making the whole background noisy.

5. **Slightly fewer glyphs per column**
   - Reduce trail density just enough to enhance contrast and readability.
   - Do not remove the effect entirely.

## Constraints
- Do not change the overall GUI3 architecture.
- Do not introduce new rendering primitives or increase frame cost meaningfully.
- Keep the number of `Text()` calls effectively the same or lower.
- Preserve Matrix theming and the existing typeface/style.
- Avoid touching unrelated screens unless absolutely required for compilation.
- Do not break navigation or build quality.

## Implementation guidance
- Keep the change localized to the rain effect logic in `MatrixBackground.kt`.
- If you need helper constants/functions, add them near the existing rain helpers.
- Prefer readable code and clear constant names over cleverness.
- If you adjust trail alpha, use a curve that falls off quickly after the head.
- If you adjust trail length, do it gently; the goal is visual polish, not a major redesign.

## Acceptance criteria
- The head of each column is clearly brighter and more prominent.
- The trail fades quickly and looks more cinematic.
- The effect still reads as Matrix-style digital rain.
- No visible UI regression in GUI3.
- The app still builds successfully.
- The change is small enough to review comfortably in a PR.

## Validation checklist
Please verify:
- `./gradlew assembleDebug` succeeds
- the app launches successfully on Android
- GUI3 background still renders correctly
- digital rain looks improved in a screenshot or emulator check
- no unrelated files were changed unless needed for compilation

## Suggested PR output
When done, provide:
1. a short summary of the change
2. the exact files changed
3. the validation steps you ran
4. any follow-up recommendations

## Tone for the co-pilot
Be conservative, practical, and PR-ready. Optimize for a clean review and a visible visual improvement.

