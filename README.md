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
| Harder tasks over time? Special characters (Patrick)? | Still open | Not built yet: every customer still orders the same six-step recipe and nothing gets harder over time. Random, lengthening orders are our Deliverable 4 feature (below). Patrick is dropped. |

### What doesn't work yet?

- Randomized orders and rising difficulty are not built yet (our Deliverable 4 feature). Every customer still orders the same six-step recipe.
- The end screen shows only the outcome, final score and ticks survived. `GameState` does not expose customers served or walkouts, so those are not shown.
- Everything lives in one package, so the model/view boundary is not enforced by the compiler (see above).
- [Missing or failing tests: name the file and test]
- No known open bugs. We found and fixed these problems when combining our independent work: a file whose name did not match its class name; test files written against different JUnit versions (every test file now uses JUnit 5); and `LinkedQueue.enqueue` leaving the front of the queue empty on the first entry, which broke `peek` and the game.

## Next Steps (Deliverable 4)

### 1. Finishing the MVP, in order

1. Show customers served and walkouts on the end screen: add `getServed()` and `getWalkouts()` to `GameState` and `KrustyKrabGame` (the counters already exist in the game), then print them in `TerminalView`'s end screen.

### 2. Our change feature

**Feature:** Random, lengthening orders (randomized ingredients plus rising difficulty).

**What it does:** Each new customer gets a random order instead of the fixed six-step recipe. The bottom bun stays first and the top bun last so every order is still a burger, but the fillings (patty, cheese, lettuce, tomato) are chosen and ordered at random. The number of fillings in an order grows as the shift goes on, so early orders are short and late orders can use all four.

**Prediction, to compare against what actually happens:**

| Layer | Touches it? | Methods we expect to change |
|---|---|---|
| Model | Yes | `KrustyKrabGame.addCustomerToLine`: pick and shuffle the fillings with the game's seeded `Random`, with the number of fillings growing with `tick` |
| View | No | None. `TerminalView.showStats` already prints each customer's next step and steps left from `Customer.getSteps()`, so it works for any order |
| Controller | No | None. `GameController.act` maps menu digits 1-6 to `Ingredient.values()`, which does not depend on the recipe |

**Should not need to change:** `LinkedQueue`, `Customer`, `Ingredient`, `GameObserver`, `GameEvent`, `TerminalView`, `GameController`. `Customer` accepts any non-empty recipe through its constructor, and the view and controller read orders only through `getSteps()` and the fixed ingredient menu, so only the place that builds the recipe changes.

### 3. Stretch goals

- **Committing to:** Randomized ingredients, rising difficulty
- **Dropping:** Upgrades, Patrick

### 4. New tests needed

- `KrustyKrabGame.addCustomerToLine`, checked through `getLine()`: every order starts with `BOTTOM_BUN`, ends with `TOP_BUN` and repeats no ingredient; the same seed gives the same orders; an order late in the shift can be longer than the first. Bad input never reaches it, so the check is that a full 60-tick shift builds only valid, non-empty orders and never throws.
- `GameControllerTest.runServesAnOrderThenQuits`: currently types the fixed `1 2 3 4 5 6`; it must read the front customer's steps from `getLine()` and type those instead.
- `getServed()` / `getWalkouts()`: both 0 at the start; 1 after serving an order; the walkout count rises when a customer leaves. A `TerminalViewTest` check that the end screen shows both numbers.

### 5. Who does what

| Item | Owner |
|---|---|
| End-screen counts: `getServed()` / `getWalkouts()` in `GameState` and `KrustyKrabGame` | Mckenna |
| End-screen counts: `TerminalView` end screen and its test | Asher |
| Change feature, model part: random, lengthening orders in `KrustyKrabGame.addCustomerToLine` | Mckenna |
| Change feature, view part | None needed |
| Tests: new model tests for random orders | Connor |
| Tests: update `GameControllerTest.runServesAnOrderThenQuits` | Asher |

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
| `TerminalView.java` | Asher | GenAI-assisted (debugging): Claude Sonnet 5.5 helped identify and resolve errors caused by independent development, such as the `getSteps()` return type mismatch and an extra closing brace in an earlier `KrustyKrabGame`. |
| `TerminalViewTest.java` | Asher | GenAI-assisted (debugging): Claude Sonnet 5.5 helped identify errors that surfaced when running the tests against the real model classes, such as the `LinkedQueue.enqueue` bug. |
| `GameController.java` | Mckenna, Connor, Asher | GenAI-assisted (debugging): Claude Sonnet 5.5 helped identify and resolve errors found when running the game end to end with scripted input, including the `LinkedQueue.enqueue` bug. |
| `GameControllerTest.java` | Mckenna, Connor, Asher | GenAI-assisted (debugging): Claude Sonnet 5.5 helped identify test-setup errors from independent development, such as the JUnit 4 versus JUnit 5 mismatch. |

### GenAI Note

Asher used Claude Sonnet 5.5 to help debug and to identify errors that came from our independently written classes not fitting together. That check found that `Customer.getSteps()` returns a `List`, not a `LinkedQueue`, so the view reads steps as a list; an extra closing brace in an earlier `KrustyKrabGame` upload; `LinkedQueue.enqueue` setting the front to `null` instead of the new node on the first entry, which made `peek` fail and four of `ConnorsTest`'s tests fail; and `ConnorsTest` importing JUnit 4 while the project uses JUnit 5. All four have since been fixed in the project.

For the change feature, Asher asked: "Which file needs to change so that there is some randomization of the ingredients in the burger?" Claude answered that only `KrustyKrabGame.addCustomerToLine` builds recipes, that `Customer` accepts any non-empty recipe, and that the view and controller already work with any order, but that tests assuming the fixed `1 2 3 4 5 6` order would break. Our prediction above follows that answer.

[State what each person reviewed or changed afterward. Mckenna and Connor: say whether you used GenAI for your files.]
