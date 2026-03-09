# Create game / Join game
**Status:** draft

## Goal

Users can create or join a game. The Guardian initiates the game and invites other players to join.
This spec covers the creation form, the join form, and the waiting room — up to and including real-time player list
updates. Starting the game itself is deferred to a future spec.

## Game Handle

- Format: 6 uppercase alphanumeric characters (e.g. `TAOXA7`)
- Generated server-side, cryptographically random (not sequential)
- URL: `/game/<handle>`

## Flow

### Create game

When the user clicks "Créer une partie", open the game creation form.

Form fields:
- **User name** (required)
- **Email** (optional — used to send the magic link)
- **Password** (optional — protects the game from uninvited players)
- **AI game toggle** (visible but greyed out, with a "Coming soon" tooltip)

Once submitted:
- the game is created
- the user is redirected to the waiting room
- the waiting room shows the game handle (shareable with other players) with a copy-to-clipboard button
- if the game is password-protected, a lock icon is displayed next to the handle

The waiting room shows the list of users that have joined the game (top-right on desktop), updated in near real-time via
WebSocket (a 1–2 second lag is acceptable).

**Magic link:**
- A magic link is sent to the provided email address on game creation
- The link expires after 7 days
- Clicking it restores the Guardian role and brings the user to the waiting room
- If no email was provided, the user is warned that leaving the page means losing Guardian access

### Join game

A player can join a game:
- either by clicking the "Rejoindre une partie" button and entering the game handle;
- or by navigating directly to `/game/<handle>`.

Both entry points display a join form:
- **User name** (required)
- **Password** (required only if the game is password-protected)

The player is assigned the "player" role (not Guardian).
Once they have joined, they are redirected to the waiting room where they can see the list of players and the game
handle.

### Waiting room

The waiting room is shared by Guardians and players. It shows:
- The game handle (with copy-to-clipboard button)
- A lock icon next to the handle if the game is password-protected
- The list of participants (top-right on desktop), updated in real-time via WebSocket

**Player presence:** there is no "Leave game" button. A player is automatically removed from the participant list when
they close the browser window or tab (WebSocket disconnect).

**Guardian absence:** if the Guardian disconnects (tab/window close), all remaining players see a prominent warning
indicating that the game cannot start without the Guardian. The warning disappears if the Guardian reconnects
(e.g. via the magic link).

**Guardian controls:** the Guardian can kick any player from the waiting room. A kicked player is redirected to an
error page explaining they have been removed.

**Player cap:** there is no system-enforced player limit — the Guardian manages the participant list by kicking players
if needed.

Note: starting the game is out of scope for this spec (future spec).

## Error Handling

| Scenario                          | Behaviour                                                                       |
|-----------------------------------|---------------------------------------------------------------------------------|
| Invalid or nonexistent handle     | Error page: "Game not found"                                                    |
| Wrong password                    | Inline error on the password field; retry allowed (no lockout)                  |
| Duplicate player names            | Allowed — names are display-only, not unique identifiers                        |
| Game already started              | Error message: "This game has already started"                                  |
| Guardian disconnects from waiting room        | Remaining players see a warning: game cannot start without the Guardian |
| Orphaned game (Guardian left, no magic link) | Game auto-expires after 24 hours of inactivity                       |
| Player kicked by Guardian             | Kicked player is redirected to an error page: "You have been removed from the game" |
| Player disconnects (tab/window close) | Player is removed from the participant list automatically via WebSocket disconnect   |

Note: no lockout on wrong passwords — game passwords are low-stakes (casual game, not account credentials).

## Validation Rules

| Field       | Rule                                                                  |
|-------------|-----------------------------------------------------------------------|
| User name   | 1–50 characters, trimmed, non-blank                                   |
| Email       | Standard RFC 5322 format (validated only when provided)               |
| Password    | 1–100 characters (no complexity requirements)                         |
| Game handle | Exactly 6 uppercase alphanumeric characters                           |

## Security

- **Game password**: hashed server-side (bcrypt), never stored in plain text
- **Magic link token**: cryptographically random (≥ 128 bits), single-use
- **Game handle**: random generation prevents enumeration of active games
- **Rate limiting**:
  - Game creation: max 10 per hour per IP
  - Password attempts: max 5 per minute per handle

## Responsive Behavior

| Breakpoint          | Behavior                                                                      |
|---------------------|-------------------------------------------------------------------------------|
| Desktop (≥ 1024px)  | Form centered; waiting room: handle on the left, user list top-right          |
| Tablet (768–1023px) | Same as desktop with reduced margins                                          |
| Mobile (< 768px)    | Form full-width; user list below the handle; stacked layout                   |

## i18n

- All labels internationalized via the backend API (same mechanism as spec 0001)
- French only for now
- New translation keys: form labels, error messages, waiting room labels, tooltips (including "Coming soon")
- Fallback: display the label code if the backend is unavailable

## Accessibility (WCAG 2.2 AA)

- Form inputs have visible labels (not placeholder-only)
- Error messages linked to inputs via `aria-describedby`
- User list updates announced via an `aria-live="polite"` region
- Lock icon has accessible text (e.g. "Partie protégée par un mot de passe")
- "Coming soon" tooltip on the AI toggle accessible to keyboard and screen reader users
- Copy-handle button has an accessible name (e.g. "Copier l'identifiant de la partie")
- Kick button has an accessible name identifying the target player (e.g. "Expulser Alice")
- All interactive elements keyboard-navigable with visible focus indicators

## Acceptance Criteria

1. Clicking "Créer une partie" navigates to the game creation form
2. The game creation form collects name (required), email (optional), and password (optional)
3. The AI game toggle is visible but disabled, with a "Coming soon" tooltip
4. On submit, the game is created and the user is redirected to the waiting room
5. The waiting room displays a 6-character game handle with a copy-to-clipboard button
6. If the game is password-protected, a lock icon appears next to the handle
7. The waiting room user list updates within 2 seconds when a player joins (WebSocket)
8. Clicking "Rejoindre une partie" prompts for a game handle
9. Navigating to `/game/<handle>` shows the join form
10. Joining a password-protected game requires entering the correct password
11. A wrong password shows an inline error without a full page reload
12. An invalid or nonexistent game handle shows a "Game not found" error
13. A magic link is sent when an email is provided; clicking it restores the Guardian role
14. All text is served from the i18n system (French)
15. Lighthouse accessibility score ≥ 90
16. Forms are usable on mobile (< 768px)
17. The waiting room user list updates within 2 seconds when a player disconnects (tab/window close)
18. The Guardian can kick a player; the kicked player is redirected to a "removed from game" error page
19. When the Guardian disconnects from the waiting room, remaining players see a warning that the game cannot start without the Guardian; the warning disappears if the Guardian reconnects
