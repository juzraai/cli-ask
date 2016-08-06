# CLI-Ask [![Release](https://jitpack.io/v/juzraai/cli-ask.svg)](https://jitpack.io/#juzraai/cli-ask) [![Build status](https://travis-ci.org/juzraai/cli-ask.svg)](https://travis-ci.org/juzraai/cli-ask)

*Ask stuff from user in terminal - fill POJO w/ a single call*

---

**[UNDER DEVELOPMENT]**

---



# Usage

Define your data class:

```java
@Ask("test data")
public class MyData {

	@Ask("GitHub username")
	private String ghuser = "juzraai";

	@Ask("GitHub repository name")
	private String ghrepo;

	@Override
	public String toString() { // for test purposes
		return ghuser + "/" + ghrepo;
	}
}
```

Call **AskFor** in your program:

```java
MyData data = AskFor.object(new MyData());
System.out.println("Result:\n" + data);
```

And you'll see this:

```
Please provide test data:

                        GitHub username
                   [default: 'juzraai'] : typed-user

                 GitHub repository name : typed-repo
Result:
typed-user/typed-repo
```



# Features

* **default values** - read from POJO, printed out, and selected when entered input is emtpy
* **reasking** - when there's no default value and input is empty; or input is invalid according to selected converter
* **converting** - automatically selects converter (String -> field type)
* **flexible** - you can replace default converters and you can add converters for custom types
* does not need fields to be public, nor getter/setter methods
* **easy-to-use** - annotation-driven, you have to call one method (per POJO)

Built-in converters:

Target type                       | Comment 
----------------------------------|--------
String                            | NOP, of course :]
integers (byte, short, int, long) | Uses `X.parseX()` method
floating points (float, double)   | Uses `X.parseX()` method
boolean                           | Uses patterns `TRUE|YES|ON|1` and `FALSE|NO|OFF|0`


 
# Future
 
* raw value validation
* (final, converted) value validation
* recursive "asking" - go deeper in POJOs