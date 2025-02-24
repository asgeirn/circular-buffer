# Lock Free Concurrent Circular Buffer

![Build Status](https://github.com/asgeirn/circular-buffer/actions/workflows/maven.yml/badge.svg)

It supports multiple independent readers, so many consumers can get updates to this circular buffer.

It is lock free, so neither writers nor readers will block.  If you try to `take()` on a buffer with no updates a null is returned, and if you `drain()` a buffer with no updates you get an empty list.

I recommend you give it a spin if you want a (simple) circular buffer.  It does detect buffer wraparounds, but will in this case reset the reader to the end of the buffer, losing any intermediate updates. Tune your buffer size accordingly.

Take a look at the unit tests to get an idea on how to use this buffer.

## Using this library as a Maven / Gradle dependency

To use this library as a Maven dependency, you'll need to add the following repository location in your `pom.xml`:

```
<repositories>
  <repository>
    <id>github-asgeirn</id>
    <url>https://maven.pkg.github.com/asgeirn/circular-buffer</url>
  </repository>
</repositories>
```

Or the following in `build.gradle`:

```
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/asgeirn/circular-buffer")
        credentials {
            username = project.findProperty("gpr.user") ?: System.getenv("USERNAME")
            password = project.findProperty("gpr.key") ?: System.getenv("TOKEN")
        }
   }
}
```

## Authenticating to use the repository

GitHub requres that you authenticate in order to be able to use the repository.  You only need the `read:packages` permission, so I recommend you create a Personal Access Token (PAT) for this sole purpose over at https://github.com/settings/tokens/new

Then add the following in `~/.m2/settings.xml`:

```
<server>
  <id>github-asgeirn</id>
  <username>your_github_username</username>
  <password>ghp_XXXXXX</password>
</server>
```

Or the following in `~/.gradle/gradle.properties`:

```
gpr.user=your_github_username
gpr.key=ghp_XXXXXX
```
