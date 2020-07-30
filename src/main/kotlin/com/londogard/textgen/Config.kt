package com.londogard.textgen

import com.londogard.textgen.languagemodels.InternalLanguageModel
import com.londogard.textgen.languagemodels.InternalVocabulary
import kotlinx.serialization.Serializable

@Serializable
data class Config(val n: Int,
                  val languageModel: InternalLanguageModel,
                  val vocab: InternalVocabulary)