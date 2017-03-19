package com.darkyen.limas;

import static com.darkyen.limas.LimaToken.*;

/** Flex-based lexer for Limen Alpha Assembly */
%%

%class LimaFlexLexer
%unicode
%function advance
%type LimaToken

LINE_TERMINATOR     = \r|\n|\r\n
WHITE_SPACE         = [ \t\f]
IDENTIFIER          = [a-zA-Z_][a-zA-Z_0-9]*

%%

// Rules

\{                                          {return SCOPE_BEGIN;}
\}                                          {return SCOPE_END;}

({WHITE_SPACE}|{LINE_TERMINATOR})+          {return WHITE_SPACE;}
"//"[^\r\n]*                                {return LINE_COMMENT;}
"/*"([^"*"]|("*"+[^"*""/"]))*("*"+"/")?     {return BLOCK_COMMENT;}

undefr                                      {return UNDEFINE_REGISTER;}
defr                                        {return DEFINE_REGISTER;}
def                                         {return DEFINE_MEMORY;}

@                                           {return ADDRESS_SPECIFIER;}
:                                           {return GROUP_SEPARATOR;}

\[                                          {return ARRAY_BEGIN;}
\]                                          {return ARRAY_END;}

\<                                          {return HIGH_BYTE;}
\>                                          {return LOW_BYTE;}

R[0-7]                                      {return REGISTER_LITERAL;}

{IDENTIFIER}                                {return IDENTIFIER;}
\${IDENTIFIER}                              {return REGISTER_IDENTIFIER;}

\+?[01]+[bB]                                {return BINARY_LITERAL;}
\+?[0-7]+[oO]                               {return OCTAL_LITERAL;}
\+?[0-9a-fA-F]+[hH]                         {return HEXADECIMAL_LITERAL;}
[\-\+]?[0-9]+[dD]?                          {return DECIMAL_LITERAL;}

"-"                                         {return LABEL_PREFIX;}

","                                         {return COMMA;}

.                                           {return UNKNOWN;}