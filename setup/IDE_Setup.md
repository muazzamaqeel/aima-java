# AIMA Java + JavaFX Setup Guide (First-Time Setup)

This guide shows the exact steps to get the `aima-java` project running in IntelliJ for the first time.

It is written for the setup that worked successfully:

* **JDK:** Temurin **17.0.18**
* **JavaFX SDK:** **17.0.18**
* **IntelliJ language level:** **17**
* **Compiler bytecode target:** **17**

At the end, the project ran successfully with:

```text
Process finished with exit code 0
```

---

## 1. What you need before starting

You need these tools installed:

1. **IntelliJ IDEA**
2. **JDK 17**
3. **JavaFX SDK 17**
4. The **aima-java** project on your computer

---

## 2. Install JDK 17

Install **Temurin JDK 17**.

After installation, open PowerShell and check:

```powershell
java -version
javac -version
```

You should see something like:

```text
openjdk version "17.0.18"
javac 17.0.18
```

If you do not see version 17, stop here and fix Java first.

---

## 3. Download and extract JavaFX 17

Download **JavaFX SDK 17** and extract it.

Example folder used in the working setup:

```text
C:\Softwares\javafx-sdk-17.0.18
```

Inside that folder, you should have:

```text
bin
legal
lib
src
```

The important folder is:

```text
C:\Softwares\javafx-sdk-17.0.18\lib
```

---

## 4. Open the project in IntelliJ

Open IntelliJ and open the `aima-java` project.

Wait until IntelliJ finishes indexing the project.

---

## 5. Set the Project SDK to JDK 17

In IntelliJ:

1. Go to **File → Project Structure**
2. Click **Project** on the left
3. Set:

```text
Project SDK = Temurin 17
Language level = 17
```

Then click **Apply**.

### What this does

This tells IntelliJ to use Java 17 for the whole project.

---

## 6. Set all module SDKs to JDK 17

Still in **Project Structure**:

1. Click **Modules** on the left
2. For each module, open **Dependencies**
3. Set **Module SDK** to **Temurin 17**

Modules that were present:

* `aima-all`
* `aima-core`
* `aima-gui`
* `aimax-osm`

Do this for **every module**.

Then click **Apply**.

### Why this matters

If one module still uses an older JDK, the build can still fail even if the project SDK is correct.

---

## 7. Set the compiler bytecode version to 17

In IntelliJ:

1. Go to **File → Settings**
2. Open **Build, Execution, Deployment → Compiler → Java Compiler**
3. Set:

```text
Project bytecode version = 17
```

4. For each module, set target bytecode version to:

```text
17
```

Then click **Apply** and **OK**.

### Why this matters

Even if the project SDK is Java 17, IntelliJ can still compile with an older target if this is not updated.

---

## 8. Remove old JavaFX libraries if you added any wrong ones before

If you previously added JavaFX 26 or another incompatible version, remove it first.

In IntelliJ:

1. Go to **File → Project Structure**
2. Click **Libraries**
3. Remove any old or incompatible JavaFX library

Only keep the correct JavaFX 17 library.

---

## 9. Add JavaFX 17 as a library

In IntelliJ:

1. Go to **File → Project Structure**
2. Click **Libraries**
3. Click **+**
4. Choose **Java**
5. Select this folder:

```text
C:\Softwares\javafx-sdk-17.0.18\lib
```

6. Click **OK**
7. When IntelliJ asks where to add it, add it at least to:

```text
aima-gui
```

Then click **Apply**.

### Why this matters

JavaFX is not included in modern JDKs by default, so the project needs the JavaFX jars separately.

---

## 10. Confirm the JavaFX library is attached to the GUI module

Still in **Project Structure**:

1. Click **Modules**
2. Select **aima-gui**
3. Open **Dependencies**
4. Make sure you see:

   * **Temurin 17**
   * the JavaFX library from:

```text
C:\Softwares\javafx-sdk-17.0.18\lib
```

If it is missing:

1. Click **+**
2. Choose **Library**
3. Add the JavaFX library

Then click **Apply** and **OK**.

---

## 11. Rebuild the whole project

Now rebuild everything:

1. Go to **Build**
2. Click **Rebuild Project**

Wait for the build to finish.

### Important

Warnings like this are not always fatal:

```text
Some input files use unchecked or unsafe operations.
Recompile with -Xlint:unchecked for details.
```

That warning alone does **not** mean the build failed.

---

## 12. Create or check the run configuration

To run a JavaFX demo:

1. Go to **Run → Edit Configurations**
2. Create a new **Application** configuration if needed
3. Use one of the demo main classes

Example that ran successfully:

```text
aima.gui.demo.agent.WumpusAgentDemo
```

Make sure the JDK for the run configuration is also Java 17 if IntelliJ shows that option.

---

## 13. Run the demo

Run the demo class.

A successful run looks like this at the end:

```text
Process finished with exit code 0
```

That means the project is working.

---

## 14. Final working version combination

This is the exact combination that worked:

```text
JDK 17.0.18
javac 17.0.18
JavaFX SDK 17.0.18
IntelliJ language level 17
Compiler bytecode target 17
```

Short version:

```text
Temurin JDK 17.0.18 + JavaFX SDK 17.0.18 + IntelliJ set to Java 17 everywhere
```

---

## 15. What failed before and why

### Problem 1: Project still used Java 8

The project originally failed with errors related to:

```text
-source 8
```

That caused issues with newer Java syntax and project dependencies.

### Problem 2: JDK 11 + JavaFX 26

This failed because the versions did not match.

Example error:

```text
class file has wrong version 68.0, should be 55.0
```

Meaning:

* JavaFX 26 was built for a much newer Java version
* JDK 11 expected Java 11 class files

So this combination did **not** work:

```text
JDK 11 + JavaFX 26
```

### Correct fix

Use matching versions:

```text
JDK 17 + JavaFX 17
```

---

## 16. Quick checklist

Before building, confirm all of these are true:

* `java -version` shows **17**
* `javac -version` shows **17**
* IntelliJ **Project SDK** = **17**
* IntelliJ **Language level** = **17**
* All **Module SDKs** = **17**
* **Project bytecode version** = **17**
* All module bytecode targets = **17**
* JavaFX library added from:

```text
C:\Softwares\javafx-sdk-17.0.18\lib
```

* JavaFX library attached to `aima-gui`

If all of these are correct, the project should build and run.

---