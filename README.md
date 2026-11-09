# Krusty Krab Game (Group 67)

A turn-based Java terminal game. Customers wait in a `LinkedQueue<Customer>`, the player builds each Krabby Patty by choosing ingredients in recipe order, and a random Plankton raid ends the game if the player does not choose Defend in time. Code is organized by MVC role: model (`LinkedQueue`, `Customer`, `Ingredient`, `KrustyKrabGame`, `GameState`, `GameObserver`, `GameEvent`), view (`TerminalView`), controller (`GameController`).

Team: Mckenna, Connor, Asher
## How to Run

Built for Eclipse (JUnit 5 on the classpath). <!-- Confirm every test file uses org.junit.jupiter.api imports. -->

1. **Model (no view needed):** right-click `ConnorsTest.java` and [Mckenna's test file] > Run As > JUnit Test. These tests create no `Scanner`, `PrintStream`, `TerminalView` or `GameController`.
2. **View alone:** right-click `TerminalViewTest.java` > Run As > JUnit Test. It drives the view with a hand-built fake `GameState`.
3. **Controller:** right-click `GameControllerTest.java` > Run As > JUnit Test. It tests `parseChoice` on its own and plays whole games from scripted input (a `Scanner` over a string, the view printing to a buffer, Plankton chance 0).
4. **Thin view / full game:** right-click `GameController.java` > Run As > Java Application. Menu: `1-6` add ingredient (bottom bun, patty, cheese, lettuce, tomato, top bun), `7` Defend the formula, `8` or `q` Quit. Plankton raids with a 5% chance per tick.

## Reflection

### What changed from the specification?

| Area | Spec said | We built | Why |
|---|---|---|---|
| `Customer.getSteps()` | Customer holds a `LinkedQueue<Ingredient>` the view can read | Returns a `List<Ingredient>` snapshot, copied by a package-private `LinkedQueue.snapshot()` | The view should not see the queue type or be able to change a customer's order (draft, verify) |
| Observer timing | The model notifies observers at the end of each tick | `runTick` notifies the action result first, then state events (raid, walkout, arrival, end) as they happen | [reason] |
| Screen redraw | `update()` redraws the screen from the `GameState` it receives | `update()` prints one line per event plus the end screen; the controller calls `showStats(state)` after each action | The action-result event fires before `runTick` advances the tick, ages customers and rolls for a raid, so the state at that moment is one tick stale; after the action returns it is current |
| Menu prompt | `showMenu()` prints the numbered menu | `showMenu()` also ends with a `Choose: ` prompt | The controller has no output stream of its own (its fields are the game, the view and the `Scanner`), so the view prints the prompt |
| Packages | Separate model, view and controller packages | Everything is in the single package `krustykrab` | `Customer.losePatience()` and `LinkedQueue.snapshot()` are package-private so only model code can call them; splitting the packages would have forced them to be public |
| Validation | `parseChoice` checks the length, then catches `NumberFormatException` and the 1-8 range; `run` re-prompts | `parseChoice` trims, rejects more than 20 characters ("Input too long"), returns 8 for `q`/`quit` in any case, then accepts only a single digit 1-8 (`matches("[1-8]")`); blank, null, letters, decimals and out-of-range numbers all give "Choose 1-8". `run` shows the message and re-prompts with no tick, and calls `quit()` when the input ends | One pattern check covers every non-choice case, so there is no separate number-parsing path to test; model rules (wrong ingredient, defend with no raid) stay in the model |
| Test cases | `parseChoice` tests only for the controller | Added `GameControllerTest` scripted games: serve an order then quit, bad input costs no tick, end of input quits, an idle player loses to walkouts with leftover input ignored, a wrong ingredient costs 2 points | `run` has behavior `parseChoice` tests cannot reach (no tick on bad input, clean exit at end of input, stopping once the game is over) |

<!-- Exceeds: every row traces the change to what prompted it (a bug, a test, a teammate's class, a rule). Delete empty rows. -->

### Did the model really stand alone?

Yes, with one caveat. No model class mentions `TerminalView` or `GameController`; `KrustyKrabGame` talks to the outside only through `GameObserver` and `GameState`, and the seven model files import only `java.util`. We checked by compiling the model classes with the view, controller and tests removed, and the model tests create no view. Nothing had to move across the line, but two things leaned on it: the controller has to call `showStats` after each action because of when the model fires events (see the screen redraw row), and because everything is in one package, the view could technically call the package-private `Customer.losePatience()` on the live `Customer` objects that `getLine()` returns. `getLine()` copies the list, not the customers. Today the view only calls public getters, so the boundary holds by convention rather than by the compiler.

### What happened to our unknowns?

| Unknown from the scope | Status | Answer |
|---|---|---|
| When does Plankton come, and how long to respond? | Figured out | Chance per tick when no raid is active (5% in play); 3 ticks to Defend |
| How many customers? | Figured out | One arrives every 4 ticks; the line holds 6 |
| Time limit on orders? | Figured out | Patience of 20 ticks; 0 means a walkout |
| How does a player win or lose? | Figured out | Survive 60 ticks; lose on a stolen formula or 3 walkouts |
| Points system? | Figured out | 10 + remaining patience per burger; -2 per wrong ingredient |
| Harder tasks over time? Special characters (Patrick)? | Still open | Not built: every customer orders the same six-step recipe and nothing gets harder over time. No Patrick. |

### What doesn't work yet?

- The end screen shows only the outcome, final score and ticks survived. `GameState` does not expose customers served or walkouts, so those are not shown.
- Everything lives in one package, so the model/view boundary is not enforced by the compiler (see above).
- [Missing or failing tests: name the file and test]
- [Known bugs]

## Next Steps (Deliverable 4)

### 1. Finishing the MVP, in order

1. [e.g. Finish `GameController.run` and `parseChoice` with all bad-input cases]
2. [e.g. Play one full shift end to end and fix bugs in the owning layer]
3. [e.g. Add the missing model tests for each public method]

### 2. Our change feature

**Feature:** [name it. It must change model state or logic and be reachable through the controller and view. Examples: priority lane, skip/requeue, undo last serve, estimated wait per customer.]

**What it does:** [two or three sentences]

**Prediction, to compare against what actually happens:**

| Layer | Touches it? | Methods we expect to change |
|---|---|---|
| Model | [yes/no] | [e.g. `KrustyKrabGame.___`, `Customer.___`] |
| View | [yes/no] | [e.g. `TerminalView.showMenu`, `showStats`] |
| Controller | [yes/no] | [e.g. `GameController.parseChoice` range, `run`] |

**Should not need to change:** [e.g. `LinkedQueue`, `Ingredient`, `GameObserver`, and why the design protects them]

### 3. Stretch goals

- **Committing to:** [list]
- **Dropping:** [list: upgrades, rising difficulty, tutorial, Patrick, ...]

### 4. New tests needed

- [method > normal case, bad-input case]
- [feature method > normal case, bad-input case]
- [any remaining `GameController` / `parseChoice` tests]

### 5. Who does what

| Item | Owner |
|---|---|
| [MVC leftover item] | [name] |
| [change feature: model part] | [name] |
| [change feature: view part] | [name] |
| [tests] | [name] |
| [README updates] | [name] |

## Attribution

| File | Who | How (wrote, revised, reviewed, tested, GenAI-assisted) |
|---|---|---|
| `LinkedQueue.java` | Connor | [how] |
| `Customer.java` | Connor | [how] |
| `Ingredient.java` | Connor | [how] |
| `ConnorsTest.java` | Connor | [how] |
| `KrustyKrabGame.java` | Mckenna | [how] |
| `GameState.java` | Mckenna | [how] |
| `GameObserver.java` | Mckenna | [how] |
| `GameEvent.java` | Mckenna | [how] |
| [Mckenna's test file] | Mckenna | [how] |
| `TerminalView.java` | Asher | [how] |
| `TerminalViewTest.java` | Asher | [how] |
| `GameController.java` | Mckenna, Connor, Asher | [how] |
| `GameControllerTest.java` | Mckenna, Connor, Asher | [how] |
### GenAI Note

