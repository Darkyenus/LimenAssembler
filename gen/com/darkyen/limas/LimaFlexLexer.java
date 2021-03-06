/* The following code was generated by JFlex 1.7.0-SNAPSHOT tweaked for IntelliJ platform */

package com.darkyen.limas;

import static com.darkyen.limas.LimaToken.*;

/** Flex-based lexer for Limen Alpha Assembly */

class LimaFlexLexer {

  /** This character denotes the end of file */
  public static final int YYEOF = -1;

  /** initial size of the lookahead buffer */
  private static final int ZZ_BUFFERSIZE = 16384;

  /** lexical states */
  public static final int YYINITIAL = 0;

  /**
   * ZZ_LEXSTATE[l] is the state in the DFA for the lexical state l
   * ZZ_LEXSTATE[l+1] is the state in the DFA for the lexical state l
   *                  at the beginning of a line
   * l is of the form l = 2*k, k a non negative integer
   */
  private static final int ZZ_LEXSTATE[] = { 
     0, 0
  };

  /** 
   * Translates characters to character classes
   * Chosen bits are [9, 6, 6]
   * Total runtime size is 1568 bytes
   */
  public static int ZZ_CMAP(int ch) {
    return ZZ_CMAP_A[(ZZ_CMAP_Y[ZZ_CMAP_Z[ch>>12]|((ch>>6)&0x3f)]<<6)|(ch&0x3f)];
  }

  /* The ZZ_CMAP_Z table has 272 entries */
  static final char ZZ_CMAP_Z[] = zzUnpackCMap(
    "\1\0\1\100\1\200\u010d\100");

  /* The ZZ_CMAP_Y table has 192 entries */
  static final char ZZ_CMAP_Y[] = zzUnpackCMap(
    "\1\0\1\1\1\2\175\3\1\4\77\3");

  /* The ZZ_CMAP_A table has 320 entries */
  static final char ZZ_CMAP_A[] = zzUnpackCMap(
    "\11\0\1\2\1\1\1\41\1\2\1\1\22\0\1\2\3\0\1\27\5\0\1\10\1\30\1\40\1\36\1\0\1"+
    "\7\2\31\6\26\2\4\1\20\1\0\1\23\1\0\1\24\1\0\1\17\1\34\1\32\1\34\1\37\2\34"+
    "\1\3\1\35\6\3\1\33\2\3\1\25\10\3\1\21\1\0\1\22\1\0\1\3\1\0\1\34\1\32\1\34"+
    "\1\13\1\14\1\15\1\3\1\35\5\3\1\12\1\33\2\3\1\16\2\3\1\11\5\3\1\5\1\0\1\6\7"+
    "\0\1\41\242\0\2\41\26\0");

  /** 
   * Translates DFA states to action switch labels.
   */
  private static final int [] ZZ_ACTION = zzUnpackAction();

  private static final String ZZ_ACTION_PACKED_0 =
    "\1\0\1\1\1\2\1\3\1\4\1\5\1\6\1\1"+
    "\2\3\1\7\1\10\1\11\1\12\1\13\1\14\1\3"+
    "\1\4\2\1\1\4\1\15\1\16\1\4\1\0\1\17"+
    "\1\20\1\21\2\3\1\22\1\23\1\24\1\25\1\4"+
    "\1\0\1\3\1\26\1\4\1\21\1\3\1\27\1\3"+
    "\1\30";

  private static int [] zzUnpackAction() {
    int [] result = new int[44];
    int offset = 0;
    offset = zzUnpackAction(ZZ_ACTION_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAction(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /** 
   * Translates a state to a row index in the transition table
   */
  private static final int [] ZZ_ROWMAP = zzUnpackRowMap();

  private static final String ZZ_ROWMAP_PACKED_0 =
    "\0\0\0\42\0\104\0\146\0\210\0\42\0\42\0\252"+
    "\0\314\0\356\0\42\0\42\0\42\0\42\0\42\0\42"+
    "\0\u0110\0\u0132\0\u0154\0\u0176\0\u0198\0\u01ba\0\42\0\u01dc"+
    "\0\u01dc\0\42\0\u01fe\0\u0220\0\u0242\0\u0264\0\146\0\42"+
    "\0\u0286\0\u01dc\0\u02a8\0\u02ca\0\u02ec\0\u030e\0\42\0\42"+
    "\0\u0330\0\146\0\u0352\0\146";

  private static int [] zzUnpackRowMap() {
    int [] result = new int[44];
    int offset = 0;
    offset = zzUnpackRowMap(ZZ_ROWMAP_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackRowMap(String packed, int offset, int [] result) {
    int i = 0;  /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int high = packed.charAt(i++) << 16;
      result[j++] = high | packed.charAt(i++);
    }
    return j;
  }

  /** 
   * The transition table of the DFA
   */
  private static final int [] ZZ_TRANS = zzUnpackTrans();

  private static final String ZZ_TRANS_PACKED_0 =
    "\1\2\2\3\1\4\1\5\1\6\1\7\1\10\1\2"+
    "\1\11\1\4\1\12\3\4\1\13\1\14\1\15\1\16"+
    "\1\17\1\20\1\21\1\22\1\23\1\24\1\25\4\4"+
    "\1\26\1\4\1\27\44\0\2\3\42\0\2\4\4\0"+
    "\6\4\6\0\2\4\2\0\5\4\1\0\1\4\6\0"+
    "\1\5\6\0\1\30\2\31\10\0\1\5\2\0\1\5"+
    "\1\31\1\0\1\31\1\32\1\0\1\30\11\0\1\33"+
    "\1\34\34\0\2\4\4\0\1\4\1\35\4\4\6\0"+
    "\2\4\2\0\5\4\1\0\1\4\5\0\2\4\4\0"+
    "\3\4\1\36\2\4\6\0\2\4\2\0\5\4\1\0"+
    "\1\4\5\0\2\4\4\0\6\4\6\0\1\4\1\37"+
    "\2\0\1\37\4\4\1\0\1\4\6\0\1\5\6\0"+
    "\1\30\2\31\10\0\1\22\2\0\1\22\1\31\1\40"+
    "\1\31\1\32\1\0\1\30\5\0\1\41\5\0\6\41"+
    "\6\0\1\41\4\0\4\41\1\0\1\41\6\0\1\5"+
    "\6\0\3\31\10\0\1\22\2\0\1\25\1\31\1\0"+
    "\1\31\2\0\1\31\6\0\1\5\6\0\1\30\2\31"+
    "\10\0\1\22\2\0\1\25\1\42\1\40\1\31\1\32"+
    "\1\0\1\30\6\0\1\43\21\0\1\43\2\0\1\43"+
    "\14\0\1\31\6\0\3\31\10\0\1\31\2\0\2\31"+
    "\1\0\1\31\1\32\1\0\1\31\2\0\1\33\1\0"+
    "\40\33\10\34\1\44\31\34\3\0\2\4\4\0\2\4"+
    "\1\45\3\4\6\0\2\4\2\0\5\4\1\0\1\4"+
    "\5\0\2\4\4\0\4\4\1\46\1\4\6\0\2\4"+
    "\2\0\5\4\1\0\1\4\5\0\2\41\4\0\6\41"+
    "\6\0\2\41\2\0\5\41\1\0\1\41\6\0\1\43"+
    "\6\0\1\47\12\0\1\43\2\0\1\43\5\0\1\47"+
    "\2\0\7\34\1\50\1\44\31\34\3\0\2\4\4\0"+
    "\3\4\1\51\2\4\6\0\2\4\2\0\5\4\1\0"+
    "\1\4\5\0\2\4\4\0\5\4\1\52\6\0\2\4"+
    "\2\0\5\4\1\0\1\4\5\0\2\4\4\0\4\4"+
    "\1\53\1\4\6\0\2\4\2\0\5\4\1\0\1\4"+
    "\5\0\2\4\4\0\5\4\1\54\6\0\2\4\2\0"+
    "\5\4\1\0\1\4\2\0";

  private static int [] zzUnpackTrans() {
    int [] result = new int[884];
    int offset = 0;
    offset = zzUnpackTrans(ZZ_TRANS_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackTrans(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      value--;
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /* error codes */
  private static final int ZZ_UNKNOWN_ERROR = 0;
  private static final int ZZ_NO_MATCH = 1;
  private static final int ZZ_PUSHBACK_2BIG = 2;

  /* error messages for the codes above */
  private static final String[] ZZ_ERROR_MSG = {
    "Unknown internal scanner error",
    "Error: could not match input",
    "Error: pushback value was too large"
  };

  /**
   * ZZ_ATTRIBUTE[aState] contains the attributes of state <code>aState</code>
   */
  private static final int [] ZZ_ATTRIBUTE = zzUnpackAttribute();

  private static final String ZZ_ATTRIBUTE_PACKED_0 =
    "\1\0\1\11\3\1\2\11\3\1\6\11\6\1\1\11"+
    "\1\1\1\0\1\11\5\1\1\11\3\1\1\0\2\1"+
    "\2\11\4\1";

  private static int [] zzUnpackAttribute() {
    int [] result = new int[44];
    int offset = 0;
    offset = zzUnpackAttribute(ZZ_ATTRIBUTE_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAttribute(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }

  /** the input device */
  private java.io.Reader zzReader;

  /** the current state of the DFA */
  private int zzState;

  /** the current lexical state */
  private int zzLexicalState = YYINITIAL;

  /** this buffer contains the current text to be matched and is
      the source of the yytext() string */
  private CharSequence zzBuffer = "";

  /** the textposition at the last accepting state */
  private int zzMarkedPos;

  /** the current text position in the buffer */
  private int zzCurrentPos;

  /** startRead marks the beginning of the yytext() string in the buffer */
  private int zzStartRead;

  /** endRead marks the last character in the buffer, that has been read
      from input */
  private int zzEndRead;

  /**
   * zzAtBOL == true <=> the scanner is currently at the beginning of a line
   */
  private boolean zzAtBOL = true;

  /** zzAtEOF == true <=> the scanner is at the EOF */
  private boolean zzAtEOF;

  /** denotes if the user-EOF-code has already been executed */
  private boolean zzEOFDone;


  /**
   * Creates a new scanner
   *
   * @param   in  the java.io.Reader to read input from.
   */
  LimaFlexLexer(java.io.Reader in) {
    this.zzReader = in;
  }


  /** 
   * Unpacks the compressed character translation table.
   *
   * @param packed   the packed character translation table
   * @return         the unpacked character translation table
   */
  private static char [] zzUnpackCMap(String packed) {
    int size = 0;
    for (int i = 0, length = packed.length(); i < length; i += 2) {
      size += packed.charAt(i);
    }
    char[] map = new char[size];
    int i = 0;  /* index in packed string  */
    int j = 0;  /* index in unpacked array */
    while (i < packed.length()) {
      int  count = packed.charAt(i++);
      char value = packed.charAt(i++);
      do map[j++] = value; while (--count > 0);
    }
    return map;
  }

  public final int getTokenStart() {
    return zzStartRead;
  }

  public final int getTokenEnd() {
    return getTokenStart() + yylength();
  }

  public void reset(CharSequence buffer, int start, int end, int initialState) {
    zzBuffer = buffer;
    zzCurrentPos = zzMarkedPos = zzStartRead = start;
    zzAtEOF  = false;
    zzAtBOL = true;
    zzEndRead = end;
    yybegin(initialState);
  }

  /**
   * Refills the input buffer.
   *
   * @return      <code>false</code>, iff there was new input.
   *
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  private boolean zzRefill() throws java.io.IOException {
    return true;
  }


  /**
   * Returns the current lexical state.
   */
  public final int yystate() {
    return zzLexicalState;
  }


  /**
   * Enters a new lexical state
   *
   * @param newState the new lexical state
   */
  public final void yybegin(int newState) {
    zzLexicalState = newState;
  }


  /**
   * Returns the text matched by the current regular expression.
   */
  public final CharSequence yytext() {
    return zzBuffer.subSequence(zzStartRead, zzMarkedPos);
  }


  /**
   * Returns the character at position <tt>pos</tt> from the
   * matched text.
   *
   * It is equivalent to yytext().charAt(pos), but faster
   *
   * @param pos the position of the character to fetch.
   *            A value from 0 to yylength()-1.
   *
   * @return the character at position pos
   */
  public final char yycharat(int pos) {
    return zzBuffer.charAt(zzStartRead+pos);
  }


  /**
   * Returns the length of the matched text region.
   */
  public final int yylength() {
    return zzMarkedPos-zzStartRead;
  }


  /**
   * Reports an error that occured while scanning.
   *
   * In a wellformed scanner (no or only correct usage of
   * yypushback(int) and a match-all fallback rule) this method
   * will only be called with things that "Can't Possibly Happen".
   * If this method is called, something is seriously wrong
   * (e.g. a JFlex bug producing a faulty scanner etc.).
   *
   * Usual syntax/scanner level error handling should be done
   * in error fallback rules.
   *
   * @param   errorCode  the code of the errormessage to display
   */
  private void zzScanError(int errorCode) {
    String message;
    try {
      message = ZZ_ERROR_MSG[errorCode];
    }
    catch (ArrayIndexOutOfBoundsException e) {
      message = ZZ_ERROR_MSG[ZZ_UNKNOWN_ERROR];
    }

    throw new Error(message);
  }


  /**
   * Pushes the specified amount of characters back into the input stream.
   *
   * They will be read again by then next call of the scanning method
   *
   * @param number  the number of characters to be read again.
   *                This number must not be greater than yylength()!
   */
  public void yypushback(int number)  {
    if ( number > yylength() )
      zzScanError(ZZ_PUSHBACK_2BIG);

    zzMarkedPos -= number;
  }


  /**
   * Resumes scanning until the next regular expression is matched,
   * the end of input is encountered or an I/O-Error occurs.
   *
   * @return      the next token
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  public LimaToken advance() throws java.io.IOException {
    int zzInput;
    int zzAction;

    // cached fields:
    int zzCurrentPosL;
    int zzMarkedPosL;
    int zzEndReadL = zzEndRead;
    CharSequence zzBufferL = zzBuffer;

    int [] zzTransL = ZZ_TRANS;
    int [] zzRowMapL = ZZ_ROWMAP;
    int [] zzAttrL = ZZ_ATTRIBUTE;

    while (true) {
      zzMarkedPosL = zzMarkedPos;

      zzAction = -1;

      zzCurrentPosL = zzCurrentPos = zzStartRead = zzMarkedPosL;

      zzState = ZZ_LEXSTATE[zzLexicalState];

      // set up zzAction for empty match case:
      int zzAttributes = zzAttrL[zzState];
      if ( (zzAttributes & 1) == 1 ) {
        zzAction = zzState;
      }


      zzForAction: {
        while (true) {

          if (zzCurrentPosL < zzEndReadL) {
            zzInput = Character.codePointAt(zzBufferL, zzCurrentPosL/*, zzEndReadL*/);
            zzCurrentPosL += Character.charCount(zzInput);
          }
          else if (zzAtEOF) {
            zzInput = YYEOF;
            break zzForAction;
          }
          else {
            // store back cached positions
            zzCurrentPos  = zzCurrentPosL;
            zzMarkedPos   = zzMarkedPosL;
            boolean eof = zzRefill();
            // get translated positions and possibly new buffer
            zzCurrentPosL  = zzCurrentPos;
            zzMarkedPosL   = zzMarkedPos;
            zzBufferL      = zzBuffer;
            zzEndReadL     = zzEndRead;
            if (eof) {
              zzInput = YYEOF;
              break zzForAction;
            }
            else {
              zzInput = Character.codePointAt(zzBufferL, zzCurrentPosL/*, zzEndReadL*/);
              zzCurrentPosL += Character.charCount(zzInput);
            }
          }
          int zzNext = zzTransL[ zzRowMapL[zzState] + ZZ_CMAP(zzInput) ];
          if (zzNext == -1) break zzForAction;
          zzState = zzNext;

          zzAttributes = zzAttrL[zzState];
          if ( (zzAttributes & 1) == 1 ) {
            zzAction = zzState;
            zzMarkedPosL = zzCurrentPosL;
            if ( (zzAttributes & 8) == 8 ) break zzForAction;
          }

        }
      }

      // store back cached position
      zzMarkedPos = zzMarkedPosL;

      if (zzInput == YYEOF && zzStartRead == zzCurrentPos) {
        zzAtEOF = true;
        return null;
      }
      else {
        switch (zzAction < 0 ? zzAction : ZZ_ACTION[zzAction]) {
          case 1: 
            { return UNKNOWN;
            }
          case 25: break;
          case 2: 
            { return WHITE_SPACE;
            }
          case 26: break;
          case 3: 
            { return IDENTIFIER;
            }
          case 27: break;
          case 4: 
            { return DECIMAL_LITERAL;
            }
          case 28: break;
          case 5: 
            { return SCOPE_BEGIN;
            }
          case 29: break;
          case 6: 
            { return SCOPE_END;
            }
          case 30: break;
          case 7: 
            { return ADDRESS_SPECIFIER;
            }
          case 31: break;
          case 8: 
            { return GROUP_SEPARATOR;
            }
          case 32: break;
          case 9: 
            { return ARRAY_BEGIN;
            }
          case 33: break;
          case 10: 
            { return ARRAY_END;
            }
          case 34: break;
          case 11: 
            { return HIGH_BYTE;
            }
          case 35: break;
          case 12: 
            { return LOW_BYTE;
            }
          case 36: break;
          case 13: 
            { return LABEL_PREFIX;
            }
          case 37: break;
          case 14: 
            { return COMMA;
            }
          case 38: break;
          case 15: 
            { return HEXADECIMAL_LITERAL;
            }
          case 39: break;
          case 16: 
            { return LINE_COMMENT;
            }
          case 40: break;
          case 17: 
            { return BLOCK_COMMENT;
            }
          case 41: break;
          case 18: 
            { return REGISTER_LITERAL;
            }
          case 42: break;
          case 19: 
            { return OCTAL_LITERAL;
            }
          case 43: break;
          case 20: 
            { return REGISTER_IDENTIFIER;
            }
          case 44: break;
          case 21: 
            { return BINARY_LITERAL;
            }
          case 45: break;
          case 22: 
            { return DEFINE_MEMORY;
            }
          case 46: break;
          case 23: 
            { return DEFINE_REGISTER;
            }
          case 47: break;
          case 24: 
            { return UNDEFINE_REGISTER;
            }
          case 48: break;
          default:
            zzScanError(ZZ_NO_MATCH);
          }
      }
    }
  }


}
