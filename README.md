<a href='https://ko-fi.com/O5O819SEH' target='_blank'><img height='24' style='border:0px;height:24px;' src='https://az743702.vo.msecnd.net/cdn/kofi2.png?v=2' border='0' alt='Buy Me a Coffee at ko-fi.com' /></a>[![](https://jitpack.io/v/com.londogard/text-gen-kt.svg)](https://jitpack.io/#com.londogard/text-gen-kt)

# text-gen-kt
Text Generation in Kotlin. Will include multiple pre-trained models &amp; ability to train your own. Easy-to-use API as a goal too.

## Installation
### Jitpack (easiest)
Add the following to your `build.gradle`. `$version` should be equal to the version supplied by tag above.
```
   repositories {
        maven { url "https://jitpack.io" }
   }
   dependencies {
         implementation 'com.londogard:text-gen-kt:$version'
   }
```
### GitHub Packages
Add the following to your `build.gradle`. `$version` (this one messed up, is 1.0-beta) should be equal to the version supplied by tag above.  
The part with logging into github repository is how I understand that you need to login. If you know a better way please ping me in an issue.
```
repositories {
   maven {
     url = uri("https://maven.pkg.github.com/londogard/text-gen-kt")
     credentials {
         username = project.findProperty("gpr.user") ?: System.getenv("GH_USERNAME")
         password = project.findProperty("gpr.key") ?: System.getenv("GH_TOKEN")
     }
}
}
dependencies {
   implementation "com.londogard:summarize-kt:$version"
}
```

## [BETA] Usage
OBS - usage might change in future as it's beta.  

#### Create model and generate text
```kotlin
val model = LanguageModelImpl(PretrainedModels.SHAKESPEARE, GenerationLevel.WORD)
val generatedText = model.generateText("have a", 150, 0.1)
```
So what are we doing here?  
1. Creating a pretrained model of **Shakespeare**
2. Selecting **word-level** generation
3. Generating text using a **prefix** with **n** units (in this case words) and a **temperature** of 0.1 (the more, the crazier) 

`val generatedText` now contains 150 words starting with "have a".

##### PretrainedModels
`CUSTOM` might not be working for sure. Works to create, but saving is currently not implemented for sure.  
The models are saved using `Concise Binary Object Representation` (RFC 7049).
```kotlin
enum class PretrainedModels {
    SHAKESPEARE,
    CARDS_AGAINST_WHITE,
    CARDS_AGAINST_BLACK,
    CUSTOM
}
```
##### GenerationLevel
No pretrained models exist for `CHAR` currently. `CHAR` is not supported either.
```kotlin
enum class GenerationLevel {
    WORD,
    CHAR
}
```
### CUSTOM
To train a custom model, initiate model as usual and then call  
`model.createCustomModel("<PATH>", "<MODEL_TO_SAVE_NAME>", <IS_EACH_LINE_NEW_DOCUMENT?>)`