# Keys and Values

**Goal:** _Implement an in-memory key-value store with the specified API_

## Submission requirements
 * 'Production' quality code (simple, tested, good naming, no duplication)
 * No time limit but early submission of well written, working code will be looked favourably upon
 * Submit via github/gitlab/bitbucket
 * Runs out of the box using maven. i.e. This must work...

```text
git clone <repo url> dir_name
cd dir_name
mvn test
```
_...clone the code from your git repo, cd to the directory, invoke maven to run the tests_

# API requirements
## Part 1
Provide an implementation of the following interface

````java
public interface KeysAndValues {
    void accept(String kvPairs);
    String display();
}
````

### void accept(String s)

 * kvPairs = zero, one or more comma separated, key-value pairs (e.g. "pi=314159,hello=world")
 * only alphanumeric keys are allowed. Trim all leading & trailing whitespace.
 * numeric integer values accumulate
 * non-integers overwrite
 * problematic values are reported via the ErrorListener interface (see below)

#### Examples
```text
14=15
A=B52
dry=D.R.Y.
14=7
14=4
dry=Don't Repeat Yourself
```

After invoking `accept()` on each of these key-value strings in the above order, 

 * key **14** is equal to _26_ (i.e. _15 + 7 + 4_  ...an integer) 
 * key **dry** is equal to _Don't repeat yourself_  (...a string)

invoking `accept("14=15, 14=7,A=B52, 14 = 4, dry = Don't Repeat Yourself")` has the same effect.

### String display()

 * String displays all key-value pairs (one pair per line)
 * Keys are sorted (alpha-ascending, case-insensitive)

#### Example

Assuming... 
```java
KeysAndValues kv = new MyKeysAndValuesImplementation(listener);
kv.accept("one=two");
kv.accept("Three=four");
kv.accept("5=6");
kv.accept("14=X");
String displayText = kv.display();
```

Then `displayText` will equal...

```text
14=X
5=6
one=two
Three=four
```

## Error reporting

````java
public interface ErrorListener {
    void onError(String msg);
    void onError(String msg, Exception e);
}
````
 * All errors / failures / warnings must go through an implementation of the ErrorListener interface.
 * Provide a way of injecting an ErrorListener into your KeysAndValues implementation. Do not create a global instance. Use D.I.



## Atomic groups
 * keys 441, 442, 500 are an 'atomic group' 
 * all 3 must be defined if any one of them appears in the csv parameter to `accept()`
 * the keys don't have to appear in the same order and can appear anywhere in the string
 * the atomic group can be defined multiple times within the csv string (e.g. `441=1,442=1,500=1, 441=2,442=2,500=2, 441=3,442=3`)
 * keys within the same group cannot 'overlap' (i.e. not: `441=1, 442=1, 441=2, 500=1, 442=2, 500=2`)  
 * if any key is missing an informative message is sent via the ErrorListener specifying the atomic group (441, 442, 500) and the missing key(s)

### Examples

#### Single atomic group

```java
kv.accept("441=one,X=Y, 442=2,500=three")
```
 * 441=one
 * 442=2
 * 500=three
 * X=Y

#### Atomic group specified twice

```java
kv.accept("18=zzz,441=one,500=three,442=2,442= A,441 =3,35=D,500=ok  ")
```
 * 18=zzz
 * 35=D
 * 441=3
 * 442=A
 * 500=ok


#### Incomplete group

```java
kv.accept("441=3,200=not ok,13=qwerty")
```
 * 13=qwerty

ErrorListener instance should be invoked with a message similar to:
````java
listner.onError("atomic group(441,442,500) missing 442");
````

#### Second group incomplete

```java
kv.accept("500= three , 6 = 7 ,441= one,442=1,442=4")
```
 * 441=one
 * 442=1
 * 500=three
 * 6=7

ErrorListener message:
````java
listner.onError("atomic group(441,442,500) missing 441,500");
````

