package com.wedesign.speech.tts;

import com.microsoft.speech.tts.Synthesizer;
import com.microsoft.speech.tts.Voice;

import com.wedesign.speech.utils.SpeechUtils;

public class SpeechTts {
    private Synthesizer mSynthesizer;

    public SpeechTts() {

        // Note: The way to get api key:
        // Free: https://www.microsoft.com/cognitive-services/en-us/subscriptions?productId=/products/Bing.Speech.Preview
        // Paid: https://portal.azure.com/#create/Microsoft.CognitiveServices/apitype/Bing.Speech/pricingtier/S0
        mSynthesizer = new Synthesizer(SpeechUtils.SpeechSubscriptionKey);
        Voice v = new Voice("en-US", "Microsoft Server Speech Text to Speech Voice (en-US, ZiraRUS)", Voice.Gender.Female, true);
        mSynthesizer.SetVoice(v, null);
    }

    public void speek(String textToSynthesize) {
        mSynthesizer.SpeakToAudio(textToSynthesize);
    }

}
