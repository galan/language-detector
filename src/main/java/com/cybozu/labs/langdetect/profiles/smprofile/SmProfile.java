package com.cybozu.labs.langdetect.profiles.smprofile;

import com.cybozu.labs.langdetect.profiles.AbstractProfile;


/**
 * Profiles from t he profiles.sm directory
 *
 * @author daniel
 */
public class SmProfile extends AbstractProfile {

	@Override
	protected String[] getNames() {
		return new String[] {"ar", "bg", "bn", "ca", "cs", "da", "de", "el", "en", "es", "et", "fa", "fi", "fr", "gu", "he", "hi", "hr", "hu", "id", "it",
				"ja", "ko", "lt", "lv", "mk", "ml", "nl", "no", "pa", "pl", "pt", "ro", "ru", "si", "sq", "sv", "ta", "te", "th", "tl", "tr", "uk", "ur", "vi",
				"zh-cn", "zh-tw"};
	}

}
