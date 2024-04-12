## Automata
A programmatic reconstruction of the various Automata that are introduced in
Computer Science Theory (CSCI262). 

The Automata defined are, in order:

- Deterministic Finite Automaton (DFA)
- Nondeterministic Finite Automaton (NFA)
- Regular Expression Operations
- Generalized NFA (GNFA)
- Context-Free Grammar (CFG)
- Pushdown Automaton (PDA). 
- ...

These Automata function as intended with all of the methods defined in CSCI262.
In order of learning and thus implementation, these are:
### Week 1:
- [Alphabet](src/main/java/automata/components/Alphabet.java) -- A specialized
  set of symbols representing all possible symbols that can appear in a
  string. Varies based on context. 

### Week 2:
DFA: 
- [States](src/main/java/automata/components/State.java) -- Just a name 
attribute indicating what state an automata is currently in.
- [Transition function](src/main/java/automata/components/DeterministicTransition.java)
  -- "Function" that takes in an input symbol and current state and returns the
next state the machine should enter. 
- [DFA](src/main/java/automata/DFA.java) -- A DFA uses these components to
start in a given state, read a string, and then determines if the string is
accepted. 
  - The 5-tuple definition (`DFA#toString()`) of a DFA, to summarize the 
  properties of a DFA in writing. 

### Week 3:
- Basic [Closure properties](src/main/java/automata/operations/AutomataCombiner.java)
of DFAs, such as 
  - Union
  - Intersection
  - Difference (?)
  - Complementation (?)
- [Non-DFA](src/main/java/automata/NFA.java) Automata, which don't always have
a single transition for a given state-symbol input. 
  - This requires a new [Transition Function](src/main/java/automata/components/Transition.java)
  that can express the set of states that can be transitioned to.
  - There is also the [Epsilon Alphabet symbol](src/main/java/automata/components/Alphabet.java#L18)
  and Epsilon Closure function to handle epsilon transitions. 
  - And also another 5-tuple definition (`NFA#toString()`). 

### Week 4:
- [DFA-NFA equivalence](src/main/java/automata/operations/AutomataConvertor.java). 
  - For DFA --> NFA: Trivial. A DFA is an NFA.
  - For NFA --> DFA: Powerset construction. 

### Week 5/6:
- [Regular expressions](src/main/java/automata/AutomataBuilder.java).
  - RegEx construction involves dividing the expression into a parse tree of
  [Tokens](src/main/java/automata/token).
  - These expressions are then combined using NFA Combination operations. 
- [Generalized NFA](src/main/java/automata/GNFA.java), which have Regex strings
as transitions between states. 
  - A NFA is a GNFA by default. 
  - By "ripping and repairing" states in the GNFA, two states will eventually
  be left over, which contain the RegEx encoded by the starting GNFA. 
  - Since an NFA is a GNFA, this allows NFAs (and DFAs) to be converted to
  RegEx strings.

### Week 7:
- DFA Minification ([`DFA#minify()`](src/main/java/automata/DFA.java)) Via the 
Myhill-Nerode Theorem
  - (Incomplete)

### Week 10:
- Context-Free Languages
- [Context-Free Grammar](src/main/java/grammar/CFG.java), including
  - String construction
  - Generation of an arbitrary number of valid strings from a CFG via streaming
  - Ambiguity checking (unimplemented)
  - Formal definition via a 4-tuple definition (`CFG#toString()`)
  - Closure Properties (unimplemented)

### Week 11:
- Chomsky Nomral Form for a CFG (unimplemented)
- [Pushdown Automata](src/main/java/automata/PDA.java), which use a stack
as part of its string validation. 
  - Includes its formal definition via a 6-tuple definition (`PDA#toString()`)

### Week 12:
- Equivalence of CFG and PDA, by converting a CFG into a PDA. 
  - ([`CFG#convertToPDA()`](src/main/java/grammar/CFG.java))
  - PDA to CFG is more involved and was not covered in lecture, but the
  implementation seems algorithmic enough to implement later.
- TURING MACHINES (unimplemented)
  - The fact that everything after the Turing Machine is talked about in such
  broad strokes makes it seem impossible to actually make a TM that can do 
  something meaninful. 

