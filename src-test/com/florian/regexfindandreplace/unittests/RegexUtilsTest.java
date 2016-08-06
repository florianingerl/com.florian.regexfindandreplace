package com.florian.regexfindandreplace.unittests;

import static org.junit.Assert.*;

import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import org.junit.Test;

import com.florian.regexfindandreplace.IMatchEvaluator;
import com.florian.regexfindandreplace.MatchEvaluatorException;
import com.florian.regexfindandreplace.RegexUtils;

public class RegexUtilsTest {

	@Test
	public void replaceAll_WhenNoMatchCanBeFound_ReturnsTheUnchangedInputString() {
		String input = "Heilig ist der Herr";
		
		try {
			input = RegexUtils.replaceAll(input, "\\d", null, 0);
		} catch (Exception e) {
			e.printStackTrace();
			fail("This shouldn't throw an exception!");
		}
		
		assertEquals("Heilig ist der Herr", input);
	}
	
	@Test
	public void replaceAll_WithAMatchEvaluator_MatchEvaluatorIsCorrectlyCalled() {
		String input = "Florian is 23 years old. His sister is 2 years older. She is 25 years old!";
		IMatchEvaluator matchEvaluator = new IMatchEvaluator()
				{

					@Override
					public String evaluateMatch(MatchResult match) throws Exception {
						int i = Integer.parseInt( match.group( ) );
						return "" + (i+1);
					}
			
				};
		try {
			input = RegexUtils.replaceAll(input, "\\d{2}", matchEvaluator, 0);
		} catch (Exception e) {
			e.printStackTrace();
			fail("This shouldn't throw an exception!");
		}
		
		assertEquals("Florian is 24 years old. His sister is 2 years older. She is 26 years old!", input);
	}
	
	@Test
	public void replaceAll_WithAMatchEvaluatorAndACaseInsensitiveSearch_MatchEvaluatorIsCorrectlyCalled() {
		String input = "All Nouns in the English Language don't start with capital Letters";
		IMatchEvaluator matchEvaluator = new IMatchEvaluator()
				{

					@Override
					public String evaluateMatch(MatchResult match) throws Exception {
						String temp = match.group();
						return Character.toLowerCase(temp.charAt(0) ) + temp.substring(1);
					}
			
				};
		try {
			input = RegexUtils.replaceAll(input, "(nouns|language|letters)", matchEvaluator, Pattern.CASE_INSENSITIVE);
		} catch (Exception e) {
			e.printStackTrace();
			fail("This shouldn't throw an exception!");
		}
		
		assertEquals("All nouns in the English language don't start with capital letters", input);
	}
	
	@Test
	public void replaceAll_WithAMatchEvaluatorAndACaseSensitiveSearch_NoMatchIsFound()
	{
		String input = "C";
		try {
			input = RegexUtils.replaceAll(input, "c", new IMatchEvaluator(){

				@Override
				public String evaluateMatch(MatchResult match) throws Exception {
					return "A";
				}}, 0);
		} catch (MatchEvaluatorException e) {
			fail("This shouldn't happen!");
		}
		assertEquals("C", input);
		
	}
	
	@Test
	public void replaceAll_WhenTheMatchEvaluatorThrowsAnException_AMatchEvaluatorExceptionIsThrown()
	{
		String input = "No matter";
		IMatchEvaluator matchEvaluator = new IMatchEvaluator()
				{
					@Override
					public String evaluateMatch(MatchResult match) throws Exception {
						return match.group(100); //The group is out of range
					}
			
				};
				
		try {
			input = RegexUtils.replaceAll(input, "M", matchEvaluator, Pattern.CASE_INSENSITIVE);
		} catch (MatchEvaluatorException e) {
			assertTrue( e.getCause() instanceof IndexOutOfBoundsException );
			return;
		}	
		fail("The previous call should have thrown the exception!");
	}


}
