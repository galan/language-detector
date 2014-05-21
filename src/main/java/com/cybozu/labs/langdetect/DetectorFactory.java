package com.cybozu.labs.langdetect;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import net.arnx.jsonic.JSON;
import net.arnx.jsonic.JSONException;

import com.cybozu.labs.langdetect.profiles.Profile;
import com.cybozu.labs.langdetect.util.LangProfile;


/**
 * Language Detector Factory Class
 *
 * This class manages an initialization and constructions of {@link Detector}.
 *
 * Before using language detection library, load profiles with {@link DetectorFactory#loadProfile(String)} method and
 * set initialization parameters.
 *
 * When the language detection, construct Detector instance via {@link DetectorFactory#create()}. See also
 * {@link Detector}'s sample code.
 *
 * <ul>
 * <li>4x faster improvement based on Elmer Garduno's code. Thanks!</li>
 * </ul>
 *
 * @see Detector
 * @author Nakatani Shuyo
 */
public class DetectorFactory {

	public HashMap<String, double[]> wordLangProbMap;
	public ArrayList<String> langlist;
	public Long seed = null;


	private DetectorFactory() {
		wordLangProbMap = new HashMap<String, double[]>();
		langlist = new ArrayList<String>();
	}

	private static DetectorFactory instance = new DetectorFactory();


	/**
	 * Load profiles from specified directory. This method must be called once before language detection.
	 *
	 * @param profileDirectory profile directory path
	 * @throws LangDetectException Can't open profiles(error code = {@link ErrorCode#FileLoadError}) or profile's format
	 *         is wrong (error code = {@link ErrorCode#FormatError})
	 */
	public static void loadProfile(String profileDirectory) throws LangDetectException {
		loadProfile(new File(profileDirectory));
	}


	public static void loadProfile(Profile profile) throws LangDetectException {
		loadProfile(profile.getProfiles());
	}


	public static void loadProfile(InputStream[] streams) throws LangDetectException {
		//int langsize = listFiles.length;
		int index = 0;
		for (InputStream stream: streams) {
			try (InputStream is = stream) {
				LangProfile profile = JSON.decode(is, LangProfile.class);
				addProfile(profile, index, streams.length);
				++index;
			}
			catch (JSONException e) {
				throw new LangDetectException(ErrorCode.FormatError, "profile format error in '" + stream + "'");
			}
			catch (IOException e) {
				throw new LangDetectException(ErrorCode.FileLoadError, "can't open stream '" + stream + "'");
			}
		}
	}


	/**
	 * Load profiles from specified directory. This method must be called once before language detection.
	 *
	 * @param profileDirectory profile directory path
	 * @throws LangDetectException Can't open profiles(error code = {@link ErrorCode#FileLoadError}) or profile's format
	 *         is wrong (error code = {@link ErrorCode#FormatError})
	 */
	public static void loadProfile(File profileDirectory) throws LangDetectException {
		File[] listFiles = profileDirectory.listFiles();
		if (listFiles == null) {
			throw new LangDetectException(ErrorCode.NeedLoadProfileError, "Not found profile: " + profileDirectory);
		}

		List<InputStream> streams = new ArrayList<>();
		for (File file: listFiles) {
			if (!file.getName().startsWith(".") && file.isFile()) {
				try {
					streams.add(new FileInputStream(file));
				}
				catch (FileNotFoundException ex) {
					throw new LangDetectException(ErrorCode.FormatError, "file not found: '" + file.getName() + "'");
				}
			}
		}
		loadProfile(streams.toArray(new InputStream[] {}));
	}


	/**
	 * Load profiles from specified directory. This method must be called once before language detection.
	 *
	 * @param profileDirectory profile directory path
	 * @throws LangDetectException Can't open profiles(error code = {@link ErrorCode#FileLoadError}) or profile's format
	 *         is wrong (error code = {@link ErrorCode#FormatError})
	 */
	public static void loadProfile(List<String> json_profiles) throws LangDetectException {
		int index = 0;
		int langsize = json_profiles.size();
		if (langsize < 2) {
			throw new LangDetectException(ErrorCode.NeedLoadProfileError, "Need more than 2 profiles");
		}

		for (String json: json_profiles) {
			try {
				LangProfile profile = JSON.decode(json, LangProfile.class);
				addProfile(profile, index, langsize);
				++index;
			}
			catch (JSONException e) {
				throw new LangDetectException(ErrorCode.FormatError, "profile format error");
			}
		}
	}


	/**
	 * @param profile
	 * @param langsize
	 * @param index
	 * @throws LangDetectException
	 */
	static/* package scope */void addProfile(LangProfile profile, int index, int langsize) throws LangDetectException {
		String lang = profile.name;
		if (instance.langlist.contains(lang)) {
			throw new LangDetectException(ErrorCode.DuplicateLangError, "duplicate the same language profile");
		}
		instance.langlist.add(lang);
		for (String word: profile.freq.keySet()) {
			if (!instance.wordLangProbMap.containsKey(word)) {
				instance.wordLangProbMap.put(word, new double[langsize]);
			}
			int length = word.length();
			if (length >= 1 && length <= 3) {
				double prob = profile.freq.get(word).doubleValue() / profile.n_words[length - 1];
				instance.wordLangProbMap.get(word)[index] = prob;
			}
		}
	}


	/**
	 * Clear loaded language profiles (reinitialization to be available)
	 */
	static public void clear() {
		instance.langlist.clear();
		instance.wordLangProbMap.clear();
	}


	/**
	 * Construct Detector instance
	 *
	 * @return Detector instance
	 * @throws LangDetectException
	 */
	static public Detector create() throws LangDetectException {
		return createDetector();
	}


	/**
	 * Construct Detector instance with smoothing parameter
	 *
	 * @param alpha smoothing parameter (default value = 0.5)
	 * @return Detector instance
	 * @throws LangDetectException
	 */
	public static Detector create(double alpha) throws LangDetectException {
		Detector detector = createDetector();
		detector.setAlpha(alpha);
		return detector;
	}


	static private Detector createDetector() throws LangDetectException {
		if (instance.langlist.size() == 0) {
			throw new LangDetectException(ErrorCode.NeedLoadProfileError, "need to load profiles");
		}
		Detector detector = new Detector(instance);
		return detector;
	}


	public static void setSeed(long seed) {
		instance.seed = seed;
	}


	public static final List<String> getLangList() {
		return Collections.unmodifiableList(instance.langlist);
	}
}
