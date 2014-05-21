package com.cybozu.labs.langdetect.profiles;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


/**
 * daniel should have written a comment here.
 *
 * @author daniel
 */
public abstract class AbstractProfile implements Profile {

	@Override
	public InputStream[] getProfiles() {
		List<InputStream> streams = new ArrayList<>();
		for (String name: getNames()) {
			streams.add(getClass().getResourceAsStream(name));
		}
		return streams.toArray(new InputStream[] {});
	}


	protected abstract String[] getNames();

}
