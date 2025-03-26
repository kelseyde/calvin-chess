package com.kelseyde.calvin.test;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import com.fathzer.chess.utils.test.Chess960Test;
import com.fathzer.chess.utils.test.PerftTest;
import com.fathzer.chess.utils.test.SANTest;

@Suite
@SelectClasses({Chess960Test.class, SANTest.class, PerftTest.class})
@SuppressWarnings("java:S2187") // This test is a test suite, it does need have any test method and its perfectly right
public class ChessUtilsSuiteTest {}