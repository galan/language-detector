package com.cybozu.labs.langdetect.profiles;

import java.io.InputStream;


/**
 * Bundle profiles in jars
 *
 * @author daniel
 */
public interface Profile {

	public InputStream[] getProfiles();

}
