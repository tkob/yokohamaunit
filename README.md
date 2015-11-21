# YokohamaUnit

[![Build Status](https://travis-ci.org/tkob/yokohamaunit.svg?branch=master)](https://travis-ci.org/tkob/yokohamaunit)

YokohamaUnit is a unit testing framework for Java.

Tests are written in a dedicated external DSL (and Groovy as an embedded
expression language) and directly compiled into JUnit test class files.

## A Taste of YokohamaUnit

```
*[StringUtils]: yokohama.unit.example.StringUtils

# Test: Test cases for `toSnakeCase`

Assert that `StringUtils.toSnakeCase(input)` is `expected`
for all input and expected in Table [1].

Assert that `StringUtils.toSnakeCase(null)` throws
an instance of `java.lang.NullPointerException`.

| input           | expected          |
| --------------- | ----------------- |
| ""              | ""                |
| "aaa"           | "aaa"             |
| "HelloWorld"    | "hello_world"     |
| "practiceJunit" | "practice_junit"  |
| "practiceJUnit" | "practice_j_unit" |
| "hello_world"   | "hello_world"     |
[1]
```

## Usage with Gradle

Add to your build.gradle the following:

```
buildscript {
    repositories {
        mavenCentral()
        jcenter()
        maven { url 'https://dl.bintray.com/tkob/maven' }
    }
    dependencies {
        classpath 'yokohama.unit:yokohamaunit:0.2.0'
    }
}

dependencies {
    testCompile group: 'org.mockito', name: 'mockito-core', version: '1.10.8'
    testCompile group: 'junit', name: 'junit', version: '4.11'
    testCompile group: 'org.codehaus.groovy', name: 'groovy-all', version: '2.3.7'
}

task compileDocy << {
    ext.classpath = configurations.testCompile.join(File.pathSeparator) +
                    File.pathSeparator + sourceSets.main.output.classesDir +
                    File.pathSeparator + sourceSets.test.output.classesDir
    ext.classesDir = sourceSets.test.output.classesDir
    ext.baseDir = "$projectDir/src/test/docy"
    ext.sourceSet = fileTree(dir: ext.baseDir, include: '**/*.docy')
    ext.args = [
        'docyc',
        '-cp', ext.classpath,
        '-d', ext.classesDir,
        '-basedir', ext.baseDir
        ]
    ext.args += ext.sourceSet.getFiles()

    if (!ext.classesDir.exists()) {
        ext.classesDir.mkdirs()
    }
    def main = new yokohama.unit.Main(new yokohama.unit.CommandFactory());
    main.run(System.in, System.out, System.err, ext.args as String[])
}
compileDocy.dependsOn compileTestJava
test.dependsOn compileDocy
```

An example project is available from https://github.com/tkob/yokohamaunit-example

## Usage from Command Line

Download and extract the
[latest release](https://github.com/tkob/yokohamaunit/releases),
and add its `bin` directory to your PATH.

Then, you can compile docy source files by typing something like:

```
yokohamaunit docyc -cp PROJECT_CLASSPATH -d build/classes/test -basedir src/test/docy DOCY_FILES...
```

where PROJECT_CLASSPATH is the classpath of your project and DOCY_FILES are
docy files to compile which are in `src/test/docy` directory.
(DOCY_FILES must be prefixed with `src/test/docy`)