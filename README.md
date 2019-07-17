# Functional Esoteric Interpreter
### Meaning of functional
Functional programming is a paradigm characterized by zero side-effects. The idea is to build your program around evaluating functions, where the functions take an input and return an output without modifying anything else. The biggest benefits of functional programming are safety and modularity, and in many cases concision.

### Usage of funtional programming in this project
The interpreters and translators are -mostly- purely functional, the UI is not. Functional programming is great for backend/core code, but it's not ideal for interactive elements. The only deviation from functional style is the optional console logging during runtime, which is for interactive programs.

### State of the project
Current Native language support:
* [Brainfuck](https://esolangs.org/wiki/Brainfuck)
* [Fluffle Puff](https://github.com/juju2143/flufflepuff)
* [Ook](https://esolangs.org/wiki/Ook!)
* [WhiteSpace](https://esolangs.org/wiki/Whitespace) ([as defined here](https://web.archive.org/web/20151108084710/http://compsoc.dur.ac.uk/whitespace/tutorial.html))
* Scala

#### Current features:
* Run program from text file
* Unoptimized, optimized, and compiled BrainFuck interpreters
* Translate to and from supported BrainFuck languages
* Compile BrainFuck programs to Scala source files
* Compile and run Scala source files
* Create and use user-defined BrainFuck languages
* User-configurable runtime parameters (logging, maximum output size, tape size, etc.)
* Convert difficult-to-read code (a la WhiteSpace) to and from readable syntax with assemblers
* Debug mode to show interpreter state during runtime

##### WIP:
* Dynamic tape size for compiled BrainFuck interpreter
* Unispace interpreter
* Streamline WhiteSpace interpreter versions (make it generic)
* Additional languages and interpreters
* Modularization
* Potentially everything

### Optimization Strategy
The first major difference in the optimizing BrainFuck interpreter is its program and data tapes. Instead of keeping two lists for each one and modifying them at each step, it uses a single static program tape and a single data tape, keeping track of its position in each with a counter. Both are stored using Vectors.

The optimizer performs a series of passes over the program:
1. Filter out all non-BrainFuck characters and replace all clear loops with a single instruction '_'.
2. Contract all sequences of repeated instructions with a single instruction. For instance, +++++ becomes (+,5).
3. Collect all unbroken sequences of pointer movements and increments/decrements with single 'bulk' operations that perform the entire sequence in a single large step. Bulk operations are represented by 'u'.
4. Replace all copy/multiplication loops with a single instruction. This amounts to finding all blocks of the form "[u]" where u does not shift the pointer and decrements the current value by 1, and replacing them with a single 'l' which performs the bulk operation once while multiplying all the increments/decrements by the current value.
5. Pair every bracket with the index of its corresponding bracket. This eliminates the need to scrub through the program looking for the next bracket on every jump or skip.

### User-Defined Translators
There are two ways to define your own BF language:
* Use the console prompt, which will ask you for the language name and syntax then handle the rest.
* Make a text file containing your language's information in this form:
```
name=...
[=...
]=...
>=...
<=...
+=...
-=...
.=...
,=...
```

### On the Scala "Interpreter"
The main purpose of the Scala interpreter is to run Scala source files generated by Eso's compiler. It assumes the source file is a Function0[String] definition of the form "new Function0[String]{...}", and returns the result of the defined function. It can run arbitrary Scala code as long as it's in that form.

This is very much a work in progress.
