package net.threeple.community.comment.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WordsFilterImpl
{
  static final Logger logger = LoggerFactory.getLogger(WordsFilterImpl.class);
  private String[] tbWords;
  private Character[] initials;
  private Character[] suffixes;
  private Character[] punctuations;
  private int maxLength;
  
  public WordsFilterImpl(Set<String> tabooWords)
  {
    setup(tabooWords);
  }
  
  private void setup(Set<String> tabooWords)
  {
    Set<Character> initialSet = new TreeSet<Character>();
    Set<Character> suffixSet =  new TreeSet<Character>();
    for (String word : tabooWords)
    {
      Character initial = Character.valueOf(word.charAt(0));
      initialSet.add(initial);
      int wordLength = word.length();
      Character suffix = Character.valueOf(word.charAt(wordLength - 1));
      suffixSet.add(suffix);
      if (this.maxLength < wordLength) {
        this.maxLength = wordLength;
      }
    }
    this.tbWords = ((String[])tabooWords.toArray(new String[0]));
    logger.debug("共配置{}个敏感词", Integer.valueOf(this.tbWords.length));
    this.initials = ((Character[])initialSet.toArray(new Character[0]));
    this.suffixes = ((Character[])suffixSet.toArray(new Character[0]));
    String pss = ",，.。:：《》”“\"、？?！!";
    this.punctuations = new Character[pss.length()];
    for (int i = 0; i < pss.length(); i++) {
      this.punctuations[i] = Character.valueOf(pss.charAt(i));
    }
    Arrays.sort(this.punctuations);
  }
  
  public String[] findTabooWordsFrom(String text)
  {
    Set<String> tbwSet =  new TreeSet<String>();
    List<Integer> initialIndexes = new ArrayList<>();
    for (int index = 0; index < text.length(); index++)
    {
      Character character = Character.valueOf(text.charAt(index));
      if (Arrays.binarySearch(this.punctuations, character) < 0)
      {
        boolean isInitial = Arrays.binarySearch(this.initials, character) >= 0;
        boolean isSuffix = Arrays.binarySearch(this.suffixes, character) >= 0;
        if ((isInitial) || (isSuffix))
        {
          if (isInitial) {
            initialIndexes.add(Integer.valueOf(index));
          }
          if (isSuffix)
          {
            Integer[] indexes = (Integer[])initialIndexes.toArray(new Integer[0]);
            if (indexes.length > 0) {
              for (int i = indexes.length - 1; i >= 0; i--) {
                if (indexes[i].intValue() != index)
                {
                  int wordLength = index - indexes[i].intValue() + 1;
                  if (wordLength > this.maxLength)
                  {
                    initialIndexes.removeAll(initialIndexes.subList(0, i));
                    break;
                  }
                  String word = text.substring(indexes[i].intValue(), index + 1);
                  if (Arrays.binarySearch(this.tbWords, word) >= 0) {
                    tbwSet.add(word);
                  }
                }
              }
            }
          }
        }
      }
    }
    return (String[])tbwSet.toArray(new String[0]);
  }
}

