# CLI-Ask [![Release](https://jitpack.io/v/juzraai/cli-ask.svg)](https://jitpack.io/#juzraai/cli-ask) [![Build status](https://travis-ci.org/juzraai/cli-ask.svg)](https://travis-ci.org/juzraai/cli-ask)

*Ask stuff from user in terminal - fill POJO w/ a single call*

---

**[UNDER DEVELOPMENT]**

---

* [Quick usage example](#quick-usage-example)
* [Features](#features)
* [Documentation](#documentation)
* [Version history](#version-history)
* [Future ideas](#future-ideas)

---



# Quick usage example

Define your data class:

```java
public static class ExamplePojo {

    @Ask
    private String name;

    @Ask("How old are you?")
    private Byte age;

    private String skippedField = "untouched";

    @Ask("Are you sure?")
    private boolean sure = true;

    @Override
    public String toString() {
        return "ExamplePojo{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", skippedField='" + skippedField + '\'' +
                ", sure=" + sure +
                '}';
    }
}
```

Call **AskFor** in your program:

```java
System.out.println(AskFor.object("Please provide sample data", new ExamplePojo()));
```

And you'll see this:

```
Please provide sample data :

                                    name : <hit ENTER>
                                           There's no default value, please try again!

                                    name : juzraai

                        How old are you? : <hit ENTER>
                                           There's no default value, please try again!

                        How old are you? : very
                                           Invalid value for: byte

                        How old are you? : 42

                           Are you sure?
                       [default: 'true'] : no
ExamplePojo{name='juzraai', age=42, skippedField='untouched', sure=false}
```



# Features

* **easy-to-use** - annotation-driven, you have to call one method (per POJO)
* **default values** - read from POJO, printed out, and selected when entered input is empty
* **converting** - automatically selects converter (String -> field type)
* **reasking** - when there's no default value and input is empty; or input is invalid according to selected converter
* **flexible** - you can replace default converters and you can add custom converters for types or fields 
* does not need fields to be public, nor getter/setter methods

Built-in converters:

Target type                       | Comment 
----------------------------------|--------
String                            | NOP, of course :]
integers (byte, short, int, long) | Uses `X.parseX()` method
floating points (float, double)   | Uses `X.parseX()` method
boolean                           | Uses patterns `TRUE|YES|ON|1` and `FALSE|NO|OFF|0`



# Documentation


## Dependency

You can add ***CLI-Ask*** as dependency using [JitPack.io](https://jitpack.io/#juzraai/cli-ask). Follow the link to get information on how to do this, click on the green *"Get it"* button besides the latest version and scroll down to code snippets.


## Simple input

You can ask for a `String` using `AskFor.string(String label, String defaultValue)` method.

* `label` will be printed out as a prompt, aligned to the center of the terminal (assuming width is 80 chars)
* `defaultValue`, if specified, will be printed under the prompt and it will be returned if user provides empty input
* if `defaultValue` is `null`, `AskFor.string` will keep reasking till user provides non-empty input

Example program:

```java
String s1 = AskFor.string("Test 1", "example");
String s2 = AskFor.string("Test 2", null);
System.out.printf("Test 1 = %s%nTest 2 = %s%n", s1, s2);
```

Example output:

```
                                  Test 1
                    [default: 'example'] : <hit ENTER>

                                  Test 2 : <hit ENTER>
                                           There's no default value, please try again!

                                  Test 2 : okay then
Test 1 = example
Test 2 = okay then
```

You can try it yourself by running `main` method of `hu.juzraai.cliask.example.SimpleInput`.

## Ask for POJO

**TODO: how to define class, how AskFor.object works, 2 way to add converters**

Check [quick example](#quick-usage-example) again, or you can test it by running `main` method of `hu.juzraai.cliask.example.PojoInput`.
 


# Version history
 
# Future ideas
 
* [#5](https://github.com/juzraai/cli-ask/issues/5) ask for simple objects
* [#3](https://github.com/juzraai/cli-ask/issues/3) optional values - accept empty input
* [#2](https://github.com/juzraai/cli-ask/issues/2) raw value validation - `@ValidateRawValue(class)`
* [#2](https://github.com/juzraai/cli-ask/issues/2) (final, converted) value validation - `@ValidateValue(class)`
* [#4](https://github.com/juzraai/cli-ask/issues/4) skip condition - e.g. skip asking GitHub repo if GitHub user is empty - `@Skip(class)`, `boolean skip(Object)`
* recursive "asking" - go deeper in POJOs - `@AskRecursively`
* colored output using [Jansi](https://github.com/fusesource/jansi)