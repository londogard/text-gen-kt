[![Gitpod ready-to-code](https://img.shields.io/badge/Gitpod-ready--to--code-blue?logo=gitpod)](https://gitpod.io/#https://github.com/londogard/text-gen-kt)

<a href='https://ko-fi.com/O5O819SEH' target='_blank'><img height='24' style='border:0px;height:24px;' src='https://az743702.vo.msecnd.net/cdn/kofi2.png?v=2' border='0' alt='Buy Me a Coffee at ko-fi.com' /></a>[![](https://jitpack.io/v/com.londogard/text-gen-kt.svg)](https://jitpack.io/#com.londogard/text-gen-kt)

# text-gen-kt
Text Generation in Kotlin that's 'light' on resources. 

 - Pre-trained models (Shakespeare & Cards Against Humanity)
 - Easy-to-use API (training & generating text)
 - Customizable

## Installation
<details open>
<summary><b>Jitpack</b> (the easiest)</summary>
<br>
Add the following to your <code>build.gradle</code>. <code>$version</code> should be equal to the version supplied by tag above.
<br>
<br>
<pre>
repositories {
  maven { url "https://jitpack.io" }
}
dependencies {
  implementation 'com.londogard:text-gen-kt:$version'
}        
</pre>
</details>
<details>
   <summary><b>GitHub Packages</b></summary>
<br>
Add the following to your <code>build.gradle</code>. <code>$version</code> should be equal to the version supplied by tag above.  
The part with logging into github repository is how I understand that you need to login. If you know a better way please ping me in an issue.
<br>
<br>
<pre>
repositories {
   maven {
     url = uri("https://maven.pkg.github.com/londogard/smile-nlp-kt")
     credentials {
         username = project.findProperty("gpr.user") ?: System.getenv("GH_USERNAME")
         password = project.findProperty("gpr.key") ?: System.getenv("GH_TOKEN")
     }
}
}
dependencies {
   implementation "com.londogard:text-gen-kt:$version"
}   
</pre>
</details>

## Usage
Only the simplest API-usages shown with no overrides. It should be straight-forward to override
 different options.  
 
**Loading a Pretrained Model and Text Generation**  
Find a few pre-trained models [here](https://github.com/londogard/text-gen-kt/blob/master/files/models/).  
Includes Shakespeare, Cards Against Humanity (Black & White Card versions).
```kotlin
// Have a pretrained model locally, in say 'shakespeare.cbor'
val absPathToModel = "/path/to/shakespeare.cbor"
val languageModel = LanguageModel.loadPretrainedModel(absPathToModel)

// There exists a lot configs to change if you'd like, but this is the simplest text generation.
val generatedSentences: List<String> = SimpleTextGeneration.generateText(languageModel = languageModel)
generatedSentences.foreach(::println)

SimpleTextGeneration
    .generateText(languageModel, seed = "This is who I am")
    .foreach(::println)

// Prints the generated sentences. All which starts with "This is who I am"
```

**Training your own Model**  
```kotlin
// Have some text you wish to run on
val documents: List<String> = listOf(File('somePath').readText)

// n selects how much you want the model to remember. We use default tokenizer here.
val trainedModel = LanguageModel.trainModel(documents, n=3)
trainModel.serialize("/path/to/model.cbor")

val generatedSentences: List<String> = SimpleTextGeneration.generateText(languageModel = trainedModel)
generatedSentences.foreach(::println)
```

## Steps in text-generation
Search calls smoothing to retrieve tokens & probabilities.   
Smoothing access the Language Model to retrieve probabilities, and if they don't 
exist smooth it out somehow, meaning that you find the closest match. 
>**Smoothing example**  
> ["hej", "där", "borta"] has never been seen in the data, then we don't know what to generate as 
>the next word. Simple back-off smoothing would then try to see if ["där", "borta"] exists in the data and try 
>to generate a word from that instead.  
>There's different 
ways to smooth data, but in its essence it's the idea of finding a value of something we've never 
seen before.
   
Smoothing then applies penalties and finally normalization.

## Structure
There's a few different components

1. Language Model
2. Tokenizer
3. Normalization
4. Smoothing
5. Search
6. Penalties

The idea is that the Language Model is basically a storage of probabilities.  
To generate text we somehow need to tap into this 'database' and fetch values 
in a interesting way. This is done using the tools in 2-6.  
This division is done in a fashion were we actually don't care if it's word-level 
or character-level text generation (or anything else really). The trained Language Model 
can simply be used to generate text in a lot of different fashions, with different penalties 
and a lot other!

### Language Model
The Language Model is basically just a storage, with some clever structure.
There's two ways to get a Language Model, either load a pretrained model through 
a config-file or train it yourself on some text!

### Tokenizer
Tokenizer is a tool to tokenize text into tokens. A simple tokenizer could be either 
tokenize characters, i.e. one character per token. Another could be to split words, e.g. 
`tokenize("vem kan hitta min keps?") = ["vem", "kan", "hitta", "min", "keps", "?"]`.  
Clever approaches sometimes split words like `kasper's` into `kasper & 's`, which 
reduces the dimensionality a bit.

### Normalization
When all probabilities are retrieved they need to be normalized to be in `[0,1)` (0-100 %).  
This can be done in different ways, the simplest being to just divide all by the sum.

### Smoothing
Smoothing in this case is to retrieve probabilities. If the Language Model does not contain 
a word we still need to generate text, how is this done? Smoothing says how the probabilities 
and tokens should be found.  
A simple method is to "backoff", that is if we don't find something for `"who is there"` the 
model can still have `"is that"`, which we then want to return.

### Search
Search is basically how we should select the tokens received by the Smoothing.  
A greedy search is to just select the top probability each time.

### Penalties
Penalty is simply a way to penalize certain features. E.g. swear words might be off-limit, we 
might not want to generate the same ngram again? It's up to you!

## Available Models
- shakespeare_char.cbor (n=100, keepMinFreq=5)
- shakespeare_word.cbor (n=100, keepMinFreq=1)
- cardsagainst_white_char.cbor (n=100, keepMinFreq=1)
- cardsagainst_white_word.cbor (n=100, keepMinFreq=1)
- cardsagainst_black_char.cbor (n=100, keepMinFreq=1)
- cardsagainst_black_word.cbor (n=100, keepMinFreq=1)