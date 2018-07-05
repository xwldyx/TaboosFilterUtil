package net.threeple.community.comment.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hankcs.algorithm.AhoCorasickDoubleArrayTrie;
import com.hankcs.algorithm.AhoCorasickDoubleArrayTrie.Hit;

public class TabooWordsFilterUtil {
	private static final Logger logger = LoggerFactory.getLogger("");

	private String dictionaryPath = "taboowords.txt";
	private AhoCorasickDoubleArrayTrie<String> ahoCorasickDoubleArrayTrie = new AhoCorasickDoubleArrayTrie<String>();

	public void init() throws IOException {
		loadDictionary(dictionaryPath);
	}
	
	public List<Hit<String>> parseText(String text) throws IOException {
		logger.debug("start parse text with length {}", text.length());
		long start = System.currentTimeMillis();
		List<Hit<String>> parseText = ahoCorasickDoubleArrayTrie.parseText(text);
		logger.debug("parseText cost time {}ms", System.currentTimeMillis() - start);
		return parseText;
	}
	
	public String replace(String text, char a) {
		logger.debug("start parse text with length {}", text.length());
		long start = System.currentTimeMillis();
		List<Hit<String>> parseText = ahoCorasickDoubleArrayTrie.parseText(text);
		char[] result = text.toCharArray();
		for(Hit<String> hit: parseText) {
			for(int i=hit.begin; i<hit.end; i++) {
				result[i] = a;
			}
		}
		logger.debug("parseText cost time {}ms", System.currentTimeMillis() - start);
		return new String(result);
	}
	
	public boolean hasTaboos(String text) {
		return ahoCorasickDoubleArrayTrie.matches(text);
	}
	
	private void loadDictionary(String path) throws IOException {
		Set<String> dictionary = new TreeSet<String>();
		BufferedReader br = new BufferedReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(path), "UTF-8"));
		String line;
		while ((line = br.readLine()) != null) {
			dictionary.add(line);
		}
		br.close();
		logger.debug("load a dictionary of {} taboo words.", dictionary.size());
		TreeMap<String, String> dictionaryMap = new TreeMap<String, String>();
		for (String word : dictionary) {
			dictionaryMap.put(word, word);
		}
		ahoCorasickDoubleArrayTrie.build(dictionaryMap);
	}
	
	public void testCompare() throws IOException {
		Set<String> dictionary = new TreeSet<String>();
		BufferedReader br = new BufferedReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("taboowords.txt"), "UTF-8"));
		String line;
		while ((line = br.readLine()) != null) {
			dictionary.add(line);
		}
		br.close();
//		logger.debug("load a dictionary of {} taboo words.", dictionary.size());
		long start = System.currentTimeMillis();
		TreeMap<String, String> dictionaryMap = new TreeMap<String, String>();
		for (String word : dictionary) {
			dictionaryMap.put(word, word);
		}
		ahoCorasickDoubleArrayTrie.build(dictionaryMap);
//		logger.debug("parseText init cost time {}ms", System.currentTimeMillis() - start);
		String text = loadText("text.txt");
		start = System.currentTimeMillis();
		List<Hit<String>> parseText = ahoCorasickDoubleArrayTrie.parseText(text);
		logger.debug("parseText cost time {}ms", System.currentTimeMillis() - start);
		start = System.currentTimeMillis();
		String result = text;
		for(Hit<String> hit: parseText) {
			char[] aaa = new char[hit.value.length()];
			Arrays.fill(aaa, '*');
			result = result.replaceFirst(hit.value, new String(aaa));
		}
//		System.out.println(result);
//		logger.debug("parseText replace cost time {}ms", System.currentTimeMillis() - start);
		
		start = System.currentTimeMillis();
		char[] result3 = text.toCharArray();
		for(Hit<String> hit: parseText) {
			for(int i=hit.begin; i<hit.end; i++) {
				result3[i] = '*';
			}
		}
//		System.out.println(new String(result3));
//		logger.debug("parseText replace cost time {}ms", System.currentTimeMillis() - start);
		
		
		start = System.currentTimeMillis();
		WordsFilterImpl wordsFilterImpl = new WordsFilterImpl(dictionary);
//		logger.debug("wordsFilterImpl init cost time {}ms", System.currentTimeMillis() - start);
		start = System.currentTimeMillis();
		String[] findTabooWordsFrom = wordsFilterImpl.findTabooWordsFrom(text);
		logger.debug("wordsFilterImpl cost time {}ms", System.currentTimeMillis() - start);
		start = System.currentTimeMillis();
		String result2 = text;
		for(String str :findTabooWordsFrom) {
			char[] aaa = new char[str.length()];
			Arrays.fill(aaa, '*');
			result2 = result2.replaceAll(str, new String(aaa));
		}
//		System.out.println(result2);
//		logger.debug("wordsFilterImpl replace cost time {}ms", System.currentTimeMillis() - start);
		
	}
	
	private String loadText(String path) throws IOException {
		StringBuilder sbText = new StringBuilder();
		BufferedReader br = new BufferedReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(path), "UTF-8"));
		String line;
		while ((line = br.readLine()) != null) {
			sbText.append(line).append("\n");
		}
		br.close();
		return sbText.toString();
	}
	/**
	 * 如果load太大的话可以把结果保存成二进制流读写加速
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "resource" })
	public void testSaveAndLoad() throws Exception
	{
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("tmp"));
		out.writeObject(ahoCorasickDoubleArrayTrie);
		out.close();
		ObjectInputStream in = new ObjectInputStream(new FileInputStream("tmp"));
		ahoCorasickDoubleArrayTrie = (AhoCorasickDoubleArrayTrie<String>) in.readObject();
	}
}
