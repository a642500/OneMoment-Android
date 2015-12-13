## Import


### local properties

You must define those

    sdk.dir=/path/to/your/Android/sdk
    KEY_ALIAS_RELEASE=example
    KEY_ALIAS_DEBUG=example
    STORE_PASSWORD=password
    KEY_PASSWORD=password
    KEYSTORE_PATH=/path/to/keystore/file

in your ```local.properties``` file in project root directory.

### Intellij IDEA/Android Studio

Execute ```gradle idea``` under root project, then directly open this project from IDEA or android studio.

### Eclipse


Add ```apply plugin: 'eclipse'``` to all projects:

```
allprojects {
    repositories {
        jcenter()
    }
    apply plugin: 'eclipse'
}
```

Then, execute ```gradle eclipse``` under root project, then directly open this project from eclipse.



## Gitignore

File [.gitignore](.gitignore) has some contains necessary rules. Please keep they and add some rules if you use different environment.

- You should ignore all build and generated files.
- You should ignore all your IDE configure and environment-dependent files.
- You should ignore file that contains private information, such as Bintray API key, Github access token.


## Format

- Only use UTF-8 encoding file
- Only use LF line break
- Never use Non-ASCII character except resource file


























.




