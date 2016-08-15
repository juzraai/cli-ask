# CLI-Ask [![Release](https://jitpack.io/v/juzraai/cli-ask.svg)](https://jitpack.io/#juzraai/cli-ask) [![Build Status](https://travis-ci.org/juzraai/cli-ask.svg?branch=master)](https://travis-ci.org/juzraai/cli-ask) [![codebeat badge](https://codebeat.co/badges/c08b14a9-8621-45f9-aef6-6c7823dfae1d)](https://codebeat.co/projects/github-com-juzraai-cli-ask)

*Ask stuff from user in terminal - fill POJO w/ a single call*

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


### Basic steps

1. **Define a data class** and add `@Ask` annotation for fields you want to ask from user. You can specify a custom label for each field here and also you can set a custom converter class.
2. **Call `AskFor.object`** and pass an instance of your data class to update it using user input.

And that's all! Check the [quick example](#quick-usage-example) on the top. You can test it by running `main` method of `hu.juzraai.cliask.example.PojoInput`.


### How it works

Let's see what happens when you call `AskFor.object(object)`:

1. Firstly, it **prepares your object** using `PreparedObject.prepare` method.
    * It constructs a `PreparedObject` instance which contains the original object itself and the list of the object's prepared and relevant fields.
    * Each field of the object is prepared using `PreparedField.prepare` method which returns a `PreparedField` object.
    * Prepared fields contain:
        * the original parent object
        * the `Field` itself
        * a boolean telling **whether the field is relevant** for asking: a field is relevant if it's accessible, not final and has `@Ask` annotation
        * the **default value**: the field's value got from the original object
        * the **label** to be printed out in front of the input cursor when asking the user: it's the field name unless you specify a custom label in the annotation: `@Ask("My custom label")` or `@Ask(value = "My custom label", ...)`
        * and the instance of the **custom converter** class if it's specified in the annotation: `@Ask(converter = MyConverter.class)`
    * So once again, the prepared object contains only the **relevant fields**.
2. Then it goes through these relevant fields and asks them:
    * Calls `AskFor.string(label, defaultValue)` ([see above](#simple-input)) to get the **raw value from user**. Uses the default value's `toString` method to pass it to `string()`.
    * If the received value is the default value, all further processing is skipped.
    * Calls `Converter.convert(raw, type, customConverter)` method to **convert** the raw value into the type of the field.
    * Then **sets this new value** for the field.
    * If conversion fails, it prints out and error messages and starts over by **asking** for the field's value **again**.


### Converting

So as you may see, there's two kind of converting:

* One with the custom converter explicitly specified in a field's `@Ask` annotation,
* and the other is the magic under the hood, which I'm going to explain hereafter.

`Converter` class has a pool for converters, which is a `Map< Class<?>, ConvertTo<?> >`.

The `addConverter(Class<T>, ConvertTo<T>)` ensures that if you add a converter for a type, the converter must convert to that type.

When `convert(raw, type, customConverter)` receives no custom converter it does a search for the appropriate converter:

* First, it uses the map's `contains` and `get` method to fastly return the **perfect converter**, if any.
* If it fails, iterates through the entries and returns with the first converter which **converts to a type assignable to the target type**. So if the field type is `ClassA`, `ClassB` extends `ClassA`, and there's a converter for `ClassB` it will be returned.

To create your custom converter, define a class as follows:

```java
public class ConvertToTargetType implements ConvertTo<TargetType> {

    @Nonnull
    @Override
    public TargetType convert(@Nonnull String rawValue) throws ConvertFailedException {
        // construct and return a TargetType instance
        // catch any error and throw ConvertFailedException
    }
}
```

And finally you can use it in two-ways:

* By adding to the converter pool: `Converter.addConverter(TargetType.class, new ConvertToTargetType())`
* By choosing it for a field: `@Ask(converter = ConvertToTargetType.class)` - in this case, make sure it can be instantiated with a **no-arg constructor**!



# Version history

Version   | Status               | Description
----------|----------------------|------------
  1.1.0   | snapshot in `master` | Converter selection in prepare phase
**1.0.0** | **current stable**   | Basic features: ask for string, ask for object, `@Ask` field annotation, labels, converting.



# Future ideas

* [#9](https://github.com/juzraai/cli-ask/issues/9) select converter earlier
* [#10](https://github.com/juzraai/cli-ask/issues/10) handle long/multiline labels
* [#5](https://github.com/juzraai/cli-ask/issues/5) ask for simple objects
* [#2](https://github.com/juzraai/cli-ask/issues/2) raw value validation
* [#2](https://github.com/juzraai/cli-ask/issues/2) (final, converted) value validation
* [#4](https://github.com/juzraai/cli-ask/issues/4) skip condition - e.g. skip asking GitHub repo if GitHub user is empty
* [#7](https://github.com/juzraai/cli-ask/issues/7) recursive "asking" - go deeper in POJOs
* [#3](https://github.com/juzraai/cli-ask/issues/3) optional values - accept empty input
* colored output using [Jansi](https://github.com/fusesource/jansi)