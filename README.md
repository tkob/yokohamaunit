# YokohamaUnit
YokohamaUnit is a unit testing framework for Java.

Tests are written in a dedicated external DSL (and an embedded expression
language like Groovy) and directly compiled into JUnit test class files.

## Installation
```
git clone https://github.com/tkob/yokohamaunit.git
cd yokohamaunit
./gradlew installApp
```

Now you have an executable named `yokohamaunit` in
`~/yokohamaunit/build/install/yokohamaunit/bin` directory.
Add the directory to your PATH.

## Usage with Gradle

Add to your build.gradle the following:

```
dependencies {
    testCompile group: 'org.mockito', name: 'mockito-core', version: '1.10.8'
    testCompile group: 'junit', name: 'junit', version: '4.11'
    testCompile group: 'org.codehaus.groovy', name: 'groovy-all', version: '2.3.7'
}

task compileDocy(type:Exec) {
    ext.classpath = configurations.testCompile.join(File.pathSeparator) +
                    File.pathSeparator + sourceSets.main.output.classesDir +
                    File.pathSeparator + sourceSets.test.output.classesDir
    ext.classesDir = sourceSets.test.output.classesDir
    ext.baseDir = "$projectDir/src/test/docy"
    ext.sourceSet = fileTree(dir: ext.baseDir, include: '**/*.docy')
    ext.command = [
        'yokohamaunit', 'docyc',
        '-cp', ext.classpath,
        '-d', ext.classesDir,
        '-basedir', ext.baseDir
        ]
    ext.command += ext.sourceSet.getFiles()

    if (!ext.classesDir.exists()) {
        ext.classesDir.mkdirs()
    }
    commandLine ext.command
}

compileDocy.dependsOn compileTestJava
test.dependsOn compileDocy
```