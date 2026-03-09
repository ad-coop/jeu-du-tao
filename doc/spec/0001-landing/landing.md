# Landing Page

**Status:** qa

## Purpose

The landing page is the first page visitors see. It must:

- Explain what the _Jeu du Tao_ is and invite visitors to play
- Convey the cooperative, positive spirit of the game
- Provide clear entry points to start or join a game

## Layout

### Hero section (full viewport height)

- Game logo (`logo_tao.jpg`) centered at top
- Title: **"Le Jeu du Tao"** — Merriweather, serif, bold
- Subtitle: **"Voulez-vous jouer à changer le monde ?"** — Open Sans
- Intro paragraph (Open Sans):
  > "Le Jeu du Tao est un jeu coopératif où les joueurs s'entraident pour avancer sur leur quête personnelle. Pas
  d'adversaire : votre seul obstacle, c'est vous-même."
- Background: `#00577C`

### Action section

- Two buttons side-by-side (stacked vertically on mobile):
    - **"Créer une partie"**
    - **"Rejoindre une partie"**
- Buttons are styled as active (not disabled)
- On click: display a toast/tooltip **"Bientôt disponible"**
- Hover and focus states must be defined (see Design System)

### Footer

Two blocks displayed side-by-side (single column on mobile):

| Block         | Content                                                                                                   |
|---------------|-----------------------------------------------------------------------------------------------------------|
| **Tao World** | Game logo + link to `taoworld.fr` + social links (Facebook, LinkedIn)                                     |
| **AD COOP**   | AD COOP logo (white variant on dark background) + link to `adcoop.fr` + social links (Facebook, LinkedIn) |

Social links:

- Tao World Facebook: `https://www.facebook.com/LeJeuDuTao`
- Tao World LinkedIn: `https://www.linkedin.com/company/jeu-du-tao/`
- AD COOP Facebook: `https://www.facebook.com/adcoop.alban.dericbourg`
- AD COOP LinkedIn: `https://www.linkedin.com/company/adcoop/`

All external links open in a new tab with `rel="noopener noreferrer"`.

## Design System

> Note: to be extracted to `doc/design-system.md`.

### Colors

| Token           | Value     | Usage                             |
|-----------------|-----------|-----------------------------------|
| `primary`       | `#00577C` | Background, primary elements      |
| `primary-light` | `#73A1B2` | Secondary elements, borders       |
| `accent`        | `#31D3C5` | CTAs, interactive elements        |
| `neutral-light` | `#ECECEB` | Text on dark backgrounds, borders |
| `text-on-dark`  | `#FFFFFF` | Headings, body text on dark bg    |

### Typography

| Role     | Font         | Weight     | Usage                     |
|----------|--------------|------------|---------------------------|
| Heading  | Merriweather | 700 (bold) | Page title                |
| Subtitle | Open Sans    | 400        | Subtitle, intro paragraph |
| Body     | Open Sans    | 400        | General text, buttons     |
| Button   | Open Sans    | 600        | CTA labels                |

### Buttons

- **Primary style**: `accent` (`#31D3C5`) background, dark text, rounded corners
- **Hover**: slight brightness increase
- **Focus**: visible outline meeting WCAG 2.2 AA (3:1 contrast ratio minimum)
- **Active/click**: triggers a "Bientôt disponible" toast notification

## Responsive Behavior

| Breakpoint          | Behavior                                                                |
|---------------------|-------------------------------------------------------------------------|
| Desktop (≥ 1024px)  | Hero centered, buttons side-by-side, footer in two columns              |
| Tablet (768–1023px) | Same as desktop with reduced margins                                    |
| Mobile (< 768px)    | Buttons stacked vertically, footer single column, font sizes scale down |

## i18n

- All labels are internationalized via the backend API
- Only French is supported for now
- If the backend is unavailable, show label code instead and issue a warn log in the browser console

## Accessibility (WCAG 2.2 AA)

- Color contrast ≥ 4.5:1 for body text, ≥ 3:1 for large text (≥ 18pt or 14pt bold)
- All images have meaningful `alt` attributes
- Buttons are keyboard-navigable with visible focus indicators
- Toast/feedback is announced to screen readers via `aria-live`
- Social links have accessible names (not icon-only)
- Semantic HTML: `<header>`, `<main>`, `<footer>`, `<nav>`
- `lang="fr"` on `<html>`

> This is to be written as a `doc`, and expanded as we go along.

## Images

| File               | Description                  |
|--------------------|------------------------------|
| `logo_tao.jpg`     | Logo of _le Jeu du Tao_      |
| `adcoop-color.png` | AD COOP logo — color variant |
| `adcoop-white.png` | AD COOP logo — white variant |

## Acceptance Criteria

1. Page loads and displays the hero section with logo, title, subtitle, and intro paragraph
2. Two CTA buttons ("Créer une partie", "Rejoindre une partie") are visible and styled as active
3. Clicking either button shows a "Bientôt disponible" feedback message
4. Footer displays both organisations (Tao World and AD COOP) with correct links and social icons
5. All external links open in a new tab with `rel="noopener noreferrer"`
6. Page layout is responsive across mobile (< 768px), tablet (768–1023px), and desktop (≥ 1024px) breakpoints
7. Lighthouse accessibility score ≥ 90
8. All visible text is served from the i18n system (French)
9. Color contrast meets WCAG 2.2 AA thresholds throughout the page
